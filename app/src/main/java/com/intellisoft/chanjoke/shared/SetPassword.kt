package com.intellisoft.chanjoke.shared

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbSetPasswordReq
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication

class SetPassword : AppCompatActivity() {

    private lateinit var etResetCode: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var etPassword: EditText

    private val retrofitCallsAuthentication = RetrofitCallsAuthentication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        etResetCode = findViewById(R.id.etResetCode)
        etIdNumber = findViewById(R.id.etIdNumber)
        etPassword = findViewById(R.id.etPassword)

        findViewById<Button>(R.id.btnResetPassword).setOnClickListener {
            val resetCode = etResetCode.text.toString()
            val idNumber = etIdNumber.text.toString()
            val password = etPassword.text.toString()

            if (TextUtils.isEmpty(resetCode)){
                etResetCode.setError("Reset code is required.")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(idNumber)){
                etIdNumber.setError("Id number is required.")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)){
                etPassword.setError("Id number is required.")
                return@setOnClickListener
            }

            val dbSetPasswordReq = DbSetPasswordReq(resetCode, idNumber, password)
            retrofitCallsAuthentication.setPassword(this, dbSetPasswordReq)

        }
    }
}