package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAddAppointmentBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar

class AddAppointment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppointmentBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private var selectedItemId = ""
    private lateinit var fhirEngine: FhirEngine

    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            )
                .get(PatientDetailsViewModel::class.java)

        createSpinner()

        binding.tvDatePicker.setOnClickListener { showDatePickerDialog() }

        binding.btnPreview.setOnClickListener {

            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            val dateScheduled = binding.tvDatePicker.text.toString()
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(dateScheduled)){

                var vaccineId = ""
                if (selectedItemId != "") {
                    vaccineId = selectedItemId
                }

                val dbAppointmentData = DbAppointmentData(
                    title,
                    description,
                    vaccineId,
                    dateScheduled
                )
                administerVaccineViewModel.createAppointment(dbAppointmentData)

                Toast.makeText(this, "Please wait as we create the appointment", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, PatientDetailActivity::class.java)
                startActivity(intent)
                finish()

            }else{
                if (TextUtils.isEmpty(title)) binding.etTitle.error = "Field cannot be empty.."
                if (TextUtils.isEmpty(description)) binding.etDescription.error = "Field cannot be empty.."
            }

        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

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

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun createSpinner() {
        val itemList = ArrayList<String>()
        val recommendationList = patientDetailsViewModel.recommendationList()
        recommendationList.forEach {
            itemList.add(it.vaccineName)
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinner.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedItem = itemList[position]
                val selectedRecommendation = recommendationList.find { it.vaccineName == selectedItem }
                if (selectedRecommendation != null) selectedItemId = selectedRecommendation.appointmentId

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

    }
}