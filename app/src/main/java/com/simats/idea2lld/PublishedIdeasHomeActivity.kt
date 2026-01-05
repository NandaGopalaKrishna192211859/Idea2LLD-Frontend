package com.simats.idea2lld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView

class PublishedIdeasHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_published_ideas_home)

        // Back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Cards (MaterialCardView extends CardView, so this is safe)
        val directCard = findViewById<CardView>(R.id.directCard)
        val hubCard = findViewById<CardView>(R.id.hubCard)

        // Soft background colors
        directCard.setCardBackgroundColor(Color.parseColor("#EEF2FF")) // Light Blue
        hubCard.setCardBackgroundColor(Color.parseColor("#ECFDF5"))    // Light Green

        // Buttons
        val btnViewDirect = findViewById<TextView>(R.id.btnViewDirect)
        val btnViewHub = findViewById<TextView>(R.id.btnViewHub)
        val btnViewChat = findViewById<TextView>(R.id.btnViewChat)



        btnViewDirect.setOnClickListener {
            startActivity(
                Intent(this, YourDirectRequestsActivity::class.java)
            )
        }

        btnViewHub.setOnClickListener {
            startActivity(
                Intent(this, YourHubRequestsActivity::class.java)
            )
        }

        btnViewChat.setOnClickListener {
            startActivity(
                Intent(this, FSConnectorsActivity::class.java)
            )
        }
    }
}
