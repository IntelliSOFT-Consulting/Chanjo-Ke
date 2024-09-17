package com.intellisoft.chanjoke.shared

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbResetPasswordData
import com.intellisoft.chanjoke.fhir.data.DbSetPasswordReq
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
                etPassword.setError("Password is required.")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {

                val progressDialog = ProgressDialog(this@SetPassword)
                progressDialog.setTitle("Please wait..")
                progressDialog.setMessage("Authentication in progress..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {

                    val dbSetPasswordReq = DbSetPasswordReq(resetCode, idNumber, password)
                    val pairReturn = retrofitCallsAuthentication
                        .setPassword(this@SetPassword, dbSetPasswordReq)

                    val messageCode = pairReturn.first
                    val messageToast = pairReturn.second

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@SetPassword, messageToast,
                            Toast.LENGTH_SHORT).show()
                        if (messageCode == 200 || messageCode == 201){
                            val intent = Intent(this@SetPassword,
                                Login::class.java)
                            startActivity(intent)
                        }
                    }

                }.join()
                progressDialog.dismiss()

            }

        }
    }
}