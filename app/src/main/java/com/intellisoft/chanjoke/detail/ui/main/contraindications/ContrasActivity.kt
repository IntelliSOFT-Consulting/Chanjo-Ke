package com.intellisoft.chanjoke.detail.ui.main.contraindications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.databinding.ActivityContrasBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import timber.log.Timber

class ContrasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContrasBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContrasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

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
                PatientDetailsViewModelFactory(
                    this.application,
                    fhirEngine,
                    patientId.toString()
                ),
            ).get(PatientDetailsViewModel::class.java)

        loadContraindications()
    }

    private fun loadContraindications() {
        val vaccineCode =
            FormatterClass().getSharedPref("vaccineNameDetails", this)
        if (vaccineCode != null) {
            val contras = patientDetailsViewModel.loadContraindications(vaccineCode)
            Timber.e("Contraindications details ****$vaccineCode,  $contras")
            if (contras.isNotEmpty()) {
                binding.apply {
                    var date = contras.first().nextDate
                    if (date.isNotEmpty()) {
                        date = FormatterClass().convertDateFormat(date).toString()
                    }
                    tvNextDate.text = date
                    tvDetails.text = contras.first().contraDetail
                }
            }

            binding.apply {
                tvVaccineName.text = vaccineCode
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}