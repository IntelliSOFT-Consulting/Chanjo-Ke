package com.intellisoft.chanjoke.detail.ui.main.contraindications

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentContraindicationsBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity

import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ContraindicationsFragment : Fragment() {
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

    private lateinit var binding: FragmentContraindicationsBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val selectedItemList = ArrayList<String>()
    private var selectedVaccineName:String? = null
    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()
    private var administrationFlowTitle :String? = null
    private var status :String = ""
    private var spinnerReasons = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContraindicationsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)        // Inflate the layout for this fragment

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)


        administrationFlowTitle = formatterClass.getSharedPref("administrationFlowTitle", requireContext())


        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        updateUI()

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

        createSpinner()
        createReasonsSpinner()

        binding.tvDatePicker.setOnClickListener { showDatePickerDialog() }

        binding.btnNext.setOnClickListener {
            /**
             * Get the list of items,
             * perform a contraindication,
             * remove them from the shared preference
             * save the new list to the shared pref
             */

            if (selectedVaccineName != null){
                val allDropdownList = selectedVaccineName!!.split(",").toList()
                val vaccineList = allDropdownList.subtract(selectedItemList.toSet())

                if (selectedItemList.isEmpty()){
                    Toast.makeText(requireContext(), "There's no vaccine available!", Toast.LENGTH_SHORT).show()
                }else{
                    formatterClass.saveSharedPref(
                        "selectedUnContraindicatedVaccine",
                        vaccineList.joinToString(","),
                        requireContext())
                    if (administrationFlowTitle != null)
                        formatterClass.saveSharedPref("vaccinationFlow",
                            administrationFlowTitle!!, requireContext())

                    val datePicker =  binding.tvDatePicker.text.toString()
                    val description =  binding.etDescription.text.toString()

                    if (!TextUtils.isEmpty(datePicker)){

                        val dobFormat = formatterClass.convertDateFormat(datePicker)
                        if (dobFormat != null) {
                            val dobDate = formatterClass.convertStringToDate(dobFormat, "MMM d yyyy")
                            if (dobDate != null) {

                                var toastMessage = ""
                                var  forecastReason = ""
                                if (administrationFlowTitle == NavigationDetails.CONTRAINDICATIONS.name){
                                    toastMessage = "The contraindication has been saved successfully."
                                    if (TextUtils.isEmpty(description)) binding.etDescription.error = "Field cannot be empty" else forecastReason = description
                                }
                                if (administrationFlowTitle == NavigationDetails.NOT_ADMINISTER_VACCINE.name) {
                                    if (spinnerReasons == "") Toast.makeText(requireContext(), "Please select a reason", Toast.LENGTH_SHORT).show() else forecastReason = spinnerReasons
                                }
                                if (forecastReason == ""){
                                    Toast.makeText(requireContext(), "Please select a reason!", Toast.LENGTH_SHORT).show()
                                }else{

                                    administerVaccineViewModel.createManualContraindication(
                                        selectedItemList.toList(),
                                        patientId,
                                        dobDate,
                                        status,
                                        null,
                                        forecastReason)


                                    if (vaccineList.isNotEmpty() && administrationFlowTitle == NavigationDetails.CONTRAINDICATIONS.name){
                                        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.administerNewFragment)
                                    }else{
                                        val blurBackgroundDialog = BlurBackgroundDialog(this, requireContext())
                                        blurBackgroundDialog.show()
                                    }
                                }



                            }

                        }
                    }else Toast.makeText(requireContext(), "Select a date", Toast.LENGTH_SHORT).show()
                }


            }else{
                Toast.makeText(requireContext(), "There's no vaccine available", Toast.LENGTH_SHORT).show()
            }


        }
        binding.btnBack.setOnClickListener { onBackPressed() }

    }



    private fun onBackPressed() {
        val intent = Intent(requireContext(), PatientDetailActivity::class.java)
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }

    private fun updateUI() {

        formatterClass.deleteSharedPref("administrationFlowTitle",requireContext())

        var titleString = ""
        if (administrationFlowTitle == NavigationDetails.CONTRAINDICATIONS.name){
            binding.etDescription.setHint("Enter Contraindications")

            binding.etDescription.visibility = View.VISIBLE
            binding.linearSpinner.visibility = View.GONE

            binding.tvInstructions.setText("Vaccines to Contraindicate")
            titleString = "Contraindications"
            status = "Contraindicated"
        }
        if (administrationFlowTitle == NavigationDetails.NOT_ADMINISTER_VACCINE.name){
            binding.etDescription.visibility = View.GONE
            binding.linearSpinner.visibility = View.VISIBLE
            binding.tvInstructions.setText("Vaccines Not Administered")
            titleString = "Not Administered"
            status = "Due"
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = titleString
//            setDisplayShowHomeEnabled(true)
//            setDisplayHomeAsUpEnabled(true)
        }


    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val maxCalendar = Calendar.getInstance()
//        maxCalendar.add(Calendar.DAY_OF_MONTH, -7)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Handle the selected date (e.g., update the TextView)
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvDatePicker.text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    // Handle back press in the fragment
    private fun createReasonsSpinner() {
        val resultList = listOf<String>(
            "Product out of stock",
            "Contraindication",
            "Cold chain break",
            "Client objection",
            "Caregiver refusal",
            "Expired product",
            "Client acquired the disease",
            "Immunization not carried out for other reasons",
            )
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resultList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinnerReasons.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinnerReasons.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item
                val selectedItem = parentView.getItemAtPosition(position).toString()
                spinnerReasons = selectedItem
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }
    }

    private fun createSpinner() {

        formatterClass.deleteSharedPref("selectedUnContraindicatedVaccine", requireContext())

        selectedVaccineName = formatterClass.getSharedPref("selectedVaccineName", requireContext())
        if (selectedVaccineName != null){
            val resultList = selectedVaccineName!!.split(",").toList()

            // Create an ArrayAdapter using the string array and a default spinner layout
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resultList)

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner
            binding.spinner.adapter = adapter

            // Set a listener to handle the item selection
            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    // Get the selected item
                    val selectedItem = parentView.getItemAtPosition(position).toString()

                    createContraindications(selectedItem)

                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // Do nothing here
                }
            }
        }


    }

    private fun createContraindications(selectedItem: String) {
        if (selectedItemList.contains(selectedItem)) selectedItemList.remove(selectedItem)
        selectedItemList.add(selectedItem)

        selectedItemList.remove("")

        val vaccineAdapter = ContraindicationsAdapter(selectedItemList,requireContext())
        binding.recyclerView.adapter = vaccineAdapter

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContraindicationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContraindicationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}