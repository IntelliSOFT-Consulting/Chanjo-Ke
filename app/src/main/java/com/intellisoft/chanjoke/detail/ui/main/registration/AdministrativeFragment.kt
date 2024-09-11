package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentAdministrativeBinding
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.County
import com.intellisoft.chanjoke.fhir.data.FhirLocation
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.SubCountyWard
import com.intellisoft.chanjoke.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdministrativeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdministrativeFragment : Fragment() {
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
    private lateinit var binding: FragmentAdministrativeBinding
    private var onNextButtonClickListener: OnNextButtonClickListener? = null


    interface OnNextButtonClickListener {
        fun onNextButtonClicked(admin: String)
    }

    fun setOnNextButtonClickListener(listener: OnNextButtonClickListener) {
        onNextButtonClickListener = listener
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAdministrativeBinding.inflate(layoutInflater)

        liveData = ViewModelProvider(this).get(AdminLiveData::class.java)
        return binding.root
    }

    private var countyList = ArrayList<String>()
    private var subCountyList = ArrayList<String>()
    private var wardList = ArrayList<String>()
    private lateinit var liveData: AdminLiveData

    val countyMap = mutableMapOf<String, String>()
    val subCountyMap = mutableMapOf<String, String>()
    val wardMap = mutableMapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())

        if (isUpdate != null) {
            displayInitialData()
        }
        try {
            lifecycleScope.launch {
                val locations = loadCountiesLazily(requireContext(), "locations.json")
                val wards = loadWardsLazily(requireContext(), "wards.json")
                countyList.clear()

                locations.forEach {
                    if (it.type == "County") {
                        countyMap[it.name] = it.id
                        countyList.add(it.name)
                    }
                    if (it.type == "Sub-County") {
                        subCountyMap[it.name] = it.id
                    }
                    if (it.type == "Ward") {
                        wardMap[it.name] = it.id
                    }
                }
                countyList.sort()
                val adapterType =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        countyList
                    )
                binding.apply {
                    county.apply {
                        setAdapter(adapterType)
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }

                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString()
                                if (value.isNotEmpty()) {
                                    binding.telCounty.error = null
                                    updatePrefs()

                                    val selectedCounty = countyMap[value]
                                    Timber.e("Selected County ****** $selectedCounty")
                                    if (selectedCounty != null) {
                                        subCountyList.clear()
                                        binding.subCounty.setText("", false)
                                        val subCounties =
                                            locations.filter { it.parent == selectedCounty }
                                        subCounties.forEach {
                                            subCountyList.add(it.name)
                                        }
                                        subCountyList.sort()
                                        val subCountyAdapter =
                                            ArrayAdapter(
                                                requireContext(),
                                                android.R.layout.simple_dropdown_item_1line,
                                                subCountyList
                                            )
                                        binding.subCounty.apply {
                                            setAdapter(subCountyAdapter)
                                        }

                                    }
                                }
                            }
                        })

                    }
                    subCounty.apply {

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }

                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString()
                                if (value.isNotEmpty()) {
                                    binding.telSubCounty.error = null
                                    updatePrefs()

                                    val selectedSubCounty = subCountyMap[value]
                                    Timber.e("Selected Sub County ****** $selectedSubCounty")
                                    if (selectedSubCounty != null) {
                                        wardList.clear()
                                        binding.ward.setText("", false)
                                        val subCounties =
                                            locations.filter { it.parent == selectedSubCounty }
                                        subCounties.forEach {
                                            wardList.add(it.name)

                                        }
                                        wardList.sort()
                                        val subCountyAdapter =
                                            ArrayAdapter(
                                                requireContext(),
                                                android.R.layout.simple_dropdown_item_1line,
                                                wardList
                                            )
                                        binding.ward.apply {
                                            setAdapter(subCountyAdapter)
                                        }
                                    }
                                }
                            }
                        })

                    }
                    ward.apply {

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }

                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString()
                                if (value.isNotEmpty()) {
                                    binding.telWard.error = null
                                    updatePrefs()

                                    val selectedWard = wardMap[value]
                                    Timber.e("Selected Ward ****** $selectedWard")

                                }
                            }
                        })

                    }
                    previousButton.apply {
                        setOnClickListener {
                            updatePrefs()
                            mListener?.onPreviousPageRequested()
                        }
                    }
                    nextButton.apply {
                        setOnClickListener {
                            if (validData()) {
                                val data = retrievedEnteredData()
                                onNextButtonClickListener?.onNextButtonClicked(data)
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000) // Delay for 1000 milliseconds (1 second)
                                    mListener?.onNextPageRequested()
                                }
                            }
                        }
                    }
                    estate.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }

                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString()
                                if (value.isNotEmpty()) {
                                    binding.telEstate.error = null
                                    updatePrefs()

                                }
                            }
                        })

                    }
                    trading.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }

                            override fun afterTextChanged(s: Editable?) {
                                val value = s.toString()
                                if (value.isNotEmpty()) {
                                    binding.telTrading.error = null
                                    updatePrefs()

                                }
                            }
                        })

                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadCountiesLazily(
        context: Context?,
        fileName: String
    ): Array<FhirLocation> {
        return withContext(Dispatchers.IO) {
            val gson = Gson()

            // Load counties data
            val inputStream = context?.assets?.open(fileName)
            val counties: Array<FhirLocation> = inputStream?.use { stream ->
                val reader = InputStreamReader(stream)
                val className = Array<FhirLocation>::class.java
                gson.fromJson(reader, className)
            } ?: emptyArray()

            // Return the loaded counties
            counties
        }
    }

    private suspend fun loadWardsLazily(context: Context?, fileName: String): Array<SubCountyWard> {
        return withContext(Dispatchers.IO) {
            val gson = Gson()

            // Load counties data
            val inputStream = context?.assets?.open(fileName)
            val counties: Array<SubCountyWard> = inputStream?.use { stream ->
                val reader = InputStreamReader(stream)
                val className = Array<SubCountyWard>::class.java
                gson.fromJson(reader, className)
            } ?: emptyArray()

            // Return the loaded counties
            counties
        }
    }

    private fun retrievedEnteredData(): String {
        val countyString = binding.county.text.toString()
        val subCountyString = binding.subCounty.text.toString()
        val wardString = binding.ward.text.toString()
        val chuString = binding.communityUnit.text.toString()
        val tradingString = binding.trading.text.toString()
        val estateString = binding.estate.text.toString()
        try {
            val payload = Administrative(
                county = countyMap[countyString].toString(),
                subCounty = subCountyMap[subCountyString].toString(),
                ward = wardMap[wardString].toString(),
                trading = tradingString,
                estate = estateString,
                chu = chuString,
                countyName = binding.county.text.toString(),
                subCountyName = binding.subCounty.text.toString(),
                wardName = binding.ward.text.toString()
            )
            return Gson().toJson(payload)
        } catch (e: Exception) {
            return ""
        }

    }

    private fun updatePrefs() {
        try {
            var countyString = binding.county.text.toString()
            var subCountyString = binding.subCounty.text.toString()
            var wardString = binding.ward.text.toString()
            val tradingString = binding.trading.text.toString()
            val estateString = binding.estate.text.toString()

            val chuString = binding.communityUnit.text.toString()
            countyString = countyMap[countyString].toString()
            subCountyString = subCountyMap[subCountyString].toString()
            wardString = wardMap[wardString].toString()

            val payload = Administrative(
                county = AppUtils().capitalizeFirstLetter(countyString),
                subCounty = AppUtils().capitalizeFirstLetter(subCountyString),
                ward = AppUtils().capitalizeFirstLetter(wardString),
                trading = AppUtils().capitalizeFirstLetter(tradingString),
                estate = AppUtils().capitalizeFirstLetter(estateString),
                countyName = binding.county.text.toString(),
                subCountyName = binding.subCounty.text.toString(),
                wardName = binding.ward.text.toString(),
                chu = chuString
            )
            formatter.saveSharedPref("administrative", Gson().toJson(payload), requireContext())
            liveData.updatePatientDetails(Gson().toJson(payload))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayInitialData() {
        try {
            val administrative = formatter.getSharedPref("administrative", requireContext())
            if (administrative != null) {
                val data = Gson().fromJson(administrative, Administrative::class.java)
                binding.apply {
                    county.setText(data.county, false)
                    subCounty.setText(data.subCounty, false)
                    ward.setText(data.ward, false)
                    trading.setText(data.trading)
                    estate.setText(data.estate)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun validData(): Boolean {

        var countyString = binding.county.text.toString()
        var subCountyString = binding.subCounty.text.toString()
        var wardString = binding.ward.text.toString()
        val tradingString = binding.trading.text.toString()
        val estateString = binding.estate.text.toString()
        val chuString = binding.communityUnit.text.toString()
        if (countyString.isEmpty()) {
            binding.telCounty.error = "Enter county"
            binding.county.requestFocus()
            return false
        }
        if (subCountyString.isEmpty()) {
            binding.telSubCounty.error = "Enter sub county"
            binding.subCounty.requestFocus()
            return false
        }
        if (wardString.isEmpty()) {
            binding.telWard.error = "Enter ward"
            binding.ward.requestFocus()
            return false
        }
//        if (tradingString.isEmpty()) {
//            binding.telTrading.error = "Enter trading center"
//            binding.trading.requestFocus()
//            return false
//        }
//        if (estateString.isEmpty()) {
//            binding.telEstate.error = "Enter estate"
//            binding.estate.requestFocus()
//            return false
//        }


        countyString = countyMap[countyString].toString()
        subCountyString = subCountyMap[subCountyString].toString()
        wardString = wardMap[wardString].toString()

        val payload = Administrative(
            county = countyString,
            subCounty = subCountyString,
            ward = wardString,
            trading = tradingString,
            estate = estateString,
            chu = chuString,
            countyName = binding.county.text.toString(),
            subCountyName = binding.subCounty.text.toString(),
            wardName = binding.ward.text.toString()
        )
        formatter.saveSharedPref("administrative", Gson().toJson(payload), requireContext())
        liveData.updatePatientDetails(Gson().toJson(payload))
        return true

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdministrativeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdministrativeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}