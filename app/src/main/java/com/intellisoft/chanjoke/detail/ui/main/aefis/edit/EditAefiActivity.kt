package com.intellisoft.chanjoke.detail.ui.main.aefis.edit

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.fhir.FhirEngine
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.intellisoft.chanjoke.databinding.ActivityEditAefiBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.detail.ui.main.registration.ViewPagerAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import com.intellisoft.chanjoke.viewmodel.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import timber.log.Timber

class EditAefiActivity : AppCompatActivity(), OnButtonClickListener {
    private lateinit var binding: ActivityEditAefiBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val formatter = FormatterClass()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val viewModel: ScreenerViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAefiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val patientId = FormatterClass().getSharedPref("patientId", this@EditAefiActivity)
        val encounterId =
            FormatterClass().getSharedPref("encounter_logical", this@EditAefiActivity)
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
        observeSubmission()
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
    }

    private fun setupViewPager(viewPager: ViewPager) {
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(TypeFragment(), "Fragment 1")
        pagerAdapter.addFragment(ActionFragment(), "Fragment 2")
        pagerAdapter.addFragment(ReviewFragment(), "Fragment 3")
        viewPager.adapter = pagerAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun observeSubmission() {

    }

    override fun onNextPageRequested() {
        if (viewPager.currentItem < pagerAdapter.count - 1) {
            // Move to the next page in ViewPager
            val nextPageIndex = viewPager.currentItem + 1
            viewPager.setCurrentItem(nextPageIndex, true)

        } else {
            val data = formatter.getSharedPref("updated_aefi_data", this@EditAefiActivity)
            Timber.e("End results *** $data")
            val patientId = FormatterClass().getSharedPref("patientId", this@EditAefiActivity)
            val encounterId =
                FormatterClass().getSharedPref("encounter_logical", this@EditAefiActivity)
            Timber.e("End results *** Patient $patientId")
            Timber.e("End results *** Encounter $encounterId")
            if (data != null) {
                progressDialog.show()
                val refinedData = Gson().fromJson(data, AEFIData::class.java)
                val allObservations =
                    patientDetailsViewModel.getObservationByEncounter(
                        patientId.toString(),
                        encounterId.toString()
                    )
                allObservations.forEach {
                    Timber.e("End results *** Observation ${it.code.codingFirstRep.code} Value ${it.value}")
                    when (it.code.codingFirstRep.code) {
                        "833-23" -> {
                            updateDateObservation(
                                it,
                                refinedData.onset,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "833-21" -> {
                            updateStringObservation(
                                it,
                                refinedData.history,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "833-22" -> {
                            updateStringObservation(
                                it,
                                refinedData.brief,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "122-22" -> {
                            updateStringObservation(
                                it,
                                refinedData.phone,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "880-11" -> {
                            updateCodingObservation(
                                it,
                                refinedData.severity,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "882-22" -> {
                            updateCodingObservation(
                                it,
                                refinedData.type,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "133-22" -> {
                            updateStringObservation(
                                it,
                                refinedData.reporter,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "808-11" -> {
                            updateCodingObservation(
                                it,
                                refinedData.outcome,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "888-1" -> {
                            updateStringObservation(
                                it,
                                refinedData.action,
                                patientId.toString(),
                                encounterId.toString()
                            )
                        }

                        "886-1" -> {
//                            updateStringObservation(
//                                it,
//                                refinedData.brief,
//                                patientId.toString(),
//                                encounterId.toString()
//                            )
                        }

                        else -> {
                            Timber.e("Done updating the AEFI")
                        }
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    // Simulate some background work
                    delay(2000) // Simulating a task that takes 2 seconds
                    // Dismiss progress dialog after 2 seconds
                    withContext(Dispatchers.Main) {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditAefiActivity,
                                "Update successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            this@EditAefiActivity.finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateCodingObservation(
        it: Observation,
        value: String,
        patientId: String,
        encounterId: String
    ) {
        val subjectReference = Reference("Patient/$patientId")
        val encounterReference = Reference("Encounter/$encounterId")
        try {
            val ans = CodeableConcept()
            ans.codingFirstRep.code = value.replace(" ", "")
            ans.codingFirstRep.display = value
            ans.codingFirstRep.system = "http://loinc.org"

            if (value.isNotEmpty()) {
                val obs = Observation()
                obs.id = it.id
                obs.value = ans
                obs.subject = subjectReference
                obs.encounter = encounterReference
                obs.code = it.code
                obs.issued = it.issued
                viewModel.updateObservation(obs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStringObservation(
        it: Observation,
        value: String,
        patientId: String,
        encounterId: String
    ) {
        val subjectReference = Reference("Patient/$patientId")
        val encounterReference = Reference("Encounter/$encounterId")
        try {
            val ans = StringType()
            ans.value = value
            if (value.isNotEmpty()) {
                val obs = Observation()
                obs.id = it.id
                obs.value = ans
                obs.subject = subjectReference
                obs.encounter = encounterReference
                obs.code = it.code
                obs.issued = it.issued
                viewModel.updateObservation(obs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateDateObservation(
        it: Observation,
        value: String,
        patientId: String,
        encounterId: String
    ) {
        val subjectReference = Reference("Patient/$patientId")
        val encounterReference = Reference("Encounter/$encounterId")
        try {
            val dateTime = DateTimeType()
            if (value.isNotEmpty()) {
                dateTime.value = FormatterClass().convertStringToDate(value, "yyyy-MM-dd")
                val obs = Observation()
                obs.id = it.id
                obs.value = dateTime
                obs.subject = subjectReference
                obs.encounter = encounterReference
                obs.code = it.code
                obs.issued = it.issued
                viewModel.updateObservation(obs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
}