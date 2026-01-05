package com.simats.idea2lld

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.widget.Toast
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.SessionManager


class LoginPageSelectionsActivity : AppCompatActivity() {

    private lateinit var founderCard: View
    private lateinit var investorCard: View

    private lateinit var founderTitle: TextView
    private lateinit var founderDesc: TextView
    private lateinit var investorTitle: TextView
    private lateinit var investorDesc: TextView

    private var selectedRole: String = "FOUNDER" // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page_selections)
        window.setBackgroundDrawableResource(android.R.color.white)

        // -------- ROLE SELECTION VIEWS --------
        founderCard = findViewById(R.id.founderCard)
        investorCard = findViewById(R.id.investorCard)

        founderTitle = findViewById(R.id.founderTitle)
        founderDesc = findViewById(R.id.founderDesc)
        investorTitle = findViewById(R.id.investorTitle)
        investorDesc = findViewById(R.id.investorDesc)

        val btnNext = findViewById<MaterialButton>(R.id.btnNext)
        val roleBack = findViewById<TextView>(R.id.roleBack)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        selectFounder()

        founderCard.setOnClickListener { selectFounder() }
        investorCard.setOnClickListener { selectInvestor() }

        // -------- SCREEN TYPE --------
        val screenType = intent.getStringExtra("SCREEN_TYPE")
        val loginLayout = findViewById<View>(R.id.loginLayout)
        val roleLayout = findViewById<View>(R.id.roleLayout)

        if (screenType == "LOGIN") {
            loginLayout.visibility = View.VISIBLE
            roleLayout.visibility = View.GONE
        } else {
            roleLayout.visibility = View.VISIBLE
            loginLayout.visibility = View.GONE
        }

        // -------- LOGIN VIEWS --------
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        forgotPassword.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ForgotPasswordEmailActivity::class.java
                )
            )
        }


        val signupgo = findViewById<TextView>(R.id.Signupgo)

        signupgo.setOnClickListener {
            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "ROLE")
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject()
            json.put("email", email)
            json.put("password", password)

            val body = RequestBody.create(
                "application/json".toMediaType(),
                json.toString()
            )

            val request = Request.Builder()
                .url(ApiClient.LOGIN_URL)
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginPageSelectionsActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""

                    runOnUiThread {
                        if (!response.isSuccessful) {
                            Toast.makeText(
                                this@LoginPageSelectionsActivity,
                                "Invalid email or password",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        val json = JSONObject(responseBody)
                        val user = json.getJSONObject("user")

                        val isInvestor = user.optInt("is_investor", 0)
                        val profileImage = user.optString("profile_image", "")
                        val token = json.getString("token")

                        val hasProfileImage =
                            profileImage.isNotEmpty() && profileImage != "null"

                        // -------- FINAL NAVIGATION LOGIC --------
                        if (isInvestor == 0) {

                            val session = SessionManager(this@LoginPageSelectionsActivity)

                            session.saveLogin(
                                uid = user.getInt("uid"),
                                token = token,
                                fullName = user.getString("name"),
                                role = "FOUNDER",
                                preferredCategories = ""
                            )


                            // ✅ FOUNDER
                            startActivity(
                                Intent(
                                    this@LoginPageSelectionsActivity,
                                    MainActivity::class.java
                                ).putExtra("ROLE", "FOUNDER")
                                    .putExtra("FULL_NAME", user.getString("name"))

                            )
                            finish()

                        } else {
                            // ✅ INVESTOR
                            if (!hasProfileImage) {
                                val session = SessionManager(this@LoginPageSelectionsActivity)

                                session.saveLogin(
                                    uid = user.getInt("uid"),
                                    token = token,
                                    fullName = user.getString("name"),
                                    role = "INVESTOR",
                                    preferredCategories = user.optString("preferred_categories", "")
                                )

                                startActivity(
                                    Intent(
                                        this@LoginPageSelectionsActivity,
                                        AddProfileImageActivity::class.java
                                    ).putExtra("TOKEN", token)

                                )
                                finish()
                            } else {
                                val session = SessionManager(this@LoginPageSelectionsActivity)

                                session.saveLogin(
                                    uid = user.getInt("uid"),
                                    token = token,
                                    fullName = user.getString("name"),
                                    role = "INVESTOR",
                                    preferredCategories = user.optString("preferred_categories", "")
                                )
                                startActivity(
                                    Intent(
                                        this@LoginPageSelectionsActivity,
                                        MainActivity::class.java
                                    ).putExtra("ROLE", "INVESTOR")
                                )
                                finish()
                            }
                        }
                    }
                }
            })
        }

        // -------- BACK BUTTONS --------
        btnBack.setOnClickListener { finish() }
        roleBack.setOnClickListener { finish() }

        // -------- NEXT (ROLE) --------
        btnNext.setOnClickListener {
            when (selectedRole) {
                "FOUNDER" -> {
                    startActivity(Intent(this, CreateAccountActivity::class.java))
                }
                "INVESTOR" -> {
                    startActivity(Intent(this, InvestorRegistrationActivity::class.java))
                }
            }
        }
    }

    private fun selectFounder() {
        selectedRole = "FOUNDER"
        founderCard.isSelected = true
        investorCard.isSelected = false
        founderTitle.setTextColor(Color.WHITE)
        founderDesc.setTextColor(Color.WHITE)
        investorTitle.setTextColor(Color.BLACK)
        investorDesc.setTextColor(Color.DKGRAY)
    }

    private fun selectInvestor() {
        selectedRole = "INVESTOR"
        investorCard.isSelected = true
        founderCard.isSelected = false
        investorTitle.setTextColor(Color.WHITE)
        investorDesc.setTextColor(Color.WHITE)
        founderTitle.setTextColor(Color.BLACK)
        founderDesc.setTextColor(Color.DKGRAY)
    }
}
