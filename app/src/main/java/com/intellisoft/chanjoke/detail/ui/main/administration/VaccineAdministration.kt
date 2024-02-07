package com.intellisoft.chanjoke.detail.ui.main.administration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.contraindications.ContraindicationsFragment
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class VaccineAdministration : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccine_administration)

        val contraindicationsFragment = ContraindicationsFragment()

//        // Add the fragment to the container
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, contraindicationsFragment)
//            .commit()

        when(intent.getStringExtra("functionToCall")){
            NavigationDetails.CONTRAINDICATIONS.name -> { contraindicationFunction() }
            NavigationDetails.ADMINISTER_VACCINE.name -> { administerFunction() }
        }

    }
    private fun administerFunction() {

        findNavController(R.id.fragment_container).navigate(
            R.id.administerNewFragment
        )
    }
    private fun contraindicationFunction() {

        findNavController(R.id.fragment_container).navigate(
            R.id.contraindicationsFragment
        )
    }
}