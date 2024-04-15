package com.intellisoft.chanjoke.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbResetPasswordData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication

class ForgotPassword : AppCompatActivity() {

    private lateinit var etEmailAddress:EditText
    private lateinit var etIdNumber:EditText

    private val formatterClass = FormatterClass()
    private val retrofitCallsAuthentication = RetrofitCallsAuthentication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<Button>(R.id.btnResetPassword).setOnClickListener {
            val emailAddress = etEmailAddress.text.toString()
            val idNumber = etIdNumber.text.toString()

            if (TextUtils.isEmpty(emailAddress)){
                etEmailAddress.setError("Email address is required")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(idNumber)){
                etIdNumber.setError("Id number is required")
                return@setOnClickListener
            }

            val dbResetPasswordData = DbResetPasswordData(idNumber, emailAddress)
            retrofitCallsAuthentication.getResetPassword(this, dbResetPasswordData)


        }
    }
}