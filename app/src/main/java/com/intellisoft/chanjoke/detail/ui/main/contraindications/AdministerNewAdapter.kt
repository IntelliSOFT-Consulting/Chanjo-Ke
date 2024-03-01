package com.intellisoft.chanjoke.detail.ui.main.contraindications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbBatchNumbers


class AdministerNewAdapter(
    private var entryList: MutableList<DbBatchNumbers>,
    private val context: Context
) : RecyclerView.Adapter<AdministerNewAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val spinner: TextView = itemView.findViewById(R.id.spinner)
        val tvDiseaseTargeted: TextView = itemView.findViewById(R.id.diseaseTargeted)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val position = adapterPosition

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {


        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.batch_number_items,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val selectedVaccine = "${entryList[position].vaccineName} batch number"
        val diseaseTargeted = entryList[position].diseaseTargeted

        holder.spinner.text = selectedVaccine
        holder.tvDiseaseTargeted.text = diseaseTargeted
    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}