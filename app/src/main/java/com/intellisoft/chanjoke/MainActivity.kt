package com.intellisoft.chanjoke

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.chanjoke.add_patient.AddPatientFragment
import com.intellisoft.chanjoke.databinding.ActivityMainBinding
import com.intellisoft.chanjoke.detail.ui.main.UpdateFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.RegistrationActivity
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.viewmodel.MainActivityViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val formatter = FormatterClass()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

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

        fhirEngine = FhirApplication.fhirEngine(this)

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
                    navController.navigate(R.id.practionerDetails)
                    true
                }

                else -> false
            }
        }


        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()
        //        load initial landing page
        navController.navigate(R.id.landing_page)

        val functionToCall = intent.getStringExtra("functionToCall")
        Log.e("---->","<----")
        println("functionToCall $functionToCall")
        Log.e("---->","<----")

        when (functionToCall) {
            "registerFunction" -> {
                registerFunction()
            }

            "listClients" -> {
                navController.navigate(R.id.patient_list)
            }

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

            NavigationDetails.APPOINTMENT.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    contraindicationFunction(patientId, R.id.appointmentsFragment)
                }
            }

            NavigationDetails.VACCINE_DETAILS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    contraindicationFunction(patientId, R.id.vaccineDetailsFragment)
                }
            }

            NavigationDetails.ADMINISTER_VACCINE.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.administerVaccine)
                }
            }

            NavigationDetails.UPDATE_VACCINE_DETAILS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    contraindicationFunction(patientId, R.id.updateVaccineHistoryFragment)
                }
            }

            NavigationDetails.RESCHEDULE.name -> {
                contraindicationFunction("", R.id.contraindicationsFragment)
            }

            NavigationDetails.LIST_VACCINE_DETAILS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.vaccineDetailsFragment)
                }
            }

            NavigationDetails.LIST_AEFI.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    patientDetailsViewModel =
                        ViewModelProvider(
                            this,
                            PatientDetailsViewModelFactory(
                                this.application,
                                fhirEngine,
                                patientId.toString()
                            ),
                        ).get(PatientDetailsViewModel::class.java)
                    val current_age = FormatterClass().getSharedPref(
                        "current_age", this
                    )
                    if (current_age != null) {
                        Timber.tag("TAG").e("Created an Encounter with Started %s", current_age)
//                        patientDetailsViewModel.createAefiEncounter(this, patientId, current_age)
                    }
                    administerVaccine(patientId, R.id.aefisFragment)
                }
            }

            NavigationDetails.CLIENT_LIST.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.patient_list)
                }
            }

            NavigationDetails.ADD_AEFI.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.administerVaccine)
                }
            }

            NavigationDetails.EDIT_CLIENT.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    navController.navigate(R.id.editPatientFragment)
                }
            }

            NavigationDetails.REFERRALS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    navController.navigate(R.id.referralsFragment)
                }
            }

            NavigationDetails.REFERRAL_DETAILS.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    navController.navigate(R.id.referralDetailFragment)
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

    private fun contraindicationFunction(patientId: String, navigateInt: Int) {

        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            navigateInt
        )
    }

    private fun registerFunction() {
        formatter.deleteSharedPref("personal", this)
        formatter.deleteSharedPref("personal", this)
        formatter.deleteSharedPref("personal", this)
        val bundle = Bundle()
        bundle.putString(
            AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY,
            "new-patient-registration-paginated.json"
        )

        FormatterClass().deleteSharedPref("patientYears", this)
        FormatterClass().deleteSharedPref("patientWeeks", this)
        FormatterClass().deleteSharedPref("patientDob", this)
        FormatterClass().deleteSharedPref("patientId", this)

        startActivity(Intent(this@MainActivity, RegistrationActivity::class.java))
//        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
//            R.id.addPatientFragment,
//            bundle
//        )
    }

}