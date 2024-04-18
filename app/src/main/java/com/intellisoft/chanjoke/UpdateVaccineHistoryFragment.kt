package com.intellisoft.chanjoke

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.databinding.FragmentUpdateVaccineHistoryBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar

class UpdateVaccineHistoryFragment : Fragment() {

    private lateinit var binding: FragmentUpdateVaccineHistoryBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private var patientYears:String? = null
    private var patientWeeks:String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentUpdateVaccineHistoryBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        sharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.vaccineList),
                Context.MODE_PRIVATE
            )

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        patientWeeks = formatterClass.getSharedPref("patientWeeks", requireContext())
        patientYears = formatterClass.getSharedPref("patientYears", requireContext())

        // Set up onBackPressedCallback to navigate back to Fragment 2 when in Fragment 3
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        })

        binding.tvDatePicker.setOnClickListener { showDatePickerDialog() }

        createVaccinationPlace()
        createVaccineType()

        // Inflate the layout for this fragment
        return binding.root


    }

    private fun createVaccineType(){

        //Check if years < 6 years
        val patientWeeksInt = patientWeeks?.toIntOrNull()
        if (patientWeeksInt != null && patientWeeksInt < 260){

            //get the vaccine not vaccinated
            val routineKeyList = sharedPreferences.getString("routineList", null)
            val expandableListTitle = routineKeyList!!.split(",").toList()

            val filteredList =expandableListTitle.filter { it.toInt() < patientWeeksInt }

            Log.e("---->","<-----")
            println("patientWeeksInt $patientWeeksInt")
            println("patientWeeks $patientWeeks")
            println("expandableListTitle $filteredList")
            Log.e("---->","<-----")
//            Get the administered list
            val administeredList = patientDetailsViewModel.getVaccineList()

            val newVaccineList = ArrayList<String>()

            filteredList.forEach { keyValue ->
                val weekNo = formatterClass.getVaccineScheduleValue(keyValue)
                val weekNoList = sharedPreferences.getStringSet(weekNo, null)
                weekNoList?.toList()?.forEach { vaccineToCheck ->
                    val exists = administeredList.any { it.vaccineName == vaccineToCheck }
                    if (!exists){
                        newVaccineList.add(vaccineToCheck)
                    }
                }
            }

            // Create an ArrayAdapter using the dataList and a default spinner layout
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                newVaccineList)

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.vaccineSpinner.adapter = adapter

            binding.vaccineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    // Handle the selection
                    val selectedItem = parent.getItemAtPosition(position) as String
                    // Perform your actions based on the selected item
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Handle the case when nothing is selected
                }
            }

        }else{
            Log.e("-=-=-=-","-=-=-=-")
        }

    }
    private fun createLastDose(){


        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.vaccine_place,
            android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding.lastDose.adapter = adapter

        binding.lastDose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selection
                val selectedItem = parent.getItemAtPosition(position) as String
                // Perform your actions based on the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case when nothing is selected
            }
        }
    }
    private fun createVaccinationPlace(){

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.vaccine_place,
            android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        binding.vaccinationPlace.adapter = adapter

        binding.vaccinationPlace.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selection
                val selectedItem = parent.getItemAtPosition(position) as String
                // Perform your actions based on the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case when nothing is selected
            }
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
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

}