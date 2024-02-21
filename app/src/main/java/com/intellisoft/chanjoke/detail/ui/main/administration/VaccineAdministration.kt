package com.intellisoft.chanjoke.detail.ui.main.administration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.contraindications.ContraindicationsFragment
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class VaccineAdministration : AppCompatActivity() {

    private var formatterClass = FormatterClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccine_administration)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val contraindicationsFragment = ContraindicationsFragment()

//        // Add the fragment to the container
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, contraindicationsFragment)
//            .commit()

        when(intent.getStringExtra("functionToCall")){
            NavigationDetails.CONTRAINDICATIONS.name -> { contraindicationFunction(NavigationDetails.CONTRAINDICATIONS.name) }
            NavigationDetails.ADMINISTER_VACCINE.name -> { administerFunction() }
            NavigationDetails.NOT_ADMINISTER_VACCINE.name -> { contraindicationFunction(NavigationDetails.NOT_ADMINISTER_VACCINE.name) }
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    private fun administerFunction() {
        findNavController(R.id.fragment_container).navigate(
            R.id.administerNewFragment
        )
    }
    private fun contraindicationFunction(name: String) {
        formatterClass.saveSharedPref("administrationFlowTitle",name, this)
        findNavController(R.id.fragment_container).navigate(
            R.id.contraindicationsFragment
        )
    }
}