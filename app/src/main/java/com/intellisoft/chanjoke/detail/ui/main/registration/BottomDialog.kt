package com.intellisoft.chanjoke.detail.ui.main.registration

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.R

class BottomDialog(
    context: Context,
    private val valueText: String,
    private val onCancel: () -> Unit,
    private val onAdd: () -> Unit,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_general_background)
        // Set window attributes to cover the entire screen
        window?.apply {
            attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes?.height = WindowManager.LayoutParams.MATCH_PARENT

            // Make the dialog cover the status bar and navigation bar
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            setBackgroundDrawableResource(android.R.color.transparent) // Set a transparent background
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawableResource(R.color.colorPrimary)

        findViewById<TextView>(R.id.info_textview).apply {
            text = valueText
        }
        val closeMaterialButton = findViewById<MaterialButton>(R.id.btn_back)
        closeMaterialButton.setOnClickListener {
            dismiss()
            onCancel()
        }
        val addMaterialButton = findViewById<MaterialButton>(R.id.btn_add)
        addMaterialButton.setOnClickListener {
            dismiss()
            onAdd()

        }
    }
}