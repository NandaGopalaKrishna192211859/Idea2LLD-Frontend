package com.simats.idea2lld.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.ProjectPackageDisplayActivity
import com.simats.idea2lld.R
import org.json.JSONArray

class ShowProjToFounderAdapter(
    private val activity: AppCompatActivity,
    private val container: LinearLayout,
    private val jsonArray: JSONArray
) {

    /**
     * Single public method
     * Used by:
     *  - YourDirectRequestsActivity
     *  - YourHubRequestsActivity
     */
    fun render() {

        container.removeAllViews()

        for (i in 0 until jsonArray.length()) {

            val obj = jsonArray.getJSONObject(i)

            val cardView = LayoutInflater.from(activity)
                .inflate(R.layout.item_project_card, container, false)

            // -------- Bind data --------
            cardView.findViewById<TextView>(R.id.tvTitle)
                .text = obj.optString("title", "Untitled Project")

            cardView.findViewById<TextView>(R.id.tvCategory)
                .text = obj.optString("category", "General")

            cardView.findViewById<TextView>(R.id.tvDevStatus)
                .text = "Dev Status: ${obj.optString("dev_status", "ongoing")}"

            // -------- Card click → OPEN PACKAGE (SAME AS INVESTOR) --------
            val requestId = obj.getInt("request_id")

            cardView.setOnClickListener {
                val intent = Intent(activity, ProjectPackageDisplayActivity::class.java)
                intent.putExtra("REQUEST_ID", requestId)
                activity.startActivity(intent)
            }

            container.addView(cardView)
        }
    }
}
