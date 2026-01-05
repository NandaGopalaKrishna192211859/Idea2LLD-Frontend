package com.simats.idea2lld.utils

import android.content.Context
import com.simats.idea2lld.network.ApiClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import com.simats.idea2lld.utils.SessionManager


class CancelRequestManager(private val context: Context) {

    private val token = SessionManager(context).getToken()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * ❌ Cancel SINGLE direct investor request
     * (ViewInvestorsActivity)
     */
    fun cancelSingleDirect(
        requestId: Int,
        pid: Int,
        investorUid: Int,
        onSuccess: () -> Unit
    ) {

        val body = JSONObject().apply {
            put("requestId", requestId)
            put("pid", pid)
            put("investorUid", investorUid)
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/cancel/direct/single")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val resBody = response.body?.string() ?: return
                val json = JSONObject(resBody)

                if (json.optBoolean("success")) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onSuccess()
                    }
                }
            }
        })
    }



    /**
     * ❌ Cancel ALL direct requests for a project
     * (YourDirectRequestsActivity)
     */
    fun cancelAllDirect(
        pid: Int,
        onSuccess: () -> Unit
    ) {

        val body = JSONObject().apply {
            put("pid", pid)
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/cancel/direct/all")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                }
            }
        })
    }


    /**
     * ❌ Cancel INVESTOR'S OWN request
     * (Investor Dashboard → Your Requests)
     */
    fun cancelInvestorMyRequest(
        requestId: Int,
        onSuccess: () -> Unit
    ) {

        val body = JSONObject().apply {
            put("requestId", requestId)
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/requests/cancel")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val res = response.body?.string() ?: return
                val json = JSONObject(res)

                if (json.optBoolean("success")) {
                    android.os.Handler(
                        android.os.Looper.getMainLooper()
                    ).post {
                        onSuccess()
                    }
                }
            }
        })
    }


    /**
     * ❌ Cancel HUB project (Founder side)
     * Deletes ALL hub requests for a project
     * (YourHubRequestsActivity)
     */
    fun cancelHubProject(
        pid: Int,
        onSuccess: () -> Unit
    ) {

        val body = JSONObject().apply {
            put("pid", pid)
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/cancel/hub")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val resBody = response.body?.string() ?: return
                val json = JSONObject(resBody)

                if (json.optBoolean("success")) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onSuccess()
                    }
                }
            }
        })
    }

}
