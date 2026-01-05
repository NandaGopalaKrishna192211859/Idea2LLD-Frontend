package com.simats.idea2lld.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ButtonUpdationAdapter(private val context: Context) {

    private val JSON = "application/json".toMediaType()

    /**
     * ✅ SINGLE METHOD
     * accepted / rejected / delete
     */
    fun updateRequest(
        requestId: Int,
        action: String,
        onSuccess: () -> Unit
    ) {

        val token = SessionManager(context).getToken()
        if (token.isNullOrBlank()) return

        val body = JSONObject().apply {
            put("requestId", requestId)
            put("action", action)
        }.toString().toRequestBody(JSON)

        val url = when (action) {
            "accepted", "rejected" ->
                "${ApiClient.BASE_URL}/investors/requests/update-status"

            "delete" ->
                "${ApiClient.BASE_URL}/investors/requests/delete"

            else -> return
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                Handler(Looper.getMainLooper()).post {
                    onSuccess()
                }
            }
        })
    }
}
