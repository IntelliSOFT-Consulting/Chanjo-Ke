package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityCompleteDetailsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompleteDetailsBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private var formatterClass = FormatterClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()
        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            ).get(PatientDetailsViewModel::class.java)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""

        }
        binding.apply {
            btnClose.apply {
                setOnClickListener {
                    onBackPressed()
                }
            }
        }

        val patientDetail = patientDetailsViewModel.getPatientInfo()
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {

                val dob = formatterClass.convertDateFormat(patientDetail.dob)
                val age =
                    formatterClass.getFormattedAge(patientDetail.dob, tvAge.context.resources)
                val ageYears =
                    formatterClass.getFormattedAgeYears(patientDetail.dob, tvAge.context.resources)
                if (ageYears >= 18) {
                    tvTitleName.text = getString(R.string.next_of_kin_details)
                }
                tvFirstname.text = patientDetail.name
                tvGender.text = AppUtils().capitalizeFirstLetter(patientDetail.gender)
                tvDateOfBirth.text = dob
                tvAge.text = "$age old"
                tvIdNumber.text = patientDetail.systemId
                tvCname.text = patientDetail.contact_name
                tvCtype.text = patientDetail.contact_gender
                tvCphone.text = patientDetail.contact_phone
                tvCounty.text = patientDetail.county
                tvSubCounty.text = patientDetail.subCounty
                tvWard.text = patientDetail.ward
                tvTrading.text = patientDetail.trading
                tvVillage.text = patientDetail.estate

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