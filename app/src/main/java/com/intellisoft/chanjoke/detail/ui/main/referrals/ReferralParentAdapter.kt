package com.intellisoft.chanjoke.detail.ui.main.referrals

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbTempData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.fhir.data.ServiceRequestPatient
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class ReferralParentAdapter(
    private var entryList: List<ServiceRequestPatient>,
    private val context: Context
) : RecyclerView.Adapter<ReferralParentAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name: TextView = itemView.findViewById(R.id.name)
        val idNumber: TextView = itemView.findViewById(R.id.idNumber)
        val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        val btnView: Button = itemView.findViewById(R.id.btnView)

        val viewPhoneNumber: TextView = itemView.findViewById(R.id.viewPhoneNumber)
        val viewId: TextView = itemView.findViewById(R.id.viewId)
        val viewName: TextView = itemView.findViewById(R.id.viewName)

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
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val payload = entryList[position]
        val patientName = entryList[position].patientName
        val patientPhone = entryList[position].patientPhone
        val patientNational = entryList[position].patientNational

        holder.name.text = patientName
        holder.idNumber.text = patientNational
        holder.tvPhoneNumber.text = patientPhone

        holder.viewPhoneNumber.text = "Phone Number"
        holder.viewId.text = "Identification No"
        holder.viewName.text = "Name"

        holder.btnView.setOnClickListener {
            Timber.e("PatientId **** ${payload.patientId}")
            val patientId = payload.patientId
            val age = try {
                FormatterClass().getFormattedAge(
                    payload.dob,
                    holder.btnView.context.resources, context
                )
            } catch (e: Exception) {
                ""
            }

            val temp = DbTempData(
                name = patientName,
                dob = payload.dob,
                gender = payload.gender,
                age = age,
            )
            FormatterClass().saveSharedPref(
                "temp_data",
                Gson().toJson(temp),
                context
            )

            FormatterClass().saveSharedPref("patientId", patientId, context)
            val intent =
                Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.REFERRALS.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}