package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.AllergicReaction
import com.intellisoft.chanjoke.fhir.data.DbVaccineHistory
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class UpdateVaccineHistoryAdapter(
    private var entryList: List<DbVaccineHistory>,
    private val context: Context
) : RecyclerView.Adapter<UpdateVaccineHistoryAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvDoseNumber: TextView = itemView.findViewById(R.id.tvDoseNumber)

        init {
            itemView.setOnClickListener(this)
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
                R.layout.update_multiple_vaccine_history,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val vaccineName = entryList[position].vaccineName
        val date = entryList[position].lastDoseDate
        val doseNumber = entryList[position].doseNumber

        holder.tvVaccineName.text = vaccineName
        holder.tvDate.text = date
        holder.tvDoseNumber.text = doseNumber

    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}