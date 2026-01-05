package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.filters.FeedFilterHelper


class InvestorDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investor_dashboard)

        val session = SessionManager(this)

        // ---------------- HEADER ----------------
        val tvHello = findViewById<TextView>(R.id.tvHelloInvestor)
        val fullName = session.getFullName().ifEmpty { "Investor" }
        tvHello.text = "Hello $fullName 👋"

        tvHello.translationX = -300f
        tvHello.alpha = 0f
        tvHello.animate().translationX(0f).alpha(1f).setDuration(500).start()

        // ---------------- CONTENT CONTAINER ----------------
        val container = findViewById<FrameLayout>(R.id.investorContentContainer)

        // ---------------- FILTER UI ----------------
        val filterIcon = findViewById<ImageView>(R.id.icFilter)
        val filterRow = findViewById<View>(R.id.layoutFilterRow)

        val tvSelectedCategory = findViewById<TextView>(R.id.tvSelectedCategory)
        tvSelectedCategory.text = "ALL"




        filterIcon.setOnClickListener {

            val isOpening = !FeedFilterHelper.isFilterOpen()

            filterIcon.animate()
                .rotation(if (isOpening) 90f else 0f)
                .setDuration(250)
                .start()

            FeedFilterHelper.toggleFilter(
                activity = this,
                rootContainer = container
            ) { selectedCategory ->

                tvSelectedCategory.text = selectedCategory
                tvSelectedCategory.setBackgroundColor(
                    android.graphics.Color.parseColor("#E3F2FD")
                )


                FeedFilterHelper.loadFeed(
                    activity = this,
                    container = container,
                    category = selectedCategory
                )

                // rotate back when closed
                filterIcon.animate()
                    .rotation(0f)
                    .setDuration(250)
                    .start()

                FeedFilterHelper.forceClose(container)
            }
        }




        val requestsSelector = findViewById<View>(R.id.layoutRequestsSelector)
        val tabYourRequests = findViewById<TextView>(R.id.tabYourRequests)
        val tabFounderRequests = findViewById<TextView>(R.id.tabFounderRequests)

        // ---------------- BOTTOM NAV ----------------
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationInvestor)

        bottomNav.setOnItemSelectedListener { item ->
            FeedFilterHelper.forceClose(container)

            filterRow.visibility = View.GONE
            requestsSelector.visibility = View.GONE

            when (item.itemId) {

                R.id.menu_feed -> {
                    filterRow.visibility = View.VISIBLE
//                    loadInvestorFeed(container)   // ✅ FIX

                    FeedFilterHelper.loadFeed(
                        activity = this,
                        container = container,
                        category = "ALL"
                    )

                }

                R.id.menu_requests -> {
                    requestsSelector.visibility = View.VISIBLE

                    tabYourRequests.setBackgroundColor(
                        android.graphics.Color.parseColor("#16A34A")
                    )
                    tabYourRequests.setTextColor(android.graphics.Color.WHITE)

                    tabFounderRequests.setBackgroundColor(
                        android.graphics.Color.parseColor("#D1FAE5")
                    )
                    tabFounderRequests.setTextColor(
                        android.graphics.Color.parseColor("#065F46")
                    )

                    // ✅ Create RecyclerView ONCE (no placeholder)
                    val recycler = RecyclerView(this).apply {
                        layoutManager = LinearLayoutManager(this@InvestorDashboardActivity)
                    }

                    // ✅ AUTO LOAD "YOUR REQUESTS"
                    YourRequests(
                        recycler = recycler,
                        session = SessionManager(this)
                    ).load()


                    container.removeAllViews()
                    container.addView(recycler)
                }


                R.id.menu_connectors -> {

                    val scroll = ScrollView(this).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }

                    val connectorContainer = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(14, 14, 14, 14)
                    }

                    scroll.addView(connectorContainer)

                    container.removeAllViews()
                    container.addView(scroll)

                    ISConnectorAdapter(
                        container = connectorContainer,
                        session = SessionManager(this)
                    ).load()

                }



            }
            true
        }

        tabYourRequests.setOnClickListener {

            // UI toggle
            tabYourRequests.setBackgroundColor(
                android.graphics.Color.parseColor("#16A34A")
            )
            tabYourRequests.setTextColor(android.graphics.Color.WHITE)

            tabFounderRequests.setBackgroundColor(
                android.graphics.Color.parseColor("#D1FAE5")
            )
            tabFounderRequests.setTextColor(
                android.graphics.Color.parseColor("#065F46")
            )

            // Create RecyclerView
            val recycler = RecyclerView(this).apply {
                layoutManager = LinearLayoutManager(this@InvestorDashboardActivity)
            }

            // Replace container content
            container.removeAllViews()
            container.addView(recycler)

            // ✅ CALL YOUR REQUESTS
            YourRequests(
                recycler = recycler,
                session = SessionManager(this)
            ).load()
        }



        tabFounderRequests.setOnClickListener {

            tabFounderRequests.setBackgroundColor(android.graphics.Color.parseColor("#16A34A"))
            tabFounderRequests.setTextColor(android.graphics.Color.WHITE)

            tabYourRequests.setBackgroundColor(android.graphics.Color.parseColor("#D1FAE5"))
            tabYourRequests.setTextColor(android.graphics.Color.parseColor("#065F46"))

            // ✅ Show Founder Requests
            val recycler = RecyclerView(this).apply {
                layoutManager = LinearLayoutManager(this@InvestorDashboardActivity)
            }

            container.removeAllViews()
            container.addView(recycler)

            FounderRequests(
                recycler = recycler,
                session = SessionManager(this)
            ).load()
        }


        bottomNav.selectedItemId = R.id.menu_feed

        // ---------------- DRAWER ----------------
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutInvestor)
        val icMenu = findViewById<ImageView>(R.id.icMenu)
        val tvInvestorName = findViewById<TextView>(R.id.tvInvestorName)

        tvInvestorName.text = fullName

        icMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // ---------------- LOGOUT ----------------
        findViewById<View>(R.id.btnLogoutInvestor).setOnClickListener {
            session.clearSession()
            startActivity(
                Intent(this, LoginPageSelectionsActivity::class.java)
                    .putExtra("SCREEN_TYPE", "LOGIN")
            )
            finish()
        }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(
                Intent(this, EditProfileActivity::class.java)
            )
        }

    }

    // ---------------- FILTER CHIPS ----------------

    // ---------------- FEED ----------------
