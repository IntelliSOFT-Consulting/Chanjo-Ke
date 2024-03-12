package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.StatusColors
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class VaccineDetailsAdapter(
    private var patientDetailsViewModel: PatientDetailsViewModel,
    private val vaccineDetailsList: ArrayList<DbVaccineScheduleChild>,
    private val onCheckBoxSelectedListener: OnCheckBoxSelectedListener,
    private val context: Context
    ) :
    RecyclerView.Adapter<VaccineDetailsAdapter.VaccineDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vaccination_schedule_vaccines, parent, false)
        return VaccineDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineDetailsViewHolder, position: Int) {
        val currentItem = vaccineDetailsList[position]
        val formatterClass = FormatterClass()
//        holder.checkBox.isChecked = currentItem.isVaccinated
        val vaccineName = currentItem.vaccineName
        val isVaccinated = currentItem.isVaccinated
        val status = currentItem.status
        val date = currentItem.date
        val canBeVaccinated = currentItem.canBeVaccinated

        var vaccineStatus = ""
        if (status == StatusColors.NORMAL.name){
            vaccineStatus = ""
        }else if (status == StatusColors.GREEN.name){
            vaccineStatus = "Administered"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.green))
        }else if (status == StatusColors.AMBER.name){
            vaccineStatus = "Contraindicated"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.amber))
            val daysTo = formatterClass.daysBetweenTodayAndGivenDate(date)
            if (daysTo != null){
                val daysToInt = daysTo.toInt()
                if (daysToInt != 0){
                    holder.checkBox.isEnabled = false
                }
            }
        }else if (status == StatusColors.NORMAL.name){
            vaccineStatus = "Due"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.amber))
        }else if (status == StatusColors.RED.name){
            vaccineStatus = "Missed"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.red))
        }else{
            vaccineStatus = ""
        }
        if (canBeVaccinated != null) {
            holder.checkBox.isEnabled = canBeVaccinated
        }

        holder.tvScheduleStatus.text = vaccineStatus
        holder.tvVaccineName.text = vaccineName
        holder.tvVaccineDate.text = date

        if(isVaccinated){
            holder.checkBox.visibility = View.INVISIBLE
            holder.checked.visibility = View.VISIBLE
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckBoxSelectedListener.onCheckBoxSelected(position, isChecked, vaccineName)
        }
    }

    override fun getItemCount(): Int {
        return vaccineDetailsList.size
    }

    inner class VaccineDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val tvScheduleStatus: TextView = itemView.findViewById(R.id.tvScheduleStatus)
        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvVaccineDate: TextView = itemView.findViewById(R.id.tvVaccineDate)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val checked: ImageButton = itemView.findViewById(R.id.checked)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            val immunizationHandler = ImmunizationHandler()

            val pos = adapterPosition
            val vaccineName = vaccineDetailsList[pos].vaccineName

            val formatterClass = FormatterClass()

            formatterClass.saveSharedPref("vaccineNameDetails", vaccineName, context)

            val patientId = FormatterClass().getSharedPref("patientId", context)
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.VACCINE_DETAILS.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)

        }
    }

    // Function to clear the data set
    fun clear() {
        vaccineDetailsList.clear()
    }

    interface OnCheckBoxSelectedListener {
        fun onCheckBoxSelected(position: Int, isChecked: Boolean, vaccineName: String)
    }


}