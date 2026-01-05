package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)

        // Login → Login Form Page
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "LOGIN")
            startActivity(intent)
        }

        // Create Account → Role Selection Page
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "ROLE")
            startActivity(intent)
        }
    }
}
