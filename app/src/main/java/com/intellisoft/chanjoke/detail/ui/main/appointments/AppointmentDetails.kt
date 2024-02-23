package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

class AppointmentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailsBinding
    private lateinit var patientId: String
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private var formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager

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
            onSupportNavigateUp()
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

        val appointmentId = formatterClass.getSharedPref("appointmentId", this)
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



    }
}