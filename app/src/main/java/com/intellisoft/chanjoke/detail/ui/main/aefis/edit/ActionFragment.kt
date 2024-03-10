package com.intellisoft.chanjoke.detail.ui.main.aefis.edit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityEditAefiBinding
import com.intellisoft.chanjoke.databinding.FragmentActionBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.Child
import com.intellisoft.chanjoke.fhir.data.FormatterClass

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionFragment : Fragment() {
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

    private var mListener: OnButtonClickListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity implements the interface
        mListener = if (context is OnButtonClickListener) {
            context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    private lateinit var binding: FragmentActionBinding
    private val formatter = FormatterClass()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()

        val actions = arrayOf(
            "Life Threatening", "Mild", "Moderate", "Severe", "Fatal"
        )
        val actionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, actions)

        val outcomes = arrayOf(
            "Recovered", "Recovering", "Not Recovered", "Unknown", "Died"
        )
        val outcomeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, outcomes)

        binding.apply {

            reaction.apply {
                setAdapter(actionAdapter)
            }
            outcome.apply {
                setAdapter(outcomeAdapter)
            }
            previousButton.apply {
                setOnClickListener {
                    mListener?.onPreviousPageRequested()
                }
            }
            nextButton.apply {
                setOnClickListener {
                    if (validateData()) {
                        mListener?.onNextPageRequested()
                    }
                }
            }
        }
    }

    private fun validateData(): Boolean {

        val reaction = binding.reaction.text.toString()
        val actionTaken = binding.actionTaken.text.toString()
        val outcome = binding.outcome.text.toString()
        val reporter = binding.reporter.text.toString()
        val phone = binding.phone.text.toString()

        if (reaction.isEmpty()) {
            Toast.makeText(requireContext(), "Please select reaction severity", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (actionTaken.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter action taken", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (outcome.isEmpty()) {
            Toast.makeText(requireContext(), "Please select outcome", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (reporter.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter reporter name", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter reporter phone", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        val data = Child(
            severity = reaction,
            action = actionTaken,
            outcome = outcome,
            reporter = reporter,
            phone = phone
        )
        formatter.saveSharedPref("child_aefi", Gson().toJson(data), requireContext())

        return true
    }

    private fun loadData() {
        val data = formatter.getSharedPref("aefi_data", requireContext())
        if (data != null) {
            val refinedData = Gson().fromJson(data, AEFIData::class.java)
            try {
                binding.apply {
                    reaction.setText(refinedData.severity.trim(), false)
                    actionTaken.setText(refinedData.action.trim())
                    outcome.setText(refinedData.outcome.trim(), false)
                    reporter.setText(refinedData.reporter.trim())
                    phone.setText(refinedData.phone.trim())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}