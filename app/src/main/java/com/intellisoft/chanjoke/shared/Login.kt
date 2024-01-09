package com.intellisoft.chanjoke.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.FormatterClass

class Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        /**
         * TODO: This is dummy login workflow
         */

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                if (username == "admin" && password == "admin") {

                    FormatterClass().saveSharedPref("isLoggedIn", "true", this@Login)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this@Login.finish()
                } else {
                    Toast.makeText(this, "Incorrect Username or Password. Try again", Toast.LENGTH_SHORT).show()
                }

            } else {
                etUsername.error = "Please Enter Username"
                etPassword.error = "Please Enter Password"
            }


        }
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }
}