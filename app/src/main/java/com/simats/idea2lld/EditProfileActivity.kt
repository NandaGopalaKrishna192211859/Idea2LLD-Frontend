package com.simats.idea2lld

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.ProfileImageLoader
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        session = SessionManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadExistingProfile()

        findViewById<Button>(R.id.btnEditImage).setOnClickListener {
            startActivity(
                Intent(this, AddProfileImageActivity::class.java)
                    .putExtra("TOKEN", session.getToken())
            )
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            updateProfile()
        }
    }

    // ---------- LOAD EXISTING DATA ----------

    private fun loadExistingProfile() {

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/auth/profile")
            .addHeader("Authorization", "Bearer ${session.getToken()}")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())

                val imagePath = json.optString("profile_image")

                runOnUiThread {
                    findViewById<EditText>(R.id.etName).setText(json.optString("name"))
                    findViewById<EditText>(R.id.etCompany).setText(json.optString("company_name"))
                    findViewById<EditText>(R.id.etPhone).setText(json.optString("phone"))
                    findViewById<EditText>(R.id.etMinInvestment).setText(json.optString("min_investment"))
                    findViewById<EditText>(R.id.etMaxInvestment).setText(json.optString("max_investment"))
                    findViewById<EditText>(R.id.etPastInvestments).setText(json.optString("past_investments"))
                    findViewById<EditText>(R.id.etBio).setText(json.optString("bio"))

                    ProfileImageLoader.load(
                        findViewById(R.id.imgProfile),
                        imagePath
                    )
                    selectChip(R.id.chipGroupInvestmentType, json.optString("investment_type"))
                    selectMultiChips(R.id.chipGroupIndustries, json.optString("preferred_categories"))
                }
            }
        })
    }

    // ---------- UPDATE PROFILE ----------

    private fun updateProfile() {

        val body = JSONObject().apply {
            put("name", findViewById<EditText>(R.id.etName).text.toString())
            put("company_name", findViewById<EditText>(R.id.etCompany).text.toString())
            put("phone", findViewById<EditText>(R.id.etPhone).text.toString())
            put("min_investment", findViewById<EditText>(R.id.etMinInvestment).text.toString())
            put("max_investment", findViewById<EditText>(R.id.etMaxInvestment).text.toString())
            put("past_investments", findViewById<EditText>(R.id.etPastInvestments).text.toString())
            put("bio", findViewById<EditText>(R.id.etBio).text.toString())
            put("investment_type", getSelectedChip(R.id.chipGroupInvestmentType))
            put("preferred_categories", getSelectedChips(R.id.chipGroupIndustries))
        }

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/auth/profile/update")
            .addHeader("Authorization", "Bearer ${session.getToken()}")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity,
                        "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        })
    }

    // ---------- HELPERS ----------

    private fun selectChip(groupId: Int, value: String) {
        val group = findViewById<ChipGroup>(groupId)
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.text.toString() == value) chip.isChecked = true
        }
    }

    private fun selectMultiChips(groupId: Int, values: String) {
        val set = values.split(",").map { it.trim() }
        val group = findViewById<ChipGroup>(groupId)
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (set.contains(chip.text.toString())) chip.isChecked = true
        }
    }

    private fun getSelectedChip(groupId: Int): String {
        val group = findViewById<ChipGroup>(groupId)
        val chip = findViewById<Chip>(group.checkedChipId)
        return chip?.text?.toString() ?: ""
    }

    private fun getSelectedChips(groupId: Int): String {
        val group = findViewById<ChipGroup>(groupId)
        return group.checkedChipIds.joinToString(",") {
            findViewById<Chip>(it).text.toString()
        }
    }
}
