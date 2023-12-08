package com.intellisoft.chanjoke.vaccine

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.DbVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.vaccine.validations.SeriesVaccine


class BottomSheetFragment : BottomSheetDialogFragment() {

    private var lastExpandedPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        val expandableListView: ExpandableListView = view.findViewById(R.id.expandableListView)

        expandableListView.setOnGroupClickListener { parent, view, groupPosition, id ->
            // Handle group click here
            if (lastExpandedPosition != -1 && lastExpandedPosition != groupPosition) {
                expandableListView.collapseGroup(lastExpandedPosition)
            }

            if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition)
                lastExpandedPosition = -1
            } else {
                expandableListView.expandGroup(groupPosition)
                lastExpandedPosition = groupPosition
            }

            true // Return true to consume the click event
        }

        val immunizationHandler = ImmunizationHandler()
        val (groupList, childList) = immunizationHandler.generateVaccineLists(requireContext())

        val adapter = BottomSheetAdapter(groupList, childList, requireContext())
        expandableListView.setAdapter(adapter)

        return view
    }

}