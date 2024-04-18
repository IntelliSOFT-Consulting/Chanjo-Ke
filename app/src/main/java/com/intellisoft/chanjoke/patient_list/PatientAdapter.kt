package com.intellisoft.chanjoke.patient_list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class PatientAdapter(
    private var dbPatientList: ArrayList<PatientListViewModel.PatientItem>,
    private val context: Context
) : RecyclerView.Adapter<PatientAdapter.Pager2ViewHolder>() {

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
            btnView.setOnClickListener(this)

        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = dbPatientList[pos].resourceId
            val dob = dbPatientList[pos].dob

            FormatterClass().saveSharedPref("patientId", id, context)
            val selectedVaccinationVenue =
                FormatterClass().getSharedPref("selectedVaccinationVenue", context)
            val isSelectedVaccinationVenue =
                FormatterClass().getSharedPref("isSelectedVaccinationVenue", context)
            val readyToUpdate =
                FormatterClass().getSharedPref("ready_to_update", context)

            val birthDateElement = FormatterClass().convertLocalDateToDate(dob)

            FormatterClass().getFormattedAge(
                birthDateElement,
                context.resources,
                context)

            if (readyToUpdate != null) {
                createDialog()
            } else {

                if (isSelectedVaccinationVenue == null) {
                    val intent = Intent(context, PatientDetailActivity::class.java)
                    intent.putExtra("patientId", id)
                    context.startActivity(intent)
                } else {
                    if (selectedVaccinationVenue != null) {
                        val intent = Intent(context, PatientDetailActivity::class.java)
                        intent.putExtra("patientId", id)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Please select a vaccination venue",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }

        }


    }

    private fun createDialog() {

        val customDialogView =
            LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null)

        val vaccineDetails: MaterialButton = customDialogView.findViewById(R.id.vaccineDetails)
        val clientDetails: MaterialButton = customDialogView.findViewById(R.id.clientDetails)
        val cancelButton: ImageButton = customDialogView.findViewById(R.id.cancel_button)

        val builder = AlertDialog.Builder(context)
        builder.setView(customDialogView)
        val customDialog = builder.create()
        customDialog.show()

        val patientId = FormatterClass().getSharedPref("patientId", context)
        vaccineDetails.setOnClickListener {
//            FormatterClass().saveSharedPref(
//                "questionnaireJson",
//                "update_history_specifics.json",
//                context
//            )
            FormatterClass().saveSharedPref(
                "vaccinationFlow",
                "updateVaccineDetails",
                context
            )
            FormatterClass().saveSharedPref(
                "title",
                "Update Vaccine Details", context
            )

            FormatterClass().deleteSharedPref("ready_to_update", context)
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.UPDATE_VACCINE_DETAILS.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)
            customDialog.dismiss() // Close the dialog
        }

        clientDetails.setOnClickListener {
            FormatterClass().saveSharedPref(
                "questionnaireJson",
                "update_history.json",
                context
            )
            FormatterClass().saveSharedPref(
                "title",
                "Update Client Details", context
            )
            FormatterClass().deleteSharedPref("ready_to_update", context)
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)
            customDialog.dismiss() // Close the dialog
        }

        // Example: Set a dismiss listener for additional actions when the dialog is dismissed
        customDialog.setOnDismissListener {
            // Additional actions when the dialog is dismissed
        }
        cancelButton.setOnClickListener {
            // Additional actions when the dialog is dismissed
            customDialog.dismiss()
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

        val name = dbPatientList[position].name
        val age = dbPatientList[position].dob
        val idNumber = dbPatientList[position].number
        var phoneNumber = dbPatientList[position].phone
        val careGiverPhone = dbPatientList[position].contact_phone
        val gender = dbPatientList[position].contact_gender
        if (phoneNumber.isEmpty()) {
            if (careGiverPhone != null) {
                phoneNumber = "$careGiverPhone - ($gender)"
            }
        }

        holder.name.text = name
        holder.idNumber.text = idNumber
        holder.tvPhoneNumber.text = phoneNumber

        holder.viewPhoneNumber.text = "Phone Number"
        holder.viewId.text = "Identification No"
        holder.viewName.text = "Name"

    }

    override fun getItemCount(): Int {
        return dbPatientList.size
    }

}