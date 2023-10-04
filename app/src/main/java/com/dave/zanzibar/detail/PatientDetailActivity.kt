package com.dave.zanzibar.detail

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import com.dave.zanzibar.R
import com.dave.zanzibar.add_patient.AddPatientFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.dave.zanzibar.detail.ui.main.SectionsPagerAdapter
import com.dave.zanzibar.databinding.ActivityPatientDetailBinding
import com.dave.zanzibar.detail.ui.main.AppointmentsFragment
import com.dave.zanzibar.detail.ui.main.VaccinesFragment

class PatientDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle =
            bundleOf(QUESTIONNAIRE_FILE_PATH_KEY to "immunization.json")
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SectionsPagerAdapter(supportFragmentManager)
        val vaccine = VaccinesFragment()
        vaccine.arguments = bundle
        val apn = AppointmentsFragment()
        apn.arguments = bundle

        adapter.addFragment(vaccine, getString(R.string.tab_text_1))
        adapter.addFragment(apn, getString(R.string.tab_text_2))

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}