package com.intellisoft.chanjoke.detail

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.SectionsPagerAdapter
import com.intellisoft.chanjoke.databinding.ActivityPatientDetailBinding
import com.intellisoft.chanjoke.detail.ui.main.AppointmentsFragment
import com.intellisoft.chanjoke.detail.ui.main.VaccinesFragment
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String

    //    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding
    private var formatterClass = FormatterClass()

    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()
    val livePatientData: MutableLiveData<PatientDetailsViewModel.PatientData> = MutableLiveData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        val bundle =
            bundleOf("patient_id" to patientId)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
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

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Set the background color of the selected tab dynamically
                tab?.view?.setBackgroundResource(R.color.colorPrimary)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection if needed
                tab?.view?.setBackgroundResource(R.color.unselectedTab)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection if needed
            }
        })

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        getPatientDetails()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home item click
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_patient -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_vaccine -> {
                    // Handle notifications item click
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    // Handle notifications item click
                    Toast.makeText(this, "Under development", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

    }


    private fun getPatientDetails() {

        CoroutineScope(Dispatchers.IO).launch {
            val patientDetail = patientDetailsViewModel.getPatientInfo()
            CoroutineScope(Dispatchers.Main).launch {
                binding.apply {
                    tvName.text = patientDetail.name
                    tvGender.text = AppUtils().capitalizeFirstLetter(patientDetail.gender)
                    tvDob.text = formatterClass.convertDateFormat(patientDetail.dob)
                    tvAge.text = formatterClass.getFormattedAge(patientDetail.dob,tvAge.context.resources)

                }
            }



//            patientDetailsViewModel.livePatientData.observe(this) {
//                binding.apply {
//                    tvName.text = it.name
//                    tvGender.text = AppUtils().capitalizeFirstLetter(it.gender)
//                    tvDob.text = formatterClass.convertDateFormat(it.dob)
////                tvContact.text = it.contact_name
////                tvPhone.text = it.contact_phone
////                tvContactGender.text = it.contact_gender
//                }
//            }
        }





    }

//    CoroutineScope(Dispatchers.IO).launch {
//        formatterClass.saveSharedPref(
//            "vaccinationFlow",
//            "recommendVaccineDetails",
//            this@PatientDetailActivity
//        )
//        administerVaccineViewModel.generateImmunizationRecord("", patientId)
//    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun proceedToNavigate(s: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", s)
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }

    fun updateFunction() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", "updateFunction")
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }

}