package com.simats.idea2lld

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.InvestorSendGuard


class ProjectPackagePreviewActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvFounderName: TextView
    private lateinit var tvFounderEmail: TextView
    private lateinit var tvFounderPhone: TextView
    private lateinit var ivProjectImage: ImageView
    private lateinit var tvSummary: TextView
    private lateinit var etBusinessNote: EditText
    private lateinit var btnSend: Button
    private lateinit var btnEditPackage: Button

    private var isHub: Boolean = false


    private var pid: Int = -1
    private var imageUrl: String = ""

    private lateinit var investorIds: ArrayList<Int>

    private var devStatus: String = "ongoing"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_package_preview)

        pid = intent.getIntExtra("PID", -1)

        investorIds =
            intent.getIntegerArrayListExtra("INVESTOR_IDS") ?: arrayListOf()

        isHub = intent.getBooleanExtra("IS_HUB", false)
        Log.d("HUB_CHECK", "Preview screen isHub = $isHub")


        if (pid == -1) {
            finish()
            return
        }

        tvTitle = findViewById(R.id.tvProjectTitle)
        tvFounderName = findViewById(R.id.tvFounderName)
        tvFounderEmail = findViewById(R.id.tvFounderEmail)
        tvFounderPhone = findViewById(R.id.tvFounderPhone)
        ivProjectImage = findViewById(R.id.ivProjectImage)
        tvSummary = findViewById(R.id.tvSummary)
        etBusinessNote = findViewById(R.id.etBusinessNote)
        btnSend = findViewById(R.id.btnSend)
        btnEditPackage = findViewById(R.id.btnEditPackage)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Business note read-only initially
        etBusinessNote.isEnabled = false
        etBusinessNote.isFocusable = false

        btnEditPackage.setOnClickListener {
            etBusinessNote.isEnabled = true
            etBusinessNote.isFocusableInTouchMode = true
            etBusinessNote.requestFocus()
        }

        val rgDevStatus = findViewById<RadioGroup>(R.id.rgDevStatus)

        rgDevStatus.setOnCheckedChangeListener { _, checkedId ->
            devStatus = when (checkedId) {
                R.id.rbCompleted -> "completed"
                else -> "ongoing"
            }
            Log.d("DEV_STATUS", "Selected = $devStatus")
        }


        btnSend.setOnClickListener {
            sendProjectToInvestors()
        }



        imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""

        if (imageUrl.isNotBlank()) {
            val finalUrl = ApiClient.IMAGE_BASE_URL + imageUrl
            Log.d("PREVIEW_IMAGE_URL", "Final image URL = ${ApiClient.IMAGE_BASE_URL + imageUrl}")

            Glide.with(this)
                .load(finalUrl)
                .placeholder(R.drawable.placeholder_project)
                .error(R.drawable.placeholder_project)
                .into(ivProjectImage)
        } else {
            ivProjectImage.setImageResource(R.drawable.placeholder_project)
        }


        ivProjectImage.setOnClickListener {

            if (imageUrl.isBlank()) return@setOnClickListener

            val intent = Intent(this, GeneratedLLDActivity::class.java)
            intent.putExtra("IMAGE_URL", imageUrl)
            intent.putExtra("PID", pid)
            startActivity(intent)
        }



        loadPackage()
    }

    private fun loadPackage() {
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.GET_PROJECT_PACKAGE_URL}/$pid")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)

                val project = json.getJSONObject("project")
                val founder = json.getJSONObject("founder")
                val contents = json.getJSONObject("contents")

                val rawSummary = contents.getString("summary")
                    .replace("**", "")
                    .replace("__", "")
                    .trim()

                runOnUiThread {
                    tvTitle.text = project.getString("title")
                    tvFounderName.text = "Name: ${founder.getString("name")}"
                    tvFounderEmail.text = "Email: ${founder.getString("email")}"
                    tvFounderPhone.text = "Phone: ${founder.getString("phone")}"

                    tvSummary.text = formatSummary(rawSummary)
                }
            }
        })
    }

    private fun formatSummary(text: String): SpannableString {
        val spannable = SpannableString(text)

        listOf("Modules:", "Data Flow:", "Features:").forEach { heading ->
            val index = text.indexOf(heading)
            if (index >= 0) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + heading.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    private fun sendProjectToInvestors() {

        val token = SessionManager(this).getToken()

        if (!isHub && investorIds.isEmpty()) {
            Toast.makeText(
                this,
                "Select Investor Hub or at least one investor",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        val investorArray = org.json.JSONArray()
        investorIds.forEach { investorArray.put(it) }

        val json = JSONObject().apply {
            put("projectId", pid)
            put("investorIds", investorArray)
            put("businessNote", etBusinessNote.text.toString().trim())
            put("is_hub", isHub)
            put("dev_status", devStatus)
        }



        val body = RequestBody.create(
            "application/json".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url(ApiClient.SEND_PROJECT_PACKAGE_URL)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()


        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ProjectPackagePreviewActivity,
                        "Failed to send request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("SEND_RESPONSE", "code=${response.code} body=$body")

                runOnUiThread {
                    if (response.isSuccessful && body != null) {

                        val json = JSONObject(body)
                        val sentCount = json.optInt("sent_to_investors", 0)

                        InvestorSendGuard.showResult(
                            this@ProjectPackagePreviewActivity,
                            investorIds.size,
                            sentCount
                        )

                        if (sentCount > 0) {
                            startActivity(
                                Intent(
                                    this@ProjectPackagePreviewActivity,
                                    SendSuccessActivity::class.java
                                )
                            )
                            finish()
                        }

                    } else {
                        Toast.makeText(
                            this@ProjectPackagePreviewActivity,
                            "Send failed (${response.code})",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

        })
    }

}
