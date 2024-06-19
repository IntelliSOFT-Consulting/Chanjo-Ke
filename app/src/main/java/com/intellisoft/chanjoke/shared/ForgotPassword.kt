package com.intellisoft.chanjoke.shared

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbResetPasswordData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ForgotPassword : AppCompatActivity() {

    private lateinit var etEmailAddress:EditText
    private lateinit var etIdNumber:EditText

    private val formatterClass = FormatterClass()
    private val retrofitCallsAuthentication = RetrofitCallsAuthentication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etEmailAddress = findViewById(R.id.etEmailAddress)
        etIdNumber = findViewById(R.id.etIdNumber)

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

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@ForgotPassword)
                progressDialog.setTitle("Please wait..")
                progressDialog.setMessage("Authentication in progress..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    val dbResetPasswordData = DbResetPasswordData(idNumber, emailAddress)
                    val pairReturn = retrofitCallsAuthentication
                        .getResetPassword(this@ForgotPassword, dbResetPasswordData)

                    val messageCode = pairReturn.first
                    val messageToast = pairReturn.second

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@ForgotPassword, messageToast,
                            Toast.LENGTH_SHORT).show()
                        if (messageCode == 200 || messageCode == 201){
                            val intent = Intent(this@ForgotPassword,
                                SetPassword::class.java)
                            startActivity(intent)
                        }
                    }

                }.join()
                progressDialog.dismiss()

            }

        }
    }
}