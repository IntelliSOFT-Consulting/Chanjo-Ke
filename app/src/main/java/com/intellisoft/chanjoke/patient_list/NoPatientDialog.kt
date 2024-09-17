package com.intellisoft.chanjoke.patient_list

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class NoPatientDialog(context: Context) : Dialog(context), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This line is optional but can be used to remove the default dialog title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.no_patient_dialog_layout)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Initialize and set up any buttons or other views in your custom layout
        val btnRegisterClient: Button = findViewById(R.id.btnRegisterClient)
        val btnCancel: ImageButton = findViewById(R.id.btnCancel)
        btnRegisterClient.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnRegisterClient -> {
                // Handle the button click event
                dismiss()
                /**
                 * Clear all shared preference data
                 *
                 */


                FormatterClass().deleteSharedPref("personal", context)
                FormatterClass().deleteSharedPref("caregiver", context)
                FormatterClass().deleteSharedPref("administrative", context)

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("functionToCall", "registerFunction")

                context.startActivity(intent)

            }

            R.id.btnCancel -> {
                // Handle the button click event
                dismiss()

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("functionToCall", "listClients")
                context.startActivity(intent)

            }
            // Add more cases if you have additional buttons in your layout
        }
    }
}
