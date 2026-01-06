package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.ButtonUpdationAdapter
import com.simats.idea2lld.utils.CancelRequestManager
import com.simats.idea2lld.utils.ProfileImageLoader
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import com.simats.idea2lld.utils.NotificationHelper


class ViewInvestorsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_direct_requests)
        // 👆 reuse SAME XML

        // ---------------- HEADER ----------------
        findViewById<TextView>(R.id.tvHeaderTitle).text = "View Investors"

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        container = findViewById(R.id.directRequestsContainer)

        loadInvestors()
    }

    /**
     * Loads investors to whom the project was sent directly
     */
    private fun loadInvestors() {

        val token = SessionManager(this).getToken()
        val projectId = intent.getIntExtra("PROJECT_ID", -1)
        val projectCategory = intent.getStringExtra("PROJECT_CATEGORY") ?: ""

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/projects/$projectId/direct-investors")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                if (!body.trim().startsWith("[")) return

                val arr = JSONArray(body)

                runOnUiThread {
                    container.removeAllViews()

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)

                        val card = LayoutInflater.from(this@ViewInvestorsActivity)
                            .inflate(R.layout.item_investor_card, container, false)

                        ProfileImageLoader.load(
                            card.findViewById(R.id.ivProfile),
                            o.optString("profile_image")
                        )

                        card.findViewById<TextView>(R.id.tvName)
                            .text = o.getString("name")

                        card.findViewById<TextView>(R.id.tvCompany)
                            .text = "Company: " + o.optString("company_name", "—")

                        card.findViewById<TextView>(R.id.tvBio)
                            .text = o.optString("bio", "")

                        // ✅ Project category (not investor category)
                        card.findViewById<TextView>(R.id.tvCategory)
                            .text = projectCategory

                        val min = o.optDouble("min_investment", 0.0)
                        val max = o.optDouble("max_investment", 0.0)

                        card.findViewById<TextView>(R.id.tvAmount)
                            .text = "Range: " + "₹${min.toInt()} – ₹${max.toInt()}"

                        // ✅ Unified footer control
                        card.findViewById<TextView>(R.id.btnAccept).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnReject).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnChat).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnPrimary).visibility = View.GONE


                        val btnCancel = card.findViewById<TextView>(R.id.btnCancel)
                        btnCancel.text = "Cancel Request"

                        // 🔑 Read response status from DB
                        val status = o.optString("response_status", "pending")

                        val btnAccept = card.findViewById<TextView>(R.id.btnAccept)
                        val btnReject = card.findViewById<TextView>(R.id.btnReject)
                        val btnChat   = card.findViewById<TextView>(R.id.btnChat)
                        val btnDelete = card.findViewById<TextView>(R.id.btnDelete)

// reset footer
                        btnAccept.visibility = View.GONE
                        btnReject.visibility = View.GONE
                        btnChat.visibility   = View.GONE
                        btnCancel.visibility = View.GONE
                        btnDelete.visibility = View.GONE

// ✅ THIS IS WHERE YOUR when(status) GOES
                        when (status) {

                            "pending" -> {
                                btnCancel.text = "Cancel Request"
                                btnCancel.visibility = View.VISIBLE
                            }

                            "accepted" -> {
                                btnAccept.text = "Accepted"
                                btnAccept.isEnabled = false
                                btnAccept.visibility = View.VISIBLE
                                btnChat.visibility = View.VISIBLE

                                btnChat.setOnClickListener {
                                    val intent =
                                        Intent(this@ViewInvestorsActivity, ChatActivity::class.java)
                                    intent.putExtra("REQUEST_ID", o.getInt("request_id"))
                                    intent.putExtra("CHAT_TITLE", o.getString("name")) // 👈 investor name
                                    startActivity(intent)
                                }


                            }

                            "rejected" -> {
                                btnReject.text = "Rejected"
                                btnReject.isEnabled = false
                                btnReject.visibility = View.VISIBLE
                                btnDelete.visibility = View.VISIBLE


                            }
                        }

                        val cancelManager = CancelRequestManager(this@ViewInvestorsActivity)

                        btnCancel.setOnClickListener {

                            val requestId = o.optInt("request_id", -1)
                            val investorUid = o.optInt("uid", -1)

                            // 🔒 Safety check (VERY IMPORTANT)
                            if (requestId == -1 || investorUid == -1) {
                                btnCancel.isEnabled = true
                                btnCancel.text = "Cancel Request"
                                return@setOnClickListener
                            }

                            btnCancel.isEnabled = false
                            btnCancel.text = "Cancelling..."

                            cancelManager.cancelSingleDirect(
                                requestId = requestId,
                                pid = projectId,
                                investorUid = investorUid
                            ) {
                                container.removeView(card)
                            }
                        }

                        val updater = ButtonUpdationAdapter(this@ViewInvestorsActivity)


                        btnDelete.setOnClickListener {

                            btnDelete.isEnabled = false
                            btnDelete.text = "Deleting..."

                            updater.updateRequest(
                                requestId = o.getInt("request_id"),
                                action = "delete"
                            ) {
                                container.removeView(card)
                            }
                        }




                        container.addView(card)
                    }
                }
            }
        })
    }

}
