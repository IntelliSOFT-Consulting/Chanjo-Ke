package com.intellisoft.chanjoke.detail.ui.main.aefis.edit

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityEditAefiBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.AdministrativeFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.CaregiverFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.detail.ui.main.registration.PersonalFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.PreviewFragment
import com.intellisoft.chanjoke.detail.ui.main.registration.ViewPagerAdapter
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import timber.log.Timber

class EditAefiActivity : AppCompatActivity(), OnButtonClickListener {
    private lateinit var binding: ActivityEditAefiBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val formatter = FormatterClass()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAefiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        observeSubmission()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Processing..")
        progressDialog.setCanceledOnTouchOutside(false)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
        tabLayout = binding.tabs
        viewPager = binding.viewpager
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(TypeFragment(), "Fragment 1")
        pagerAdapter.addFragment(ActionFragment(), "Fragment 2")
        pagerAdapter.addFragment(ReviewFragment(), "Fragment 3")
        viewPager.adapter = pagerAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun observeSubmission() {

    }

    override fun onNextPageRequested() {
        if (viewPager.currentItem < pagerAdapter.count - 1) {
            // Move to the next page in ViewPager
            val nextPageIndex = viewPager.currentItem + 1
            viewPager.setCurrentItem(nextPageIndex, true)

        } else {
            Timber.e("About to end the flow")
        }
    }

    override fun onPreviousPageRequested() {
        if (viewPager.currentItem > 0) {
            // Move to the previous page in ViewPager
            val previousPageIndex = viewPager.currentItem - 1
            viewPager.setCurrentItem(previousPageIndex, true)
        } else {
            Timber.e("TAG: First Item")
        }
    }
}