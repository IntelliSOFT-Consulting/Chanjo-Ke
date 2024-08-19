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
        var statusValue = currentItem.statusValue

        var vaccineStatus = ""
        /**
         * Handle different statues
         * 1. COMPLETED
         * 2. CONTRAINDICATE
         * 3. NOT_ADMINISTERED
         * 4. RESCHEDULED
         */

        if (statusValue.equals(Reasons.COMPLETED.name, true)){
            statusValue = "Administered"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.green))
        }else if (statusValue.equals(Reasons.CONTRAINDICATE.name, true)){
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.contra))
        }else if (statusValue.equals(Reasons.NOT_ADMINISTERED.name, true)){
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.colorAccent))
        }else if (statusValue.equals(Reasons.RESCHEDULE.name, true)){
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.amber))
        }else if (statusValue.equals("Missed", true)){
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.red))
        }else if (statusValue.equals("Due", true)){
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.black))
        }else{
            statusValue = "Upcoming"
            holder.tvScheduleStatus.setTextColor(context.resources.getColor(R.color.black))
        }

        statusValue = when(statusValue){
            "Contraindicate", "Reschedule" -> statusValue +"d"
            "Not_administered" -> "Not Administered"
            else -> statusValue
        }


        holder.iconDisabled.setOnClickListener {

            val message = if (status == Reasons.CONTRAINDICATE.name){
                "The vaccine cannot be administered, it has been contraindicated."
            }else{
                "You are not eligible for this vaccine."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }


        holder.tvScheduleStatus.text = statusValue
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