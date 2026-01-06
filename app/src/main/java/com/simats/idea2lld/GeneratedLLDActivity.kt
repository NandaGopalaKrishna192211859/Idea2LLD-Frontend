package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.simats.idea2lld.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import com.simats.idea2lld.utils.NotificationHelper

class GeneratedLLDActivity : AppCompatActivity() {

    private var notificationSent = false
    private val KEY_NOTIFICATION_SENT = "notification_sent"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationSent = savedInstanceState?.getBoolean(KEY_NOTIFICATION_SENT) ?: false

        setContentView(R.layout.activity_generated_lldactivity)

        val imageView = findViewById<PhotoView>(R.id.ivDiagram)
        val btnDone = findViewById<Button>(R.id.btnDone)



        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val renameOnly = intent.getBooleanExtra("RENAME_ONLY", false)
        val pid = intent.getIntExtra("PID", -1)

        if (pid == -1) {
            finish()
            return
        }


        if (renameOnly) {
            showNameDialog(pid)
        } else {
            val finalUrl = ApiClient.IMAGE_BASE_URL + imageUrl
            Log.d("IMAGE_URL", finalUrl)
            Glide.with(this)
                .load(finalUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                .dontTransform()
                .into(imageView)

            if (!notificationSent) {
                NotificationHelper.push(
                    this,
                    "Image generated successfully",
                    "Your Low-Level Design is ready"
                )
                notificationSent = true
            }

        }


        btnDone.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_NOTIFICATION_SENT, notificationSent)
    }
    fun showNameDialog(pid: Int) {
        val input = EditText(this)
        input.hint = "Project name"

        AlertDialog.Builder(this)
            .setTitle("Save Project")
            .setMessage("Give a name to your project")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                saveProjectName(pid, name)
            }
            .setNegativeButton("Skip") { _, _ ->
                goHome()
            }
            .show()
    }

    private fun saveProjectName(pid: Int, name: String) {

        if (name.isBlank()) {
            return
        }

        val token = com.simats.idea2lld.utils.SessionManager(this).getToken()
        val url = "${ApiClient.RENAME_PROJECT_URL}/$pid"

        val json = org.json.JSONObject()
        json.put("new_title", name)

        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = okhttp3.Request.Builder()
            .url(url)
            .put(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        okhttp3.OkHttpClient().newCall(request)
            .enqueue(object : okhttp3.Callback {

                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    runOnUiThread {
                        android.widget.Toast.makeText(
                            this@GeneratedLLDActivity,
                            "Failed to save project name",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            setResult(RESULT_OK)
                            finish()
                        }
                        else {
                            android.widget.Toast.makeText(
                                this@GeneratedLLDActivity,
                                "Rename failed",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }


    private fun goHome() {
        startActivity(Intent(this, FounderDashboardActivity::class.java))
        finish()
    }


}
