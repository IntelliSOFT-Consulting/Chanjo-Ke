package com.intellisoft.chanjoke.detail.ui.main.aefis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.chanjoke.databinding.ActivityAdverseEventBinding
import com.intellisoft.chanjoke.detail.ui.main.aefis.edit.EditAefiActivity
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import timber.log.Timber

class AdverseEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdverseEventBinding
    private lateinit var fhirEngine: FhirEngine
    private val formatter = FormatterClass()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdverseEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.apply {
            btnClose.setOnClickListener {
//                onBackPressed()
                val data = AEFIData(
                    specimen = binding.specimenTextView.text.toString(),
                    type = binding.typeOfAEFITextView.text.toString(),
                    brief = binding.briefDetailsTextView.text.toString(),
                    onset = binding.onsetOfEventTextView.text.toString(),
                    history = binding.pastMedicalHistoryTextView.text.toString(),
                    severity = binding.reactionSeverityTextView.text.toString(),
                    action = binding.actionTakenTextView.text.toString(),
                    outcome = binding.aefiOutcomeTextView.text.toString(),
                    reporter = binding.nameOfPersonTextView.text.toString(),
                    phone = binding.contactTextView.text.toString()
                )
                formatter.saveSharedPref(
                    "aefi_data",
                    Gson().toJson(data),
                    this@AdverseEventActivity
                )
                if (!activePatient()) {
                    Toast.makeText(
                        this@AdverseEventActivity,
                        "Patient is deceased",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }
                startActivity(Intent(this@AdverseEventActivity, EditAefiActivity::class.java))
            }
        }

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""

        }
        val patientId = FormatterClass().getSharedPref("patientId", this@AdverseEventActivity)
        val encounterId =
            FormatterClass().getSharedPref("encounter_logical", this@AdverseEventActivity)
        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    this.application,
                    fhirEngine,
                    patientId.toString()
                ),
            ).get(PatientDetailsViewModel::class.java)
        loadAEFIData()

    }

    override fun onResume() {
        super.onResume()
        try {
            loadAEFIData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun activePatient(): Boolean {
        try {
            val patient = patientDetailsViewModel.getPatientInfo()

            return patient.isAlive
        } catch (e: Exception) {
            return false
        }

    }
    private fun loadAEFIData() {

        val patientId = FormatterClass().getSharedPref("patientId", this@AdverseEventActivity)
        val encounterId =
            FormatterClass().getSharedPref("encounter_logical", this@AdverseEventActivity)
        binding.apply {
            val type = extractAefiData(patientId.toString(), encounterId.toString(), "882-22")
            if (encounterId != null) {
                displayPractitionerDetails(patientId.toString(), encounterId)
            }

            try {
                val trimmedText = type.trim()

                if (trimmedText.equals("Other", ignoreCase = true)) {

                    lnOtherType.visibility = View.VISIBLE
                    vOtherType.visibility = View.VISIBLE
                    typeOfAEFIOtherTextView.text = extractAefiData(
                        patientId.toString(),
                        encounterId.toString(),
                        "303-22"
                    )
                }
            } catch (e: Exception) {

            }
            typeOfAEFITextView.text = type

            // Brief details on AEFI
            briefDetailsTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "833-22"
            )
            // Onset of Event
            onsetOfEventTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "833-23"
            )
            // Specimen
            specimenTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "886-1"
            )
            // Past Medical History
            pastMedicalHistoryTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "833-21"
            )
            // Reaction Severity
            reactionSeverityTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "880-11"
            )
            // Action Taken
            actionTakenTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "888-1"
            )
            // AEFI Outcome
            aefiOutcomeTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "808-11"
            )
            nameOfPersonTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "133-22"
            )
            contactTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                "122-22"
            )
            dateReportedTextView.text = extractAefiOccuranceData(
                patientId.toString(),
                encounterId.toString(),
                "882-22"
            )
        }
    }

    private fun displayPractitionerDetails(patientId: String, encounterId: String) {
        try {
            val adverseEvent = patientDetailsViewModel.getAdverseEvent(patientId, encounterId)
            if (adverseEvent != null) {

                // Process the adverse event (e.g., display details, perform actions)
                binding.apply {
                    healthcareWorkerTextView.apply {
                        text = adverseEvent.practitionerId.name
                    }
                    designationTextView.apply {
                        text = adverseEvent.practitionerId.role
                    }
                    facilityTextView.apply {
                        text = adverseEvent.locDisplay
                    }
                }
            } else {
                println("No Adverse Event found for encounterId: $encounterId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun extractAefiOccuranceData(
        patientId: String,
        encounterId: String,
        code: String
    ): CharSequence? {
        var text = "\t"

        /***
         * TODO: extract Observation by code
         */
        val data = patientDetailsViewModel.getObservationByCode(patientId, encounterId, code)
        if (data != null) {
            val refinedDate = FormatterClass().convertDateFormat(data.date)
            text = "\t $refinedDate"
        }

        return text
    }

    private fun extractAefiData(patientId: String, encounterId: String, code: String): String {
        var text = "\t"

        /***
         * TODO: extract Observation by code
         */
        val data = patientDetailsViewModel.getObservationByCode(patientId, encounterId, code)
        if (data != null) {
            text = "\t ${data.value}"
        }

        return text
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}