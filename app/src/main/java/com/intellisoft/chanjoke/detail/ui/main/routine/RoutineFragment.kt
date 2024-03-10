package com.intellisoft.chanjoke.detail.ui.main.routine

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.VaccineDetailsAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleGroup
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.BottomSheetDialog
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
class RoutineFragment : Fragment(), VaccineDetailsAdapter.OnCheckBoxSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRoutineBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private var patientYears:String? = null
    private var selectedVaccineList = ArrayList<String>()

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

        patientYears = formatterClass.getSharedPref("patientYears", requireContext())
        if (patientYears != null){
            val patientYearsInt = patientYears!!.toIntOrNull()
            if (patientYearsInt != null){
                if (patientYearsInt < 16){
                    getRoutine()
                }
            }
        }

        binding.tvAdministerVaccine.setOnClickListener {

            if (selectedVaccineList.isNotEmpty()){
                formatterClass.saveSharedPref(
                    "selectedVaccineName",
                    selectedVaccineList.joinToString(","),
                    requireContext())
                formatterClass.saveSharedPref(
                    "selectedUnContraindicatedVaccine",
                    selectedVaccineList.joinToString(","),
                    requireContext())

                val bottomSheet = BottomSheetDialog()
                fragmentManager?.let { it1 ->
                    bottomSheet.show(it1,
                        "ModalBottomSheet") }
            }else
                Toast.makeText(requireContext(), "Please select a vaccine", Toast.LENGTH_SHORT).show()




        }



        return binding.root

    }


    private fun getRoutine() {

        CoroutineScope(Dispatchers.IO).launch {

            val sharedPreferences: SharedPreferences = requireContext()
                .getSharedPreferences(getString(R.string.vaccineList),
                    Context.MODE_PRIVATE
                )

            val routineKeyList = sharedPreferences.getString("routineList", null)
            val expandableListTitle = routineKeyList!!.split(",").toList()

//            Get the administered list
            val recommendationList = patientDetailsViewModel.recommendationList("Contraindicated")

            val administeredList = patientDetailsViewModel.getVaccineList()
            val dbVaccineScheduleChildList = ArrayList<DbVaccineScheduleChild>()

            val dbVaccineScheduleGroupList = ArrayList<DbVaccineScheduleGroup>()
            expandableListTitle.forEach { keyValue->

                val weekNo = formatterClass.getVaccineScheduleValue(keyValue)
                val weekNoList = sharedPreferences.getStringSet(weekNo, null)
                val vaccineList = weekNoList?.toList()
                vaccineList?.forEach { vaccineName ->

                    val dbVaccineScheduleChild =  formatterClass.getVaccineChildStatus(
                        vaccineName, administeredList, recommendationList)
                    dbVaccineScheduleChildList.add(dbVaccineScheduleChild)
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

                val inflater = LayoutInflater.from(requireContext())

                for (group in sortedExpandableListTitle){

                    val groupLayout = inflater.inflate(R.layout.vaccination_schedule, null) as RelativeLayout
                    val tvScheduleTime = groupLayout.findViewById<TextView>(R.id.tvScheduleTime)
                    val tvAefi = groupLayout.findViewById<TextView>(R.id.tvAefi)
                    val imageViewSchedule = groupLayout.findViewById<ImageView>(R.id.imageViewSchedule)
                    val recyclerView = groupLayout.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.setHasFixedSize(true)

                    val vaccineSchedule = group.vaccineSchedule
                    val colorCode = group.colorCode

                    // Populate views with data from DbVaccineScheduleGroup
                    tvScheduleTime.text = vaccineSchedule
                    tvAefi.text = "Aefi(0)"
                    when (colorCode) {
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

                    groupLayout.setOnClickListener {

                        val vaccineList = ArrayList<DbVaccineScheduleChild>()

                        val weekNoList = sharedPreferences.getStringSet(vaccineSchedule, null)
                        if (weekNoList != null){
                            val savedVaccineList = weekNoList.toList()
                            for (dbVaccineScheduleChild in dbVaccineScheduleChildList) {
                                if (savedVaccineList.contains(dbVaccineScheduleChild.vaccineName)){
                                    vaccineList.add(dbVaccineScheduleChild)
                                }
                            }
                        }

                        val adapter = VaccineDetailsAdapter(
                            vaccineList,
                            this@RoutineFragment,
                            requireContext())

                        recyclerView.adapter = adapter

                    }
                    // Add the cardview_item to linearLayoutId2
                    binding.groupLayout.addView(groupLayout)

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

    override fun onCheckBoxSelected(position: Int, isChecked: Boolean, vaccineName: String) {

        if (isChecked){
            selectedVaccineList.add(vaccineName)
        }else{
            selectedVaccineList.remove(vaccineName)
        }

        val administerText = "Administer (${selectedVaccineList.size})"
        binding.tvAdministerVaccine.text = administerText

    }
}