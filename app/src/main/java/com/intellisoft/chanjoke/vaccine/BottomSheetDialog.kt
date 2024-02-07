package com.intellisoft.chanjoke.vaccine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.administration.VaccineAdministration
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class BottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

        val btnAdministerVaccine = view.findViewById<Button>(R.id.btnAdministerVaccine)
        val btnContraindications = view.findViewById<Button>(R.id.btnContraindications)

        btnContraindications.setOnClickListener {
            val intent = Intent(context, VaccineAdministration::class.java)
            intent.putExtra("functionToCall", NavigationDetails.CONTRAINDICATIONS.name)
            context?.startActivity(intent)
            dismiss()
        }

        btnAdministerVaccine.setOnClickListener {
            val intent = Intent(context, VaccineAdministration::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            context?.startActivity(intent)
            dismiss()
        }

        return view
    }
}