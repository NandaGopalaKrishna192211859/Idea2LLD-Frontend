package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class CreateAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val loginText = findViewById<TextView>(R.id.loginText)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnBack.setOnClickListener { finish() }

        loginText.setOnClickListener {
            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "LOGIN")
            startActivity(intent)
            finish()
        }

        btnCreateAccount.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 JSON BODY
            val json = JSONObject()
            json.put("name", name)
            json.put("email", email)
            json.put("phone", phone)
            json.put("password", password)
            json.put("is_investor", 0)

            val body = RequestBody.create(
                "application/json".toMediaType(),
                json.toString()
            )

            val request = Request.Builder()
                .url(ApiClient.REGISTER_URL)
                .post(body)
                .build()

            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CreateAccountActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@CreateAccountActivity,
                                "Registration successful. Please login.",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@CreateAccountActivity,
                                    LoginPageSelectionsActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                this@CreateAccountActivity,
                                "Registration failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }
}
