package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var btnSkipForNow: MaterialButton
    private lateinit var btnSubscribe: MaterialButton

    // 🔑 Subscription SKUs (future use – DO NOT CHANGE ONCE LIVE)
    companion object {
        const val SKU_PRO_MONTHLY = "idea2lld_pro_monthly"
        const val SKU_PRO_YEARLY = "idea2lld_pro_yearly"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        btnSkipForNow = findViewById(R.id.btnSkipForNow)
        btnSubscribe = findViewById(R.id.btnSubscribe)

        // Skip → Same flow as onboarding end
        btnSkipForNow.setOnClickListener {
            openMain()
        }

        // Subscribe → Billing will be added later
        btnSubscribe.setOnClickListener {
            // TODO: Start Google Play Billing flow here
            // Example (future):
            // startSubscription(SKU_PRO_MONTHLY)

            openMain()
        }
    }

    /**
     * Common navigation after subscription or skip
     */
    private fun openMain() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
