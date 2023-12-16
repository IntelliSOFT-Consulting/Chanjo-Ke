package com.intellisoft.chanjoke.detail.ui.main.appointments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAddAppointmentBinding
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class AddAppointment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppointmentBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private var selectedItem = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        createSpinner()

        binding.btnPreview.setOnClickListener {

            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && selectedItem != ""){



            }else{
                if (selectedItem == "") Toast.makeText(this, "Please make a selection", Toast.LENGTH_SHORT).show()
                if (TextUtils.isEmpty(title)) binding.etTitle.error = "Field cannot be empty.."
                if (TextUtils.isEmpty(description)) binding.etDescription.error = "Field cannot be empty.."
            }

        }

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
                selectedItem = itemList[position]


            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

    }
}