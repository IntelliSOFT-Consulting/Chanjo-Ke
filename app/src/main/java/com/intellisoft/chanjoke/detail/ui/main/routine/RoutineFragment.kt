package com.intellisoft.chanjoke.detail.ui.main.routine

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.databinding.FragmentRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.adapters.VaccineScheduleAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbStatusColor
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.BottomSheetDialog
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RoutineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoutineFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRoutineBinding
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
        binding = FragmentRoutineBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        getRoutine()

        return binding.root

    }


    private fun getRoutine() {

        val expandableListDetail = ImmunizationHandler().generateDbVaccineSchedule()
        val expandableListTitle = ArrayList<String>(expandableListDetail.keys)

//        var  daysToValue = 0
//        val patientDob = FormatterClass().getSharedPref("patientDob", requireContext())
//        if (patientDob != null){
//            val daysTo = FormatterClass().daysBetweenTodayAndGivenDate(patientDob)
//            if (daysTo != null){
//                daysToValue = daysTo.toInt()
//            }
//        }

        //Get the administered list
        val administeredList = patientDetailsViewModel.getVaccineList()
        val statusColorsList = ArrayList<DbStatusColor>()
        for (keys in expandableListTitle){

            val vaccines = expandableListDetail[keys]
            val administeredVaccineNames = administeredList.map { it.vaccineName }

            var statusColor = ""
            if (vaccines != null) {
                if (vaccines.all { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
                    //Checks if all have been vaccinated
                    statusColor = StatusColors.GREEN.name
                }else if (vaccines.any { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
                    //Checks if there's any that has been vaccinated
                    statusColor = StatusColors.AMBER.name
                }else{
                    //Everything under here does not have any vaccines
                    /**
                     * 1. Check if current date is past the number of weeks from birth
                     * 2. If current date is past the number of weeks from birth, eka red if not weka black
                     */
                    statusColor = StatusColors.NORMAL.name
                }
            }

            val dbStatusColor = DbStatusColor(keys, statusColor)
            statusColorsList.add(dbStatusColor)
        }


        val vaccineScheduleAdapter = VaccineScheduleAdapter(
            requireContext(),
            administeredList,
            statusColorsList,
            expandableListTitle,
            expandableListDetail,
            patientDetailsViewModel,
            binding.tvAdministerVaccine)

        binding.expandableListView.setAdapter(vaccineScheduleAdapter)

        binding.tvAdministerVaccine.setOnClickListener {

            val checkedStates = vaccineScheduleAdapter.getCheckedStates()
            val vaccineNameList = ArrayList<String>()
            checkedStates.forEach {
                val vaccineName = it.vaccineName
                vaccineNameList.add(vaccineName)
            }
            formatterClass.saveSharedPref(
                "selectedVaccineName",
                vaccineNameList.joinToString(","),
                requireContext())
            formatterClass.saveSharedPref(
                "selectedUnContraindicatedVaccine",
                vaccineNameList.joinToString(","),
                requireContext())

            val bottomSheet = BottomSheetDialog()
            fragmentManager?.let { it1 ->
                bottomSheet.show(it1,
                "ModalBottomSheet") }


        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RoutineFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RoutineFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}