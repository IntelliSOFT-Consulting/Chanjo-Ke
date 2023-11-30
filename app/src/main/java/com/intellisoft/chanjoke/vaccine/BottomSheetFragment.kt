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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        val expandableListView: ExpandableListView = view.findViewById(R.id.expandableListView)

        val immunizationHandler = ImmunizationHandler()
        val (groupList, childList) = immunizationHandler.generateVaccineLists()

        val adapter = BottomSheetAdapter(groupList, childList, requireContext())
        expandableListView.setAdapter(adapter)

        return view
    }

}