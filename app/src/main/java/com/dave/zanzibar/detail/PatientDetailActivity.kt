package com.dave.zanzibar.detail

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navArgs
import com.dave.zanzibar.R
import com.dave.zanzibar.add_patient.AddPatientFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.dave.zanzibar.detail.ui.main.SectionsPagerAdapter
import com.dave.zanzibar.databinding.ActivityPatientDetailBinding
import com.dave.zanzibar.detail.ui.main.AppointmentsFragment
import com.dave.zanzibar.detail.ui.main.VaccinesFragment
import com.dave.zanzibar.fhir.FhirApplication
import com.dave.zanzibar.viewmodel.PatientDetailsViewModel
import com.dave.zanzibar.viewmodel.PatientDetailsViewModelFactory
import com.google.android.fhir.FhirEngine
import timber.log.Timber

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle =
            bundleOf(QUESTIONNAIRE_FILE_PATH_KEY to "immunization.json")
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, args.patientId),
            )
                .get(PatientDetailsViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = SectionsPagerAdapter(supportFragmentManager)
        val vaccine = VaccinesFragment()
        vaccine.arguments = bundle
        val apn = AppointmentsFragment()
        apn.arguments = bundle

        adapter.addFragment(vaccine, getString(R.string.tab_text_1))
        adapter.addFragment(apn, getString(R.string.tab_text_2))

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab
        patientDetailsViewModel.livePatientData.observe(this) {
            binding.apply {
                tvName.text = it.name
                tvGender.text = it.gender
                tvDob.text = it.dob
            }
        }
        patientDetailsViewModel.getPatientDetailData()
        fab.setOnClickListener { view ->

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the up button click here
                onBackPressed() // or navigateUp()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}