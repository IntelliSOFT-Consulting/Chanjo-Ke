package com.intellisoft.chanjoke.detail.ui.main.contraindications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityContrasBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

class ContrasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContrasBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContrasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btnClose.setOnClickListener {
                onBackPressed()
            }
            supportActionBar?.apply {
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                title = ""

            }
        }
        fhirEngine = FhirApplication.fhirEngine(this)
        val patientId = FormatterClass().getSharedPref("patientId", this)

        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application,
                    fhirEngine,
                    patientId.toString()
                ),
            ).get(PatientDetailsViewModel::class.java)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}