package com.dave.zanzibar.vaccine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ca.uhn.fhir.context.FhirContext
import com.dave.zanzibar.R
import com.dave.zanzibar.add_patient.AddPatientFragment
import com.dave.zanzibar.databinding.ActivityAdministerVaccineBinding
import com.dave.zanzibar.databinding.ActivityMainBinding
import com.dave.zanzibar.detail.ui.main.AppointmentsFragment
import com.dave.zanzibar.detail.ui.main.VaccinesFragment
import com.dave.zanzibar.fhir.data.FormatterClass

class AdministerVaccine : AppCompatActivity() {

    private val formatterClass = FormatterClass()
    private lateinit var binding: ActivityAdministerVaccineBinding
    private var patientId :String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdministerVaccineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        patientId = formatterClass.getSharedPref("patientId",this)

        val bundle = Bundle()
        bundle.putString(
            AdministerVaccineFragment.QUESTIONNAIRE_FILE_PATH_KEY,
            "vaccine-administration.json")

        val vaccine = AdministerVaccineFragment()
        vaccine.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_administer_vaccine, vaccine)
            .commit()

        println(patientId)

    }


}