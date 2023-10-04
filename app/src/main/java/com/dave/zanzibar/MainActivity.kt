package com.dave.zanzibar

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dave.zanzibar.databinding.ActivityMainBinding
import com.dave.zanzibar.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()

        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.patient_list,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

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
                showToast("Option 2 selected")
                true
            }
            // Add more cases for additional menu items if needed

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}