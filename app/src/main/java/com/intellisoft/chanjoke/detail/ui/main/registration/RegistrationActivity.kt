package com.intellisoft.chanjoke.detail.ui.main.registration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.ActivityRegistrationBinding
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.CompletePatient
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.ActivityBlurBackground
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import timber.log.Timber

class RegistrationActivity : AppCompatActivity(), OnButtonClickListener {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val formatter = FormatterClass()
    private val viewModel: AddPatientViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        observeSubmission()
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

    private fun observeSubmission() {
        viewModel.isPatientSaved.observe(this) {
            if (!it) {
                Toast.makeText(this, "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val blurBackgroundDialog =
                ActivityBlurBackground(this@RegistrationActivity)
            blurBackgroundDialog.show()

        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(PersonalFragment(), "Fragment 1")
        pagerAdapter.addFragment(CaregiverFragment(), "Fragment 2")
        pagerAdapter.addFragment(AdministrativeFragment(), "Fragment 3")
        pagerAdapter.addFragment(PreviewFragment(), "Fragment 4")
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
        } else {
            Timber.e("TAG: Last Item")

            val personal = formatter.getSharedPref("personal", this)
            val caregiver = formatter.getSharedPref("caregiver", this)
            val administrative = formatter.getSharedPref("administrative", this)

            if (personal != null && caregiver != null && administrative != null) {

                val refinedPersonal = Gson().fromJson(personal, CustomPatient::class.java)
                val refinedCaregiver = Gson().fromJson(caregiver, CareGiver::class.java)
                val refinedAdministrative = Gson().fromJson(personal, Administrative::class.java)
                val caregivers = ArrayList<CareGiver>()
                caregivers.clear()
                caregivers.add(refinedCaregiver)
                var completePatient = CompletePatient(
                    personal = refinedPersonal,
                    caregivers = caregivers,
                    administrative = refinedAdministrative
                )
                viewModel.saveCustomPatient(this, completePatient)

            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
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
}