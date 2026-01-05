package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.utils.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        // Try to get from intent first, fallback to session
        val role = intent.getStringExtra("ROLE") ?: session.getRole()
        val fullName = intent.getStringExtra("FULL_NAME") ?: session.getFullName()

        if (role == "INVESTOR") {
            // 🔜 Later: InvestorDashboardActivity
            startActivity(
                Intent(this, InvestorDashboardActivity::class.java)
                    .putExtra("FULL_NAME", fullName)
            )
        } else {
            // ✅ Founder dashboard
            startActivity(
                Intent(this, FounderDashboardActivity::class.java)
                    .putExtra("FULL_NAME", fullName)
            )
        }

        // Important: close MainActivity
        finish()
    }
}
