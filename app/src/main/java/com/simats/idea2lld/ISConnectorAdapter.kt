package com.simats.idea2lld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ISConnectorAdapter(
    private val container: LinearLayout,
    private val session: SessionManager
) {

    fun load() {

        val token = session.getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/investors/connectors/accepted")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                // optional: log
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                val arr = JSONObject(body).getJSONArray("connectors")

                container.post {
                    container.removeAllViews()

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)

                        val card = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_founder_card, container, false)

                        // -------- DATA BINDING (FOUNDER CARD) --------
                        card.findViewById<TextView>(R.id.tvFounderName).text =
                            o.optString("name", "Founder")

                        card.findViewById<TextView>(R.id.tvProjectTitle).text =
                            o.optString("project_title", "Project")

                        card.findViewById<TextView>(R.id.tvCategory).text =
                            o.optString("category", "—")

                        card.findViewById<TextView>(R.id.tvDevStatus).text =
                            o.optString("dev_status", "—")

                        // -------- FOOTER: CHAT ONLY --------
                        card.findViewById<TextView>(R.id.btnAccept).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnReject).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnCancel).visibility = View.GONE
                        card.findViewById<TextView>(R.id.btnDelete).visibility = View.GONE

                        val btnChat = card.findViewById<TextView>(R.id.btnChat)
                        btnChat.visibility = View.VISIBLE

                        btnChat.setOnClickListener {
                            val intent =
                                Intent(container.context, ChatActivity::class.java)
                            intent.putExtra("REQUEST_ID", o.getInt("request_id"))
                            intent.putExtra("CHAT_TITLE", o.optString("name"))
                            container.context.startActivity(intent)
                        }

                        container.addView(card)
                    }
                }
            }
        })
    }
}
