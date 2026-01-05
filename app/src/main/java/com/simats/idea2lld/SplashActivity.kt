package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.simats.idea2lld.utils.SessionManager


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.appIcon)
        val title = findViewById<TextView>(R.id.appTitle)
        val subtitle = findViewById<TextView>(R.id.appSubTitle)

        val animLogo = AnimationUtils.loadAnimation(this, R.anim.zoom_fade_in)
        val animText = AnimationUtils.loadAnimation(this, R.anim.zoom_fade_in2)

        logo.startAnimation(animLogo)
        title.startAnimation(animText)
        subtitle.startAnimation(animText)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({

            if (session.isLoggedIn()) {
                // ✅ User already logged in → go to dashboard
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .putExtra("ROLE", session.getRole())
                        .putExtra("FULL_NAME", session.getFullName())
                )
            } else {
                // ❌ First time / logged out → onboarding
                startActivity(
                    Intent(this, OnboardingActivity::class.java)
                )
            }

            finish()

        }, 2000)
    }
}
