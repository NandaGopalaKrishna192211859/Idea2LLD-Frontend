package com.simats.idea2lld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.appcompat.view.menu.MenuView

class InvestorListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private val investors = mutableListOf<Investor>()
    private lateinit var adapter: InvestorAdapter
    private lateinit var category: String

    private var isHubSelected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investor_list)

        category = intent.getStringExtra("CATEGORY") ?: ""
        if (category.isBlank()) finish()

        recycler = findViewById(R.id.recyclerInvestors)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = InvestorAdapter(investors)
        recycler.adapter = adapter

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnRotate).setOnClickListener {
            loadInvestors()
        }


        findViewById<Button>(R.id.btnNext).setOnClickListener {

            val projectId = intent.getIntExtra("PID", -1)
            val imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""



            if (projectId == -1) {
                Toast.makeText(this, "Invalid project", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIds = investors
                .filter { it.isSelected }
                .map { it.uid }

            if (!isHubSelected && selectedIds.isEmpty()) {
                Toast.makeText(
                    this,
                    "Select Investor Hub or at least one investor",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


            val intent = Intent(this, ProjectPackagePreviewActivity::class.java)
            intent.putExtra("PID", projectId)
            intent.putExtra("IMAGE_URL", imageUrl)
            intent.putExtra("IS_HUB", isHubSelected)
            intent.putIntegerArrayListExtra(
                "INVESTOR_IDS",
                ArrayList(selectedIds)
            )

            startActivity(intent)   // ✅ START AT THE END
        }



        // Investor Hub Card (unchanged logic)
         val hubCard = findViewById<View>(R.id.investorHubCard)

        hubCard.setOnClickListener {
            isHubSelected = !isHubSelected
            hubCard.setBackgroundColor(
                if (isHubSelected) Color.parseColor("#E8F5E9")
                else Color.WHITE
            )
        }

        loadInvestors()
    }

    private fun loadInvestors() {
        val token = SessionManager(this).getToken()
        val url = "${ApiClient.GET_INVESTORS_BY_CATEGORY_URL}/$category"

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(req).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, res: Response) {
                val body = res.body?.string() ?: return
                val arr = JSONObject(body).getJSONArray("investors")

                runOnUiThread {
                    investors.clear()

                    val total = arr.length()
                    val picked = mutableSetOf<Int>()
//                    count of investors in the screen  - change the no. if you want
                    while (picked.size < minOf(5, total)) {
                        picked.add(Random.nextInt(total))
                    }

                    for (i in picked) {
                        val o = arr.getJSONObject(i)
                        val preferred = o.optString("preferred_categories", "[]")

                        val matchedCategory =
                            if (preferred.contains(category)) category else ""

                        investors.add(
                            Investor(
                                uid = o.getInt("uid"),
                                name = o.optString("name"),
                                company = o.optString("company_name"),
                                bio = o.optString("bio"),
                                min = o.optString("min_investment"),
                                max = o.optString("max_investment"),
                                img = o.optString("profile_image"),
                                linkedin = o.optString("linkedin_url"),
                                website = o.optString("website_url"),
                                matchedCategory = matchedCategory
                            )
                        )
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    // ================= DATA MODEL =================
    data class Investor(
        val uid: Int,
        val name: String,
        val company: String,
        val bio: String,
        val min: String,
        val max: String,
        val img: String,
        val linkedin: String,
        val website: String,
        val matchedCategory: String,
        var isSelected: Boolean = false
    )

    // ================= ADAPTER =================
    class InvestorAdapter(
        private val list: List<Investor>
    ) : RecyclerView.Adapter<InvestorAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView = v.findViewById(R.id.ivProfile)
            val name: TextView = v.findViewById(R.id.tvName)
            val company: TextView = v.findViewById(R.id.tvCompany)
            val bio: TextView = v.findViewById(R.id.tvBio)
            val amount: TextView = v.findViewById(R.id.tvAmount)
            val category: TextView = v.findViewById(R.id.tvCategory)

            val btnAccept: TextView = v.findViewById(R.id.btnAccept)
            val btnReject: TextView = v.findViewById(R.id.btnReject)
            val btnChat: TextView = v.findViewById(R.id.btnChat)
            val btnCancel: TextView = v.findViewById(R.id.btnCancel)
            val footerLayout: View = v.findViewById(R.id.footerLayout)
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_investor_card1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val inv = list[position]

            // Name (unchanged)
            holder.name.text = inv.name

// Company
            val companyText = "Company: ${inv.company}"
            val companySpan = SpannableString(companyText)
            companySpan.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                "Company:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            companySpan.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                "Company:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.company.text = companySpan

// Bio
            val bioText = "Bio: ${inv.bio}"
            val bioSpan = SpannableString(bioText)
            bioSpan.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                "Bio:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            bioSpan.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                "Bio:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.bio.text = bioSpan

// Investment Range
            val amountText = "Range of Investment: ₹${inv.min} – ₹${inv.max}"
            val amountSpan = SpannableString(amountText)
            amountSpan.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                "Range of Investment:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            amountSpan.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                "Range of Investment:".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.amount.text = amountSpan


            // Category chip
            if (inv.matchedCategory.isNotBlank()) {
                holder.category.text = inv.matchedCategory
                holder.category.visibility = View.VISIBLE
            } else {
                holder.category.visibility = View.GONE
            }

            // Hide action buttons for this screen
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
            holder.btnChat.visibility = View.GONE
            holder.btnCancel.visibility = View.GONE
            holder.footerLayout.visibility = View.GONE


            // Selection UI (RecyclerView safe)
            holder.itemView.setBackgroundColor(
                if (inv.isSelected) Color.parseColor("#E8F5E9")
                else Color.WHITE
            )

            holder.itemView.setOnClickListener {
                inv.isSelected = !inv.isSelected
                notifyItemChanged(position)
            }

            if (inv.img.isNotBlank()) {
                Glide.with(holder.itemView)
                    .load(inv.img)
                    .placeholder(R.drawable.ic_user)
                    .into(holder.img)
            } else {
                holder.img.setImageResource(R.drawable.ic_user)
            }
        }

        override fun getItemCount(): Int = list.size
    }
}
