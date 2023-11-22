package com.intellisoft.chanjoke.vaccine

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.chanjoke.R


class BottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        val expandableListView: ExpandableListView = view.findViewById(R.id.expandableListView)

        val groupList = listOf("POLIO", "YELLOW FEVER", "COVID", "MEASLES")
        val childList = mapOf(
            "POLIO" to listOf("bOPV", "OPV I", "OPV II", "OPV III"),
            "YELLOW FEVER" to listOf("YELLOW FEVER"),
            "COVID" to listOf("Astrazeneca","Moderna","JohnsonAndJohnson","PhizerBioNTech","Sinopharm"),
            "MEASLES" to listOf("MEASLES")
        )

        val adapter = BottomSheetAdapter(groupList, childList, requireContext())
        expandableListView.setAdapter(adapter)

        return view
    }

}