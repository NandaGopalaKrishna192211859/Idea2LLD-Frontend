package com.simats.idea2lld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.ButtonUpdationAdapter
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.ProjectRequestClickHandler


class FounderRequests(
    private val recycler: RecyclerView,
    private val session: SessionManager
) {

    /**
     * ✅ SINGLE METHOD
     * Call this from InvestorDashboard
     */
    fun load() {

        val token = session.getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/requests/founder")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                android.util.Log.d("FOUNDER_API", body)
                val json = JSONObject(body)
                val arr = json.getJSONArray("requests")
                android.util.Log.d("FOUNDER_ROW", arr.toString())


                val list = mutableListOf<Item>()

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    list.add(
                        Item(
                            requestId = o.getInt("request_id"),
                            founderName = o.getString("founder_name"),
                            projectTitle = o.getString("project_title"),
                            category = o.getString("category"),
                            devStatus = o.getString("dev_status"),
                            responseStatus = o.optString("response_status", "pending")
                        )
                    )
                }

                recycler.post {
                    recycler.adapter = Adapter(list)
                }
            }
        })
    }

    // ---------------- MODEL ----------------
    data class Item(
        val requestId: Int,
        val founderName: String,
        val projectTitle: String,
        val category: String,
        val devStatus: String,
        val responseStatus: String
    )

    // ---------------- ADAPTER ----------------
    class Adapter(private val list: List<Item>) :
        RecyclerView.Adapter<Adapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val founderName: TextView = v.findViewById(R.id.tvFounderName)
            val projectTitle: TextView = v.findViewById(R.id.tvProjectTitle)
            val category: TextView = v.findViewById(R.id.tvCategory)
            val devStatus: TextView = v.findViewById(R.id.tvDevStatus)

            val btnAccept: TextView = v.findViewById(R.id.btnAccept)
            val btnReject: TextView = v.findViewById(R.id.btnReject)
            val btnChat: TextView = v.findViewById(R.id.btnChat)
            val btnDelete: TextView = v.findViewById(R.id.btnDelete)
            val btnCancel: TextView = v.findViewById(R.id.btnCancel)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_founder_card, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]

            h.founderName.text = item.founderName
            h.projectTitle.text = item.projectTitle
            h.category.text = item.category
            h.devStatus.text = item.devStatus

            // reset all buttons
            h.btnAccept.visibility = View.GONE
            h.btnReject.visibility = View.GONE
            h.btnChat.visibility = View.GONE
            h.btnDelete.visibility = View.GONE
            h.btnCancel.visibility = View.GONE

            val updater = ButtonUpdationAdapter(h.itemView.context)

            h.itemView.setOnClickListener {
                ProjectRequestClickHandler.open(
                    h.itemView.context,
                    item.requestId
                )
            }

            when (item.responseStatus) {

                "pending" -> {
                    h.btnAccept.visibility = View.VISIBLE
                    h.btnReject.visibility = View.VISIBLE

                    h.btnAccept.setOnClickListener {
                        updater.updateRequest(
                            requestId = item.requestId,
                            action = "accepted"
                        ) {
                            h.btnAccept.text = "Accepted"
                            h.btnAccept.isEnabled = false
                            h.btnReject.visibility = View.GONE
                            h.btnChat.visibility = View.VISIBLE
                        }
                    }

                    h.btnReject.setOnClickListener {
                        updater.updateRequest(
                            requestId = item.requestId,
                            action = "rejected"
                        ) {
                            (list as MutableList).removeAt(h.adapterPosition)
                            notifyItemRemoved(h.adapterPosition)
                        }
                    }
                }

                "accepted" -> {
                    h.btnAccept.text = "Accepted"
                    h.btnAccept.isEnabled = false
                    h.btnAccept.visibility = View.VISIBLE
                    h.btnChat.visibility = View.VISIBLE

                    h.btnChat.setOnClickListener {
                        val intent = Intent(h.itemView.context, ChatActivity::class.java)
                        intent.putExtra("REQUEST_ID", item.requestId)
                        intent.putExtra("CHAT_TITLE", item.founderName) // 👈 founder name
                        h.itemView.context.startActivity(intent)
                    }

                }

                "rejected" -> {
                    // safest: remove from UI
                    (list as MutableList).removeAt(h.adapterPosition)
                    notifyItemRemoved(h.adapterPosition)
                }
            }

            h.btnAccept.setOnClickListener {
                updater.updateRequest(
                    requestId = item.requestId,
                    action = "accepted"
                ) {
                    h.btnAccept.text = "Accepted"
                    h.btnAccept.isEnabled = false
                    h.btnReject.visibility = View.GONE
                    h.btnChat.visibility = View.VISIBLE
                }
            }

// REJECT
            h.btnReject.setOnClickListener {
                updater.updateRequest(
                    requestId = item.requestId,
                    action = "rejected"
                ) {
                    // Investor side → remove card
                    (list as MutableList).removeAt(h.adapterPosition)
                    notifyItemRemoved(h.adapterPosition)
                }
            }

        }

        override fun getItemCount(): Int = list.size
    }
}
