package com.intellisoft.chanjoke.detail.ui.main.routine

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.databinding.FragmentRoutineBinding
import com.intellisoft.chanjoke.detail.ui.main.adapters.VaccineScheduleAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory


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

        val expandableListDetail = HashMap<String, List<String>>()

        val cricket: MutableList<String> = ArrayList()
        cricket.add("India")
        cricket.add("Pakistan")
        cricket.add("Australia")
        cricket.add("England")
        cricket.add("South Africa")

        val football: MutableList<String> = ArrayList()
        football.add("Brazil")
        football.add("Spain")
        football.add("Germany")
        football.add("Netherlands")
        football.add("Italy")

        val basketball: MutableList<String> = ArrayList()
        basketball.add("United States")
        basketball.add("Spain")
        basketball.add("Argentina")
        basketball.add("France")
        basketball.add("Russia")

        expandableListDetail["CRICKET TEAMS"] = cricket
        expandableListDetail["FOOTBALL TEAMS"] = football
        expandableListDetail["BASKETBALL TEAMS"] = basketball

        val expandableListTitle = ArrayList<String>(expandableListDetail.keys)


        val vaccineScheduleAdapter = VaccineScheduleAdapter(requireContext(), expandableListTitle, expandableListDetail)
        binding.expandableListView.setAdapter(vaccineScheduleAdapter)
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