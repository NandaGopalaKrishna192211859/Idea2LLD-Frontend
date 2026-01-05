package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordEmailActivity : AppCompatActivity() {

    private lateinit var loadingOtp: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val btnSendOtp = findViewById<MaterialButton>(R.id.btnSendOtp)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        loadingOtp = findViewById(R.id.loadingOtp)

        btnBack.setOnClickListener { finish() }

        btnSendOtp.setOnClickListener {

            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject()
            json.put("email", email)

            val body = RequestBody.create(
                "application/json".toMediaType(),
                json.toString()
            )

            val request = Request.Builder()
                .url(ApiClient.FORGOT_PASSWORD_URL)
                .post(body)
                .build()

            loadingOtp.visibility = View.VISIBLE
            btnSendOtp.isEnabled = false

            OkHttpClient().newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        loadingOtp.visibility = View.GONE
                        btnSendOtp.isEnabled = true
                        Toast.makeText(
                            this@ForgotPasswordEmailActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {

                    val responseText = response.body?.string() ?: ""

                    runOnUiThread {

                        loadingOtp.visibility = View.GONE
                        btnSendOtp.isEnabled = true

                        if (!response.isSuccessful) {
                            val msg = try {
                                JSONObject(responseText)
                                    .optString("error", "Something went wrong")
                            } catch (e: Exception) {
                                "Something went wrong"
                            }

                            Toast.makeText(
                                this@ForgotPasswordEmailActivity,
                                msg,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        Toast.makeText(
                            this@ForgotPasswordEmailActivity,
                            "OTP sent to your email",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this@ForgotPasswordEmailActivity,
                                VerifyOtpActivity::class.java
                            ).putExtra("EMAIL", email)
                        )
                        finish()
                    }
                }
            })
        }
    }
}

