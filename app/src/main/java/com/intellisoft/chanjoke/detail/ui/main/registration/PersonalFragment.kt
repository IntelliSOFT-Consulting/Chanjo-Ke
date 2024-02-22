package com.intellisoft.chanjoke.detail.ui.main.registration

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientViewModel
import com.intellisoft.chanjoke.databinding.FragmentPersonalBinding
import com.intellisoft.chanjoke.fhir.data.CustomPatient
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.utils.AppUtils
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import timber.log.Timber
import java.util.Calendar


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

        val suggestions = arrayOf(
            "Birth Certificate",
            "National ID",
            "Passport",
            "NEMIS No",
            "Birth Notification Number"
        )
        // Create ArrayAdapter with the array of strings
        val adapterType =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)

        binding.apply {

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    val dataValue = when (checkedId) {
                        R.id.radioButtonYes -> "Male"
                        R.id.radioButtonNo -> "Female"
                        else -> null
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
                setOnClickListener { }
            }
        }

    }

    private fun calculateUserAge(valueCurrent: String) {
        val result = formatter.calculateAge(valueCurrent)
        binding.calculatedAge.setText(result)

    }

    private fun validateData() {
        var gender = ""
        var dateType = ""
        val firstName = binding.firstname.text.toString()
        val lastName = binding.lastname.text.toString()
        val middleName = binding.middlename.text.toString()
        val dateOfBirthString = binding.dateOfBirth.text.toString()
        val age = binding.calculatedAge.text.toString()
        val identificationType = binding.identificationType.text.toString()
        val identificationNumberString = binding.identificationNumber.text.toString()
        val telephone = binding.telephone.text.toString()

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
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedRadioButtonId)
            gender = selectedRadioButton.text.toString()

        } else {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
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

        val payload = CustomPatient(
            firstname = firstName,
            middlename = middleName,
            lastname = lastName,
            gender = gender,
            age =age ,
            dateOfBirth = dateOfBirthString,
            identification = identificationType,
            identificationNumber = identificationNumberString,
            telephone = telephone
        )

        formatter.saveSharedPref("personal", Gson().toJson(payload), requireContext())
        Timber.e("TAG Patient payload ***** $payload")
        mListener?.onNextPageRequested()


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