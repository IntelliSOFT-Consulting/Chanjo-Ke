package com.intellisoft.chanjoke.shared

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.databinding.ActivitySplashBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.time.LocalDate

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val fhirEngine = FhirApplication.fhirEngine(this)
        val patientId = FormatterClass().getSharedPref("patientId", this).toString()

        val patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModelFactory(
                application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        val immunizationHandler = ImmunizationHandler()

        val ageInWeeks = 6 // Assuming age is 18 weeks

//        val vaccineList = patientDetailsViewModel.getVaccineList()
        val administeredList = ArrayList<BasicVaccine>()

//        val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName("BCG")
//        if (basicVaccine != null) {
//            administeredList.add(basicVaccine)
//        }

        val (groupList, childList) = immunizationHandler.getEligibleRoutineVaccines(
            administeredList, ageInWeeks)





        Handler().postDelayed({
            if (FormatterClass().getSharedPref("isLoggedIn", this@Splash) == "true") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }

        }, 1000)
    }
}