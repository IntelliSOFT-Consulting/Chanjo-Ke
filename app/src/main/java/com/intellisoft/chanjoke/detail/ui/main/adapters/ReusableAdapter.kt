package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.ReusableViewModel
import com.intellisoft.chanjoke.fhir.data.ReusableListItem
import com.intellisoft.chanjoke.fhir.data.Status

class ReusableAdapter(private val items: MutableList<ReusableListItem>, private val viewModel: ReusableViewModel) :
    RecyclerView.Adapter<ReusableAdapter.ListViewHolder>() {

    inner class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val btnCancel: ImageButton = view.findViewById(R.id.btnCancel)
        val itemContainer: LinearLayout = view.findViewById(R.id.item_container)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reusable_list_item, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemName.text = item.name

        // Set color based on the status
        val backgroundColor: Int
        val borderColor: Int
        val cancelButtonColor: Int

        when (item.status) {
            Status.NOT_ADMINISTERED -> {
                backgroundColor = Color.parseColor("#FFCDD2") // Light Red
                borderColor = Color.parseColor("#F44336") // Darker Red
                cancelButtonColor = Color.parseColor("#F44336") // Same as border
            }
            Status.ADMINISTERED -> {
                backgroundColor = Color.parseColor("#C8E6C9") // Light Green
                borderColor = Color.parseColor("#4CAF50") // Darker Green
                cancelButtonColor = Color.parseColor("#4CAF50") // Same as border
            }
            Status.RESCHEDULED -> {
                backgroundColor = Color.parseColor("#FFF9C4") // Light Yellow
                borderColor = Color.parseColor("#FFEB3B") // Darker Yellow
                cancelButtonColor = Color.parseColor("#FFEB3B") // Same as border
            }
        }

        holder.itemContainer.setBackgroundColor(backgroundColor)

        // Apply a custom background with border to the card
        val backgroundDrawable = GradientDrawable().apply {
            setColor(backgroundColor)
            setStroke(4, borderColor) // 4dp border with the darker color
            cornerRadius = 8f
        }
        holder.itemContainer.background = backgroundDrawable

        // Set cancel button color
        holder.btnCancel.setColorFilter(cancelButtonColor)


        holder.btnCancel.setOnClickListener {
            viewModel.removeItem(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<ReusableListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
