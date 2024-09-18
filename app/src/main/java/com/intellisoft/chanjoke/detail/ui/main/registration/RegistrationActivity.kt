package com.intellisoft.chanjoke.detail.ui.main.registration

import android.app.ProgressDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.ActivityRegistrationBinding
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.CompletePatient
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.PatientIdentification
import com.intellisoft.chanjoke.utils.ActivityBlurBackground
import timber.log.Timber

class RegistrationActivity : AppCompatActivity(), OnButtonClickListener,
    AdministrativeFragment.OnNextButtonClickListener {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val formatter = FormatterClass()
    private val viewModel: AddPatientViewModel by viewModels()
    private lateinit var progressDialog: ProgressDialog
    private var isClientUpdate: Boolean = false
    private lateinit var liveData: AdminLiveData
    private var allPatients = mutableListOf<PatientIdentification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        observeSubmission()

        liveData = ViewModelProvider(this).get(AdminLiveData::class.java)
        val update = intent.extras?.getString("update")
        if (update == "true") {
            isClientUpdate = true
            binding.tvTitle.text = getString(R.string.edit_client_detail)
            formatter.saveSharedPref("isUpdate", "true", this@RegistrationActivity)
        }
//        formatter.deleteSharedPref("caregiver",this)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Processing..")
        progressDialog.setCanceledOnTouchOutside(false)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
        tabLayout = binding.tabs
        viewPager = binding.viewpager

        setupViewPager(viewPager)

        tabLayout.setupWithViewPager(viewPager)
        allPatients = viewModel.loadRegisteredClients()

    }

    private fun observeSubmission() {
        viewModel.isPatientSaved.observe(this) {
            progressDialog.dismiss()
            if (!it) {
                Toast.makeText(this, "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val blurBackgroundDialog =
                ActivityBlurBackground(this, this@RegistrationActivity)
            blurBackgroundDialog.show()


        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(PersonalFragment(), "Fragment 1")
        pagerAdapter.addFragment(CaregiverFragment(), "Fragment 2")
        pagerAdapter.addFragment(AdministrativeFragment(), "Fragment 3")
        pagerAdapter.addFragment(PreviewFragment(), "Fragment 5")
        viewPager.adapter = pagerAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onNextPageRequested() {
        if (viewPager.currentItem < pagerAdapter.count - 1) {
            // Move to the next page in ViewPager
            val nextPageIndex = viewPager.currentItem + 1
            viewPager.setCurrentItem(nextPageIndex, true)

            val administrative = formatter.getSharedPref("administrative", this)
            if (administrative != null) {
                liveData.updatePatientDetails(administrative)
            }
        } else {

            val personal = formatter.getSharedPref("personal", this)
            val caregiver = formatter.getSharedPref("caregiver", this)
            val administrative = formatter.getSharedPref("administrative", this)

            if (personal != null && administrative != null) {

                val refinedPersonal = Gson().fromJson(personal, CustomPatient::class.java)
//                val refinedCaregiver = Gson().fromJson(caregiver, CareGiver::class.java)
                val refinedAdministrative =
                    Gson().fromJson(administrative, Administrative::class.java)
                val caregivers = ArrayList<CareGiver>()
                if (caregiver != null) {
                    val type = object : TypeToken<List<CareGiver>>() {}.type
                    val caregiverList: List<CareGiver> = Gson().fromJson(caregiver, type)

                    caregivers.clear()
                    caregivers.addAll(caregiverList)
                }
                val completePatient = CompletePatient(
                    personal = refinedPersonal,
                    caregivers = caregivers,
                    administrative = refinedAdministrative
                )

                val fhirPractitionerId = formatter.getSharedPref("fhirPractitionerId", this)
//                if (fhirPractitionerId != null) {
                val isUpdate = formatter.getSharedPref("isUpdate", this@RegistrationActivity)
                if (isUpdate != null) {
                    progressDialog.show()
                    viewModel.saveCustomPatient(
                        this,
                        completePatient,
                        fhirPractitionerId,
                        isClientUpdate
                    )
                } else {
                    if (noSimilarDocumentNumbers(
                            refinedPersonal.identification,
                            refinedPersonal.identificationNumber
                        )
                    ) {

                        Timber.e("Registering a Caregiver $caregivers")

                        progressDialog.show()
                        viewModel.saveCustomPatient(
                            this,
                            completePatient,
                            fhirPractitionerId,
                            isClientUpdate
                        )




                    } else {
                        Toast.makeText(
                            this,
                            "Client Identification Document Number already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

//                } else {
//                    Toast.makeText(this, "Please contact administrator", Toast.LENGTH_SHORT).show()
//                }
                }
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun noSimilarDocumentNumbers(type: String, number: String): Boolean {
        val similarIdentificationNumbers = arrayListOf<String>()
        if (allPatients.isEmpty()) {
            //No Patient record -> return true
            return true
        } else {
            similarIdentificationNumbers.clear()
            allPatients.forEach {
                if (it.document.trim() == type.trim()) {
                    similarIdentificationNumbers.add(it.number)
                }
            }
            return if (similarIdentificationNumbers.isEmpty()) {
                // no matching doc type -> return true
                true
            } else {
                // the doc exist
                val hasSimilar = similarIdentificationNumbers.contains(number)
                !hasSimilar

            }
        }

    }

    override fun onPreviousPageRequested() {
        if (viewPager.currentItem > 0) {
            // Move to the previous page in ViewPager
            val previousPageIndex = viewPager.currentItem - 1
            viewPager.setCurrentItem(previousPageIndex, true)
        } else {
            Timber.e("TAG: First Item")
        }
    }

    override fun onCancelPageRequested() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to cancel registration?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            finish() // Exit the activity
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onNextButtonClicked(admin: String) {
        formatter.saveSharedPref("administrative", admin, this)
        liveData.updatePatientDetails(admin)
    }
}