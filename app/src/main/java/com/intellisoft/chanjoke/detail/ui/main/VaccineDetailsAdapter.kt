package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.Reasons
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
        val immunizationHandler = ImmunizationHandler()
//        holder.checkBox.isChecked = currentItem.isVaccinated
        val vaccineName = currentItem.vaccineName
        val isVaccinated = currentItem.isVaccinated
        val statusColor = currentItem.statusColor
        val status = currentItem.status
        val date = currentItem.date
        val canBeVaccinated = currentItem.canBeVaccinated
        val daysTo = formatterClass.daysBetweenTodayAndGivenDate(date)

        var vaccineStatus = ""
        if (statusColor == StatusColors.GREEN.name){
            vaccineStatus = "Administered"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.green))
        }
        else if (statusColor == StatusColors.AMBER.name){
            vaccineStatus = "Rescheduled"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.amber))
            if (daysTo != null){
                val daysToInt = daysTo.toInt()
                if (daysToInt != 0){
                    holder.checkBox.isEnabled = false
                }
            }
        }
        else if (statusColor == StatusColors.NORMAL.name){

            // "All the others are upcoming"
            vaccineStatus = "Upcoming"
            //Check if the date is within 14 days
            if (daysTo != null) {
                val daysToInt = daysTo.toInt()
                if (daysToInt < 14){
                    vaccineStatus = "Due"
                }
            }

        }
        else if (statusColor == StatusColors.RED.name){
            vaccineStatus = "Missed"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.red))

            //For BCG and bOpv
            if (vaccineName == "BCG" || vaccineName == "bOPV"){
                //Check the date and change status

                val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
                if (basicVaccine != null){
                    /**
                     * BCG can be given till 255
                     * bOPV will be till 2 weeks
                     */
                    if (daysTo != null){
                        val daysToInt = daysTo.toInt()
                        if (daysToInt < 15 && vaccineName == "bOPV"){
                            vaccineStatus = "Due"
                            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.black))
                        }
                        if (daysToInt < 255 && vaccineName == "BCG"){
                            //Due till 59 months
                            vaccineStatus = "Due"
                            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.black))

                        }
                    }

                }
            }
        }
        else if (statusColor == StatusColors.NOT_DONE.name){

            if (status == Reasons.CONTRAINDICATE.name){
                vaccineStatus = "Contraindicated"
            }

            //Add validation to check if in the Not administered we have a Contraindication.
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.amber))
        }
        else{
            vaccineStatus = ""
        }


        holder.iconDisabled.setOnClickListener {

            val message = if (status == Reasons.CONTRAINDICATE.name){
                "The vaccine cannot be administered, it has been contraindicated."
            }else{
                "You are not eligible for this vaccine."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }


        holder.tvScheduleStatus.text = vaccineStatus
        holder.tvVaccineName.text = vaccineName
        holder.tvVaccineDate.text = date

        /**
         * 1. Check if its administered and show administered icon
         *  Disable the rest of the icons
         *
         * 2. Check if it can be vaccinated and show vaccinate icon
         */

        holder.checkBox.isEnabled = canBeVaccinated ?: false

        if (canBeVaccinated == true){
            holder.checkBox.visibility = View.VISIBLE

            holder.iconDisabled.visibility = View.GONE
            holder.imgBtnView.visibility = View.GONE
            holder.checked.visibility = View.GONE
        }else{

            holder.iconDisabled.visibility = View.VISIBLE

            holder.checkBox.visibility = View.GONE
            holder.imgBtnView.visibility = View.GONE
            holder.checked.visibility = View.GONE
        }

        if (isVaccinated){
            holder.checked.visibility = View.VISIBLE

            holder.iconDisabled.visibility = View.GONE
            holder.imgBtnView.visibility = View.GONE
            holder.checkBox.visibility = View.GONE
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
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val checked: ImageButton = itemView.findViewById(R.id.checked)
        val iconDisabled: ImageButton = itemView.findViewById(R.id.iconDisabled)
        val imgBtnView: ImageButton = itemView.findViewById(R.id.imgBtnView)


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