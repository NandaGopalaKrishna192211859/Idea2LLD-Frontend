package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnSkipForNow: MaterialButton
    private lateinit var billingClient: BillingClient

    private var productDetails: ProductDetails? = null

    companion object {
        private const val TAG = "SubscriptionActivity"

        // 🔑 DO NOT CHANGE ONCE LIVE
        const val SKU_PRO_MONTHLY = "idea2lld_pro_monthly"
        const val SKU_PRO_YEARLY = "idea2lld_pro_yearly"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnSkipForNow = findViewById(R.id.btnSkipForNow)

        setupBillingClient()
        setupClickListeners()
    }

    // ---------------- BILLING SETUP ----------------

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected")
                    querySubscription()
                } else {
                    Log.e(TAG, "Billing failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected")
            }
        })
    }

    private fun querySubscription() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PRO_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsList ->

            if (result.responseCode == BillingClient.BillingResponseCode.OK &&
                detailsList != null &&
                detailsList.size > 0
            ) {
                productDetails = detailsList.first()
                Log.d(TAG, "Subscription loaded")
            } else {
                Log.e(TAG, "Subscription not found")
                Toast.makeText(this, "Subscription not available", Toast.LENGTH_LONG).show()
            }
        }

    }

    // ---------------- BUTTON ACTIONS ----------------

    private fun setupClickListeners() {

        // SKIP → Login
        btnSkipForNow.setOnClickListener {
            openLogin()
        }

        // SUBSCRIBE
        btnSubscribe.setOnClickListener {
            startSubscription()
        }
    }

    private fun startSubscription() {
        if (!billingClient.isReady) {
            Toast.makeText(this, "Billing not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val details = productDetails ?: run {
            Toast.makeText(this, "Subscription unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        val offer = details.subscriptionOfferDetails?.firstOrNull() ?: run {
            Toast.makeText(this, "No offers available", Toast.LENGTH_SHORT).show()
            return
        }

        val productParams = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offer.offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productParams)
            .build()

        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    // ---------------- PURCHASE RESULT ----------------

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(this, "Purchase cancelled", Toast.LENGTH_SHORT).show()
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                savePremium()
                openLogin()
            }

            else -> {
                Toast.makeText(this, "Purchase failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        savePremium()
                        openLogin()
                    }
                }
            } else {
                savePremium()
                openLogin()
            }
        }
    }

    // ---------------- HELPERS ----------------

    private fun savePremium() {
        val prefs = getSharedPreferences("subscription", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isPremium", true)
            .apply()

        Toast.makeText(this, "Premium Activated 🎉", Toast.LENGTH_LONG).show()
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}
