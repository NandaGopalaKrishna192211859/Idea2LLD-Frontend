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
import com.simats.idea2lld.utils.ProfileImageLoader
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class FSConnectorsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fsconnectors)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        container = findViewById(R.id.ConnectorContainer)

        loadConnectors() // ✅ single method
    }

    /**
     * ✅ Loads ONLY accepted investor connectors (Founder side)
     */
    private fun loadConnectors() {

        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/founders/connectors")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                // optional: log or show empty state
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                val arr = JSONArray(body)

                runOnUiThread {
                    container.removeAllViews()

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)

                        android.util.Log.d("CONNECTORS_JSON", o.toString())

                        val card = LayoutInflater.from(this@FSConnectorsActivity)
                            .inflate(R.layout.item_investor_card, container, false)

                        ProfileImageLoader.load(
                            card.findViewById(R.id.ivProfile),
                            o.optString("profile_image")
                        )

                        // -------- DATA --------
                        card.findViewById<TextView>(R.id.tvName)
                            .text = o.optString("name", "—")

                        card.findViewById<TextView>(R.id.tvCompany)
                            .text = o.optString("company_name", "—")

                        card.findViewById<TextView>(R.id.tvBio)
                            .text = o.optString("bio", "")

                        card.findViewById<TextView>(R.id.tvCategory)
                            .text = o.optString("category", "—")

                        card.findViewById<TextView>(R.id.tvAmount)
                            .text = o.optString("investment_range", "—")

                        // -------- FOOTER (Chat only) --------
                        card.findViewById<TextView>(R.id.btnAccept).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnReject).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnCancel).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnDelete).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnPrimary).visibility = View.GONE

                        val btnChat = card.findViewById<TextView>(R.id.btnChat)
                        btnChat.visibility = View.VISIBLE

                        btnChat.setOnClickListener {
                            val intent =
                                Intent(this@FSConnectorsActivity, ChatActivity::class.java)
                            intent.putExtra("REQUEST_ID", o.getInt("request_id"))
                            intent.putExtra("CHAT_TITLE", o.optString("name"))
                            startActivity(intent)
                        }

                        container.addView(card)
                    }
                }
            }
        })
    }
}
