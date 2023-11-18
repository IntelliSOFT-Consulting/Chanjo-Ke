package com.intellisoft.chanjoke

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.intellisoft.chanjoke.databinding.ActivityMainBinding
import com.intellisoft.chanjoke.detail.ui.main.UpdateFragment
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val formatter = FormatterClass()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()
        when (intent.getStringExtra("functionToCall")) {
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
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.patient_list, R.id.updateFragment, R.id.editPatientFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    private fun editFunction(patientId: String) {

        /*      findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
                  .navigate(MainActivityDirections.actionToEditPatientFragment(patientId))
         */
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_option1 -> {

                findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(R.id.patientDetailActivity)
                true
            }

            R.id.menu_item_option2 -> {
                 viewModel.triggerOneTimeSync()
                true
            }
            // Add more cases for additional menu items if needed

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

}