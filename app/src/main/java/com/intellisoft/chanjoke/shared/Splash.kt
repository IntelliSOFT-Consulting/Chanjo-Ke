package com.intellisoft.chanjoke.shared

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivitySplashBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Arrays

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


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

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            val expandableListDetail = ImmunizationHandler().generateDbVaccineSchedule()
            val expandableListTitle = ArrayList<String>(expandableListDetail.keys)

            val sharedPreferences: SharedPreferences =
                getSharedPreferences(getString(R.string.vaccineList), MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString("routineList",expandableListTitle.joinToString(","))
            editor.apply()

            expandableListTitle.forEach { keyValue ->
                val weekNo = formatterClass.getVaccineScheduleValue(keyValue)

                val setVaccineNameList = HashSet<String>()
                val vaccineList = expandableListDetail[keyValue]
                vaccineList?.forEach {basicVaccine ->
                    val vaccineName = basicVaccine.vaccineName
                    setVaccineNameList.add(vaccineName)
                }
                editor.putStringSet(weekNo,setVaccineNameList)
                editor.apply();
            }


//            FormatterClass().saveSharedPref("expandableListDetail",expandableListDetail.toString(),this@Splash)
//            FormatterClass().saveSharedPref("expandableListTitle",expandableListTitle.toString(),this@Splash)
//
//            val xxx = FormatterClass().getSharedPref("expandableListDetail",this@Splash) as HashMap<String, List<BasicVaccine>>
//            val yyy = FormatterClass().getSharedPref("expandableListTitle",this@Splash) as  ArrayList<String>
//
//            Log.e("*******","******")
//            println(xxx)
//            println(yyy)
//            Log.e("*******","******")
        }


    }
}