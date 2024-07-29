package com.intellisoft.chanjoke.detail.ui.main.non_routine

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentNonRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.VaccineDetailsAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbRecycler
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleGroup
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.BottomSheetDialog
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [NonRoutineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NonRoutineFragment : Fragment(), VaccineDetailsAdapter.OnCheckBoxSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentNonRoutineBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private var patientYears:String? = null
    private var selectedVaccineList = ArrayList<String>()
    private var dbRecyclerList = HashSet<DbRecycler>()
    private val immunizationHandler = ImmunizationHandler()
    private lateinit var sharedPreferences:SharedPreferences

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

        sharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.vaccineList),
                Context.MODE_PRIVATE
            )

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()
        patientGender = formatterClass.getSharedPref("patientGender", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        patientYears = formatterClass.getSharedPref("patientYears", requireContext())

        getNonRoutine()

        binding.tvAdministerVaccine.setOnClickListener {

            if (selectedVaccineList.isNotEmpty()){

                val shouldBeIn = listHasSimilarNames(selectedVaccineList)

                if (!shouldBeIn){
                    formatterClass.saveSharedPref(
                        "selectedVaccineName",
                        selectedVaccineList.joinToString(","),
                        requireContext())
                    formatterClass.saveSharedPref(
                        "selectedUnContraindicatedVaccine",
                        selectedVaccineList.joinToString(","),
                        requireContext())
                    formatterClass.saveSharedPref(
                        "workflowVaccinationType",
                        "NON-ROUTINE", requireContext()
                    )

                    val bottomSheet = BottomSheetDialog()
                    fragmentManager?.let { it1 ->
                        bottomSheet.show(it1,
                            "ModalBottomSheet") }
                }else{
                    Toast.makeText(requireContext(), "You cannot have the same vaccine type.", Toast.LENGTH_SHORT).show()
                }

            }else
                Toast.makeText(requireContext(), "Select at least one vaccine", Toast.LENGTH_SHORT).show()

        }

        return binding.root
    }

    private fun getNonRoutine() {

        CoroutineScope(Dispatchers.IO).launch {

            val sharedPreferences: SharedPreferences = requireContext()
                .getSharedPreferences(getString(R.string.vaccineList),
                    Context.MODE_PRIVATE
                )

            val routineKeyList = sharedPreferences.getString("nonRoutineList", null)
            val expandableListTitle = routineKeyList!!.split(",").toList()

            var filteredList = ArrayList(expandableListTitle)

            if (patientGender == "male"){
                filteredList = ArrayList(expandableListTitle.filterNot { it.contains("HPV") })
            }

//            Get the administered list
            val recommendationList = patientDetailsViewModel.recommendationList("Contraindicated")

            val administeredList = patientDetailsViewModel.getVaccineList()
            val dbVaccineScheduleChildList = ArrayList<DbVaccineScheduleChild>()

            val dbVaccineScheduleGroupList = ArrayList<DbVaccineScheduleGroup>()
            filteredList.forEach { keyValue->

                val weekNoList = sharedPreferences.getStringSet(keyValue, null)
                val vaccineList = weekNoList?.toList()


                vaccineList?.forEach { vaccineName ->

                    /**
                     * TODO: CHECK ON NON-ROUTINE
                     */
                    val dbVaccineScheduleChild =  formatterClass.getVaccineChildNonRoutineStatus(
                        requireContext(),"NON-ROUTINE", keyValue, vaccineName, administeredList, recommendationList)
                    dbVaccineScheduleChildList.add(dbVaccineScheduleChild)
                }

                //Get the group color Code
                val statusColor = formatterClass.getNonRoutineVaccineGroupDetails(vaccineList, administeredList,recommendationList)

                dbVaccineScheduleChildList.sortBy {
                    it.vaccineName
                }

                val dbVaccineScheduleGroup = DbVaccineScheduleGroup(
                    keyValue,
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

            CoroutineScope(Dispatchers.Main).launch {

                val inflater = LayoutInflater.from(requireContext())

                for (group in newExpandableListTitle){

                    val groupLayout = inflater.inflate(R.layout.vaccination_schedule, null) as RelativeLayout
                    val tvScheduleTime = groupLayout.findViewById<TextView>(R.id.tvScheduleTime)
                    val tvAefi = groupLayout.findViewById<TextView>(R.id.tvAefi)
                    val imageViewSchedule = groupLayout.findViewById<ImageView>(R.id.imageViewSchedule)
                    val recyclerView = groupLayout.findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.setHasFixedSize(true)

                    val vaccineSchedule = group.vaccineSchedule
                    val colorCode = group.colorCode

                    val dbRecycler = DbRecycler(recyclerView, vaccineSchedule)
                    dbRecyclerList.add(dbRecycler)

                    // Populate views with data from DbVaccineScheduleGroup
                    tvScheduleTime.text = vaccineSchedule
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
                    tvAefi.text = "Aefi(0)"
                    val counter = patientDetailsViewModel.generateCurrentCount(
                        vaccineSchedule,
                        patientId
                    )
                    tvAefi.text = "AEFIs ($counter)"

                    tvAefi.setOnClickListener {
                        FormatterClass().saveSharedPref("current_age", vaccineSchedule, requireContext())
                        FormatterClass().saveSharedPref(
                            "title",
                            "AEFI", requireContext()
                        )
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("functionToCall", NavigationDetails.LIST_AEFI.name)
                        intent.putExtra("patientId", patientId)
                        startActivity(intent)
                    }

                    groupLayout.setOnClickListener {

                        performVisibility(vaccineSchedule)

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

                        vaccineList.sortBy { it.vaccineName }

                        //Remove HPV if it's a male
                        if (patientGender == "male"){
                            vaccineList.removeIf { it.vaccineName.contains("HPV") }
                        }

                        /**
                         * Check if client is below 9 months; Maintain Yellow fever alone
                         * Otherwise have all non routines
                         */

                        generateVaccineList(vaccineSchedule, recyclerView, dbVaccineScheduleChildList)

                    }
                    // Add the cardview_item to linearLayoutId2
                    binding.groupLayout.addView(groupLayout)

                    generateVaccineList(
                        vaccineSchedule,
                        recyclerView,
                        dbVaccineScheduleChildList)


                }
            }
        }


    }

    private fun generateVaccineList(
        vaccineSchedule: String,
        recyclerView: RecyclerView,
        dbVaccineScheduleChildList: ArrayList<DbVaccineScheduleChild>
    ) {

        val vaccineList = ArrayList<DbVaccineScheduleChild>()

        val weekNoList = mutableSetOf<String>()
        val scheduleSet = sharedPreferences.getStringSet(vaccineSchedule, null)
        scheduleSet?.let { weekNoList.addAll(it) }

        val savedVaccineList = weekNoList.toList()
        for (dbVaccineScheduleChild in dbVaccineScheduleChildList) {
            if (savedVaccineList.contains(dbVaccineScheduleChild.vaccineName)){
                vaccineList.add(dbVaccineScheduleChild)
            }
        }

        val uniqueVaccineList = vaccineList.distinctBy { it.vaccineName }.toCollection(ArrayList())

        // If you want to reassign it back to the original list variable
        vaccineList.clear()
        vaccineList.addAll(uniqueVaccineList)

        val patientGender = formatterClass.getSharedPref("patientGender", requireContext())

        //Remove HPV if it's a male
        if (patientGender != null && patientGender == "male"){
            vaccineList.removeIf { it.vaccineName.contains("HPV") }
        }


        val adapter = VaccineDetailsAdapter(
            patientDetailsViewModel,
            vaccineList,
            this@NonRoutineFragment,
            requireContext())

        recyclerView.adapter = adapter

    }

    private fun performVisibility(vaccineSchedule:String) {

        dbRecyclerList.forEach {
            val recyclerView = it.recyclerView
            val dbVaccineSchedule = it.vaccineSchedule
            if (vaccineSchedule == dbVaccineSchedule){
                if (recyclerView.visibility == View.VISIBLE){
                    recyclerView.visibility = View.GONE
                }else{
                    recyclerView.visibility = View.VISIBLE
                }

            }else{
                recyclerView.visibility = View.GONE
            }
        }

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
    override fun onCheckBoxSelected(position: Int, isChecked: Boolean, vaccineName: String) {

        //Get the series of the selected vaccines
//        val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
//        val routineVaccine = basicVaccine?.let { immunizationHandler.getSeriesByBasicVaccine(it) }
//        val vaccineList = routineVaccine?.vaccineList?.map { it.vaccineName }
//
//        val shouldBeIn = checkForOtherVaccines(vaccineName, vaccineList!!, selectedVaccineList)
//
//

        if (isChecked){
            selectedVaccineList.add(vaccineName)
        }else{
            selectedVaccineList.remove(vaccineName)
        }

        val administerText = "Administer (${selectedVaccineList.size})"
        binding.tvAdministerVaccine.text = administerText


    }

    // Function to compute the Levenshtein distance between two strings
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1, // Deletion
                        dp[i][j - 1] + 1, // Insertion
                        dp[i - 1][j - 1] + (if (str1[i - 1] == str2[j - 1]) 0 else 1) // Substitution
                    )
                }
            }
        }

        return dp[str1.length][str2.length]
    }

    // Function to check if the list has similar names
    fun listHasSimilarNames(list: List<String>, threshold: Int = 3): Boolean {
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                if (levenshteinDistance(list[i], list[j]) <= threshold) {
                    return true
                }
            }
        }
        return false
    }
}