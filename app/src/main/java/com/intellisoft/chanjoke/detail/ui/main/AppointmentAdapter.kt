package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.BottomSheetFragment
import com.intellisoft.chanjoke.vaccine.stock_management.VaccineStockManagement
import org.hl7.fhir.r4.model.codesystems.ImmunizationRecommendationStatus

class AppointmentAdapter(
    private var entryList: ArrayList<DbAppointmentDetails>,
    private val context: Context
) : RecyclerView.Adapter<AppointmentAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvAppointment: TextView = itemView.findViewById(R.id.tvAppointment)
        val tvDateScheduled: TextView = itemView.findViewById(R.id.tvDateScheduled)
        val tvDoseNumber: TextView = itemView.findViewById(R.id.tvDoseNumber)
        val btnAdministerVaccine: TextView = itemView.findViewById(R.id.btnAdministerVaccine)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val chipAppointment: Chip = itemView.findViewById(R.id.chipAppointment)

        init {
            itemView.setOnClickListener(this)
            btnAdministerVaccine.setOnClickListener {
                val pos = adapterPosition
                val formatterClass = FormatterClass()
                val patientId = FormatterClass().getSharedPref("patientId", context)
                val targetDisease = entryList[pos].targetDisease
                val administeredProduct = entryList[pos].vaccineName
                val appointmentStatus = entryList[pos].appointmentStatus.trim()
                val appointmentId = entryList[pos].appointmentId.trim()

                formatterClass.saveSharedPref(
                    "questionnaireJson",
                    "contraindications.json",
                    context)

                formatterClass.saveSharedPref(
                    "vaccinationFlow",
                    "createVaccineDetails",
                    context
                )


                formatterClass.saveSharedPref(
                    "vaccinationTargetDisease",
                    targetDisease,
                    context
                )
                formatterClass.saveSharedPref(
                    "administeredProduct",
                    administeredProduct,
                    context
                )

//                if (appointmentStatus == "Contraindicated" && appointmentId != ""){
//                    formatterClass.saveSharedPref(
//                        "isContraindicated",
//                        appointmentId,
//                        context
//                    )
//                }


                val intent = Intent(context, VaccineStockManagement::class.java)
                intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
                intent.putExtra("patientId", patientId)
                context.startActivity(intent)

            }

        }

        override fun onClick(p0: View) {

            val pos = adapterPosition

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.appointments,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val targetDisease = entryList[position].targetDisease
        val vaccineName = entryList[position].vaccineName
        val dateScheduled = entryList[position].dateScheduled
        val doseNumber = entryList[position].doseNumber
        val appointmentStatus = entryList[position].appointmentStatus

        val dateScheduledFormat = FormatterClass().convertDateFormat(dateScheduled)

        holder.tvAppointment.text = vaccineName
        holder.tvDateScheduled.text = dateScheduledFormat
        holder.tvDoseNumber.text = doseNumber
        holder.chipAppointment.text = appointmentStatus
        holder.tvStatus.text = appointmentStatus

        if (appointmentStatus.equals(ImmunizationRecommendationStatus.DUE.name, ignoreCase = true)){
            holder.chipAppointment.setChipBackgroundColorResource(android.R.color.holo_red_light)
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.CONTRAINDICATED.name, ignoreCase = true)){
            holder.chipAppointment.setChipBackgroundColorResource(android.R.color.darker_gray)
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.COMPLETE.name, ignoreCase = true)){
            holder.btnAdministerVaccine.isVisible = false
            holder.chipAppointment.setChipBackgroundColorResource(android.R.color.holo_green_dark)
        }else{
            holder.chipAppointment.setChipBackgroundColorResource(android.R.color.holo_red_light)
        }

        if (appointmentStatus.equals(ImmunizationRecommendationStatus.DUE.name, ignoreCase = true)){
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.CONTRAINDICATED.name, ignoreCase = true)){
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.darker_gray))
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.COMPLETE.name, ignoreCase = true)){
            holder.btnAdministerVaccine.isVisible = false
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else{
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}