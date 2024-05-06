package com.intellisoft.chanjoke.detail.ui.main.registration

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentPersonalBinding
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalFragment : Fragment() {
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

    private lateinit var binding: FragmentPersonalBinding
    private val formatter = FormatterClass()
    private var mListener: OnButtonClickListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPersonalBinding.inflate(layoutInflater)

        return binding.root
    }

    // Define an interface
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity implements the interface
        mListener = if (context is OnButtonClickListener) {
            context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils().disableEditing(binding.dateOfBirth)
        AppUtils().disableEditing(binding.calculatedAge)
        AppUtils().disableEditing(binding.tvEstimatedDob)

        updateMandatoryFields()

        binding.telephone.apply {
            setOnFocusChangeListener { _, hasFocus ->
                hint = if (hasFocus) {
                    "07xxxxxxxx"
                } else {
                    null
                }
            }
        }
        val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())
        if (isUpdate != null) {
            displayInitialData()
        }
        val identifications = arrayOf(
            "Birth Certificate",
            "Passport",
            "Birth Notification Number"
        )

        // Create ArrayAdapter with the array of strings
        val adapterType =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                identifications
            )

        binding.apply {

            editTextOne.apply {
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
                            updateCalculatedAge()
                            try {
                                val intValue = value.toInt()
                                if (intValue >= 18) {
                                    telephone.visibility = View.VISIBLE

                                    formatter.saveSharedPref("isAbove", "true", requireContext())
                                } else {
                                    telephone.visibility = View.GONE
                                    formatter.saveSharedPref("isAbove", "false", requireContext())
                                }
                            } catch (e: NumberFormatException) {
                                // Handle the case where the input is not a valid integer
                            }
                        }
                    }
                })
            }
            editTextTwo.apply {
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
                            updateCalculatedAge()

                        }
                    }
                })
            }
            editTextThree.apply {
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
                            updateCalculatedAge()

                        }
                    }
                })
            }
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    val dataValue = when (checkedId) {
                        R.id.radioButtonYes -> "Male"
                        R.id.radioButtonNo -> "Female"
                        else -> null
                    }

                }
            }
            radioGroupChild.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    when (checkedId) {
                        R.id.radioButtonYesChild -> {
                            lnAgeInput.visibility = View.GONE
                            radioButtonYesDob.isChecked = true
                        }

                        R.id.radioButtonNoChild -> {
                            lnAgeInput.visibility = View.VISIBLE
                            radioButtonYesDob.isChecked = false

                        }
                    }
                }
            }
            radioGroupDob.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    when (checkedId) {
                        R.id.radioButtonYesDob -> {
                            lnEstimated.visibility = View.GONE
                            telDateOfBirth.visibility = View.VISIBLE
                        }

                        R.id.radioButtonNoDob -> {
                            lnEstimated.visibility = View.VISIBLE

                            telDateOfBirth.visibility = View.GONE
                        }

                        else -> {

                            lnEstimated.visibility = View.GONE

                            telDateOfBirth.visibility = View.GONE
                        }
                    }

                }
            }
            dateOfBirth.apply {

                setOnClickListener {
                    val calendar: Calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
                            val valueCurrent: String = formatter.getDate(year, month, day)
                            setText(valueCurrent)
                            calculateUserAge(valueCurrent)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.datePicker.maxDate = calendar.getTimeInMillis()
                    datePickerDialog.show()
                }
            }
            identificationType.apply {
                setAdapter(adapterType)
            }
            nextButton.apply {
                setOnClickListener {
                    validateData()
                }
            }
            previousButton.apply {
                setOnClickListener {
                    mListener?.onCancelPageRequested()
                }
            }
        }

    }


    private fun updateMandatoryFields() {

    }

    private fun displayInitialData() {
        try {
            val personal = formatter.getSharedPref("personal", requireContext())

            if (personal != null) {
                val data = Gson().fromJson(personal, CustomPatient::class.java)
                binding.apply {
                    //remove commas
                    val parts = data.firstname.split(" ")
                    when (parts.size) {
                        2 -> {
                            val (firstName, lastName) = parts
                            firstname.setText(firstName)
                            lastname.setText(lastName)
                        }

                        3 -> {
                            val (firstName, middleName, lastName) = parts
                            firstname.setText(firstName)
                            lastname.setText(lastName)
                            middlename.setText(middleName)
                        }

                        4 -> {
                            val (firstName, middleName, lastName) = parts
                            firstname.setText(firstName)
                            lastname.setText(lastName)
                            middlename.setText(middleName)
                        }

                        else -> {
                            println("Invalid name format")
                        }
                    }

                    val gender = data.gender
                    if (gender.lowercase() == "male") {
                        radioButtonYes.isChecked = true
                    } else {
                        radioButtonNo.isChecked = true
                    }
                    radioButtonYesDob.isChecked = true
                    telDateOfBirth.visibility = View.VISIBLE
                    dateOfBirth.setText(data.dateOfBirth)
                    calculateUserAge(data.dateOfBirth)
                    identificationType.setText(data.identification, false)
                    identificationNumber.setText(data.identificationNumber)
                    telephone.setText(data.telephone)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCalculatedAge() {
        try {
            val enteredYear = binding.editTextOne.text.toString().toIntOrNull() ?: 0
            val enteredMonths = binding.editTextTwo.text.toString().toIntOrNull() ?: 0
            val enteredWeeks = binding.editTextThree.text.toString().toIntOrNull() ?: 0

            binding.calculatedAge.setText("$enteredYear years $enteredMonths months $enteredWeeks weeks")

            if (enteredYear >= 18) {
                formatter.saveSharedPref("isAbove", "true", requireContext())
            } else {
                formatter.saveSharedPref("isAbove", "false", requireContext())
            }
            binding.apply {
                tvEstimatedDob.apply {
                    setText(calculateDateOfBirth(enteredYear, enteredMonths, enteredWeeks))
                }
            }
            updateIdentifications(enteredYear)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateIdentifications(age: Int) {
        val identifications = when {
            age < 3 -> {
                arrayOf(
                    "Birth Certificate",
                    "Passport",
                    "Birth Notification Number"
                )
            }

            age in 3..17 -> {
                arrayOf(
                    "Birth Certificate",
                    "Passport",
                    "NEMIS"
                )
            }

            else -> {
                arrayOf(
                    "Birth Certificate",
                    "ID Number",
                    "Passport"
                )
            }
        }

        val adapterType =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                identifications
            )

        binding.apply {
            identificationType.apply {
                setAdapter(adapterType)
            }
            if (age >= 18) {
                telTelephone.visibility = View.VISIBLE
            } else {
                telTelephone.visibility = View.GONE

            }
        }


    }

    private fun calculateUserAge(valueCurrent: String) {
        val result = formatter.calculateAge(valueCurrent)
        binding.calculatedAge.setText(result)

        // check the year as well
        val year = formatter.calculateAgeYear(valueCurrent)
        if (year >= 18) {
            binding.telephone.visibility = View.VISIBLE
            formatter.saveSharedPref("isAbove", "true", requireContext())

        } else {
            binding.telephone.visibility = View.GONE
            formatter.saveSharedPref("isAbove", "false", requireContext())

        }
        updateIdentifications(year)
    }

    private fun validateData() {
        var gender = ""
        var dateType = ""
        val firstName = binding.firstname.text.toString()
        val lastName = binding.lastname.text.toString()
        val middleName = binding.middlename.text.toString()
        var dateOfBirthString = binding.dateOfBirth.text.toString()
        val age = binding.calculatedAge.text.toString()
        val identificationType = binding.identificationType.text.toString()
        val identificationNumberString = binding.identificationNumber.text.toString()
        val tel = binding.telephone.text.toString()

        if (firstName.isEmpty()) {
            binding.apply {
                telFirstname.error = "Enter firstname"
                firstname.requestFocus()
                return
            }
        }
        if (lastName.isEmpty()) {
            binding.apply {
                telLastName.error = "Enter lastname"
                lastname.requestFocus()
                return
            }
        }
        val checkedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        if (checkedRadioButtonId != -1) {
            // RadioButton is selected, find the selected RadioButton
            val selectedRadioButton =
                binding.root.findViewById<RadioButton>(checkedRadioButtonId)
            gender = selectedRadioButton.text.toString()

        } else {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val checkedRadioButtonIdInput = binding.radioGroupChild.checkedRadioButtonId
        if (checkedRadioButtonIdInput == -1) {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(requireContext(), "Please select vaccination category", Toast.LENGTH_SHORT)
                .show()
            return
        }


        val estimatedID = binding.radioGroupDob.checkedRadioButtonId
        if (estimatedID != -1) {
            // RadioButton is selected, find the selected RadioButton
            val selectedRadioButtonDob = binding.root.findViewById<RadioButton>(estimatedID)
            dateType = selectedRadioButtonDob.text.toString()

        } else {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(
                requireContext(),
                "Please select date of birth option",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (dateType == "Actual") {

            if (dateOfBirthString.isEmpty()) {
                binding.apply {
                    telDateOfBirth.error = "Enter date of birth"
                    dateOfBirth.requestFocus()
                    return
                }
            }
        } else {
            // check all the fields
            val year = binding.editTextOne.text.toString()
            val months = binding.editTextTwo.text.toString()
            val weeks = binding.editTextThree.text.toString()

            if (year.isEmpty() && months.isEmpty() && weeks.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter estimate age",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

            val enteredYear = binding.editTextOne.text.toString().toIntOrNull() ?: 0
            val enteredMonths = binding.editTextTwo.text.toString().toIntOrNull() ?: 0
            val enteredWeeks = binding.editTextThree.text.toString().toIntOrNull() ?: 0
            val dateOfBirth = calculateDateOfBirth(enteredYear, enteredMonths, enteredWeeks)

            dateOfBirthString = dateOfBirth

        }
        if (identificationType.isEmpty()) {
            binding.apply {
                Toast.makeText(
                    requireContext(),
                    "Please select identification type",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        if (identificationNumberString.isEmpty()) {
            binding.apply {
                identificationNumber.requestFocus()
                telIdentificationNumber.error = "Please enter value"
                return
            }
        }

        var estimate = true

        if (binding.radioButtonYesDob.isChecked) {
            estimate = false
        }

        if (binding.telTelephone.isVisible) {
            if (tel.length != 10) {
                binding.apply {
                    telTelephone.error = "Enter Valid phone number"
                    telephone.requestFocus()
                    return
                }
            }
        }

        val payload = CustomPatient(
            firstname = AppUtils().capitalizeFirstLetter(firstName),
            middlename = AppUtils().capitalizeFirstLetter(middleName),
            lastname = AppUtils().capitalizeFirstLetter(lastName),
            gender = gender,
            age = age,
            dateOfBirth = dateOfBirthString,
            identification = identificationType,
            identificationNumber = identificationNumberString,
            telephone = tel,
            estimate = estimate,

            )

        formatter.saveSharedPref("personal", Gson().toJson(payload), requireContext())

        mListener?.onNextPageRequested()


    }

    private fun calculateDateOfBirth(year: Int, months: Int, weeks: Int): String {
        // Get current date
        val currentDate = Calendar.getInstance()
        // Subtract years from the current date
        currentDate.add(Calendar.YEAR, -year)
        // Subtract months
        currentDate.add(Calendar.MONTH, -months)
        // Subtract weeks
        currentDate.add(Calendar.WEEK_OF_YEAR, -weeks)
        // Format the date to yyyy-MM-dd
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(currentDate.time)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}