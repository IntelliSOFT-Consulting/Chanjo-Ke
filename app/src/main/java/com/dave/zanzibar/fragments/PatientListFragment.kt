package com.dave.zanzibar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dave.zanzibar.MainActivity
import com.dave.zanzibar.R
import com.dave.zanzibar.databinding.FragmentPatientListViewBinding
import org.hl7.fhir.r4.model.Patient

class PatientListFragment : Fragment() {

    private lateinit var _binding:FragmentPatientListViewBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListViewBinding.inflate(inflater, container, false)



        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }


    }




}