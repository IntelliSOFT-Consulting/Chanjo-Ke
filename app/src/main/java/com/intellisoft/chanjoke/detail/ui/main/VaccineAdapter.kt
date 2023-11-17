package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineData

class VaccineAdapter(private var entryList: ArrayList<DbVaccineData>,
                     private val context: Context
) : RecyclerView.Adapter<VaccineAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)

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
                R.layout.administered_vaccines,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val vaccineName = entryList[position].vaccineName
        val vaccineDosage = entryList[position].vaccineDosage

        holder.tvVaccineName.text = "Vaccine: $vaccineName"
        holder.tvDosage.text = "Dosage: $vaccineDosage"


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}