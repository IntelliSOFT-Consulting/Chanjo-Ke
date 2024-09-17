package com.intellisoft.chanjoke.detail.ui.main.aefis

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAddAefiBinding
import com.intellisoft.chanjoke.detail.ui.main.aefis.edit.ActionFragment
import com.intellisoft.chanjoke.detail.ui.main.aefis.edit.ReviewFragment
import com.intellisoft.chanjoke.detail.ui.main.aefis.edit.TypeFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.detail.ui.main.registration.ViewPagerAdapter
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.ActivityBlurBackground
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.viewmodel.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Type
import timber.log.Timber
import java.util.Date
import java.util.UUID

class AddAefiActivity : AppCompatActivity(), OnButtonClickListener {
    private lateinit var binding: ActivityAddAefiBinding
    private val viewModel: ScreenerViewModel by viewModels()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val formatter = FormatterClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAefiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Processing..")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.apply {
            supportActionBar?.apply {
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                title = ""

            }
            setupViewPager(binding.viewpager)
            binding.tabs.setupWithViewPager(binding.viewpager)
        }
        observeSubmission()
    }

    private fun observeSubmission() {
        viewModel.isResourcesSaved.observe(this) {
            progressDialog.dismiss()
            if (!it) {
                Toast.makeText(this, "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val blurBackgroundDialog =
                ActivityBlurBackground(this, this@AddAefiActivity)
            blurBackgroundDialog.show()


        }
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

    override fun onNextPageRequested() {
        binding.apply {
            if (viewpager.currentItem < pagerAdapter.count - 1) {
                // Move to the next page in ViewPager
                val nextPageIndex = viewpager.currentItem + 1
                viewpager.setCurrentItem(nextPageIndex, true)

            } else {
                val data = formatter.getSharedPref("updated_aefi_data", this@AddAefiActivity)

                val patientId = FormatterClass().getSharedPref("patientId", this@AddAefiActivity)

                if (data != null) {
                    val refinedData = Gson().fromJson(data, AEFIData::class.java)
                    // create observations
                    val encounterId = generateUuid()

                    val encounterReference = Reference("Encounter/$encounterId")
                    val observations = mutableListOf<Observation>()

                    val isDead: Boolean = refinedData.outcome == "Died"
                    Timber.e("Patient Status ****** $isDead")
                    // Add each observation to the list
                    observations.add(
                        createFHIRObservation(
                            refinedData.type,
                            "882-22",
                            encounterReference, "Type of AEFI", "code"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.brief,
                            "833-22",
                            encounterReference, "Brief details of the Event", "string"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.onset,
                            "833-23",
                            encounterReference, "Date of Onset of Event", "date"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.history,
                            "833-21",
                            encounterReference, "Past Medical History", "string"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.specimen,
                            "886-1",
                            encounterReference, "Specimen Collected", "string"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.severity,
                            "880-11",
                            encounterReference, "Reaction Severity", "code"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.action,
                            "888-1",
                            encounterReference, "Action Taken", "string"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.outcome,
                            "808-11",
                            encounterReference, "Reaction Outcome", "code"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.reporter,
                            "133-22",
                            encounterReference, "Person Reporting", "string"
                        )
                    )
                    observations.add(
                        createFHIRObservation(
                            refinedData.phone,
                            "122-22",
                            encounterReference, "Reporter Phone", "string"
                        )
                    )

                    viewModel.addAeFi(
                        this@AddAefiActivity,
                        patientId.toString(),
                        encounterId,
                        observations,isDead
                    )
                }
            }
        }

    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun createFHIRObservation(
        valueData: String,
        code: String,
        encounterReference: Reference,
        display: String,
        type: String
    ): Observation {

        val patientId = FormatterClass().getSharedPref("patientId", this@AddAefiActivity)
        val subjectReference = Reference("Patient/$patientId")
        val obs = Observation()
        val coding = Coding()
        coding.code = code
        coding.system = "http://loinc.org"
        coding.display = display
        val dataCode = CodeableConcept()
        dataCode.text = display
        dataCode.addCoding(coding)
        obs.id = generateUuid()
        obs.encounter = encounterReference
        obs.subject = subjectReference
        obs.issued = Date()
        obs.code = dataCode

        when (type) {
            "string" -> {
                val value = StringType()
                value.value = valueData
                obs.value = value
            }

            "code" -> {
                val value = CodeableConcept()
                value.codingFirstRep.code = code
                value.codingFirstRep.display = valueData
                value.codingFirstRep.system = "http://loinc.org"
                obs.value = value
            }

            "date" -> {
                val dateTime = DateTimeType()
                dateTime.value = FormatterClass().convertStringToDate(valueData, "yyyy-MM-dd")
                obs.value = dateTime
            }
        }
        return obs

    }

    override fun onPreviousPageRequested() {
        binding.apply {
            if (viewpager.currentItem > 0) {
                // Move to the previous page in ViewPager
                val previousPageIndex = viewpager.currentItem - 1
                viewpager.setCurrentItem(previousPageIndex, true)
            } else {
                Timber.e("TAG: First Item")
            }
        }
    }

    override fun onCancelPageRequested() {

    }
}