package com.intellisoft.chanjoke.detail.ui.main.appointments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAppointmentDetailsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

class AppointmentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailsBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private var formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()
        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            )
                .get(PatientDetailsViewModel::class.java)

        getAppointments()
    }

    private fun getAppointments() {

        val title = formatterClass.getSharedPref("title", this)
        val description = formatterClass.getSharedPref("description", this)
        val dateScheduled = formatterClass.getSharedPref("dateScheduled", this)
        val recommendationList = formatterClass.getSharedPref("recommendationList", this)

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvDateScheduled.text = dateScheduled

        Log.e("----","----")
        println("recommendationList $recommendationList")
        Log.e("----","----")


    }
}