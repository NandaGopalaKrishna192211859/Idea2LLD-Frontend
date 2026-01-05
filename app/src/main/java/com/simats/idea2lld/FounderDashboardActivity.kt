package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.simats.idea2lld.utils.SessionManager

class FounderDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_founder_dashboard)

        // -------------------------------
        // Drawer
        // -------------------------------
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val icMenu = findViewById<ImageView>(R.id.icMenu)

        icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        android.util.Log.d(
            "FOUNDER_SESSION",
            "uid=${SessionManager(this).getUid()}"
        )

        // -------------------------------
        // Session
        // -------------------------------
        val session = SessionManager(this)

        // -------------------------------
        // Hello Name Animation
        // -------------------------------
        val tvHelloName = findViewById<TextView>(R.id.tvHelloName)
        val fullName = intent.getStringExtra("FULL_NAME") ?: session.getFullName()
        tvHelloName.text = "Hello $fullName 👋"

        tvHelloName.translationX = -300f
        tvHelloName.alpha = 0f

        tvHelloName.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(600)
            .start()

        // -------------------------------
        // Cards
        // -------------------------------
        val cardCreate = findViewById<MaterialCardView>(R.id.cardCreate)
        val cardSaved = findViewById<MaterialCardView>(R.id.cardSaved)
        val cardPublished = findViewById<MaterialCardView>(R.id.cardPublished)
        val cardDraft = findViewById<MaterialCardView>(R.id.cardDraft)

        cardCreate.setOnClickListener {
            startActivity(Intent(this, CreateLLDActivity::class.java))
        }
        cardPublished.setOnClickListener {
            startActivity(Intent(this, PublishedIdeasHomeActivity::class.java))
        }



        cardSaved.setOnClickListener {
            startActivity(Intent(this, SavedProjectsActivity::class.java))
        }

        cardDraft.setOnClickListener {
            startActivity(Intent(this, DraftProjectsActivity::class.java))
        }


        // -------------------------------
        // Drawer Logout
        // -------------------------------
        val logoutBtn = findViewById<TextView>(R.id.btnLogout)

        logoutBtn.setOnClickListener {
            session.clearSession()

            val intent = Intent(this, LoginPageSelectionsActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "LOGIN")
            startActivity(intent)
            finish()
        }
    }
}
