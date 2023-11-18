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

        val groupList = listOf("Group 1", "Group 2")
        val childList = mapOf(
            "Group 1" to listOf("Child 1.1", "Child 1.2"),
            "Group 2" to listOf("Child 2.1", "Child 2.2")
        )

        val adapter = BottomSheetAdapter(groupList, childList)
        expandableListView.setAdapter(adapter)

        return view
    }

}