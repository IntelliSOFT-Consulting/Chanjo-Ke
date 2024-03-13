package com.intellisoft.chanjoke.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbSignIn
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication

class Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()
    private var formatterClass = FormatterClass()
    private lateinit var spinnerLocation:Spinner
    val resultList = listOf("","Facility", "Outreach")
    private var selectedItem = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        spinnerLocation = findViewById(R.id.spinnerLocation)

        formatterClass.practionerInfoShared(this)

        /**
         * TODO: This is dummy login workflow
         */

        createSpinner()
        
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && selectedItem != "") {

                formatterClass.saveSharedPref(
                    "selectedFacility",
                    selectedItem,
                    this@Login
                )

                val dbSignIn = DbSignIn(username, password)
                retrofitCallsAuthentication.loginUser(this, dbSignIn)

            } else {
                if (TextUtils.isEmpty(username)) etUsername.error = "Please Enter Username"
                if (TextUtils.isEmpty(password)) etPassword.error = "Please Enter Password"
                if (selectedItem == "") Toast.makeText(this, "Kindly select a Location", Toast.LENGTH_SHORT).show()
            }


        }
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }

    private fun createSpinner() {

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, resultList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinnerLocation.adapter = adapter

        // Set a listener to handle the item selection
        spinnerLocation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    // Get the selected item
                    selectedItem = parentView.getItemAtPosition(position).toString()


                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // Do nothing here
                }
            }

    }

}