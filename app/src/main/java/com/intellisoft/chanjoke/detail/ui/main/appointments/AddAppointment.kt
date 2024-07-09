package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAddAppointmentBinding
import com.intellisoft.chanjoke.detail.ui.main.contraindications.ContraindicationsAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar
import kotlin.math.round

class AddAppointment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppointmentBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
//    private var selectedVaccineName = ""
    private lateinit var fhirEngine: FhirEngine


    private val formatterClass = FormatterClass()
    private val immunizationHandler = ImmunizationHandler()
    private val selectedItemList = ArrayList<String>()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var appointmentId: String? = null
    private var selectedItemValue: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var patientDob:String? = null

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

        sharedPreferences = getSharedPreferences(getString(R.string.vaccineList),
                Context.MODE_PRIVATE
            )
        patientDob = formatterClass.getSharedPref("patientDob", this)

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
            if (!TextUtils.isEmpty(dateScheduled) && selectedItemList.isNotEmpty() && selectedItemValue != null && dateScheduled != "Appointment Date *"){

                //Add a preview page
                formatterClass.saveSharedPref("appointmentListData", selectedItemList.joinToString(","), this)
                formatterClass.saveSharedPref("appointmentDateScheduled", dateScheduled, this)
                formatterClass.saveSharedPref("appointmentVaccineTitle", selectedItemValue!!, this)
                formatterClass.saveSharedPref("appointmentFlow", "addAppointment", this)

                val intent = Intent(this, AppointmentDetails::class.java)
                startActivity(intent)
                finish()

            }else{
                if (TextUtils.isEmpty(dateScheduled) ) binding.tvDatePicker.error = "Field cannot be empty.."
                if (selectedItemList.isEmpty()) Toast.makeText(this, "Vaccine list is empty..", Toast.LENGTH_SHORT).show()
                if (dateScheduled == "Appointment Date *") binding.tvDatePicker.error = "Select an appointment date."
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

        selectedItemList.remove("")

        val vaccineAdapter = ContraindicationsAdapter(selectedItemList,this)
        binding.recyclerView.adapter = vaccineAdapter

        val routineRecommendationList = patientDetailsViewModel.recommendationList(null)
        if (routineRecommendationList.isNotEmpty() && selectedItemList.isNotEmpty()){

            //Get date from recommendation list
            val dbRecommendationData = routineRecommendationList.find { it.vaccineName == selectedItemList.first() }
            binding.tvScheduleDate.text = dbRecommendationData?.earliestDate


        }

        //Get appointments
        val appointmentList = patientDetailsViewModel.getAppointmentList()
        if (appointmentList.isNotEmpty() && selectedItemList.isNotEmpty()){

            binding.tvAppointmentNo.text = "${appointmentList.size} Appointments"

        }

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

    // Function to add a vaccine to the map
    private fun addVaccine(
        map: MutableMap<String, List<BasicVaccine>>,
        key: String,
        vaccine: BasicVaccine) {
        val currentList = map[key] ?: emptyList()
        map[key] = currentList + vaccine
    }

    private fun createSpinner() {

        //retain only the nearest

        val entryList = mutableMapOf<String, List<BasicVaccine>>()
        val spinnerList = ArrayList<String>()

        val routineKeyList = sharedPreferences.getString("routineList", null)
        val expandableListTitle = routineKeyList!!.split(",").toList()

        expandableListTitle.forEach { keyValue ->
            val weekNo = formatterClass.getVaccineScheduleValue(keyValue)
            val weekNoList = sharedPreferences.getStringSet(weekNo, null)
            val vaccineList = weekNoList?.toList()

            if (patientDob != null){
                try{
                    val numberOfBirthWeek = formatterClass.calculateWeeksFromDate(patientDob!!)

                    if (numberOfBirthWeek != null){
                        val numberOfWeek = formatterClass.convertVaccineScheduleToWeeks(weekNo)

                        val isWithinPlusOrMinus14 = numberOfBirthWeek - numberOfWeek

                        if (isWithinPlusOrMinus14 in -4..4){
                            //Add to list
                            vaccineList?.forEach { vaccineName ->
                                val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
                                if (basicVaccine != null){
                                    spinnerList.add(weekNo)
                                    addVaccine(entryList, weekNo, basicVaccine)
                                }
                            }

                        }

                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

        }


        val uniqueSpinnerList = ArrayList(spinnerList.toSet())


        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniqueSpinnerList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinner.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedItem = uniqueSpinnerList[position]

                selectedItemValue = selectedItem
                selectedItemList.clear()

                val weekNoList = sharedPreferences.getStringSet(selectedItem, null)
                val vaccineList = weekNoList?.toList()
                vaccineList?.forEach {
                    createAppointment(it)
                }

//                for ((key, basicVaccineList) in entryList) {
//                    basicVaccineList.forEach {
//                        createAppointment(it.vaccineName)
//                    }
//                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

    }
}