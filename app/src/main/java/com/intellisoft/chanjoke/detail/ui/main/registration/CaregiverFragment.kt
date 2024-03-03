package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentCaregiverBinding
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.FormatterClass

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CaregiverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CaregiverFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val formatter = FormatterClass()
    private val viewModel: AddPatientViewModel by viewModels()
    private var mListener: OnButtonClickListener? = null
    private lateinit var binding: FragmentCaregiverBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity implements the interface
        mListener = if (context is OnButtonClickListener) {
            context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCaregiverBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())
        if (isUpdate != null) {
            displayInitialData()
        }

        val isAbove = formatter.getSharedPref("isAbove", requireContext())
        if (isAbove != null) {
            if (isAbove == "true") {
                binding.apply {
                    tvTitleName.text = "Next of Kin Details"
                }
            }
        }
        val suggestions = arrayOf(
            "Father",
            "Mother",
            "Guardian",
        )
        // Create ArrayAdapter with the array of strings
        val adapterType =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)

        binding.apply {
            identificationType.apply {
                setAdapter(adapterType)
//                adapter = adapterType
            }
            previousButton.apply {
                setOnClickListener {
                    mListener?.onPreviousPageRequested()
                }
            }
            nextButton.apply {
                setOnClickListener {
                    if (validData()) {
                        mListener?.onNextPageRequested()
                    }
                }
            }

        }
    }

    private fun displayInitialData() {
        try {
            val caregiver = formatter.getSharedPref("caregiver", requireContext())
            if (caregiver != null) {
                val data = Gson().fromJson(caregiver, CareGiver::class.java)
                binding.apply {
                    identificationType.setText(data.type)
                    name.setText(data.name)
                    phone.setText(data.phone)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validData(): Boolean {
        val kinType = binding.identificationType.text.toString()
        val kinName = binding.name.text.toString()
        val kinPhone = binding.phone.text.toString()

        if (kinType.isEmpty()) {
            Toast.makeText(requireContext(), "Please select type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (kinName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (kinPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter phone", Toast.LENGTH_SHORT).show()
            return false
        }
        val payload = CareGiver(kinType, kinName, kinPhone)
        formatter.saveSharedPref("caregiver", Gson().toJson(payload), requireContext())
        return true

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CaregiverFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CaregiverFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}