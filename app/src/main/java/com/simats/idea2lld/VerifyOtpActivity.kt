package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VerifyOtpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        val email = intent.getStringExtra("EMAIL") ?: ""

        val otpInput = findViewById<EditText>(R.id.otpInput)
        val btnVerify = findViewById<MaterialButton>(R.id.btnVerifyOtp)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnVerify.setOnClickListener {

            val otp = otpInput.text.toString().trim()

            if (otp.length != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Move to new password screen
            startActivity(
                Intent(this, ResetPasswordActivity::class.java)
                    .putExtra("EMAIL", email)
                    .putExtra("OTP", otp)
            )
        }
    }
}
