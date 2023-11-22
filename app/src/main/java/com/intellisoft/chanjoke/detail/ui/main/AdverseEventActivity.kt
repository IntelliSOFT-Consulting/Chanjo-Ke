package com.intellisoft.chanjoke.detail.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAdverseEventBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import timber.log.Timber

class AdverseEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdverseEventBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdverseEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

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
        binding.apply {
            typeOfAEFITextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                ""
            )
            // Brief details on AEFI
            briefDetailsTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.brief_details_on_aefi)
            )
            // Onset of Event
            onsetOfEventTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.onset_of_event)
            )
            // Past Medical History
            pastMedicalHistoryTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.past_medical_history)
            )
            // Reaction Severity
            reactionSeverityTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.reaction_severity)
            )
            // Action Taken
            actionTakenTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.action_taken)
            )
            // AEFI Outcome
            aefiOutcomeTextView.text = extractAefiData(
                patientId.toString(),
                encounterId.toString(),
                getString(R.string.aefi_outcome)
            )
        }

    }

    private fun extractAefiData(patientId: String, encounterId: String, code: String): String {
        val text = "\tresponse"
        /***
         * TODO: extract Observation by code
         */
        patientDetailsViewModel.getObservationByCode(patientId, encounterId, code)

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