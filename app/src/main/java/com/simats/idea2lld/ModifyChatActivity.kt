package com.simats.idea2lld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ModifyChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var input: EditText
    private lateinit var btnSend: Button
    private lateinit var btnDone: Button

    private val chatList = mutableListOf<ChatMessage>()
    private val userMessages = mutableListOf<String>()
    private var pid = -1
    private var botIndex = 0

    private val botQuestions = listOf(
        "What additional features would you like to add?",
        "Do you want role-based access like admin or owner?",
        "Any changes in user authentication or security?",
        "Should we add push notifications or alerts?",
        "Do you want subscription or pricing plans?",
        "Any payment or refund logic changes?",
        "Do you need real-time tracking or live updates?",
        "Any performance or scalability concerns?",
        "Should we add analytics or reports?",
        "Any database changes required?",
        "Do you want third-party integrations?",
        "Any UI or UX improvements needed?",
        "Should users be able to save favourites?",
        "Any offline support required?",
        "Do you need chat or support system?",
        "Any admin dashboard requirements?",
        "Do you want audit logs or activity history?",
        "Any compliance or safety features?",
        "Should we add ratings and reviews?",
        "Any automation or background jobs needed?",
        "Do you want email or SMS notifications?",
        "Any search or filter improvements?",
        "Should we support multi-language?",
        "Any cloud or deployment preference?",
        "Do you want caching for faster access?",
        "Any API or integration constraints?",
        "Should we add AI-based recommendations?",
        "Any data privacy requirements?",
        "Do you want modular or microservice architecture?",
        "Anything else you would like to modify?"
    )


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_chat)

        pid = intent.getIntExtra("PID", -1)
        if (pid == -1) finish()

        recyclerView = findViewById(R.id.recyclerChat)
        input = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnDone = findViewById(R.id.btnDone)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChatAdapter(chatList)


        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // ---------- Chat ----------
        addBotMessage(botQuestions[0])

        btnSend.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addUserMessage(text)
            input.text.clear()

            if (isDone(text)) {
                callModifyApi()
            } else {
                botIndex = (botIndex + 1) % botQuestions.size
                addBotMessage(botQuestions[botIndex])
            }
        }

//        old
//        btnDone.setOnClickListener {
//            callModifyApi()
//        }

        btnDone.setOnClickListener {
            val i = Intent(this, GrowthLoadingActivity::class.java)
            i.putExtra("PID", pid)
            i.putExtra("MOD_TEXT", userMessages.joinToString("\n"))
            startActivity(i)
            finish()
        }

    }

    private fun addUserMessage(text: String) {
        chatList.add(ChatMessage(text, true))
        userMessages.add(text)
        recyclerView.adapter?.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)
    }

    private fun addBotMessage(text: String) {
        chatList.add(ChatMessage(text, false))
        recyclerView.adapter?.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)
    }

    private fun isDone(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("done") || t.contains("finish") || t.contains("that's it") || t == "no"
    }

    private fun callModifyApi() {
        val token = SessionManager(this).getToken()
        val url = "${ApiClient.MODIFY_PROJECT_URL}/$pid"

        val json = JSONObject()
        json.put("modification_text", userMessages.joinToString("\n"))

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { finish() }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string() ?: ""
                val img = JSONObject(res).optString("diagram_url", "")

                runOnUiThread {

                    val i = Intent(this@ModifyChatActivity, GeneratedLLDActivity::class.java)
                    i.putExtra("PID", pid)
                    i.putExtra("IMAGE_URL", img)
                    startActivity(i)
                    setResult(RESULT_OK)
                    finish()
                }
            }
        })
    }

    data class ChatMessage(val text: String, val isUser: Boolean)

    class ChatAdapter(private val items: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.Holder>() {

        class Holder(val tv: TextView) : RecyclerView.ViewHolder(tv)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val tv = TextView(parent.context)
            tv.setPadding(24, 16, 24, 16)
            return Holder(tv)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val msg = items[position]
            holder.tv.text = msg.text
            holder.tv.setBackgroundColor(
                if (msg.isUser) Color.parseColor("#1E88E5")
                else Color.parseColor("#E3F2FD")
            )
            holder.tv.setTextColor(if (msg.isUser) Color.WHITE else Color.BLACK)
            holder.tv.gravity = if (msg.isUser) Gravity.END else Gravity.START
        }

        override fun getItemCount() = items.size
    }
}
