package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentAdministrativeBinding
import com.intellisoft.chanjoke.databinding.FragmentPreviewBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.CareGiver
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.AppUtils
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreviewFragment : Fragment() {
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

    private val formatter = FormatterClass()
    private val viewModel: AddPatientViewModel by viewModels()
    private var mListener: OnButtonClickListener? = null
    private lateinit var binding: FragmentPreviewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPreviewBinding.inflate(layoutInflater)

        liveData = ViewModelProvider(this).get(AdminLiveData::class.java)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity implements the interface
        mListener = if (context is OnButtonClickListener) {
            context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }


    private lateinit var liveData: AdminLiveData
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveData = ViewModelProvider(this).get(AdminLiveData::class.java)
        val isAbove = formatter.getSharedPref("isAbove", requireContext())
        if (isAbove != null) {
            if (isAbove == "true") {
                binding.apply {
                    tvTitleName.text = "Next of Kin Details"
                }
            }
        }

        loadData()
        binding.apply {

            previousButton.apply {
                setOnClickListener {
                    mListener?.onPreviousPageRequested()
                }
            }
            nextButton.apply {
                setOnClickListener {
                    formatter.deleteSharedPref("isAbove", requireContext())
//                    isEnabled = false
                    mListener?.onNextPageRequested()

                }
            }
        }
    }

    private fun loadData() {
        val personal = formatter.getSharedPref("personal", requireContext())
        val caregiver = formatter.getSharedPref("caregiver", requireContext())
        val administrative = formatter.getSharedPref("administrative", requireContext())
        binding.apply {

            if (personal != null) {
                val refinedPersonal = Gson().fromJson(personal, CustomPatient::class.java)

                tvFirstname.text = refinedPersonal.firstname
                tvLastname.text = refinedPersonal.lastname
                tvMiddlename.text = refinedPersonal.middlename
                tvDateOfBirth.text =
                    formatter.convertDateFormatWithDesiredFormat(
                        refinedPersonal.dateOfBirth,
                        "dd/MM/yyyy"
                    )
                tvGender.text = refinedPersonal.gender
                tvAge.text = refinedPersonal.age
                tvIdNumber.text = refinedPersonal.identificationNumber
            }

            if (caregiver != null) {

                try {
                    val type = object : TypeToken<List<CareGiver>>() {}.type
                    val caregiverList: List<CareGiver> = Gson().fromJson(caregiver, type)

                    lnCaregiver.removeAllViews()
                    // Assuming this code is inside a fragment or activity method
                    caregiverList.forEach { caregiver ->
                        val inflater = LayoutInflater.from(requireContext())
                        val itemView = inflater.inflate(R.layout.caregiver, lnCaregiver, false) as LinearLayout

                        val tvCname = itemView.findViewById<TextView>(R.id.tv_cname)
                        val tvCtype = itemView.findViewById<TextView>(R.id.tv_ctype)
                        val tvCphone = itemView.findViewById<TextView>(R.id.tv_cphone)
                        val tvCnational = itemView.findViewById<TextView>(R.id.tv_cnational)

                        // Set the text for each TextView with caregiver information
                        tvCname.text = AppUtils().capitalizeFirstLetter(caregiver.name)
                        tvCtype.text = caregiver.type
                        tvCphone.text = caregiver.phone
                        tvCnational.text = caregiver.nationalID

                        // Add the itemView (LinearLayout) to lnCaregiver (LinearLayout)
                        lnCaregiver.addView(itemView)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (administrative != null) {

                val refinedAdministrative =
                    Gson().fromJson(administrative, Administrative::class.java)

                tvCounty.text = AppUtils().capitalizeFirstLetter(refinedAdministrative.countyName)
                tvSubCounty.text = AppUtils().capitalizeFirstLetter(refinedAdministrative.subCountyName)
                tvWard.text = AppUtils().capitalizeFirstLetter(refinedAdministrative.wardName)
                tvChu.text=AppUtils().capitalizeFirstLetter(refinedAdministrative.chu)
                tvTrading.text = AppUtils().capitalizeFirstLetter(refinedAdministrative.trading)
                tvVillage.text = AppUtils().capitalizeFirstLetter(refinedAdministrative.estate)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PreviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}