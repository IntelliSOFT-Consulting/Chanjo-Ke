package com.intellisoft.chanjoke.detail.ui.main.referrals

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentActiveReferralsBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.PatientIdentification
import com.intellisoft.chanjoke.fhir.data.ServiceRequestPatient
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.utils.AppUtils
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private var allServiceRequests = mutableListOf<ServiceRequestPatient>()
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
            title = "Community Referrals"
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

        binding.apply {
            AppUtils().disableEditing(startDate)
            AppUtils().disableEditing(endDate)
            startDate.apply {
                setOnClickListener {
                    showDatePickerDialog(startDate, true)
                }
            }
            endDate.apply {
                setOnClickListener {
                    showDatePickerDialog(endDate, false)
                }
            }

            nextButton.apply {
                setOnClickListener {
                    val start = startDate.text.toString()
                    val end = endDate.text.toString()

                    if (start.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please select start date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (end.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please select end date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    /***
                     * Filter results based on selected dates  between start and end
                     * */

                    // Format for start and end dates (MM/dd/yyyy)
                    val inputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    // Format for authoredOn dates (EEE MMM dd HH:mm:ss zzz yyyy)
                    val authoredDateFormat =
                        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())

                    try {
                        // Parse the start and end dates
                        val startDate = inputDateFormat.parse(start)
                        val endDate = inputDateFormat.parse(end)


                        if (startDate != null && endDate != null) {
                            val filteredRequests = allServiceRequests.filter {
                                val authoredOnDate = authoredDateFormat.parse(it.authoredOn)
                                authoredOnDate != null && authoredOnDate.after(startDate) && authoredOnDate.before(
                                    endDate
                                )
                            }
                            // Do something with filteredRequests, for example:
                            // updateRecyclerView(filteredRequests)


                            binding.apply {
                                pbLoading.visibility = View.GONE
                                tvEmptyList.visibility = View.GONE
                            }
                            if (filteredRequests.isEmpty()) {
                                binding.apply {
                                    tvEmptyList.visibility = View.VISIBLE
                                }
                            }
                            val patientAdapter =
                                ReferralParentAdapter(filteredRequests, requireContext())
                            binding.aefiParentList.adapter = patientAdapter

                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Toast.makeText(
                            requireContext(),
                            "Invalid date format",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        loadServiceRequests()

        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDatePickerDialog(textInputEditText: TextInputEditText, isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Handle the selected date (e.g., update the TextView)
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                textInputEditText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        if (!isStart) { // Should have a minimum

            /**
             * Get the start date and set it as minimum date
             */
            datePickerDialog.datePicker.minDate =
                calculateStartDate(calendar.getTimeInMillis())
            datePickerDialog.show()

        }

        datePickerDialog.datePicker.maxDate =
            System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun calculateStartDate(timeInMillis: Long): Long {

        var minTimeInMillis = timeInMillis
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val minimumDate = binding.startDate.text.toString()
            if (minimumDate.isNotEmpty()) {
                try {
                    val date = dateFormat.parse(minimumDate)
                    if (date != null) {
                        minTimeInMillis = date.time
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return minTimeInMillis

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
                allServiceRequests = requests
                binding.apply {
                    pbLoading.visibility = View.GONE
                    tvEmptyList.visibility = View.GONE
                }
                if (requests.isEmpty()) {
                    binding.apply {
                        tvEmptyList.visibility = View.VISIBLE
                    }
                }

                val patientAdapter = ReferralParentAdapter(allServiceRequests, requireContext())
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

            Log.e("--->","<----")
            println("Back button has been pressed")
            Log.e("--->","<----")

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