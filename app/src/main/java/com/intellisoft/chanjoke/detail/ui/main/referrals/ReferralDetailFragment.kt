package com.intellisoft.chanjoke.detail.ui.main.referrals

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentReferralDetailBinding
import com.intellisoft.chanjoke.databinding.FragmentReferralsBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.DbServiceRequest
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import org.hl7.fhir.r4.model.Immunization
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReferralDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReferralDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private  var immunizationHandler = ImmunizationHandler()
    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentReferralDetailBinding

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReferralDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Community Referrals"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        onBackPressed()


        handleDataPopulation()
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
        binding.apply {
            previousButton.apply {
                setOnClickListener {
                    showCancelScreenerQuestionnaireAlertDialog()
                }
            }

            nextButton.apply {
                setOnClickListener {
                    //proceed to administer the vaccine
                    /**
                     * 1. Get the vaccine name and check if the vaccine name exists
                     * 2. Get a list of all vaccine list and compare if there's a match
                     * 3. Get the basic vaccine and vaccinate
                     */
                    val vaccineNameTxt = vaccineReferredTextView.text.toString()
                    if(!TextUtils.isEmpty(vaccineNameTxt)){

                        val resultList = ArrayList<String>()

                        val routineVaccineList = immunizationHandler.getAllRoutineDiseases()

                        for(routineVaccine in routineVaccineList){
                            val targetDisease = routineVaccine.targetDisease

                            if (vaccineNameTxt.contains(targetDisease)){
                                val basicVaccine = routineVaccine.vaccineList.firstOrNull()
                                if (basicVaccine != null){
                                    val vaccineName = basicVaccine.vaccineName
                                    resultList.add(vaccineName)
                                    break
                                }

                            }
                        }

                        if (resultList.isNotEmpty()){

                            administerVaccineViewModel.createManualImmunizationResource(
                                resultList,
                                formatterClass.generateUuid(),
                                patientId,
                                requireContext(),
                                null,
                                Immunization.ImmunizationStatus.COMPLETED)

                            val serviceRequestId = tvServiceId.text.toString()

                            if (serviceRequestId != null) {
                                patientDetailsViewModel.updateServiceRequestStatus(serviceRequestId)
                            }

                            FormatterClass().saveSharedPref(
                                "vaccinationFlow",
                                NavigationDetails.REFERRALS.name,
                                requireContext())

                            FormatterClass().saveSharedPref(
                                "isVaccineAdministered",
                                NavigationDetails.REFERRALS.name,
                                requireContext())

                            val blurBackgroundDialog = BlurBackgroundDialog(
                                this@ReferralDetailFragment,
                                requireContext())
                            blurBackgroundDialog.show()

                        }else{
                            Toast.makeText(
                                requireContext(),
                                "We could not find the referred vaccine.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }


                }
            }
        }
    }

    private fun handleDataPopulation() {
        try {

            val referral = FormatterClass().getSharedPref("selected_referral", requireContext())
            if (referral != null) {
                val data = Gson().fromJson(referral, DbServiceRequest::class.java)
                binding.apply {

                    val inputDateFormat =
                        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    val outputDateFormat =
                        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    val referralDate = inputDateFormat.parse(data.referralDate)
                    val scheduledDate = inputDateFormat.parse(data.scheduledDate)
                    val formattedDateStr = outputDateFormat.format(referralDate)
                    val scheduledDateStr = outputDateFormat.format(scheduledDate)
                    tvServiceId.text = data.logicalId
                    referringCHPTextView.text = data.referringCHP
                    vaccineReferredTextView.text = data.vaccineName
                    detailsTextView.text = data.detailsGiven
                    dateOfReferralTextView.text = formattedDateStr
                    scheduledVaccineDateTextView.text = scheduledDateStr
                    dateVaccineAdministeredTextView.text = data.dateAdministered
                    healthFacilityReferredToTextView.text = data.healthFacility

                }
            }
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
        val patientId = FormatterClass().getSharedPref("patientId", requireContext())
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("functionToCall", NavigationDetails.REFERRALS.name)
        intent.putExtra("patientId", patientId)
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
         * @return A new instance of fragment ReferralDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReferralDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}