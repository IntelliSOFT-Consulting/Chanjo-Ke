package com.intellisoft.chanjoke.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.SectionsPagerAdapter
import com.intellisoft.chanjoke.databinding.ActivityPatientDetailBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.Sync
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.intellisoft.chanjoke.detail.ui.main.appointments.AppointmentsFragment
import com.intellisoft.chanjoke.detail.ui.main.campaigns.CampaignsDetailsFragment
import com.intellisoft.chanjoke.detail.ui.main.non_routine.NonRoutineFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.CompleteDetailsActivity
import com.intellisoft.chanjoke.detail.ui.main.routine.RoutineFragment
import com.intellisoft.chanjoke.fhir.data.DbTempData
import com.intellisoft.chanjoke.fhir.data.FhirSyncWorker
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.Reasons
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String

    //    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding
    private var formatterClass = FormatterClass()
    private val adapterSection = SectionsPagerAdapter(supportFragmentManager)
    private var patientYears: String? = null


    private val immunizationHandler = ImmunizationHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        val patientDob = FormatterClass().getSharedPref("patientDob", this).toString()
        val convertedDob = formatterClass.convertChildDateFormat(patientDob)
        if (convertedDob != null) {
            formatterClass.saveSharedPref("patientDob", convertedDob, this)
        }

        patientYears = formatterClass.getSharedPref("patientYears", this)

        setupSpinner()
        val bundle =
            bundleOf("patient_id" to patientId)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    this.application,
                    fhirEngine,
                    patientId
                ),
            )
                .get(PatientDetailsViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Check if this is a Campaigns
        val selectedFacility = formatterClass.getSharedPref("selectedFacility", this)


        val routineFragment = RoutineFragment()
        routineFragment.arguments = bundle

        val nonRoutineFragment = NonRoutineFragment()
        nonRoutineFragment.arguments = bundle

//        val appointment = AppointmentsFragment()
//        appointment.arguments = bundle

        val campaignsFragment = CampaignsDetailsFragment()
        campaignsFragment.arguments = bundle

        //Perform campaigns related job
        if (selectedFacility == "Campaigns"){
            adapterSection.addFragment(campaignsFragment, getString(R.string.campaigns))

        }else{
            //Perform a check if user is more than 5 years old
            if (isBelowFive()) {
                adapterSection.addFragment(routineFragment, getString(R.string.tab_text_1))
            }

            adapterSection.addFragment(nonRoutineFragment, getString(R.string.tab_text_2))
//        adapter.addFragment(appointment, getString(R.string.tab_text_4))
        }



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
        viewPager.adapter = adapterSection
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        binding.tvAppointment.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.APPOINTMENT.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        binding.tvAllDetails.apply {
            setOnClickListener {
                startActivity(
                    Intent(
                        this@PatientDetailActivity,
                        CompleteDetailsActivity::class.java
                    )
                )
            }
        }

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
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }

    }

    private fun isBelowFive(): Boolean {
        if (patientYears != null) {
            val patientYearsInt = patientYears!!.toIntOrNull()
            if (patientYearsInt != null) {
                if (patientYearsInt < 6) {
                    return true
                }
            }
        }
        return false
    }

    private fun setupSpinner() {

        val actionList = listOf("", "All Details", "Appointment", "Community Referrals")
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, actionList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinnerLocation.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinnerLocation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedAction = actionList[position]
                    when (selectedAction) {
                        "All Details" -> {
                            startActivity(
                                Intent(
                                    this@PatientDetailActivity,
                                    CompleteDetailsActivity::class.java
                                )
                            )
                        }

                        "Appointment" -> {
                            val intent =
                                Intent(this@PatientDetailActivity, MainActivity::class.java)
                            intent.putExtra("functionToCall", NavigationDetails.APPOINTMENT.name)
                            intent.putExtra("patientId", patientId)
                            startActivity(intent)
                        }

                        "Community Referrals" -> {

                            // temporarily store details
                            val temp = DbTempData(
                                name = binding.tvName.text.toString(),
                                dob = binding.tvDob.text.toString(),
                                gender = binding.tvGender.text.toString(),
                                age = binding.tvAge.text.toString(),
                            )
                            FormatterClass().saveSharedPref(
                                "temp_data",
                                Gson().toJson(temp),
                                this@PatientDetailActivity
                            )
                            val intent =
                                Intent(this@PatientDetailActivity, MainActivity::class.java)
                            intent.putExtra("functionToCall", NavigationDetails.REFERRALS.name)
                            intent.putExtra("patientId", patientId)
                            startActivity(intent)

                        }

                        else -> {
                            // Handle default case or do nothing
                        }
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // Do nothing here
                }
            }
    }


    override fun onStart() {
        super.onStart()

        Sync.oneTimeSync<FhirSyncWorker>(this)


        getPatientDetails()


        val patientListAction = formatterClass.getSharedPref("patientListAction", this)
        if (patientListAction != null && patientListAction == NavigationDetails.APPOINTMENT.name) {
            formatterClass.deleteSharedPref("patientListAction", this)
            goAppointments()
        }

    }

    private fun goAppointments() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", NavigationDetails.APPOINTMENT.name)
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }


    private fun getPatientDetails() {

        CoroutineScope(Dispatchers.IO).launch {

            formatterClass.clearVaccineShared(this@PatientDetailActivity)

            formatterClass.saveSharedPref("isPaged", "false", this@PatientDetailActivity)

            val observationDateValue =
                patientDetailsViewModel.getObservationByCode(patientId, null, "861-122")
            val isPaged = observationDateValue.value.replace(" ", "")
            if (isPaged != "" && isPaged == "Yes") {
                formatterClass.saveSharedPref("isPaged", "true", this@PatientDetailActivity)
            }

            val patientDetail = patientDetailsViewModel.getPatientInfo()
            val gender = patientDetail.gender
            val isAlive = if (patientDetail.isAlive) "YES" else "NO"
            formatterClass.saveSharedPref("patientGender", gender, this@PatientDetailActivity)
            formatterClass.saveSharedPref("patientAlive", isAlive, this@PatientDetailActivity)


            getRecommendation()

            CoroutineScope(Dispatchers.Main).launch {
                binding.apply {
                    val formattedName = if(patientDetail.isAlive){
                        patientDetail.name
                    }else{
                        "${patientDetail.name} (Deceased)"
                    }
                    tvName.text = formattedName
                    tvGender.text = AppUtils().capitalizeFirstLetter(patientDetail.gender)
                    tvSystemId.text = patientDetail.systemId

                    val dob = formatterClass.convertDateFormat(patientDetail.dob)
                    val age = formatterClass.getFormattedAge(
                        patientDetail.dob,
                        tvAge.context.resources,
                        this@PatientDetailActivity
                    )
//                    val dobAge = "$dob ($age old)"

                    val dobFormatted = dob?.let { formatterClass.convertViewDateFormats(it) }
                    tvDob.text = dobFormatted
                    tvAge.text = "$age old"

                }
            }


        }
    }

    private fun getRecommendation() {

        val notAdministeredList = ArrayList(
            listOf(
                "371900001",
                )
        )
        val clientObjectionList = patientDetailsViewModel.getRecommendationStatus(notAdministeredList)

        var message = "This patient has not received certain vaccines. \nFind the details:\n"
        var isDialog = false

        clientObjectionList.forEach {
            val statusValue = it.statusValue
            val vaccineName = it.vaccineName

            if (statusValue != null ){
                if(statusValue.contains("Client objection") ||
                    statusValue.contains("Religion/Culture")){
                    val dialogMessage = "$vaccineName because of $statusValue \n"
                    message += dialogMessage
                    isDialog = true
                }
                if (statusValue.contains("Contraindicate")){
                    formatterClass.saveSharedPref("${Reasons.CONTRAINDICATE.name} VALUES",vaccineName, this)
                }
            }
        }
        message += "Please review the patient's vaccination status and proceed accordingly."

//        if (isDialog){
//            CoroutineScope(Dispatchers.Main).launch { showCancelDialog("Vaccine Administration Notice", message) }
//        }

    }

    private fun showCancelDialog(title: String, message: String) {
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.apply {
            setTitle(title)
            setMessage(message)
            setCancelable(true) // Allows the dialog to be canceled by tapping outside of it
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Closes the dialog when the "Cancel" button is clicked
            }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", NavigationDetails.CLIENT_LIST.name)
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_client_details -> {
                startActivity(
                    Intent(
                        this@PatientDetailActivity,
                        CompleteDetailsActivity::class.java
                    )
                )
                true
            }

            R.id.menu_appointments -> {
                goAppointments()
                true
            }

            R.id.menu_referrals -> {
                // temporarily store details
                val temp = DbTempData(
                    name = binding.tvName.text.toString(),
                    dob = binding.tvDob.text.toString(),
                    gender = binding.tvGender.text.toString(),
                    age = binding.tvAge.text.toString(),
                )
                FormatterClass().saveSharedPref(
                    "temp_data",
                    Gson().toJson(temp),
                    this@PatientDetailActivity
                )
                val intent =
                    Intent(this@PatientDetailActivity, MainActivity::class.java)
                intent.putExtra("functionToCall", NavigationDetails.REFERRALS.name)
                intent.putExtra("patientId", patientId)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}