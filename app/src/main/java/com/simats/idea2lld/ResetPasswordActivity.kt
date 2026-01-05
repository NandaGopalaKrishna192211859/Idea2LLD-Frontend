package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val email = intent.getStringExtra("EMAIL") ?: ""
        val otp = intent.getStringExtra("OTP") ?: ""

        val newPasswordInput = findViewById<EditText>(R.id.newPasswordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val btnReset = findViewById<MaterialButton>(R.id.btnResetPassword)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnReset.setOnClickListener {

            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject()
            json.put("email", email)
            json.put("otp", otp)
            json.put("newPassword", newPassword)

            val body = RequestBody.create(
                "application/json".toMediaType(),
                json.toString()
            )

            val request = Request.Builder()
                .url(ApiClient.RESET_PASSWORD_URL)
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {

                    val responseText = response.body?.string() ?: ""

                    runOnUiThread {

                        if (!response.isSuccessful) {
                            val msg = try {
                                JSONObject(responseText)
                                    .optString("error", "Failed to reset password")
                            } catch (e: Exception) {
                                "Failed to reset password"
                            }

                            Toast.makeText(
                                this@ResetPasswordActivity,
                                msg,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Password reset successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirect to Login
                        val intent = Intent(
                            this@ResetPasswordActivity,
                            LoginPageSelectionsActivity::class.java
                        )
                        intent.putExtra("SCREEN_TYPE", "LOGIN")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                }
            })
        }
    }
}
