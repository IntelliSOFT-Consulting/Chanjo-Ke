package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class VaccineAefiAdapter(
    private val patientDetailsViewModel: PatientDetailsViewModel,
    private var entryList: ArrayList<DbVaccineData>,
    private val context: Context
) : RecyclerView.Adapter<VaccineAefiAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val aefiList: RecyclerView = itemView.findViewById(R.id.aefi_list)
        val tvVaccineName: TextView = itemView.findViewById(R.id.tv_vaccine_name)

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
                R.layout.aefi_parent_card_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val vaccineName = entryList[position].vaccineName
        holder.tvVaccineName.text = vaccineName

        val logicalId = FormatterClass().getSharedPref("current_immunization", context)

        if (logicalId != null) {
            val adverseEvents = patientDetailsViewModel.loadImmunizationAefis(logicalId)

            val vaccineAdapter = EventsAdapter(adverseEvents, context)
            holder.aefiList.adapter = vaccineAdapter
        }


    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}