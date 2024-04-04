package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.CareGiver

class CareGiverAdapter(
    private var entryList: MutableList<CareGiver>,
    private val context: Context
) : RecyclerView.Adapter<CareGiverAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.care_givers,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val tvType = entryList[position].type
        val tvPhone = entryList[position].phone
        val tvName = entryList[position].name

        holder.tvType.text = tvType
        holder.tvName.text = tvName
        holder.tvPhone.text = tvPhone

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

    fun addItem(careGiver: CareGiver) {
        //
        if (!entryList.contains(careGiver)) {
            entryList.add(careGiver)
            notifyItemInserted(entryList.size - 1)
        }
    }
}