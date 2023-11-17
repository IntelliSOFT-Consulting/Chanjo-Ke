package com.intellisoft.chanjoke.detail

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navArgs
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.SectionsPagerAdapter
import com.intellisoft.chanjoke.databinding.ActivityPatientDetailBinding
import com.intellisoft.chanjoke.detail.ui.main.AppointmentsFragment
import com.intellisoft.chanjoke.detail.ui.main.ClientDetailsFragment
import com.intellisoft.chanjoke.detail.ui.main.VaccinesFragment
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import com.google.android.fhir.FhirEngine

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle =
            bundleOf("patient_id" to args.patientId)
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
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
//        val cd = ClientDetailsFragment()
//        cd.arguments = bundle

        adapter.addFragment(vaccine, getString(R.string.tab_text_1))
        adapter.addFragment(apn, getString(R.string.tab_text_2))
//        adapter.addFragment(cd, getString(R.string.tab_text_3))

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        patientDetailsViewModel.livePatientData.observe(this) {
            binding.apply {
                tvName.text = it.name
                tvGender.text = AppUtils().capitalizeFirstLetter(it.gender)
                tvDob.text = it.dob
//                tvContact.text = it.contact_name
//                tvPhone.text = it.contact_phone
//                tvContactGender.text = it.contact_gender
            }
        }
        patientDetailsViewModel.getPatientDetailData()

    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.patient_caregivers_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                // Handle the up button click here
//                onBackPressed() // or navigateUp()
//                true
//            }
//
//            R.id.menu_item_option2 -> {
//                proceedToNavigate("editFunction")
//
//                true
//            }
//
//            R.id.menu_item_care_giver -> {
//                proceedToNavigate("careFunction")
//
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun proceedToNavigate(s: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", s)
        intent.putExtra("patientId", args.patientId)
        startActivity(intent)
    }

    fun updateFunction() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", "updateFunction")
        intent.putExtra("patientId", args.patientId)
        startActivity(intent)
    }

}