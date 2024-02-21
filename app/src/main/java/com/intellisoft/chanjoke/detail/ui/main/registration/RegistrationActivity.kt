package com.intellisoft.chanjoke.detail.ui.main.registration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var previousButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""

        }
        tabLayout = binding.tabs
        viewPager = binding.viewpager

        setupViewPager(viewPager)

        tabLayout.setupWithViewPager(viewPager)

//        previousButton = findViewById(R.id.previous_button)
//        nextButton = findViewById(R.id.next_button)
//
//        // Initially hide the previous button if it's the first fragment
//        if (viewPager.currentItem == 0) {
//            previousButton.visibility = View.GONE
//        }
//
//        previousButton.setOnClickListener {
//            val currentItem = viewPager.currentItem
//            if (currentItem > 0) {
//                viewPager.currentItem = currentItem - 1
//            }
//        }
//
//        nextButton.setOnClickListener {
//            val currentItem = viewPager.currentItem
//            if (currentItem < viewPager.adapter!!.count - 1) {
//                viewPager.currentItem = currentItem + 1
//            } else {
//                // If it's the last fragment, change the button text to "Review"
//                nextButton.text = "Review"
//            }
//        }
//
//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
//
//            override fun onPageSelected(position: Int) {
//                // Update visibility of previous button when page changes
//                if (position == 0) {
//                    previousButton.visibility = View.GONE
//                } else {
//                    previousButton.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {}
//        })
    }
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(PersonalFragment(), "Fragment 1")
        adapter.addFragment(CaregiverFragment(), "Fragment 2")
        adapter.addFragment(AdministrativeFragment(), "Fragment 3")
        viewPager.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}