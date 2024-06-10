package com.intellisoft.chanjoke.detail.ui.main.referrals

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentActiveReferralsBinding
import com.intellisoft.chanjoke.databinding.FragmentReferralsBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbTempData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.patient_list.PatientAdapter
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.MainScope
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActiveReferralsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActiveReferralsFragment : Fragment() {
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

    private lateinit var binding: FragmentActiveReferralsBinding
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActiveReferralsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Referrals"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        onBackPressed()

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[PatientListViewModel::class.java]
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.aefiParentList.layoutManager = layoutManager
        binding.aefiParentList.setHasFixedSize(true)

        loadServiceRequests()

        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadServiceRequests()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadServiceRequests() {
        try {
            /**
             * Pull all active ServiceRequests
             *
             **/
            patientListViewModel.liveServiceRequests.observe(viewLifecycleOwner) { requests ->
                binding.apply {
                    pbLoading.visibility = View.GONE
                }
                if (requests.isEmpty()) {
                    binding.apply {
                        tvEmptyList.visibility = View.VISIBLE
                    }
                }

                val patientAdapter = ReferralParentAdapter(requests, requireContext())
                binding.aefiParentList.adapter = patientAdapter
            }
            // Trigger data fetch
            patientListViewModel.loadActiveServiceRequest()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showCancelScreenerQuestionnaireAlertDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
    }
    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActiveReferralsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActiveReferralsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}