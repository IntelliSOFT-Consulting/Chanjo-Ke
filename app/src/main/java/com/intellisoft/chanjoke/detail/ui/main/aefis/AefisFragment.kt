package com.intellisoft.chanjoke.detail.ui.main.aefis

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentAefisBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.detail.ui.main.adapters.VaccineAefiAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.AllergicReaction
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AefisFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AefisFragment : Fragment() {
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

    private lateinit var binding: FragmentAefisBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAefisBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = FormatterClass().getSharedPref("title", requireContext())
                ?: getString(R.string.administer_vaccine)
            setDisplayHomeAsUpEnabled(true)
        }
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.aefiParentList.layoutManager = layoutManager
        binding.aefiParentList.setHasFixedSize(true)

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModelFactory(
                requireContext().applicationContext as Application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]
        setHasOptionsMenu(true)
        onBackPressed()
        pullVaccinesWithAefis()

        binding.btnAdd.apply {
            setOnClickListener {
                val patientId = FormatterClass().getSharedPref("patientId", context)

                FormatterClass().saveSharedPref(
                    "questionnaireJson",
                    "adverse_effects.json", context
                )
                FormatterClass().saveSharedPref(
                    "title",
                    "AEFI", context
                )

                FormatterClass().saveSharedPref("vaccinationFlow", "addAefi", context)
//                FormatterClass().saveSharedPref(
//                    "encounter_logical_id", logicalId.text.toString(), context
//                )
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("functionToCall", NavigationDetails.ADD_AEFI.name)
                intent.putExtra("patientId", patientId)
                context.startActivity(intent)
            }
        }
    }

    private fun pullVaccinesWithAefis() {
        val vaccineList = patientDetailsViewModel.getVaccineList()

        val groupedByStatus = vaccineList.groupBy { it.status }

        val allergicReactions = groupedByStatus
            .toList()
            .sortedWith(compareBy { entry ->
                val status = entry.first
                FormatterClass().orderedDurations().indexOf(status)
            })
            .map { (status, vaccines) ->
                val reactions = ArrayList<DbVaccineData>(vaccines)

                AllergicReaction(
                    status,
                    "",
                    reactions = reactions
                )
            }

        val vaccineAdapter =
            VaccineAefiAdapter(
                patientDetailsViewModel,
                allergicReactions,
                requireContext()
            )

        binding.aefiParentList.adapter = vaccineAdapter
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {

            NavHostFragment.findNavController(this@AefisFragment)
                .navigateUp()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val patientId = FormatterClass().getSharedPref("patientId", requireContext())
                val intent = Intent(context, PatientDetailActivity::class.java)
                intent.putExtra("patientId", patientId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(intent)

                true
            }

            else -> {
                super.onOptionsItemSelected(item)
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
         * @return A new instance of fragment AefisFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AefisFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}