package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityCompleteDetailsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

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
                    formatterClass.getFormattedAge(patientDetail.dob, tvAge.context.resources, this@CompleteDetailsActivity)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_sync, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_edit -> {
                // Handle search action
                val patientDetail = patientDetailsViewModel.getPatientInfo()
                formatterClass.deleteSharedPref("personal", this)
                formatterClass.deleteSharedPref("caregiver", this)
                formatterClass.deleteSharedPref("administrative", this)

                handleClientDetailsEdit(patientDetail)

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveTempData(key: String, value: String) {
        formatterClass.saveSharedPref(key, value, this@CompleteDetailsActivity)
    }

    private fun handleClientDetailsEdit(data: PatientDetailsViewModel.PatientData) {
        try {
            saveTempData("patientId", data.logicalId)
            val payload = CustomPatient(
                firstname = data.name,
                middlename = data.name,
                lastname = data.name,
                gender = data.gender,
                age = "",
                dateOfBirth = data.dob,
                identification = data.type.toString(),
                identificationNumber = data.systemId.toString(),
                telephone = data.phone
            )
            saveTempData("personal", Gson().toJson(payload))
            val careGiver = CareGiver(
                data.contact_gender.toString(),
                data.contact_name.toString(),
                data.contact_phone.toString()
            )
            saveTempData("caregiver", Gson().toJson(careGiver))
            val administrative = Administrative(
                county = data.county.toString(),
                subCounty = data.subCounty.toString(),
                ward = data.ward.toString(),
                trading = data.trading.toString(),
                estate = data.estate.toString()
            )
            saveTempData("administrative", Gson().toJson(administrative))
            CoroutineScope(Dispatchers.Main).launch {
                // Delay for 2 seconds
                delay(2000)
                // Open another page (Activity, Fragment, etc.)
                // Example: Opening another activity
                val intent = Intent(this@CompleteDetailsActivity, RegistrationActivity::class.java)
                intent.putExtra("update", "true")
                startActivity(intent)

                // Finish the current activity if necessary
                finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
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