package com.intellisoft.chanjoke.detail.ui.main.referrals

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.Administrative
import com.intellisoft.chanjoke.fhir.data.DbServiceRequest
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.utils.AppUtils
import java.text.SimpleDateFormat
import java.util.Locale

class ReferralAdapter(
    private var entryList: List<DbServiceRequest>,
    private val context: Context
) : RecyclerView.Adapter<ReferralAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val imvNextAction: ImageView = itemView.findViewById(R.id.imv_next_action)
        val linearView: LinearLayout = itemView.findViewById(R.id.ln_linearView)

        init {
            itemView.setOnClickListener(this)
            linearView.setOnClickListener {


            }
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
                R.layout.list_service_request,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val payload = entryList[position]
        val tvType = entryList[position].authoredOn
        val tvDate = entryList[position].vaccineName
        try {
            val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputDateFormat =
                SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val date = inputDateFormat.parse(tvType)
            val formattedDate = outputDateFormat.format(date)
            holder.tvType.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.tvName.text = tvDate
        holder.imvNextAction.apply {
            setOnClickListener {

                FormatterClass().saveSharedPref(
                    "selected_referral",
                    Gson().toJson(payload),
                    context
                )


                val patientId = FormatterClass().getSharedPref("patientId", context).toString()
                val intent =
                    Intent(context, MainActivity::class.java)
                intent.putExtra("functionToCall", NavigationDetails.REFERRAL_DETAILS.name)
                intent.putExtra("patientId", patientId)
                context.startActivity(intent)
            }
        }


    }

    private fun getDayOfMonthSuffix(day: Int): String {

        if (day in 11..13) {
            return "th"
        }
        return when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}