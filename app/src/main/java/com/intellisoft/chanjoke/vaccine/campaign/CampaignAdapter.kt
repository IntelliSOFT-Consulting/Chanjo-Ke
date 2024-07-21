package com.intellisoft.chanjoke.vaccine.campaign

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.DbCarePlan
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.stock_management.VaccineStockManagement
import org.hl7.fhir.r4.model.codesystems.ImmunizationRecommendationStatus
import java.time.LocalDate

class CampaignAdapter(
    private var entryList: ArrayList<DbCarePlan>,
    private val context: Context
) : RecyclerView.Adapter<CampaignAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvCampaignName: TextView = itemView.findViewById(R.id.tvCampaignName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStart: TextView = itemView.findViewById(R.id.tvStart)

        init {
            itemView.setOnClickListener(this)
            tvStart.setOnClickListener {
                val pos = adapterPosition
                val formatterClass = FormatterClass()
                val patientId = FormatterClass().getSharedPref("patientId", context)



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
                R.layout.campaigns_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val campaignName = entryList[position].title
        val dateCreated = entryList[position].createdOn

        holder.tvCampaignName.text = campaignName
        holder.tvDate.text = dateCreated

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}