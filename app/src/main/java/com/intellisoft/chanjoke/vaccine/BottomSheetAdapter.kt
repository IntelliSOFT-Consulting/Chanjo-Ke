package com.intellisoft.chanjoke.vaccine

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.stock_management.VaccineStockManagement
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import java.util.Locale

class BottomSheetAdapter(
    private val groupList: List<String>,
    private val childList: Map<String, List<String>>,
    private val context: Context
    ) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return groupList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return childList[groupList[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childList[groupList[groupPosition]]?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.group_item_layout, parent, false)
        val textView: TextView = view.findViewById(R.id.headerTextView)

        // Implement your group view here
        val value = getGroup(groupPosition).toString()
        textView.text = value
        return textView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.child_item_layout, parent, false)
        val textView: TextView = view.findViewById(R.id.childTextView)

        // Implement your child view here
        val valueText = getChild(groupPosition, childPosition).toString()
        textView.text = valueText

//        val valueGroup = getGroup(groupPosition).toString()

        textView.setOnClickListener {

            val immunizationHandler = ImmunizationHandler()
            val vaccineDetails = immunizationHandler.getVaccineDetailsByVaccineName(valueText)

            Log.e("------->","<-------")
            println(vaccineDetails)
            Log.e("------->","<-------")

//            FormatterClass().saveSharedPref("targetDisease", value, context)
//            val patientId = FormatterClass().getSharedPref("patientId", context)
//            val intent = Intent(context, VaccineStockManagement::class.java)
//            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
//            intent.putExtra("patientId", patientId)
//            context.startActivity(intent)
        }

        return textView
    }
}