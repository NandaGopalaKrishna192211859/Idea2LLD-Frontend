package com.simats.idea2lld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.CancelRequestManager
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.ButtonUpdationAdapter
import com.simats.idea2lld.utils.ProjectRequestClickHandler


class YourRequests(
    private val recycler: RecyclerView,
    private val session: SessionManager
) {

    /**
     * ✅ SINGLE METHOD
     * Call this from InvestorDashboardActivity
     */
    fun load() {

        val token = session.getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/requests/mine")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                val arr = JSONObject(body).getJSONArray("requests")

                val list = mutableListOf<Item>()

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    list.add(
                        Item(
                            id = o.getInt("id"),
                            founderName = o.getString("founder_name"),
                            projectTitle = o.getString("title"),
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
        val id: Int,
        val founderName: String,
        val projectTitle: String,
        val category: String,
        val devStatus: String,
        val responseStatus: String   // 👈 ADD THIS
    )

    // ---------------- ADAPTER ----------------
    class Adapter(
        private val list: MutableList<Item>
    ) : RecyclerView.Adapter<Adapter.VH>() {


        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val founderName: TextView = v.findViewById(R.id.tvFounderName)
            val projectTitle: TextView = v.findViewById(R.id.tvProjectTitle)
            val category: TextView = v.findViewById(R.id.tvCategory)
            val devStatus: TextView = v.findViewById(R.id.tvDevStatus)

            val btnCancel: TextView = v.findViewById(R.id.btnCancel)
            val btnAccept: TextView = v.findViewById(R.id.btnAccept)
            val btnReject: TextView = v.findViewById(R.id.btnReject)
            val btnChat: TextView = v.findViewById(R.id.btnChat)
            val btnDelete: TextView = v.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_founder_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]

            h.founderName.text = item.founderName
            h.projectTitle.text = item.projectTitle
            h.category.text = item.category
            h.devStatus.text = item.devStatus

            // ✅ ONLY Cancel button visible

            h.itemView.setOnClickListener {
                ProjectRequestClickHandler.open(
                    h.itemView.context,
                    item.id
                )
            }



            // reset all
            h.btnCancel.visibility = View.GONE
            h.btnAccept.visibility = View.GONE
            h.btnReject.visibility = View.GONE
            h.btnChat.visibility = View.GONE
            h.btnDelete.visibility = View.GONE

            when (item.responseStatus) {

                "pending" -> {
                    h.btnCancel.visibility = View.VISIBLE

                    h.btnCancel.setOnClickListener {

                        val cancelManager = CancelRequestManager(h.itemView.context)

                        h.btnCancel.isEnabled = false
                        h.btnCancel.text = "Cancelling..."

                        cancelManager.cancelInvestorMyRequest(
                            requestId = item.id
                        ) {
                            list.removeAt(h.adapterPosition)
                            notifyItemRemoved(h.adapterPosition)
                        }
                    }
                }

                "accepted" -> {
                    h.btnAccept.visibility = View.VISIBLE
                    h.btnAccept.text = "Accepted"
                    h.btnAccept.isEnabled = false
                    h.btnChat.visibility = View.VISIBLE

                    h.btnChat.setOnClickListener {
                        val intent = Intent(h.itemView.context, ChatActivity::class.java)
                        intent.putExtra("REQUEST_ID", item.id)
                        intent.putExtra("CHAT_TITLE", item.founderName) // 👈 show founder name
                        h.itemView.context.startActivity(intent)
                    }

                }

                "rejected" -> {
                    h.btnReject.visibility = View.VISIBLE
                    h.btnReject.text = "Rejected"
                    h.btnReject.isEnabled = false
                    h.btnDelete.visibility = View.VISIBLE
                }
            }



            h.btnDelete.setOnClickListener {

                val updater = ButtonUpdationAdapter(h.itemView.context)

                h.btnDelete.isEnabled = false
                h.btnDelete.text = "Deleting..."

                updater.updateRequest(
                    requestId = item.id,
                    action = "delete"
                ) {
                    list.removeAt(h.adapterPosition)
                    notifyItemRemoved(h.adapterPosition)
                }
            }





        }

        override fun getItemCount(): Int = list.size
    }
}


