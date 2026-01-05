package com.simats.idea2lld

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AddProfileImageActivity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 101
    private val PERMISSION_CODE = 1001

    private var selectedImageUri: Uri? = null
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_profile_image)

        token = intent.getStringExtra("TOKEN") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!hasImagePermission()) {
            requestImagePermission()
        }

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val btnUpload = findViewById<MaterialButton>(R.id.btnUpload)
        val skipText = findViewById<TextView>(R.id.skipText)

        // Pick image (ONLY JPG / PNG)
        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/jpeg", "image/png", "image/jpg")
                )
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Upload image
        btnUpload.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImage()
        }

        // Skip
        skipText.setOnClickListener {
            goToInvestorHome()
        }
    }

    // ---------- Permission helpers ----------

    private fun hasImagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestImagePermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        }
    }

    // ---------- Image picker result ----------

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            findViewById<ImageView>(R.id.imgProfile).setImageURI(selectedImageUri)
        }
    }

    // ---------- Upload logic ----------

    private fun uploadImage() {

        val mimeType = contentResolver.getType(selectedImageUri!!)
        if (mimeType !in listOf("image/jpeg", "image/png", "image/jpg")) {
            Toast.makeText(
                this,
                "Only JPG or PNG images are allowed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val imageBytes = contentResolver.openInputStream(selectedImageUri!!)?.use {
            it.readBytes()
        } ?: run {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBody = imageBytes.toRequestBody(mimeType?.toMediaType())

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            // 🔥 MUST be "image" to match backend
            .addFormDataPart("image", "profile.png", imageBody)
            .build()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/auth/profile-image")
            .addHeader("Authorization", "Bearer $token")
            .post(multipartBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@AddProfileImageActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val responseText = response.body?.string() ?: ""

                runOnUiThread {
                    if (response.isSuccessful) {
                        goToInvestorHome()
                    } else {
                        val msg = try {
                            JSONObject(responseText)
                                .optString("error", "Upload failed")
                        } catch (e: Exception) {
                            "Upload failed"
                        }

                        Toast.makeText(
                            this@AddProfileImageActivity,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // ---------- Navigation ----------

    private fun goToInvestorHome() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("ROLE", "INVESTOR")
        )
        finish()
    }
}
