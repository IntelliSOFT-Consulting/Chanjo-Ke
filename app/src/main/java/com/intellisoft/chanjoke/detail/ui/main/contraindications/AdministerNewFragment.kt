package com.intellisoft.chanjoke.detail.ui.main.contraindications

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentAdministerNewBinding
import com.intellisoft.chanjoke.databinding.FragmentContraindicationsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdministerNewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdministerNewFragment : Fragment() {
    private lateinit var resultList: MutableList<String>

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAdministerNewBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()

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
        // Inflate the layout for this fragment

        binding = FragmentAdministerNewBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.btnAdministerVaccine.setOnClickListener {
            if (resultList.isNotEmpty()){
                administerVaccineViewModel.createManualImmunizationResource(resultList, formatterClass.generateUuid(), patientId)
                val blurBackgroundDialog = BlurBackgroundDialog(this, requireContext())
                blurBackgroundDialog.show()
            }

        }

        getBatchNumbers()

        return binding.root
    }

    private fun getBatchNumbers() {

        val selectedUnContraindicatedVaccine = formatterClass.getSharedPref("selectedUnContraindicatedVaccine", requireContext())
        if (selectedUnContraindicatedVaccine != null) {
            resultList = selectedUnContraindicatedVaccine.split(",").toList().toMutableList()
            val vaccineAdapter = AdministerNewAdapter(resultList,requireContext())
            binding.recyclerView.adapter = vaccineAdapter

        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdministerNewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdministerNewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}