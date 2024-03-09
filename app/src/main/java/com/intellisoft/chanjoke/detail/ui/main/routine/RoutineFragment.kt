package com.intellisoft.chanjoke.detail.ui.main.routine

import android.app.Application
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.adapters.VaccineScheduleAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbStatusColor
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleGroup
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
import java.time.LocalDate
import java.time.Period
import kotlin.math.round


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

        CoroutineScope(Dispatchers.IO).launch {

            val expandableListDetail = ImmunizationHandler().generateDbVaccineSchedule()
            val expandableListTitle = ArrayList<String>(expandableListDetail.keys)
//            Get the administered list
            val recommendationList = patientDetailsViewModel.recommendationList()

            val administeredList = patientDetailsViewModel.getVaccineList()

            val dbVaccineScheduleGroupList = ArrayList<DbVaccineScheduleGroup>()
            expandableListTitle.forEach { keyValue->

                val vaccineList = expandableListDetail[keyValue]
                val weekNo = formatterClass.getVaccineScheduleValue(keyValue)

                val dbVaccineScheduleChildList = ArrayList<DbVaccineScheduleChild>()
                if (!vaccineList.isNullOrEmpty()){
                    vaccineList.forEach {basicVaccine: BasicVaccine ->
                        val vaccineName = basicVaccine.vaccineName

                        /**
                         * Check if the vaccine has already been vaccinated and get the color and the date administered
                         */
                        val dbVaccineScheduleChild =  formatterClass.getVaccineChildStatus(vaccineName, administeredList)
                        dbVaccineScheduleChildList.add(dbVaccineScheduleChild)
                    }
                }

                //Get the group color Code
                val statusColor = formatterClass.getVaccineGroupDetails(vaccineList, administeredList)

                val dbVaccineScheduleGroup = DbVaccineScheduleGroup(
                    weekNo,
                    statusColor,
                    "",
                    dbVaccineScheduleChildList
                )
                dbVaccineScheduleGroupList.add(dbVaccineScheduleGroup)

            }

            val newExpandableListDetail = HashMap<DbVaccineScheduleGroup, List<DbVaccineScheduleChild>>()

            for (group in dbVaccineScheduleGroupList) {
                newExpandableListDetail[group] = group.dbVaccineScheduleChildList
            }

            val newExpandableListTitle = ArrayList(newExpandableListDetail.keys)
            val sortedExpandableListTitle = newExpandableListTitle.sortedBy {
                when{
                    it.vaccineSchedule.startsWith("At Birth") -> 1
                    it.vaccineSchedule.endsWith("weeks") -> 2
                    it.vaccineSchedule.endsWith("months") -> 3
                    it.vaccineSchedule.endsWith("years") -> 4
                    else -> 5
                }
            }



            CoroutineScope(Dispatchers.Main).launch {

                for (group in sortedExpandableListTitle){
                    val groupLayout = layoutInflater.inflate(R.layout.vaccination_schedule, null) as RelativeLayout
                    val tvScheduleTime = groupLayout.findViewById<TextView>(R.id.tvScheduleTime)
                    val tvAefi = groupLayout.findViewById<TextView>(R.id.tvAefi)
                    val imageViewSchedule = groupLayout.findViewById<ImageView>(R.id.imageViewSchedule)

                    // Populate views with data from DbVaccineScheduleGroup
                    tvScheduleTime.text = group.vaccineSchedule
                    tvAefi.text = "Aefi(0)"
                    when (group.colorCode) {
                        StatusColors.GREEN.name -> {
                            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_green)
                        }

                        StatusColors.AMBER.name -> {
                            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_amber)
                        }

                        StatusColors.RED.name -> {
                            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_red)
                        }

                        else -> {
                            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_normal_dark)
                        }
                    }

                    // Add the cardview_item to linearLayoutId2
                    binding.mainLayout.addView(groupLayout)

                }






            }











//        var  daysToValue = 0
//        val patientDob = FormatterClass().getSharedPref("patientDob", requireContext())
//        if (patientDob != null){
//            val daysTo = FormatterClass().daysBetweenTodayAndGivenDate(patientDob)
//            if (daysTo != null){
//                daysToValue = daysTo.toInt()
//            }
//        }


//            val statusColorsList = ArrayList<DbStatusColor>()
//            for (keys in expandableListTitle){
//
//                val vaccines = expandableListDetail[keys]
//                val administeredVaccineNames = administeredList.map { it.vaccineName }
//
//                var statusColor = ""
//                if (vaccines != null) {
//                    statusColor = if (vaccines.all { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
//                        //Checks if all have been vaccinated
//                        StatusColors.GREEN.name
//                    }else if (vaccines.any { basicVaccine -> administeredVaccineNames.contains(basicVaccine.vaccineName) }){
//                        //Checks if there's any that has been vaccinated
//                        StatusColors.AMBER.name
//                    }else{
//                        //Everything under here does not have any vaccines
//                        StatusColors.NORMAL.name
//                    }
//                }
//
//                val dbStatusColor = DbStatusColor(keys, statusColor)
//                statusColorsList.add(dbStatusColor)
//            }
//
//            val patientDob = formatterClass.getSharedPref("patientDob",requireContext())
//            var years = 0
//            if (patientDob != null) {
//
//                val dob = formatterClass.convertDateFormat(patientDob)
//                if (dob != null){
//                    val dobDate = formatterClass.convertStringToDate(dob, "MMM d yyyy")
//                    if (dobDate != null) {
//                        val finalDate = formatterClass.convertDateToLocalDate(dobDate)
//                        val period = Period.between(finalDate, LocalDate.now())
//                        years = period.years
//
//                    }
//                }
//            }
//
//            //Convert to weeks
//            val weeks = years * 52
//
//            val vaccineScheduleAdapter = VaccineScheduleAdapter(
//                requireContext(),
//                administeredList,
//                recommendationList,
//                statusColorsList,
//                expandableListTitle,
//                expandableListDetail,
//                patientDetailsViewModel,
//                binding.tvAdministerVaccine, weeks)
//
//            if (years < 16){
//                binding.expandableListView.setAdapter(vaccineScheduleAdapter)
//            }
//


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