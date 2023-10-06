package com.dave.zanzibar.detail.ui.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dave.zanzibar.MainActivity
import com.dave.zanzibar.R
import com.dave.zanzibar.databinding.FragmentHomeBinding
import com.dave.zanzibar.databinding.FragmentPatientDetailBinding
import com.dave.zanzibar.databinding.FragmentVaccinesBinding
import com.dave.zanzibar.detail.PatientDetailActivity
import com.dave.zanzibar.fhir.FhirApplication
import com.dave.zanzibar.fhir.data.FormatterClass
import com.dave.zanzibar.vaccine.AdministerVaccine
import com.dave.zanzibar.viewmodel.PatientDetailsViewModel
import com.dave.zanzibar.viewmodel.PatientDetailsViewModelFactory
import com.google.android.fhir.FhirEngine
import timber.log.Timber

/**
 * A placeholder fragment containing a simple view.
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class VaccinesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentVaccinesBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVaccinesBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        patientDetailsViewModel = ViewModelProvider(this,
           PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        binding.btnUpdate.apply {
            setOnClickListener {

                val activity = requireActivity()
                if (activity is PatientDetailActivity) {
                    activity.updateFunction()
                } else {
                    Timber.tag("YourFragment").e("Activity is not of type YourActivity")
                }
            }
        }
        binding.btnAdminister.setOnClickListener {
            val intent = Intent(requireContext(), AdministerVaccine::class.java)
            startActivity(intent)
        }

        getVaccinations()
        return binding.root
    }

    private fun getVaccinations() {

        val encounterList = patientDetailsViewModel.getEncounterList()

        println(encounterList)


        val vaccineAdapter = VaccineAdapter(encounterList,requireContext())
        binding.recyclerView.adapter = vaccineAdapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppointmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppointmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}