package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAddAppointmentBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.detail.ui.main.contraindications.ContraindicationsAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDataDetails
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AddAppointment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppointmentBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
//    private var selectedVaccineName = ""
    private lateinit var fhirEngine: FhirEngine

    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()

    private val formatterClass = FormatterClass()
    private val immunizationHandler = ImmunizationHandler()
    private val selectedItemList = ArrayList<String>()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var appointmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        appointmentId = intent.getStringExtra("appointmentId")

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            ).get(PatientDetailsViewModel::class.java)

        getPastAppointment()

        createSpinner()

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.tvDatePicker.setOnClickListener { showDatePickerDialog() }

        binding.btnCancel.setOnClickListener {
            onBackPressed()
        }

        binding.btnPreview.setOnClickListener {

            val dateScheduled = binding.tvDatePicker.text.toString()
            if (!TextUtils.isEmpty(dateScheduled)){

                CoroutineScope(Dispatchers.IO).launch {

                    selectedItemList.forEach {
                        val dbAppointmentData = DbAppointmentDataDetails(
                            null,
                            it,
                            dateScheduled
                        )
                        administerVaccineViewModel.createAppointment(dbAppointmentData)
                    }

                }



                Toast.makeText(this, "Please wait as we create the appointment", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, PatientDetailActivity::class.java)
                startActivity(intent)
                finish()

            }else{
                if (TextUtils.isEmpty(dateScheduled)) binding.tvDatePicker.error = "Field cannot be empty.."
            }

        }

    }

    private fun getPastAppointment() {

        if (appointmentId != null){
            val appointmentList = patientDetailsViewModel.getAppointmentList()
            val recommendationList: ArrayList<DbAppointmentDetails>

            val dbAppointmentData = appointmentList.find { it.id == appointmentId }

            if (dbAppointmentData != null) {
                recommendationList = dbAppointmentData.recommendationList!!

                val dateScheduled = dbAppointmentData.dateScheduled
                binding.tvDatePicker.text = dateScheduled
            }
        }

    }

    private fun createAppointment(selectedItem: String) {
        if (selectedItemList.contains(selectedItem)) selectedItemList.remove(selectedItem)
        selectedItemList.add(selectedItem)

        Log.e("----->","<------")
        println(selectedItemList)
        Log.e("----->","<------")

        selectedItemList.remove("")

        val vaccineAdapter = ContraindicationsAdapter(selectedItemList,this)
        binding.recyclerView.adapter = vaccineAdapter

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val maxCalendar = Calendar.getInstance()
//        maxCalendar.add(Calendar.DAY_OF_MONTH, -7)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Handle the selected date (e.g., update the TextView)
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvDatePicker.text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun createSpinner() {
        val recommendationList = ArrayList<BasicVaccine>()

        val patientDob = formatterClass.getSharedPref("patientDob", this)
        if (patientDob != null){

            val ageInWeeks = formatterClass.calculateWeeksFromDate(patientDob)
            if (ageInWeeks != null){

                val administeredList = ArrayList<BasicVaccine>()
                val vaccineList = patientDetailsViewModel.getVaccineList()
                vaccineList.forEach {
                    val vaccineName = it.vaccineName
                    val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
                    if (basicVaccine != null) {
                        administeredList.add(basicVaccine)
                    }
                }

                val weeksList = ArrayList<Double>()
                recommendationList.add(
                    BasicVaccine(
                        "",
                        "",
                        "",
                        0,
                        weeksList,
                        "",
                        "",
                        )
                )
                val (routineList, nonRoutineVaccineList,  pregnancyVaccineList) =
                    immunizationHandler.getAllVaccineList(administeredList, ageInWeeks, this)



                routineList.forEach { routineVaccine ->
                    val basicVaccineList = routineVaccine.vaccineList
                    recommendationList += ArrayList(basicVaccineList)
                }
                nonRoutineVaccineList.forEach {nonRoutineVaccine ->
                    val routineVaccineList = nonRoutineVaccine.vaccineList
                    routineVaccineList.forEach {routineVaccine ->
                        val basicVaccineList = routineVaccine.vaccineList
                        recommendationList += ArrayList(basicVaccineList)
                    }
                }
                pregnancyVaccineList.forEach {pregnancyVaccine ->
                    val basicVaccineList = pregnancyVaccine.vaccineList
                    recommendationList += ArrayList(basicVaccineList)
                }

            }

        }



        val itemList = ArrayList<String>()
        //Remove the vaccines in an appointment
        val givenRecommendationList = ArrayList<String>()
        val appointmentList = patientDetailsViewModel.getAppointmentList()
        appointmentList.forEach {
            it.recommendationList?.forEach {dbAppointmentDetails ->
                val vaccineName = dbAppointmentDetails.vaccineName
                givenRecommendationList.add(vaccineName)
            }
        }

        recommendationList.forEach {
            itemList.add(it.vaccineName)
        }

        // Convert strings and remove extra whitespaces
        val recommendationListLower = givenRecommendationList.map { it.trim() }
        val itemListLower = itemList.map { it.trim() }

        // Remove common elements
//        val uniqueRecommendations = givenRecommendationList.filter { it.trim() !in itemListLower }
//        val uniqueItemsList = itemList.filter { it.trim() !in recommendationListLower }


        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemListLower)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinner.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedItem = itemListLower[position]
                val selectedVaccine = recommendationList.find { it.vaccineName == selectedItem }
                if (selectedVaccine != null) {
                    val selectedVaccineName = selectedVaccine.vaccineName
                    createAppointment(selectedVaccineName)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

    }
}