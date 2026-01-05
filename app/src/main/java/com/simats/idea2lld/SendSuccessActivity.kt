package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SendSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_success)

        val rocket = findViewById<ImageView>(R.id.ivRocket)
        val btnDone = findViewById<Button>(R.id.btnDone)

        // INITIAL STATE (CENTER)
        rocket.scaleX = 0.8f
        rocket.scaleY = 0.8f
        rocket.alpha = 1f

        // 🚀 BURN EFFECT (2 seconds)
        rocket.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {

                // 🔥 Flame flicker simulation
                rocket.animate()
                    .scaleY(1.05f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator())
                    .withEndAction {

                        // 🚀 MOVE TO TOP
                        rocket.animate()
                            .translationY(-resources.displayMetrics.heightPixels.toFloat())
                            .alpha(0f)
                            .setDuration(900)
                            .setInterpolator(AccelerateInterpolator())
                            .start()
                    }
                    .start()
            }
            .start()

        btnDone.setOnClickListener {
            val intent = Intent(this, SavedProjectsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
