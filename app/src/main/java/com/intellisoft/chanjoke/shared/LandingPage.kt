package com.intellisoft.chanjoke.shared

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientFragment
import com.intellisoft.chanjoke.databinding.FragmentLandingPageBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.RegistrationActivity
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.viewmodel.LayoutListViewModel
import com.intellisoft.chanjoke.viewmodel.LayoutsRecyclerViewAdapter
import timber.log.Timber


class LandingPage : Fragment() {
    private val layoutViewModel: LayoutListViewModel by viewModels()
    private lateinit var viewModel: LandingPageViewModel
    private lateinit var _binding: FragmentLandingPageBinding
    private val binding get() = _binding
    private var formatterClass = FormatterClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }

        val practitionerFullNames =
            formatterClass.getSharedPref("practitionerFullNames", requireContext())
        if (practitionerFullNames != null) {
            binding.topBarLayout.tvFullName.text = practitionerFullNames
        }


        createSpinner()
        formatterClass.deleteSharedPref("patientListAction", requireContext())
        formatterClass.deleteSharedPref("ready_to_update", requireContext())


        return _binding.root

    }

    override fun onResume() {
        super.onResume()
        formatterClass.deleteSharedPref("ready_to_update", requireContext())
    }

    private fun createSpinner() {

        val resultList = listOf("Facility", "Outreach")
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resultList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinnerLocation.adapter = adapter

        // Set a listener to handle the item selection
        binding.spinnerLocation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    // Get the selected item
                    val selectedItem = parentView.getItemAtPosition(position).toString()
                    formatterClass.saveSharedPref(
                        "selectedFacility",
                        selectedItem,
                        requireContext()
                    )

                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // Do nothing here
                }
            }

    }

    private fun onItemClick(layout: LayoutListViewModel.Layout) {

        when (layout.textId) {
            "Search Client" -> {
                findNavController().navigate(R.id.patient_list)
            }

            "Register Client" -> {
                val bundle = Bundle()
                bundle.putString(
                    AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY,
                    "new-patient-registration-paginated.json"
                )
//                findNavController().navigate(R.id.addPatientFragment, bundle)
                formatterClass.deleteSharedPref("personal", requireContext())
                formatterClass.deleteSharedPref("caregiver", requireContext())
                formatterClass.deleteSharedPref("administrative", requireContext())

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("functionToCall", "registerFunction")
                startActivity(intent)
//                startActivity(Intent(requireContext(), RegistrationActivity::class.java))
            }

            "Update Vaccine History" -> {
                findNavController().navigate(R.id.patient_list)
                formatterClass.saveSharedPref(
                    "patientListAction",
                    NavigationDetails.UPDATE_CLIENT_HISTORY.name, requireContext()
                )
                formatterClass.saveSharedPref("ready_to_update", "true", requireContext())
            }

            "Administer vaccine" -> {
                findNavController().navigate(R.id.patient_list)
//                formatterClass.saveSharedPref(
//                    "patientListAction",
//                    NavigationDetails.ADMINISTER_VACCINE.name, requireContext()
//                )
            }

            "Reports" -> {
                findNavController().navigate(R.id.reportsFragment)
            }

            "Community Referrals" -> {
                formatterClass.saveSharedPref(
                    "patientListAction",
                    NavigationDetails.REFERRALS.name, requireContext()
                )
                findNavController().navigate(R.id.activeReferralsFragment)
            }

            "Appointments" -> {
                formatterClass.saveSharedPref(
                    "patientListAction",
                    NavigationDetails.APPOINTMENT.name, requireContext()
                )
                findNavController().navigate(R.id.patient_list)
            }

//            "Campaigns" -> {
//                formatterClass.saveSharedPref(
//                    "patientListAction",
//                    NavigationDetails.CAMPAIGN.name, requireContext()
//                )
//                findNavController().navigate(R.id.campaignFragment)
//            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuListItems = layoutViewModel.getLayoutList()

        //Check if the Campaigns was selected. If so, display it only. Otherwise don't
        val selectedFacility = formatterClass.getSharedPref("selectedFacility", requireContext())
        val formattedList = if (selectedFacility == "Campaigns"){
            //Remove all but Campaigns
            menuListItems.filter { it == LayoutListViewModel.Layout.CAMPAIGNS }
        }else{
            //Remove Campaigns
            menuListItems.filter { it != LayoutListViewModel.Layout.CAMPAIGNS }
        }



        val adapter =
            LayoutsRecyclerViewAdapter(::onItemClick).apply { submitList(formattedList) }
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.sdcLayoutsRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)


    }


}