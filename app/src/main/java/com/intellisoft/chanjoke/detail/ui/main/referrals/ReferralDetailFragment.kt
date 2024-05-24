package com.intellisoft.chanjoke.detail.ui.main.referrals

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
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentReferralDetailBinding
import com.intellisoft.chanjoke.databinding.FragmentReferralsBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.DbServiceRequest
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentReferralDetailBinding
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
            title = "Referrals"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        onBackPressed()


        handleDataPopulation()

        binding.apply {
            previousButton.apply {
                setOnClickListener {
                    showCancelScreenerQuestionnaireAlertDialog()
                }
            }

            nextButton.apply {
                setOnClickListener {
                    //proceed to administer the vaccine
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