package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAppointmentDetailsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AppointmentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailsBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private var formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var appointmentId:String? = null
    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        val toolBar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        appointmentId = formatterClass.getSharedPref("appointmentId", this)
        val appointmentFlow = formatterClass.getSharedPref("appointmentFlow", this)
        if (appointmentFlow != null){
            if (appointmentFlow == "addAppointment") binding.btnCloseAppointment.text = "Save"
            if (appointmentFlow == "viewAppointment") binding.btnCloseAppointment.text = "Close"
        } else binding.btnCloseAppointment.text = "Close"

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            )
                .get(PatientDetailsViewModel::class.java)

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.btnCloseAppointment.setOnClickListener {

            if (appointmentFlow != null ){
                if (appointmentFlow == "addAppointment"){
                    CoroutineScope(Dispatchers.IO).launch {
                        val tripleRecommendation = getAppointmentDetails()
                        administerVaccineViewModel.createAppointment(tripleRecommendation)
                    }
                    Toast.makeText(this, "Please wait as we create the appointment", Toast.LENGTH_SHORT).show()
                }

                formatterClass.deleteSharedPref("appointmentFlow", this)
            }

            onSupportNavigateUp()
        }
        binding.btnEditAppointment.setOnClickListener {
            if (appointmentId != null){

                val intent = Intent(this, AddAppointment::class.java)
                intent.putExtra("appointmentId", appointmentId)
                startActivity(intent)

            }else
                Toast.makeText(this, "You cannot edit the appointment", Toast.LENGTH_SHORT).show()

        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
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
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        getAppointments()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun getAppointments() {

        if (appointmentId != null){
            val appointmentList = patientDetailsViewModel.getAppointmentList()
            val recommendationList: ArrayList<DbAppointmentDetails>
            val dbAppointmentData = appointmentList.find { it.id == appointmentId }

            if (dbAppointmentData != null){
                recommendationList = dbAppointmentData.recommendationList!!

                val dateScheduled = dbAppointmentData.dateScheduled

                binding.tvDateScheduled.text = dateScheduled

                val appointmentDetailsAdapter = AppointmentDetailsAdapter(recommendationList, this)
                binding.recyclerView.adapter = appointmentDetailsAdapter
            }
        }else{
            val pairRecommendation = getAppointmentDetails()
            val appointmentDetailsAdapter = AppointmentDetailsAdapter(pairRecommendation.first, this)
            binding.recyclerView.adapter = appointmentDetailsAdapter

            binding.tvDateScheduled.text = pairRecommendation.second

        }





    }

    private fun getAppointmentDetails():Triple<ArrayList<DbAppointmentDetails>, String,String> {
        // This will handle the Preview data
        val appointmentVaccineTitle = formatterClass.getSharedPref("appointmentVaccineTitle",this)
        val appointmentListData = formatterClass.getSharedPref("appointmentListData",this)
        val appointmentDateScheduled = formatterClass.getSharedPref("appointmentDateScheduled",this)
        val recommendationList = ArrayList<DbAppointmentDetails>()


        val dateScheduled = appointmentDateScheduled ?: ""
        val title = appointmentVaccineTitle ?: ""
        if (appointmentListData != null && appointmentDateScheduled != null){

            val stringList = appointmentListData.split(",").toMutableList()
            val charList = ArrayList<String>(stringList)
            charList.forEach {
                val dbAppointmentDetails = DbAppointmentDetails(
                    "",
                    appointmentDateScheduled,
                    "",
                    "",
                    it,
                    ""
                )
                recommendationList.add(dbAppointmentDetails)
            }
        }
        return Triple(recommendationList, dateScheduled,title)



    }
}