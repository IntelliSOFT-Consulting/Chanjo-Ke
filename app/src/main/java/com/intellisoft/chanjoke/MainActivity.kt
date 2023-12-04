package com.intellisoft.chanjoke

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.chanjoke.databinding.ActivityMainBinding
import com.intellisoft.chanjoke.detail.ui.main.UpdateFragment
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.shared.Login
import com.intellisoft.chanjoke.viewmodel.MainActivityViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val formatter = FormatterClass()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }

//        //1
//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))
//
//        //2

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.landing_page)
                    true
                }

                R.id.navigation_patient -> {
                    navController.navigate(R.id.patient_list)
                    true
                }

                R.id.navigation_vaccine -> {
                    navController.navigate(R.id.patient_list)
                    true
                }

                R.id.navigation_profile -> {
                    viewModel.triggerOneTimeSync()

                    val intent = Intent(this, Login::class.java)
                    FormatterClass().saveSharedPref("isLoggedIn", "false", this)
                    startActivity(intent)
                    finish()

                    true
                }

                else -> false
            }
        }


        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()
        //        load initial landing page
        navController.navigate(R.id.landing_page)
        Timber.e("*********")
        Timber.e(intent.getStringExtra("functionToCall"))
        Timber.e("*********")
        when (intent.getStringExtra("functionToCall")) {
            "updateFunction" -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    updateFunction(patientId)
                }
            }

            "careFunction" -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    careFunction(patientId)
                }
            }

            "editFunction" -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    editFunction(patientId)
                }
            }

            NavigationDetails.ADMINISTER_VACCINE.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.administerVaccine)
                }
            }

            NavigationDetails.LIST_VACCINE_DETAILS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.vaccineDetailsFragment)
                }
            }
        }


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.landing_page,
                R.id.patient_list, R.id.updateFragment, R.id.editPatientFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)


    }

    private fun administerVaccine(patientId: String, administerVaccine: Int) {
        val questionnaireJson = formatter.getSharedPref("questionnaireJson", this)
        formatter.saveSharedPref("patientId", patientId, this)

        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, questionnaireJson)
        bundle.putString("patientId", patientId)
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            administerVaccine, bundle
        )

    }

    private fun editFunction(patientId: String) {

        formatter.saveSharedPref("patientId", patientId, this)
        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, "update.json")
        bundle.putString("patientId", patientId)
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.editPatientFragment, bundle
        )
    }

    private fun careFunction(patientId: String) {
        formatter.saveSharedPref("patientId", patientId, this)
        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, "update.json")
        bundle.putString("patientId", patientId)
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.careGiverFragment, bundle
        )
    }

    private fun updateFunction(patientId: String) {
        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, "update.json")
        bundle.putString("patientId", patientId)
        formatter.saveSharedPref("patientId", patientId, this)

        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.updateFragment,
            bundle
        )
    }

}