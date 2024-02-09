package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbStatusColor
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class VaccineScheduleAdapter(
    private val context: Context,
    private val administeredList: ArrayList<DbVaccineData>,
    private val dbStatusColorList: ArrayList<DbStatusColor>,
    private val expandableListTitle: List<String>,
    private val expandableListDetail: Map<String, List<BasicVaccine>>,
    private val patientDetailsViewModel: PatientDetailsViewModel,
    private val tvAdministerVaccine: TextView
) : BaseExpandableListAdapter() {

    // Maintain a map to store the checked state of each checkbox
    private val checkedStates = HashMap<Pair<Int, Int>, Boolean>()

    init {
        updateAdministerVaccineText()
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    // Modify getChildView to handle checkbox state
    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as BasicVaccine
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.vaccination_schedule_vaccines, null)
        }
        val expandedListTextView = convertView!!.findViewById<TextView>(R.id.tvVaccineName)
        val tvVaccineDate = convertView!!.findViewById<TextView>(R.id.tvVaccineDate)
        val tvScheduleStatus = convertView!!.findViewById<TextView>(R.id.tvScheduleStatus)
        val checkBox = convertView.findViewById<CheckBox>(R.id.checkbox)

        val vaccineName = expandedListText.vaccineName
        expandedListTextView.text = vaccineName

        // Set checkbox state based on stored checked state
        val key = Pair(listPosition, expandedListPosition)
        checkBox.isChecked = checkedStates[key] ?: false

        // Update checked state when checkbox state changes
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedStates[key] = isChecked
            updateAdministerVaccineText()
        }

        //Check vaccine status
        for (administeredVaccine in administeredList) {

            if (vaccineName == administeredVaccine.vaccineName) {
                val dateAdministered = administeredVaccine.dateAdministered
                val status = administeredVaccine.status

                var vaccineStatus = ""
                if ("COMPLETED" == status) {
                    vaccineStatus = "administered"
                    tvScheduleStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
                    checkBox.visibility = View.INVISIBLE
                } else if ("NOTDONE" == status) {
                    vaccineStatus = "contraindicated"
                    tvScheduleStatus.setTextColor(ContextCompat.getColor(context, R.color.amber))
                } else {
                    tvScheduleStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.darker_gray
                        )
                    )
                }

                tvVaccineDate.text = dateAdministered
                tvScheduleStatus.text = vaccineStatus

            }

        }


        return convertView
    }

    // Method to update the tvAdministerVaccine TextView with the number of selected checkboxes
    private fun updateAdministerVaccineText() {
        val selectedCount = checkedStates.count { it.value }
        val value = "Administer Vaccine ($selectedCount)"
        tvAdministerVaccine.text = value
    }

    // Method to get the checked states
    // Method to get the list of selected BasicVaccine items
    fun getCheckedStates(): List<BasicVaccine> {
        val selectedVaccines = mutableListOf<BasicVaccine>()
        for ((positionPair, isChecked) in checkedStates) {
            if (isChecked) {
                val (groupPosition, childPosition) = positionPair
                val vaccine =
                    expandableListDetail[expandableListTitle[groupPosition]]?.get(childPosition)
                vaccine?.let {
                    selectedVaccines.add(it)
                }
            }
        }
        return selectedVaccines
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.vaccination_schedule, null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.tvScheduleTime)
        val imageViewSchedule = convertView!!.findViewById<ImageView>(R.id.imageViewSchedule)
        val aefiTextview = convertView!!.findViewById<TextView>(R.id.tvAefi)


        listTitleTextView.setTypeface(null, Typeface.BOLD)

        var weekNo = ""
        if (listTitle.toIntOrNull() != null) {
            weekNo = if (listTitle == "0") {
                "At Birth"
            } else {
                "$listTitle weeks"
            }
        } else {
            weekNo = listTitle
        }
        val patientId = FormatterClass().getSharedPref("patientId", context)
        val counter = patientDetailsViewModel.generateCurrentCount(
            weekNo,
            patientId.toString()
        )

        aefiTextview.text =
            "AEFIs ($counter)"
        aefiTextview.setOnClickListener {
            FormatterClass().saveSharedPref(
                "current_age", weekNo, context
            )

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.LIST_AEFI.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)
        }
        listTitleTextView.text = weekNo
        //Check if its immunised

        var statusColorValue = StatusColors.NORMAL.name
        for (dbStatusColor in dbStatusColorList) {
            if (dbStatusColor.keyTitle == listTitle) {
                statusColorValue = dbStatusColor.statusColor
            }
        }

        if (statusColorValue == StatusColors.GREEN.name) {
            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_green)
        } else if (statusColorValue == StatusColors.AMBER.name) {
            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_amber)
        } else if (statusColorValue == StatusColors.RED.name) {
            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_red)
        } else {
            imageViewSchedule.setImageResource(R.drawable.ic_action_schedule_normal)
        }


        return convertView
    }


    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
