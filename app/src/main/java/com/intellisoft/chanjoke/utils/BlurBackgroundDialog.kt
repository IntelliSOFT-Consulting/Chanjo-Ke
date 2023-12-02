package com.intellisoft.chanjoke.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.data.FormatterClass

class BlurBackgroundDialog(
    private val fragment: Fragment, context: Context
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_blur_background)
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams

        // Make the dialog cover the status bar and navigation bar
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawableResource(R.color.colorPrimary)
        val vaccinationFlow = FormatterClass().getSharedPref("vaccinationFlow", context)
        val valueText = if (vaccinationFlow == "addAefi") {
            "AEFI Saved successfully!"
        } else if (vaccinationFlow == "createVaccineDetails") {
            "Vaccine details captured successfully!"
        } else if (vaccinationFlow == "updateVaccineDetails") {
            "Record has been updated successfully!"
        } else {
            "Record has been captured successfully!"
        }

        findViewById<TextView>(R.id.info_textview).apply {
            text = valueText
        }
        val closeMaterialButton = findViewById<MaterialButton>(R.id.closeMaterialButton)
        closeMaterialButton.setOnClickListener {
            dismiss()
            val patientId = FormatterClass().getSharedPref("patientId", context)

            val isRegistration = FormatterClass().getSharedPref("isRegistration", context)
            if (isRegistration != null) {
                if (isRegistration == "true") {
                    val intent = Intent(context, PatientDetailActivity::class.java)
                    intent.putExtra("patientId", patientId)
                    context.startActivity(intent)
                    FormatterClass().deleteSharedPref("isRegistration", context)
                }
            }else{
                val intent = Intent(context, PatientDetailActivity::class.java)
                intent.putExtra("patientId", patientId)
                context.startActivity(intent)
            }
        }
    }
}