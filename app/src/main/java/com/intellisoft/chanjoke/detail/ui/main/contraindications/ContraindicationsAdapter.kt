package com.intellisoft.chanjoke.detail.ui.main.contraindications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R


class ContraindicationsAdapter(
    private var entryList: MutableList<String>,
    private val context: Context
) : RecyclerView.Adapter<ContraindicationsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvContraindicated: TextView = itemView.findViewById(R.id.tvContraindicated)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                entryList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.contraindications_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val selectedVaccine = entryList[position]
        holder.tvContraindicated.text = selectedVaccine
    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}