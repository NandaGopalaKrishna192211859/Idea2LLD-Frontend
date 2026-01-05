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


class YourDirectRequestsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_direct_requests)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        container = findViewById(R.id.directRequestsContainer)

        // ✅ ONE method call – LeetCode style
        loadDirectRequests()
    }

    /**
     * This method:
     * 1. Takes raw (duplicate) data
     * 2. Filters is_hub = 0
     * 3. Groups by pid
     * 4. Shows ONLY ONE card per project
     * 5. Adds footer buttons
     */
    private fun loadDirectRequests() {

        val token = SessionManager(this).getToken()

        val request = okhttp3.Request.Builder()
            .url(ApiClient.BASE_URL + "/investors/projects/sent/direct")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    // optional: show error
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) return

                val jsonArray = org.json.JSONArray(response.body!!.string())

                runOnUiThread {
                    container.removeAllViews()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val cardView = layoutInflater.inflate(
                            R.layout.item_project_card,
                            container,
                            false
                        )

                        // ✅ Card click → Open project details (Founder view)
                        val requestId = obj.getInt("request_id")

                        cardView.setOnClickListener {
                            val intent = Intent(
                                this@YourDirectRequestsActivity,
                                ProjectPackageDisplayActivity::class.java
                            )
                            intent.putExtra("REQUEST_ID", requestId)
                            intent.putExtra("FROM_FOUNDER", true)
                            startActivity(intent)
                        }




                        cardView.findViewById<TextView>(R.id.tvTitle)
                            .text = obj.getString("title")

                        cardView.findViewById<TextView>(R.id.tvCategory)
                            .text = obj.getString("category")

                        cardView.findViewById<TextView>(R.id.tvDevStatus)
                            .text = "Dev Status: ${obj.getString("dev_status")}"

                        val projectId = obj.getInt("pid")
                        val projectCategory = obj.getString("category")

                        val btnPrimary = cardView.findViewById<TextView>(R.id.btnPrimary)
                        btnPrimary.text = "View Investors"

                        btnPrimary.setOnClickListener {
                            val intent = Intent(
                                this@YourDirectRequestsActivity,
                                ViewInvestorsActivity::class.java
                            )
                            intent.putExtra("PROJECT_ID", projectId)
                            intent.putExtra("PROJECT_CATEGORY", projectCategory)
                            startActivity(intent)

                        }

                        val cancelManager = CancelRequestManager(this@YourDirectRequestsActivity)

                        val btnCancel = cardView.findViewById<TextView>(R.id.btnCancel)
                        btnCancel.setOnClickListener {
                            cancelManager.cancelAllDirect(projectId) {
                                runOnUiThread {
                                    container.removeView(cardView) // remove project card
                                }
                            }
                        }


                        container.addView(cardView)
                    }
                }
            }
        })
    }


}
