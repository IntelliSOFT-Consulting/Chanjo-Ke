package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentCaregiverBinding
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.AppUtils
import timber.log.Timber

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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CareGiverAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCaregiverBinding.inflate(layoutInflater)

        return binding.root
    }

    private val careGivers = ArrayList<CareGiver>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        careGivers.clear()
        adapter = CareGiverAdapter(
            ArrayList(),
            requireContext(),
            this::handleClick
        ) // Initialize with an empty list
        val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())
        Timber.e("TAG******* Caregiver Message $isUpdate")
        if (isUpdate != null) {
            displayInitialData()
        }
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }

        binding.phone.apply {
            setOnFocusChangeListener { _, hasFocus ->
                hint = if (hasFocus) {
                    "07xxxxxxxx"
                } else {
                    null
                }
            }
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
            }
            previousButton.apply {
                setOnClickListener {
                    mListener?.onPreviousPageRequested()
                }
            }
            nextButton.apply {
                setOnClickListener {
                    if (careGivers.isNotEmpty()) {
                        formatter.saveSharedPref(
                            "caregiver",
                            Gson().toJson(careGivers),
                            requireContext()
                        )
                        mListener?.onNextPageRequested()
                    }
                }
            }

            addBtn.apply {
                setOnClickListener {
                    if (validData()) {
                        val kinType = binding.identificationType.text.toString()
                        val kinName = binding.name.text.toString()
                        val kinPhone = binding.phone.text.toString()
                        val careGiver = CareGiver(
                            phone = kinPhone,
                            name = kinName,
                            type = kinType
                        )
                        val existingCareGiverIndex = careGivers.indexOfFirst { it.type == kinType }
                        if (existingCareGiverIndex != -1) {
                            binding.apply {
                                nextButton.isEnabled = true
                            }
                        } else {
                            careGivers.add(careGiver)
                            adapter.addItem(careGiver)
                        }
                        binding.apply {
                            identificationType.text = null
                            name.text = null
                            phone.text = null
                            nextButton.isEnabled = true
                        }
                    }
                }
            }

            phone.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString()
                        if (value.isNotEmpty()) {
                            // check if length is greater/equal to 10
                            if (value.length >= 10) {
                                updateCareGiver()
                            }

                        }
                    }
                })

            }

        }
    }

    private fun handleClick(careGiver: CareGiver) {
        /**
         * Handle click
         */
        val existingCareGiverIndex = careGivers.indexOfFirst { it.type == careGiver.type }
        if (existingCareGiverIndex != -1) {
            careGivers.remove(careGiver)
            adapter.removeItem(careGiver)
        }
    }

    private fun updateCareGiver() {
        if (validData()) {
            val kinType = binding.identificationType.text.toString()
            val kinName = binding.name.text.toString()
            val kinPhone = binding.phone.text.toString()
            val careGiver = CareGiver(
                phone = kinPhone,
                name = AppUtils().capitalizeFirstLetter(kinName),
                type = kinType
            )
            //only add if there is no caregiver type, else update that index
            val existingCareGiverIndex = careGivers.indexOfFirst { it.type == kinType }
            if (existingCareGiverIndex != -1) {
                // Update existing caregiver
                careGivers[existingCareGiverIndex] = careGiver
//                adapter.updateItem(existingCareGiverIndex, careGiver)
            } else {
                // Add new caregiver
                if (!careGivers.contains(careGiver)) {
                    careGivers.add(careGiver)
                    adapter.addItem(careGiver)
                }


            }
            binding.apply {
                nextButton.isEnabled = true
            }
        }
    }

    private fun displayInitialData() {
        try {
            val caregiver = formatter.getSharedPref("caregiver", requireContext())

            if (caregiver != null) {

                val type = object : TypeToken<List<CareGiver>>() {}.type
                val caregiverList: List<CareGiver> = Gson().fromJson(caregiver, type)

                caregiverList.forEach { data ->
                    binding.apply {
                        identificationType.setText(data.type)
                        name.setText(data.name)
                        phone.setText(data.phone)
                    }

                    //add initial caregiver

                    try {
                        if (data.phone.length >= 10) {
                            updateCareGiver()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        val isAbove = formatter.getSharedPref("isAbove", requireContext())
        if (isAbove != null) {
            if (isAbove == "true") {
                binding.apply {
                    tvTitleName.text = "Next of Kin Details"
                }
            }
        }
        try {
            val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())
            if (isUpdate != null) {
                displayInitialData()
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
        if (kinPhone.length != 10) {
            Toast.makeText(requireContext(), "Please valid enter phone", Toast.LENGTH_SHORT).show()
            return false
        }
//        val payload = CareGiver(kinType, kinName, kinPhone)
//        formatter.saveSharedPref("caregiver", Gson().toJson(payload), requireContext())
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