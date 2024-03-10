package com.intellisoft.chanjoke.detail.ui.main.aefis.edit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentReviewBinding
import com.intellisoft.chanjoke.detail.ui.main.registration.OnButtonClickListener
import com.intellisoft.chanjoke.fhir.data.AEFIData
import com.intellisoft.chanjoke.fhir.data.Child
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Parent

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReviewFragment : Fragment() {
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

    private var mListener: OnButtonClickListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity implements the interface
        mListener = if (context is OnButtonClickListener) {
            context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    private lateinit var binding: FragmentReviewBinding
    private val formatter = FormatterClass()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReviewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        binding.apply {
            previousButton.apply {
                setOnClickListener {
                    mListener?.onPreviousPageRequested()
                }
            }
            nextButton.apply {
                setOnClickListener {
                    mListener?.onNextPageRequested()

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        loadData()
    }

    private fun loadData() {
        try {
            val parentData = formatter.getSharedPref("parent_aefi", requireContext())
            if (parentData != null) {
                val refinedParent = Gson().fromJson(parentData, Parent::class.java)
                binding.apply {
                    typeOfAEFITextView.text = refinedParent.type
                    briefDetailsTextView.text = refinedParent.brief
                    onsetOfEventTextView.text = refinedParent.onset
                    pastMedicalHistoryTextView.text = refinedParent.history
                }
            }

            val childData = formatter.getSharedPref("child_aefi", requireContext())
            if (childData != null) {
                val refinedChild = Gson().fromJson(childData, Child::class.java)
                binding.apply {
                    reactionSeverityTextView.text = refinedChild.severity
                    actionTakenTextView.text = refinedChild.action
                    aefiOutcomeTextView.text = refinedChild.outcome
                    nameOfPersonTextView.text = refinedChild.reporter
                    contactTextView.text = refinedChild.phone

                }
            }
            if (parentData != null && childData != null) {
                val data = AEFIData(
                    type = binding.typeOfAEFITextView.text.toString(),
                    brief = binding.briefDetailsTextView.text.toString(),
                    onset = binding.onsetOfEventTextView.text.toString(),
                    history = binding.pastMedicalHistoryTextView.text.toString(),
                    severity = binding.reactionSeverityTextView.text.toString(),
                    action = binding.actionTakenTextView.text.toString(),
                    outcome = binding.aefiOutcomeTextView.text.toString(),
                    reporter = binding.nameOfPersonTextView.text.toString(),
                    phone = binding.contactTextView.text.toString()
                )
                formatter.saveSharedPref(
                    "updated_aefi_data",
                    Gson().toJson(data),
                    requireContext()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}