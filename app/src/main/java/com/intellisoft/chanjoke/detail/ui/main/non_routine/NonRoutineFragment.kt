package com.intellisoft.chanjoke.detail.ui.main.non_routine

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.databinding.FragmentNonRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.adapters.VaccineScheduleAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbStatusColor
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.BottomSheetDialog
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import java.time.LocalDate
import java.time.Period

/**
 * A simple [Fragment] subclass.
 * Use the [NonRoutineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NonRoutineFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentNonRoutineBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

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
        binding = FragmentNonRoutineBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

//        getNonRoutine()

        return binding.root
    }

    private fun getNonRoutine() {



        val expandableListDetail = ImmunizationHandler().generateNonRoutineVaccineSchedule()
        val expandableListTitle = ArrayList<String>(expandableListDetail.keys)

        //Get the administered list
        val administeredList = patientDetailsViewModel.getVaccineList()
        val recommendationList = patientDetailsViewModel.recommendationList(null)

        val statusColorsList = ArrayList<DbStatusColor>()
        for (keys in expandableListTitle){

            val vaccines = expandableListDetail[keys]
            val administeredVaccineNames = administeredList.map { it.vaccineName }

            var statusColor = ""
            if (vaccines != null) {
                if (vaccines.all { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
                    statusColor = StatusColors.GREEN.name
                }else if (vaccines.any { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
                    statusColor = StatusColors.AMBER.name
                }else{
                    statusColor = StatusColors.NORMAL.name
                }
            }

            val dbStatusColor = DbStatusColor(keys, statusColor)
            statusColorsList.add(dbStatusColor)
        }

        val patientDob = formatterClass.getSharedPref("patientDob",requireContext())
        var years = 0
        if (patientDob != null) {

            val dob = formatterClass.convertDateFormat(patientDob)
            if (dob != null){
                val dobDate = formatterClass.convertStringToDate(dob, "MMM d yyyy")
                if (dobDate != null) {
                    val finalDate = formatterClass.convertDateToLocalDate(dobDate)
                    val period = Period.between(finalDate, LocalDate.now())
                    years = period.years

                }
            }
        }
        //Convert to weeks
        val weeks = years * 52

//        val vaccineScheduleAdapter = VaccineScheduleAdapter(
//            requireContext(),
//            administeredList,
//            recommendationList,
//            statusColorsList,
//            expandableListTitle,
//            expandableListDetail,
//            patientDetailsViewModel,
//            binding.tvAdministerVaccine,
//            weeks
//        )
//        binding.expandableListView.setAdapter(vaccineScheduleAdapter)
//
//        binding.tvAdministerVaccine.setOnClickListener {
//
//            val checkedStates = vaccineScheduleAdapter.getCheckedStates()
//            val vaccineNameList = ArrayList<String>()
//            checkedStates.forEach {
//                val vaccineName = it.vaccineName
//                vaccineNameList.add(vaccineName)
//            }
//            formatterClass.saveSharedPref(
//                "selectedVaccineName",
//                vaccineNameList.joinToString(","),
//                requireContext())
//            formatterClass.saveSharedPref(
//                "selectedUnContraindicatedVaccine",
//                vaccineNameList.joinToString(","),
//                requireContext())
//
//            val bottomSheet = BottomSheetDialog()
//            fragmentManager?.let { it1 ->
//                bottomSheet.show(it1,
//                    "ModalBottomSheet") }
//
//
//        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NonRoutineFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NonRoutineFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}