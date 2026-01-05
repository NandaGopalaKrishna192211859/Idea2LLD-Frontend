package com.simats.idea2lld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.adapters.ShowProjToFounderAdapter
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.CancelRequestManager
import com.simats.idea2lld.utils.SessionManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException


class YourHubRequestsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_hub_requests)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        container = findViewById(R.id.hubRequestsContainer)

        // ✅ ONE clean method call
        loadHubRequests()
    }

    /**
     * This method:
     * 1. Takes raw DB-like rows (duplicates exist)
     * 2. Filters is_hub = 1
     * 3. Groups by pid
     * 4. Shows ONLY ONE card per project
     * 5. Adds Hub-specific footer buttons
     */
    private fun loadHubRequests() {

        val token = SessionManager(this).getToken()

        val request = okhttp3.Request.Builder()
            .url(ApiClient.BASE_URL + "/investors/projects/sent/hub")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    // optional: show error / empty state
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                val jsonArray = org.json.JSONArray(body)

                runOnUiThread {
                    container.removeAllViews()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        // Inflate card
                        val cardView = layoutInflater.inflate(
                            R.layout.item_project_card,
                            container,
                            false
                        )

                        // ✅ Card click → Open project details (Founder view)
                        val requestId = obj.getInt("request_id")

                        cardView.setOnClickListener {
                            val intent = Intent(
                                this@YourHubRequestsActivity,
                                ProjectPackageDisplayActivity::class.java
                            )
                            intent.putExtra("REQUEST_ID", requestId)
                            intent.putExtra("FROM_FOUNDER", true)
                            startActivity(intent)
                        }






                        // Bind data
                        cardView.findViewById<TextView>(R.id.tvTitle)
                            .text = obj.getString("title")

                        cardView.findViewById<TextView>(R.id.tvCategory)
                            .text = obj.getString("category")

                        cardView.findViewById<TextView>(R.id.tvDevStatus)
                            .text = "Dev Status: ${obj.getString("dev_status")}"

                        // Footer buttons
                        val btnPrimary = cardView.findViewById<TextView>(R.id.btnPrimary)
                        btnPrimary.text = "Investor Requests"

                        btnPrimary.setOnClickListener {
                            val intent = Intent(
                                this@YourHubRequestsActivity,
                                InvestorRequestsActivity::class.java
                            )
                            intent.putExtra("PID", obj.getInt("pid"))
                            startActivity(intent)
                        }



                        val btnCancel = cardView.findViewById<TextView>(R.id.btnCancel)

                        btnCancel.setOnClickListener {

                            btnCancel.isEnabled = false
                            btnCancel.text = "Cancelling..."

                            val cancelManager = CancelRequestManager(this@YourHubRequestsActivity)

                            cancelManager.cancelHubProject(
                                pid = obj.getInt("pid")
                            ) {
                                container.removeView(cardView)
                            }
                        }


                        // Add card to screen
                        container.addView(cardView)
                    }
                }
            }
        })
    }

}
