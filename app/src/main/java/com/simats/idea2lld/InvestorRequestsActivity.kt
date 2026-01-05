package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.ButtonUpdationAdapter
import com.simats.idea2lld.utils.ProfileImageLoader

class InvestorRequestsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_direct_requests)

        // Header
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Investor Requests"
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        container = findViewById(R.id.directRequestsContainer)

        val pid = intent.getIntExtra("PID", -1)
        if (pid == -1) {
            finish()
            return
        }
        loadInvestorRequests(pid)

    }

    /**
     * ✅ SINGLE METHOD
     * Loads all investor requests for a project (pid)
     */
    private fun loadInvestorRequests(pid: Int) {

        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/founder/investor-requests/$pid")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                android.util.Log.d("INVESTOR_REQ_API", body)

                val arr = try {
                    JSONObject(body).getJSONArray("investors")
                } catch (e: Exception) {
                    return
                }


                runOnUiThread {
                    container.removeAllViews()

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)

                        val status = o.optString("response_status", "pending")


                        val card = layoutInflater.inflate(
                            R.layout.item_investor_card,
                            null
                        )
                        ProfileImageLoader.load(
                            card.findViewById(R.id.ivProfile),
                            o.optString("profile_image")
                        )


                        card.findViewById<TextView>(R.id.tvName).text = o.getString("name")
                        card.findViewById<TextView>(R.id.tvCompany).text = o.optString("company_name")
                        card.findViewById<TextView>(R.id.tvBio).text = o.optString("bio")
                        card.findViewById<TextView>(R.id.tvCategory).text = o.optString("category")
                        card.findViewById<TextView>(R.id.tvAmount).text =
                            o.optString("investment_range")

                        val btnAccept = card.findViewById<TextView>(R.id.btnAccept)
                        val btnReject = card.findViewById<TextView>(R.id.btnReject)
                        val btnChat   = card.findViewById<TextView>(R.id.btnChat)
                        val btnDelete = card.findViewById<TextView>(R.id.btnDelete)
                        val btnPrimary = card.findViewById<TextView>(R.id.btnPrimary)
                        val btnCancel = card.findViewById<TextView>(R.id.btnCancel)

// reset all
                        btnAccept.visibility = View.GONE
                        btnReject.visibility = View.GONE
                        btnChat.visibility = View.GONE
                        btnDelete.visibility = View.GONE
                        btnCancel.visibility = View.GONE
                        btnPrimary.visibility = View.GONE

                        val updater = ButtonUpdationAdapter(this@InvestorRequestsActivity)

                        when (status) {

                            "pending" -> {
                                btnAccept.visibility = View.VISIBLE
                                btnReject.visibility = View.VISIBLE

                                btnAccept.setOnClickListener {
                                    updater.updateRequest(
                                        requestId = o.getInt("id"),
                                        action = "accepted"
                                    ) {
                                        btnAccept.text = "Accepted"
                                        btnAccept.isEnabled = false
                                        btnReject.visibility = View.GONE
                                        btnChat.visibility = View.VISIBLE
                                    }
                                }

                                btnReject.setOnClickListener {
                                    updater.updateRequest(
                                        requestId = o.getInt("id"),
                                        action = "rejected"
                                    ) {
                                        container.removeView(card)
                                    }
                                }
                            }

                            "accepted" -> {
                                btnAccept.text = "Accepted"
                                btnAccept.isEnabled = false
                                btnAccept.visibility = View.VISIBLE
                                btnChat.visibility = View.VISIBLE

                                btnChat.setOnClickListener {
                                    val intent = Intent(
                                        this@InvestorRequestsActivity,
                                        ChatActivity::class.java
                                    )
                                    intent.putExtra("REQUEST_ID", o.getInt("id"))
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


                        container.addView(card)
                    }
                }
            }
        })
    }
}
