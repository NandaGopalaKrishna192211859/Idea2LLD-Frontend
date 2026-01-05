package com.simats.idea2lld

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.InvestorSendGuard
import com.simats.idea2lld.utils.SessionManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.bumptech.glide.request.target.Target


class ProjectPackageDisplayActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvFounderName: TextView
    private lateinit var tvFounderEmail: TextView
    private lateinit var tvFounderPhone: TextView
    private lateinit var tvSummary: TextView
    private lateinit var ivProjectImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_package_preview)

        val requestId = intent.getIntExtra("REQUEST_ID", -1)
        if (requestId == -1) {
            finish()
            return
        }




        tvTitle = findViewById(R.id.tvProjectTitle)
        tvFounderName = findViewById(R.id.tvFounderName)
        tvFounderEmail = findViewById(R.id.tvFounderEmail)
        tvFounderPhone = findViewById(R.id.tvFounderPhone)
        tvSummary = findViewById(R.id.tvSummary)
        ivProjectImage = findViewById(R.id.ivProjectImage)

        // 🔒 READ ONLY
        val btnSend = findViewById<Button>(R.id.btnSend)
        btnSend.visibility = View.VISIBLE

        btnSend.setOnClickListener {
            sendInvestorRequest(requestId)
        }

        val fromFounder = intent.getBooleanExtra("FROM_FOUNDER", false)

        if (fromFounder) {
            btnSend.visibility = View.GONE   // ✅ hide only for Founder
        }

        findViewById<Button>(R.id.btnEditPackage).visibility = View.GONE
        findViewById<EditText>(R.id.etBusinessNote).isEnabled = false
        findViewById<RadioGroup>(R.id.rgDevStatus).visibility = View.GONE

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadPackage(requestId)
    }

    private fun loadPackage(requestId: Int) {
        val token = SessionManager(this).getToken()

        val req = Request.Builder()
            .url("${ApiClient.GET_PROJECT_REQUEST_PACKAGE_URL}/$requestId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(req).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)

                val devStatus = json.optString("dev_status", "ongoing")

                val pkg = json.optJSONObject("package") ?: return
                val project = pkg.getJSONObject("project")
                val founder = pkg.getJSONObject("founder")
                val contents = pkg.getJSONObject("contents")

                val rawSummary = contents
                    .optString("summary", "")
                    .replace("**", "")
                    .replace("__", "")
                    .trim()

                runOnUiThread {

                    // ---- Project ----
                    tvTitle.text = project.optString("title", "Untitled Project")

                    // ---- Founder ----
                    tvFounderName.text = "Name: ${founder.optString("name", "N/A")}"
                    tvFounderEmail.text = "Email: ${founder.optString("email", "N/A")}"
                    tvFounderPhone.text = "Phone: ${founder.optString("phone", "N/A")}"

                    // ---- Summary (same as Preview screen) ----
                    tvSummary.text = formatSummary(rawSummary)

                    // ---- Image ----
                    val rawImagePath = contents.optString("image_path")

                    if (rawImagePath.isNotBlank()) {

                        // 🔧 Trim server file path → public URL path
                        val publicImagePath = when {
                            rawImagePath.contains("/diagrams/") ->
                                rawImagePath.substring(rawImagePath.indexOf("/diagrams/"))

                            rawImagePath.contains("diagrams/") ->
                                "/" + rawImagePath.substring(rawImagePath.indexOf("diagrams/"))

                            else -> ""
                        }

                        if (publicImagePath.isNotBlank()) {

                            val fullImageUrl = ApiClient.IMAGE_BASE_URL + publicImagePath

                            Glide.with(this@ProjectPackageDisplayActivity)
                                .load(fullImageUrl)
                                .dontTransform()                // ✅ no bitmap scaling
                                .dontAnimate()
                                .override(Target.SIZE_ORIGINAL) // ✅ load full resolution
                                .placeholder(R.drawable.placeholder_project)
                                .error(R.drawable.placeholder_project)
                                .into(ivProjectImage)


                            ivProjectImage.setOnClickListener {
                                val intent = android.content.Intent(
                                    this@ProjectPackageDisplayActivity,
                                    ImagePreviewActivity::class.java
                                )
                                intent.putExtra("IMAGE_URL", fullImageUrl)
                                startActivity(intent)
                            }

                        } else {
                            ivProjectImage.setImageResource(R.drawable.placeholder_project)
                        }

                    } else {
                        ivProjectImage.setImageResource(R.drawable.placeholder_project)
                    }



                    // ---- Dev Status (XML already exists) ----
                    val rgDevStatus = findViewById<RadioGroup>(R.id.rgDevStatus)
                    rgDevStatus.visibility = View.VISIBLE

// 🔒 Disable interaction (VIEW ONLY)
                    rgDevStatus.isEnabled = false

                    for (i in 0 until rgDevStatus.childCount) {
                        rgDevStatus.getChildAt(i).isEnabled = false
                    }

                    when (devStatus) {
                        "completed" -> rgDevStatus.check(R.id.rbCompleted)
                        else -> rgDevStatus.check(R.id.rbOngoing)
                    }

                }
            }

        })
    }

    private fun formatSummary(text: String): CharSequence {
        val clean = text
            .replace("**", "")
            .replace("__", "")
            .trim()

        val spannable = android.text.SpannableString(clean)

        listOf("Modules:", "Data Flow:", "Features:").forEach { heading ->
            val index = clean.indexOf(heading)
            if (index >= 0) {
                spannable.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    index,
                    index + heading.length,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    private fun sendInvestorRequest(requestId: Int) {

        val token = SessionManager(this).getToken()

        val body = JSONObject().apply {
            put("request_id", requestId)
        }

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/send-request")
            .addHeader("Authorization", "Bearer $token")
            .post(
                RequestBody.create(
                    "application/json".toMediaType(),
                    body.toString()
                )
            )
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    InvestorSendGuard.requestByInvestor(
                        this@ProjectPackageDisplayActivity,
                        isDuplicate = false
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val bodyStr = response.body?.string()
                val json = bodyStr?.let { JSONObject(it) }

                val isDuplicate = json?.optBoolean("duplicate", false) ?: false

                runOnUiThread {

                    // ✅ SAFE UI CALL
                    InvestorSendGuard.requestByInvestor(
                        this@ProjectPackageDisplayActivity,
                        isDuplicate
                    )

                    if (response.isSuccessful && !isDuplicate) {
                        findViewById<Button>(R.id.btnSend).isEnabled = false
                        finish()
                    }
                }
            }
        })
    }


}
