package com.simats.idea2lld

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.net.Uri
import android.content.Intent
import android.widget.TextView
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException



class InvestorRegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investor_registration)

        val tvLogin = findViewById<TextView>(R.id.loginText)

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "LOGIN")
            startActivity(intent)
            finish()
        }


        // ---- BACK BUTTON ----
        val backBtn = findViewById<ImageView>(R.id.btnBack)
        backBtn.setOnClickListener {
            finish()
        }




        // ---- INPUT FIELDS ----
        val etName = findViewById<EditText>(R.id.etName)
        val etLinkdin = findViewById<EditText>(R.id.etLinkdin)
        val etWebsite = findViewById<EditText>(R.id.etWebsite)
        val etCompany = findViewById<EditText>(R.id.etCompany)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etMinInvestment = findViewById<EditText>(R.id.etMinInvestment)
        val etMaxInvestment = findViewById<EditText>(R.id.etMaxInvestment)
        val etPastInvestments = findViewById<EditText>(R.id.etPastInvestments)
        val etBio = findViewById<EditText>(R.id.etBio)


        // ---- CHIP GROUPS ----
        val industryChipGroup = findViewById<ChipGroup>(R.id.chipGroupIndustries)
        val investmentTypeChipGroup =
            findViewById<ChipGroup>(R.id.chipGroupInvestmentType)

        // ---- SUBMIT BUTTON ----
        val submitBtn = findViewById<Button>(R.id.btnSubmit)

        submitBtn.setOnClickListener {

            // Collect text inputs
            val name = etName.text.toString().trim()
            val company = etCompany.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val minInvestment = etMinInvestment.text.toString().trim()
            val maxInvestment = etMaxInvestment.text.toString().trim()
            val bio = etBio.text.toString().trim()

            // Basic validation (safe & minimal)
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill name, email, and phone",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Get selected investment type (single)
            var investmentType = ""
            if (investmentTypeChipGroup.checkedChipId != -1) {
                val chip =
                    findViewById<Chip>(investmentTypeChipGroup.checkedChipId)
                investmentType = chip.text.toString()
            }

            // Get selected industries (multiple)
            val selectedIndustries = mutableListOf<String>()
            for (id in industryChipGroup.checkedChipIds) {
                val chip = findViewById<Chip>(id)
                selectedIndustries.add(chip.text.toString())
            }

            // ---- LOG DATA (FINAL CHECK) ----
            Log.d(
                "InvestorRegistration",
                """
                Name: $name
                Company: $company
                Email: $email
                Phone: $phone
                Investment Type: $investmentType
                Min Investment: $minInvestment
                Max Investment: $maxInvestment
                Bio: $bio
                json.put("is_investor", 1)
                Industries: $selectedIndustries
                """.trimIndent()
            )

            Toast.makeText(
                this,
                "Registration data captured successfully",
                Toast.LENGTH_SHORT
            ).show()


            // 👉 Next step: send to backend API
            // -------- API CALL (Investor Registration) --------

            val client = OkHttpClient()

            val formBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

// REQUIRED FIELDS
            formBuilder.addFormDataPart("name", name)
            formBuilder.addFormDataPart("email", email)
            formBuilder.addFormDataPart("phone", phone)
            formBuilder.addFormDataPart("password", etPassword.text.toString())
            formBuilder.addFormDataPart("is_investor", "1")

// OPTIONAL INVESTOR FIELDS
            formBuilder.addFormDataPart("company_name", company)
            formBuilder.addFormDataPart("bio", bio)
            formBuilder.addFormDataPart("investment_type", investmentType)
            formBuilder.addFormDataPart("min_investment", minInvestment)
            formBuilder.addFormDataPart("max_investment", maxInvestment)
            formBuilder.addFormDataPart(
                "preferred_categories",
                selectedIndustries.toString()
            )



            val request = Request.Builder()
                .url(ApiClient.REGISTER_URL)
                .post(formBuilder.build())
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@InvestorRegistrationActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@InvestorRegistrationActivity,
                                "Registration successful. Please login.",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@InvestorRegistrationActivity,
                                    LoginPageSelectionsActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                this@InvestorRegistrationActivity,
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
