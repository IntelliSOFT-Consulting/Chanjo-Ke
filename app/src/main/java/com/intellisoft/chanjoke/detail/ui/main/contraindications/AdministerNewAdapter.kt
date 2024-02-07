package com.intellisoft.chanjoke.detail.ui.main.contraindications

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.AdverseEventActivity
import com.intellisoft.chanjoke.fhir.data.AdverseEventData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails


class AdministerNewAdapter(
    private var entryList: MutableList<String>,
    private val context: Context
) : RecyclerView.Adapter<AdministerNewAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val spinner: TextView = itemView.findViewById(R.id.spinner)
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
        val selectedVaccine = "${entryList[position]} batch number"
        holder.spinner.text = selectedVaccine
    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}