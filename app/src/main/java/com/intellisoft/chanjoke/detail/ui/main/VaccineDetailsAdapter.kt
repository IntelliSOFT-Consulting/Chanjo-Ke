package com.intellisoft.chanjoke.detail.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineScheduleChild

class VaccineDetailsAdapter(
    private val vaccineDetailsList: ArrayList<DbVaccineScheduleChild>,
    private val onCheckBoxSelectedListener: OnCheckBoxSelectedListener) :
    RecyclerView.Adapter<VaccineDetailsAdapter.VaccineDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vaccination_schedule_vaccines, parent, false)
        return VaccineDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineDetailsViewHolder, position: Int) {
        val currentItem = vaccineDetailsList[position]
//        holder.checkBox.isChecked = currentItem.isVaccinated
        holder.tvVaccineName.text = currentItem.vaccineName

        if(currentItem.isVaccinated){
            holder.checkBox.visibility = View.INVISIBLE
            holder.checked.visibility = View.VISIBLE
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckBoxSelectedListener.onCheckBoxSelected(position, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return vaccineDetailsList.size
    }

    inner class VaccineDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val checked: ImageButton = itemView.findViewById(R.id.checked)
    }

    interface OnCheckBoxSelectedListener {
        fun onCheckBoxSelected(position: Int, isChecked: Boolean)
    }


}