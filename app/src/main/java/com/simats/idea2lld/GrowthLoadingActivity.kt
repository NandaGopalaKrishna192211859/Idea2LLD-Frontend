package com.simats.idea2lld

import android.content.Intent
import android.os.*
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GrowthLoadingActivity : AppCompatActivity() {

    private lateinit var imgJug: ImageView

    private var pid: Int = -1
    private var imageUrl: String = ""

    private lateinit var imgDrop: ImageView
    private lateinit var btnCheck: Button

    private lateinit var dot1: ImageView
    private lateinit var dot2: ImageView
    private lateinit var dot3: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_growth_loading)

        imgJug = findViewById(R.id.imgJug)
        imgDrop = findViewById(R.id.imgDrop)
        btnCheck = findViewById(R.id.btnCheck)

        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)

        startDotAnimation()


        btnCheck.visibility = View.GONE

        pid = intent.getIntExtra("PID", -1)
        val modText = intent.getStringExtra("MOD_TEXT") ?: ""

        if (pid == -1) {
            finish()
            return
        }

        callModifyApi(modText)


        // ⏱ 1 second idle
        // ⏱ 1 sec idle → rotate jug
        Handler(Looper.getMainLooper()).postDelayed({
            startJugPouring()
        }, 1000)

// ⏱ After jug finishes (1.5s) → start droplets
        Handler(Looper.getMainLooper()).postDelayed({
            startDroplets()
        }, 2500) // 1s idle + 1.5s rotation


        // ⏱ After 8 seconds → show button
        Handler(Looper.getMainLooper()).postDelayed({

            btnCheck.visibility = View.VISIBLE
            btnCheck.alpha = 0f
            btnCheck.animate().alpha(1f).setDuration(500).start()

            rotateJugBackAfterButton()

            findViewById<View>(R.id.loadingTextContainer).visibility = View.GONE


        }, 8000)


        btnCheck.setOnClickListener {

            // Safety: if API is slow, still allow navigation
            val intent = Intent(this, GeneratedLLDActivity::class.java)
            intent.putExtra("PID", pid)
            intent.putExtra("IMAGE_URL", imageUrl)
            startActivity(intent)
            finish()
        }

    }

    private fun callModifyApi(text: String) {
        val token = SessionManager(this).getToken()

        val json = JSONObject()
        json.put("modification_text", text)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url("${ApiClient.MODIFY_PROJECT_URL}/$pid")
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                // No crash, user can still proceed
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string() ?: return
                imageUrl = JSONObject(res).optString("diagram_url", "")
            }
        })
    }

    private fun startDotAnimation() {

        val dots = listOf(dot1, dot2, dot3)
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val runnable = object : Runnable {
            override fun run() {

                // reset all dots
                dots.forEach { it.alpha = 0.3f }

                // highlight one dot
                dots[index].alpha = 1f

                index = (index + 1) % dots.size

                handler.postDelayed(this, 400)
            }
        }

        handler.post(runnable)
    }


    private fun startJugPouring() {
        imgJug.animate()
            .rotation(30f)
            .setDuration(1500)
            .start()
    }

    private fun rotateJugBackAfterButton() {
        imgJug.animate()
            .rotation(0f)
            .setDuration(800)
            .setStartDelay(300) // small pause after button shows
            .start()
    }



    private fun startDroplets() {

        imgJug.post {

            val plant = findViewById<ImageView>(R.id.imgPlant)
            val plantCenterY = plant.y + plant.height / 2f

            val dropStartY = imgDrop.y
            val distance = plantCenterY - dropStartY

            val dropInterval = 900L
            val dropDuration = 700L
            val totalDrops = 8

            repeat(totalDrops) { index ->

                Handler(Looper.getMainLooper()).postDelayed({

                    // 🔒 Cancel any pending animations (IMPORTANT)
                    imgDrop.animate().cancel()

                    imgDrop.visibility = View.VISIBLE
                    imgDrop.translationY = 0f

                    imgDrop.animate()
                        .translationY(distance)
                        .setDuration(dropDuration)
                        .withEndAction {

                            imgDrop.visibility = View.INVISIBLE
                            imgDrop.translationY = 0f

                            // ✅ ONLY last drop triggers jug reset
                                                   }
                        .start()

                }, index * dropInterval)
            }
        }
    }





}
