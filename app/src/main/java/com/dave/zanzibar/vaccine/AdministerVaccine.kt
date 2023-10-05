package com.dave.zanzibar.vaccine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dave.zanzibar.R
import com.dave.zanzibar.databinding.ActivityAdministerVaccineBinding
import com.dave.zanzibar.databinding.ActivityMainBinding
import com.dave.zanzibar.fhir.data.FormatterClass

class AdministerVaccine : AppCompatActivity() {

    private val formatterClass = FormatterClass()
    private lateinit var binding: ActivityAdministerVaccineBinding
    private var patientId :String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdministerVaccineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = formatterClass.getSharedPref("patientId",this)

        println(patientId)

    }
}