package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.material.button.MaterialButton
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
//                    onBackPressed()
                    createDialog()
                }
            }
        }

        val patientDetail = patientDetailsViewModel.getPatientInfo()
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {

                val dob = formatterClass.convertDateFormat(patientDetail.dob)
                val age =
                    formatterClass.getFormattedAge(
                        patientDetail.dob,
                        tvAge.context.resources,
                        this@CompleteDetailsActivity
                    )
                val ageYears =
                    formatterClass.getFormattedAgeYears(patientDetail.dob, tvAge.context.resources)
                if (ageYears >= 18) {
                    tvTitleName.text = getString(R.string.next_of_kin_details)
                }
                tvFirstname.text = patientDetail.name
                tvGender.text = AppUtils().capitalizeFirstLetter(patientDetail.gender)
                try {
                    tvDateOfBirth.text =
                        formatterClass.convertDateFormatWithDesiredFormat(
                            dob.toString(),
                            "dd/MM/yyyy"
                        )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                tvAge.text = "$age old"
                tvIdNumber.text = patientDetail.systemId
                tvCname.text = patientDetail.contact_name
                tvCtype.text = patientDetail.contact_gender
                tvCphone.text = patientDetail.contact_phone
                tvCounty.text = AppUtils().capitalizeFirstLetter(patientDetail.county.toString())
                tvSubCounty.text = AppUtils().capitalizeFirstLetter(patientDetail.subCounty.toString())
                tvWard.text = AppUtils().capitalizeFirstLetter(patientDetail.ward.toString())
                tvTrading.text = AppUtils().capitalizeFirstLetter(patientDetail.trading.toString())
                tvVillage.text = AppUtils().capitalizeFirstLetter(patientDetail.estate.toString())

                try {
                    if (patientDetail.kins != null) {
                        // Assuming this code is inside a fragment or activity method
                        patientDetail.kins.forEach { caregiver ->
                            val inflater = LayoutInflater.from(this@CompleteDetailsActivity)
                            val itemView = inflater.inflate(
                                R.layout.caregiver,
                                lnCaregiver,
                                false
                            ) as LinearLayout

                            val tvCname = itemView.findViewById<TextView>(R.id.tv_cname)
                            val tvCtype = itemView.findViewById<TextView>(R.id.tv_ctype)
                            val tvCphone = itemView.findViewById<TextView>(R.id.tv_cphone)

                            // Set the text for each TextView with caregiver information
                            tvCname.text = caregiver.name
                            tvCtype.text = caregiver.type
                            tvCphone.text = caregiver.phone

                            // Add the itemView (LinearLayout) to lnCaregiver (LinearLayout)
                            lnCaregiver.addView(itemView)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun createDialog() {

        val customDialogView =
            LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)

        // Find views within the custom layout
        val vaccineDetails: MaterialButton = customDialogView.findViewById(R.id.vaccineDetails)
        val clientDetails: MaterialButton = customDialogView.findViewById(R.id.clientDetails)
        val cancelButton: ImageButton = customDialogView.findViewById(R.id.cancel_button)

        // Set up the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(customDialogView)
        // Set up any other UI interactions or logic here

        // Create and show the AlertDialog
        val customDialog = builder.create()
        customDialog.show()

        // Example: Set an onClickListener for the "Close" button
        vaccineDetails.setOnClickListener {
            formatterClass.saveSharedPref(
                "questionnaireJson",
                "update_history_specifics.json",
                this
            )
            formatterClass.saveSharedPref(
                "vaccinationFlow",
                "updateVaccineDetails",
                this
            )
            FormatterClass().saveSharedPref(
                "title",
                "Update Vaccine Details", this
            )
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
            customDialog.dismiss() // Close the dialog
        }

        clientDetails.setOnClickListener {
            formatterClass.saveSharedPref(
                "questionnaireJson",
                "hiv.json",
                this
            )
            FormatterClass().saveSharedPref(
                "title",
                "Update HIV Status", this
            )
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
            customDialog.dismiss() // Close the dialog
        }

        cancelButton.setOnClickListener {
            // Additional actions when the dialog is dismissed
            customDialog.dismiss()
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
                telephone = data.phone,
                estimate = false
            )
            saveTempData("personal", Gson().toJson(payload))

            val caregiver = Gson().toJson(data.kins)
            saveTempData("caregiver", caregiver)
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