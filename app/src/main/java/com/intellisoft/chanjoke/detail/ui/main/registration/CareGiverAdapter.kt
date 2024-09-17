package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.CareGiver

class CareGiverAdapter(
    private var entryList: MutableList<CareGiver>,
    private val context: Context,
    private val click: (CareGiver) -> Unit,
) : RecyclerView.Adapter<CareGiverAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        val tvIdType: TextView = itemView.findViewById(R.id.tv_id_type)
        val tvIdNumber: TextView = itemView.findViewById(R.id.tv_id_number)
        val imvCancel: ImageView = itemView.findViewById(R.id.imv_cancel)

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
        val careGiverIdType = entryList[position].careGiverIdType
        val careGiverIdNumber = entryList[position].careGiverIdNumber
        val data = entryList[position]

        holder.tvType.text = tvType
        holder.tvName.text = tvName
        holder.tvPhone.text = tvPhone
        holder.tvIdType.text = careGiverIdType
        holder.tvIdNumber.text = careGiverIdNumber
        holder.imvCancel.apply {
            setOnClickListener {
                val layoutInflater = LayoutInflater.from(context)
                val dialogView = layoutInflater.inflate(R.layout.full_screen_dialog_layout, null)
                val builder = AlertDialog.Builder(context)
                builder.setView(dialogView)

                val alertDialog = builder.create()
                alertDialog.window?.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(
                            context,
                            R.color.colorPrimary
                        )
                    )
                )

                alertDialog.show()
                val layoutParams = WindowManager.LayoutParams().apply {
                    copyFrom(alertDialog.window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                    gravity = Gravity.BOTTOM
                }
                // Get reference to buttons
                val buttonYes = dialogView.findViewById<MaterialButton>(R.id.button_yes)
                val buttonNo = dialogView.findViewById<MaterialButton>(R.id.button_no)

                buttonYes.setOnClickListener {
                    alertDialog.dismiss() // Dismiss dialog if necessary
                    click(data)
                }

                buttonNo.setOnClickListener {
                    alertDialog.dismiss()
                }
                alertDialog.window?.attributes = layoutParams
                alertDialog.window?.setLayout(layoutParams.width, layoutParams.height)

            }
        }

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

    fun removeItem(careGiver: CareGiver) {
        //
        if (entryList.contains(careGiver)) {
            entryList.remove(careGiver)
            notifyDataSetChanged()
        }
    }
}