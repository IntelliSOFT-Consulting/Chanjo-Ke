package com.intellisoft.chanjoke

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.chanjoke.add_patient.AddPatientFragment
import com.intellisoft.chanjoke.databinding.FragmentHomeBinding
import com.intellisoft.chanjoke.databinding.FragmentPractionerDetailsBinding
import com.intellisoft.chanjoke.fhir.data.FormatterClass

class PractionerDetails : Fragment() {

    private lateinit var _binding: FragmentPractionerDetailsBinding
    private val binding get() = _binding

    private val formatterClass = FormatterClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPractionerDetailsBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }
        getUserDetails()

        binding.bottomNavigationView

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_patient -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_vaccine -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }


    }

    private fun getUserDetails() {

        val practitionerFullNames = formatterClass.getSharedPref("practitionerFullNames", requireContext())
        val practitionerIdNumber = formatterClass.getSharedPref("practitionerIdNumber", requireContext())
        val practitionerRole = formatterClass.getSharedPref("practitionerRole", requireContext())
        val fhirPractitionerId = formatterClass.getSharedPref("fhirPractitionerId", requireContext())
        val practitionerId = formatterClass.getSharedPref("practitionerId", requireContext())

        binding.tvEmailAddress.text = ""
        binding.tvPhoneNumber.text = ""
        binding.tvIdNumber.text = practitionerIdNumber
        binding.tvFullName.text = practitionerFullNames

    }
}