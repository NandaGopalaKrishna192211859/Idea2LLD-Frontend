package com.simats.idea2lld.filters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.InvestorDashboardActivity
import com.simats.idea2lld.ProjectPackageDisplayActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

@SuppressLint("StaticFieldLeak")
object FeedFilterHelper {

    private var filterView: View? = null
    private var isOpen = false

    private var selectedCategory: String = "ALL"


    /**
     * ✅ SINGLE ENTRY POINT
     * - Toggle filter UI
     * - Load categories from DB
     * - Handle close/open safely
     */
    fun toggleFilter(
        activity: AppCompatActivity,
        rootContainer: FrameLayout,
        onCategorySelected: (String) -> Unit
    ) {
        if (isOpen) {
            closeFilter(rootContainer)
            return
        }

        isOpen = true

        val filterContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.WHITE)
            elevation = 12f
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 10
        filterContainer.layoutParams = params

        filterView = filterContainer
//        ....
        filterContainer.translationY = -40f
        filterContainer.alpha = 0f
        rootContainer.addView(filterContainer)

        filterContainer.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()


        loadCategories(activity, filterContainer, onCategorySelected)
    }

    private fun closeFilter(rootContainer: FrameLayout) {
        filterView?.animate()
            ?.translationY(-40f)
            ?.alpha(0f)
            ?.setDuration(200)
            ?.withEndAction {
                filterView?.let { rootContainer.removeView(it) }
                filterView = null
                isOpen = false
            }
            ?.start()
    }


    /**
     * ✅ Load categories from DB
     */
    private fun loadCategories(
        activity: AppCompatActivity,
        container: LinearLayout,
        onCategorySelected: (String) -> Unit
    ) {

        val token = SessionManager(activity).getToken()

        val request = Request.Builder()
            .url(ApiClient.GET_CATEGORIES_URL)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val arr = json.getJSONArray("categories")

                activity.runOnUiThread {

                    // ✅ ALL option
                    container.addView(createItem(activity, "ALL", onCategorySelected))

                    for (i in 0 until arr.length()) {
                        val name = arr.getJSONObject(i).getString("category_name")
                        container.addView(createItem(activity, name, onCategorySelected))
                    }
                }
            }
        })
    }

    private fun createItem(
        activity: AppCompatActivity,
        text: String,
        onCategorySelected: (String) -> Unit
    ): TextView {

        return TextView(activity).apply {

            this.text = text
            textSize = 16f
            setPadding(24, 20, 24, 20)

            fun applyStyle() {
                if (text == selectedCategory) {
                    setBackgroundColor(Color.parseColor("#E3F2FD")) // light blue
                    setTextColor(Color.parseColor("#1D4ED8"))       // blue text
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                    setTextColor(Color.BLACK)
                }
            }

            applyStyle()

            setOnClickListener {
                selectedCategory = text
                onCategorySelected(text)
            }
        }
    }


    /**
     * ✅ FEED LOADER (REUSED)
     */
    fun loadFeed(
        activity: AppCompatActivity,
        container: FrameLayout,
        category: String
    ) {

        val token = SessionManager(activity).getToken()

        val recycler = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity)
        }

        container.removeAllViews()
        container.addView(recycler)

        val url =
            "${ApiClient.BASE_URL}/investors/filter/feed?category=$category"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val arr = json.getJSONArray("feed")

                val list = mutableListOf<InvestorDashboardActivity.ProjectCardItem>()

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        InvestorDashboardActivity.ProjectCardItem(
                            requestId = o.getInt("request_id"),
                            title = o.getString("title"),
                            category = o.getString("category"),
                            devStatus = o.getString("dev_status")
                        )
                    )
                }

                activity.runOnUiThread {
                    recycler.adapter =
                        InvestorDashboardActivity.InvestorFeedAdapter(
                            list = list,
                            onCardClick = { requestId ->
                                activity.startActivity(
                                    android.content.Intent(
                                        activity,
                                        ProjectPackageDisplayActivity::class.java
                                    ).putExtra("REQUEST_ID", requestId)
                                )
                            },
                            onCategoryClick = { category ->
                                loadFeed(
                                    activity = activity,
                                    container = container,
                                    category = category
                                )
                            }
                        )
                }

            }
        })
    }

    /**
     * ✅ Must be called when navigating away
     */
    fun forceClose(rootContainer: FrameLayout) {
        closeFilter(rootContainer)
    }

    fun isFilterOpen(): Boolean = isOpen

}
