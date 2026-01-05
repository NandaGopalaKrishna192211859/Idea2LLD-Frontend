package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class PlaneSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plane_splash)

        val banner = findViewById<View>(R.id.bannerView)
        val rope = findViewById<View>(R.id.rope)
        val plane = findViewById<View>(R.id.ivPlane)

        // Start banner waving AFTER layout
        banner.post {
            startBannerWave(banner)
        }

        plane.post {

            val screenW = resources.displayMetrics.widthPixels.toFloat()

            // Start everything fully off-screen LEFT
            val startX = -plane.right.toFloat()

            banner.translationX = startX
            rope.translationX = startX
            plane.translationX = startX

            // Animate all three together
            banner.animate()
                .translationX(screenW)
                .setDuration(2400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            rope.animate()
                .translationX(screenW)
                .setDuration(2400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            plane.animate()
                .translationX(screenW)
                .setDuration(2400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    startActivity(
                        Intent(this, CreateLLDActivity::class.java)
                            .putExtra("START_FLOW", true)
                    )
                    finish()
                }
                .start()
        }
    }

    // 🎏 Cloth-like banner wave
    private fun startBannerWave(banner: View) {
        banner.pivotX = 0f
        banner.pivotY = banner.height / 2f

        ObjectAnimator.ofFloat(
            banner,
            View.ROTATION,
            -4f, 4f
        ).apply {
            duration = 700
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
