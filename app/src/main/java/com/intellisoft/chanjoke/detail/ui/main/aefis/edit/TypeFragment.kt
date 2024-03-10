package com.intellisoft.chanjoke.detail.ui.main.aefis.edit

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentActionBinding
import com.intellisoft.chanjoke.databinding.FragmentTypeBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Parent
import com.intellisoft.chanjoke.utils.AppUtils
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TypeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TypeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val formatter = FormatterClass()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentTypeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        AppUtils().disableEditing(binding.eventDate)

        val types = arrayOf(
            "High fever",
            "Convulsion",
            "Anaphylaxis",
            "Paralysis",
            "Toxic shock",
            "Injection site abcess",
            "Severe local reaction",
            "Generalised urticaria(hives)",
            "BCG Lymphadenitis",
            "Encaphalopathy, Encephalitis/menengitis",

        )
        // Create ArrayAdapter with the array of strings
        val adapterType =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)

        binding.apply {
            type.apply {
                setAdapter(adapterType)
            }
            eventDate.apply {
                setOnClickListener {
                    val calendar: Calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
                            val valueCurrent: String = formatter.getDate(year, month, day)
                            setText(valueCurrent)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.datePicker.maxDate = calendar.getTimeInMillis()
                    datePickerDialog.show()
                }
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
        val type = binding.type.text.toString()
        val brief = binding.brief.text.toString()
        val onset = binding.eventDate.text.toString()
        val history = binding.history.text.toString()

        if (type.isEmpty()) {
            binding.type.requestFocus()
            return false
        }
        if (brief.isEmpty()) {
            binding.brief.requestFocus()
            return false
        }
        if (onset.isEmpty()) {
            Toast.makeText(requireContext(), "Please select event date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (history.isEmpty()) {
            binding.history.requestFocus()
            return false
        }
        val data = Parent(
            type = type,
            brief = brief,
            onset = onset,
            history = history
        )
        formatter.saveSharedPref("parent_aefi", Gson().toJson(data), requireContext())

        return true
    }

    private fun loadData() {
        val data = formatter.getSharedPref("aefi_data", requireContext())
        if (data != null) {
            val refinedData = Gson().fromJson(data, AEFIData::class.java)
            try {
                binding.apply {
                    type.setText(refinedData.type.trim(), false)
                    brief.setText(refinedData.brief.trim())
                    eventDate.setText(refinedData.onset.trim())
                    history.setText(refinedData.history.trim())
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
         * @return A new instance of fragment TypeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TypeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}