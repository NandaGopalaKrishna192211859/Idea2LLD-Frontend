package com.simats.idea2lld

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView

    private lateinit var scrollMessages: ScrollView


    private var chatId = -1
    private var requestId = -1

    private val handler = Handler(Looper.getMainLooper())
    private val pollInterval = 3000L

    private var lastMessageCount = 0   // ✅ NEW


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        scrollMessages = findViewById(R.id.scrollMessages)


        messagesContainer = findViewById(R.id.messagesContainer)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        requestId = intent.getIntExtra("REQUEST_ID", -1)

        val title = intent.getStringExtra("CHAT_TITLE") ?: "Chat"
        findViewById<TextView>(R.id.tvHeaderTitle).text = title

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        getOrCreateChat()

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isNotEmpty() && chatId != -1) {
                sendMessage(msg)
                etMessage.setText("")
            }
        }
    }

    // ---------------- CHAT INIT ----------------

    private fun getOrCreateChat() {
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/chat/$requestId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                android.util.Log.d("CHAT_CREATE", body)

                val json = JSONObject(body)
                chatId = json.getJSONObject("chat").getInt("chat_id")

                android.util.Log.d("CHAT_ID", "chatId = $chatId")

                startPolling()
            }

        })
    }

    // ---------------- POLLING ----------------

    private fun startPolling() {
        android.util.Log.d("CHAT_POLL", "Polling started")
        handler.post(object : Runnable {
            override fun run() {
                fetchMessages()
                handler.postDelayed(this, pollInterval)
            }
        })
    }

    private fun fetchMessages() {
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/chat/messages/$chatId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val body = response.body?.string() ?: return
                android.util.Log.d("CHAT_API", body)

                val arr = JSONObject(body).getJSONArray("messages")

                runOnUiThread {
                    renderMessages(arr)
                }

            }

        })
    }

    // ---------------- SEND MESSAGE ----------------

    private fun sendMessage(message: String) {
        val token = SessionManager(this).getToken()
        val JSON = "application/json".toMediaType()

        val body = JSONObject().apply {
            put("chatId", chatId)
            put("message", message)
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/investors/chat/send")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        ApiClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {}
        })
    }

    // ---------------- UI ----------------

    private fun renderMessages(arr: JSONArray) {
        android.util.Log.d(
            "CHAT_UID_CHECK",
            "myUid=${SessionManager(this).getUid()}"
        )

        val myUid = SessionManager(this).getUid()

        for (i in lastMessageCount until arr.length()) {


            val o = arr.getJSONObject(i)
            android.util.Log.d(
                "CHAT_UID_CHECK",
                "senderUid=${o.getInt("sender_uid")}"
            )

            val isMe = o.getInt("sender_uid") == myUid
            val messageText = o.getString("message")
            val createdAt = o.optString("created_at", "")


            // ---------- ROW ----------
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowParams.topMargin = 12
            row.layoutParams = rowParams

            // ---------- BUBBLE ----------
            val bubble = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            val msgTv = TextView(this).apply {
                text = messageText
                textSize = 15f
                setPadding(28, 18, 28, 18)
                maxWidth = (resources.displayMetrics.widthPixels * 0.7).toInt()
                setTextColor(android.graphics.Color.parseColor("#111827"))
                setBackgroundResource(
                    if (isMe) R.drawable.bg_msg_me else R.drawable.bg_msg_other
                )
            }

            val timeTv = TextView(this).apply {
                text = formatTime(createdAt)
                textSize = 11f
                setTextColor(android.graphics.Color.parseColor("#6B7280"))
            }

            bubble.addView(msgTv)
            bubble.addView(timeTv)

            // ---------- SPACER ----------
            val spacer = TextView(this)
            val spacerParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            spacer.layoutParams = spacerParams

            // ---------- ALIGN ----------
            if (isMe) {
                row.addView(spacer)   // push right
                row.addView(bubble)
            } else {
                row.addView(bubble)
                row.addView(spacer)   // push left
            }

            messagesContainer.addView(row)
        }

        lastMessageCount = arr.length()

        scrollMessages.post {
            scrollMessages.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }


    private fun formatTime(dateTime: String): String {
        return try {
            val formats = arrayOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            )

            val date = formats.firstNotNullOf {
                try {
                    java.text.SimpleDateFormat(it, java.util.Locale.getDefault())
                        .parse(dateTime)
                } catch (e: Exception) {
                    null
                }
            }

            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                .format(date)

        } catch (e: Exception) {
            ""
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
