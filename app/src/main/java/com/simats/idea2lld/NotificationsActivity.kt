package com.simats.idea2lld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.utils.NotificationHelper
import org.json.JSONObject

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerSaved)
        val empty = findViewById<TextView>(R.id.tvEmptySaved)

        recycler.layoutManager = LinearLayoutManager(this)

        val list = NotificationHelper.getAll(this)

        if (list.isEmpty()) {
            empty.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            recycler.adapter = NotificationAdapter(list)
        }
    }

    class NotificationAdapter(
        private val items: List<JSONObject>
    ) : RecyclerView.Adapter<NotificationAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvTitle)
            val msg: TextView = v.findViewById(R.id.tvMessage)
            val time: TextView = v.findViewById(R.id.tvTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val o = items[i]
            h.title.text = o.getString("title")
            h.msg.text = o.getString("message")
            h.time.text = o.getString("time")
        }

        override fun getItemCount() = items.size
    }
}
