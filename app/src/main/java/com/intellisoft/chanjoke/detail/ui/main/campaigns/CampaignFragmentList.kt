package com.intellisoft.chanjoke.detail.ui.main.campaigns

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentCampaignBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbCarePlan
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.campaign.CampaignAdapter
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CampaignFragmentList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCampaignBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var campaignList = ArrayList<DbCarePlan>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment



        binding = FragmentCampaignBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModelFactory(
                requireContext().applicationContext as Application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        getCampaignList()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.btnSearch.setOnClickListener {

            val search = binding.search.text.toString()
            if (!TextUtils.isEmpty(search)){

                if (campaignList.isNotEmpty()){

                    val returnCampaignList = findTitles(campaignList, search)
                    populateList(returnCampaignList)

                }else binding.search.error = "Nothing similar was found."

            }else{
                binding.search.error = "Field cannot be empty."
            }

        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Campaigns"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        onBackPressed()
    }


    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_campaignFragment_pop)
        }
    }

    private fun findTitles(carePlans: ArrayList<DbCarePlan>, title: String): ArrayList<DbCarePlan> {

        val newCareplanList = ArrayList<DbCarePlan>()

        carePlans.forEach {

            val titleDb = it.title
            if (titleDb.contains(title)){
                newCareplanList.add(it)
            }
        }
        return newCareplanList
    }

    private fun populateList(campaignDataList: ArrayList<DbCarePlan>) {
        CoroutineScope(Dispatchers.Main).launch {
            val vaccineAdapter =
                CampaignAdapter(this@CampaignFragmentList, campaignDataList, requireContext())
            binding.recyclerView.adapter = vaccineAdapter
        }
    }
    private fun getCampaignList(){

        CoroutineScope(Dispatchers.IO).launch {
            campaignList =  patientDetailsViewModel.getCampaignList()
            populateList(campaignList)
        }
    }


}