//    private fun loadInvestorFeed(container: FrameLayout) {
//        val token = SessionManager(this).getToken()
//
//        val recycler = RecyclerView(this).apply {
//            layoutManager = LinearLayoutManager(this@InvestorDashboardActivity)
//        }
//
//        container.removeAllViews()
//        container.addView(recycler)
//
//        val request = Request.Builder()
//            .url(ApiClient.GET_INVESTOR_FEED_URL)
//            .addHeader("Authorization", "Bearer $token")
//            .get()
//            .build()
//
//        OkHttpClient().newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {}
//
//            override fun onResponse(call: Call, response: Response) {
//                val body = response.body?.string() ?: return
//                val json = JSONObject(body)
//                val arr = json.getJSONArray("feed")
//
//                val list = mutableListOf<ProjectCardItem>()
//
//                for (i in 0 until arr.length()) {
//                    val o = arr.getJSONObject(i)
//
//                    list.add(
//                        ProjectCardItem(
//                            requestId = o.getInt("request_id"),
//                            title = o.optString("title", "Untitled Project"),
//                            category = o.optString("category", "General"),
//                            devStatus = o.optString("dev_status", "Ongoing")
//                        )
//                    )
//                }
//
//                runOnUiThread {
//                    recycler.adapter = InvestorFeedAdapter(list) { requestId ->
//                        val intent = Intent(
//                            this@InvestorDashboardActivity,
//                            ProjectPackageDisplayActivity::class.java
//                        )
//                        intent.putExtra("REQUEST_ID", requestId)
//                        startActivity(intent)
//                    }
//                }
//            }
//        })
//    }


    // ---------------- MODELS ----------------
    data class ProjectCardItem(
        val requestId: Int,
        val title: String,
        val category: String,
        val devStatus: String
    )

    // ---------------- ADAPTER ----------------
    class InvestorFeedAdapter(
        private val list: List<ProjectCardItem>,
        private val onCardClick: (Int) -> Unit,
        private val onCategoryClick: (String) -> Unit
    )
        : RecyclerView.Adapter<InvestorFeedAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvTitle)
            val category: TextView = v.findViewById(R.id.tvCategory)
            val devStatus: TextView = v.findViewById(R.id.tvDevStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_project_card, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            h.title.text = item.title
            h.category.text = item.category
            h.devStatus.text = "Dev Status: ${item.devStatus}"

            h.itemView.findViewById<View>(R.id.footerLayout)
                ?.visibility = View.GONE

            h.itemView.setOnClickListener {
                onCardClick(item.requestId)
            }


        }

        override fun getItemCount(): Int = list.size
    }

    // ---------------- PLACEHOLDER ----------------
    private fun showPlaceholder(container: FrameLayout, title: String) {
        container.removeAllViews()
        container.addView(
            TextView(this).apply {
                text = title
                textSize = 18f
                setPadding(32, 32, 32, 32)
            }
        )
    }
}
