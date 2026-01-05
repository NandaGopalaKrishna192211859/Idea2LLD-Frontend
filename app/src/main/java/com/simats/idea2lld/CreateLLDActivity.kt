package com.simats.idea2lld

import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class CreateLLDActivity : AppCompatActivity() {

    // ---------------- UI ----------------
    private lateinit var startLayout: LinearLayout
    private lateinit var btnStartNow: Button

    private lateinit var questionLayout: LinearLayout
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var etAnswer: EditText
    private lateinit var spCategory: Spinner
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private var isRestoringDraft = false


    // ---------------- STATE ----------------
    private var currentIndex = 0

    private var projectId: Int = -1

    private val answers = mutableMapOf<Int, String>()
    private var selectedCategory: String? = null

    // ---------------- QUESTIONS ----------------
    private val generalQuestions = listOf(
        "What is the main idea or purpose of your app?",
        "Who are the main people who will use this app?",
        "What problem does your app solve for the users?",
        "What are the most important things you want your app to do?",
        "How do you want users to feel or what outcome should they get after using your app?"
    )

    private val extraQuestion =
        "What additional ideas, special features, or extra points do you want to include?"

    private val finalQuestions = mutableListOf<String>()
    private val categoryList = mutableListOf<String>()
    private val categoryQuestionList = mutableListOf<String>()

    // ---------------- LIFECYCLE ----------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lld)

        bindViews()

        val resumePid = intent.getIntExtra("RESUME_PID", -1)
        val startFlow = intent.getBooleanExtra("START_FLOW", false)

        when {
            // ✅ RESUME DRAFT FLOW
            resumePid != -1 -> {
                startLayout.visibility = View.GONE
                questionLayout.visibility = View.VISIBLE
                projectId = resumePid
                initFromExistingDraft()
            }

            // ✅ CREATE NEW FLOW
            startFlow -> {
                startLayout.visibility = View.GONE
                createDraft()
            }

            // ✅ DEFAULT ENTRY (show start screen)
            else -> {
                showStart()
            }
        }

        btnStartNow.setOnClickListener {
            startActivity(Intent(this, PlaneSplashActivity::class.java))
            finish()
        }

        btnNext.setOnClickListener { next() }
        btnBack.setOnClickListener { back() }

        handleSystemBack()
    }


    // ---------------- VIEW BINDING ----------------
    private fun bindViews() {
        startLayout = findViewById(R.id.btnGetStarted)
        btnStartNow = findViewById(R.id.btnStartNow)

        questionLayout = findViewById(R.id.questionLayout)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        etAnswer = findViewById(R.id.etAnswer)
        spCategory = findViewById(R.id.spCategory)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)
    }

    // ---------------- START ----------------
    private fun showStart() {
        startLayout.visibility = View.VISIBLE
        questionLayout.visibility = View.GONE
    }

    // ---------------- CREATE DRAFT ----------------
    private fun createDraft() {

        // 🔒 Draft already exists — do NOT recreate
        if (projectId != -1) return

        startLayout.visibility = View.GONE

        val body = RequestBody.create(
            "application/json".toMediaType(),
            JSONObject().toString() // empty body is fine for draft creation
        )

        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url(ApiClient.CREATE_DRAFT_URL)
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DRAFT_CREATE", "Failed to create draft", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                projectId = json.getInt("pid")

                Log.d("PID_CHECK", "Draft initialized with pid=$projectId")

                runOnUiThread {
                    buildInitialQuestions()
                    questionLayout.visibility = View.VISIBLE
                    render()
                }
            }
        })
    }


    // ---------------- QUESTION BUILD ----------------
    private fun buildInitialQuestions() {
        finalQuestions.clear()
        finalQuestions.addAll(generalQuestions)   // Q1–Q5
        finalQuestions.add("Please choose a category") // Q6
        finalQuestions.add(extraQuestion)         // Q18 placeholder
    }

    // ---------------- RENDER ----------------
    private fun render() {
        tvQuestionNumber.text = "Question ${currentIndex + 1} of 18"
        tvQuestionText.text = finalQuestions[currentIndex]

        if (currentIndex == 5) {
            spCategory.visibility = View.VISIBLE
            etAnswer.visibility = View.GONE

            // ✅ Load categories only once
            if (spCategory.adapter == null) {
                loadCategories()
            }
        } else {
            spCategory.visibility = View.GONE
            etAnswer.visibility = View.VISIBLE
        }

        etAnswer.setText(answers[currentIndex] ?: "")
        progressBar.progress = ((currentIndex + 1) * 100) / 18
        isRestoringDraft = false
    }

    // ---------------- LOAD CATEGORIES ----------------
    private fun loadCategories() {
        val request = Request.Builder()
            .url(ApiClient.GET_CATEGORIES_URL)
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val arr = JSONObject(response.body!!.string()).getJSONArray("categories")
                categoryList.clear()
                categoryList.add("Select a category")

                for (i in 0 until arr.length()) {
                    categoryList.add(arr.getJSONObject(i).getString("category_name"))
                }

                runOnUiThread {
                    val adapter = CategorySpinnerAdapter(this@CreateLLDActivity, categoryList)
                    spCategory.adapter = adapter
                    // ✅ Restore previously selected category (draft resume)
                    if (selectedCategory != null) {
                        val index = categoryList.indexOf(selectedCategory)
                        if (index >= 0) {
                            spCategory.setSelection(index, false)
                        }
                    }

                    spCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View?, pos: Int, id: Long
                            ) {
                                if (pos == 0) return

                                // ✅ IGNORE spinner callback while restoring draft
                                if (isRestoringDraft) return

                                selectedCategory = categoryList[pos]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                }
            }
        })
    }

    // ---------------- LOAD Q7–Q17 ----------------
    private fun loadCategoryQuestions(category: String) {

        val request = Request.Builder()
            .url("${ApiClient.GET_CATEGORY_QUESTIONS_URL}/$category")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val qObj = JSONObject(response.body!!.string()).getJSONObject("questions")
                categoryQuestionList.clear()
                for (i in 7..17) categoryQuestionList.add(qObj.getString("q$i"))

                runOnUiThread {
                    finalQuestions.addAll(6, categoryQuestionList)
                    currentIndex++
                    render()
                }
            }
        })
    }

    // ---------------- NAVIGATION ----------------
    private fun next() {

        saveCurrentAnswer()
        if (currentIndex == 5) {
            if (selectedCategory == null) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return
            }
            answers[currentIndex] = selectedCategory!!
            loadCategoryQuestions(selectedCategory!!)
            return
        }

        answers[currentIndex] = etAnswer.text.toString()

        if (currentIndex == 17) {
            showSubmitDialog()
            return
        }

        currentIndex++
        render()
    }

    private fun back() {

        saveCurrentAnswer()

        if (currentIndex == 0) finish()
        else {
            currentIndex--
            render()
        }
    }

    private fun saveCurrentAnswer() {
        if (projectId == -1) return

        val token = SessionManager(this).getToken()
        val client = OkHttpClient()

        /* =================================================
           Q1–Q6 → UPDATE BASE DRAFT (pid present)
        ================================================= */
        if (currentIndex in 0..5) {

            if (currentIndex in 0..4) {
                answers[currentIndex] = etAnswer.text.toString()
            }

            val bodyJson = JSONObject().apply {
                put("pid", projectId)
                put("q1", answers[0] ?: "")
                put("q2", answers[1] ?: "")
                put("q3", answers[2] ?: "")
                put("q4", answers[3] ?: "")
                put("q5", answers[4] ?: "")
                put("category_id", selectedCategory ?: "")
            }

            val body = RequestBody.create(
                "application/json".toMediaType(),
                bodyJson.toString()
            )

            val request = Request.Builder()
                .url(ApiClient.CREATE_DRAFT_URL) // same API, UPDATE mode
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("AUTO_SAVE", "Failed saving Q1–Q6", e)
                }
                override fun onResponse(call: Call, response: Response) {}
            })

            return
        }

        /* =================================================
           Q7–Q17 → CATEGORY ANSWERS
        ================================================= */
        if (currentIndex in 6..16) {
            val bodyJson = JSONObject()
            bodyJson.put("q${currentIndex + 1}", etAnswer.text.toString())

            val body = RequestBody.create(
                "application/json".toMediaType(),
                bodyJson.toString()
            )

            val request = Request.Builder()
                .url("${ApiClient.SAVE_CATEGORY_URL}/$projectId")
                .put(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {}
            })
        }

        /* =================================================
           Q18 → EXTRA FEATURES
        ================================================= */
        if (currentIndex == 17) {
            val bodyJson = JSONObject()
            bodyJson.put("q18", etAnswer.text.toString())

            val body = RequestBody.create(
                "application/json".toMediaType(),
                bodyJson.toString()
            )

            val request = Request.Builder()
                .url("${ApiClient.SAVE_FEATURES_URL}/$projectId")
                .put(body)
                .addHeader("Authorization", "Bearer $token")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {}
            })
        }
    }



    // ---------------- SUBMIT ----------------
    private fun showSubmitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ready to Generate LLD?")
            .setMessage("Do you want to generate the Low-Level Design now?")
            .setPositiveButton("Generate") { _, _ ->
                generateLLD()
            }
            .setNegativeButton("Back") { _, _ ->
                currentIndex = 17
                render()
            }
            .show()
    }


    private fun handleSystemBack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@CreateLLDActivity)
                    .setTitle("Exit?")
                    .setMessage("Are you sure you want to leave?")
                    .setPositiveButton("Yes") { _, _ ->
                        answers[currentIndex] = etAnswer.text.toString()
                        saveCurrentAnswer()
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })
    }

    private fun generateLLD() {
        if (projectId == -1) return

        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/projects/dgenerate-lld/$projectId")
            .post(RequestBody.create("application/json".toMediaType(), ""))
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateLLDActivity, "Generation failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val diagramUrl = json.getString("diagram_url")

                runOnUiThread {
                    val intent = Intent(this@CreateLLDActivity, GeneratedLLDActivity::class.java)
                    intent.putExtra("IMAGE_URL", diagramUrl)
                    intent.putExtra("PID", projectId)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun initFromExistingDraft() {

        // 1️⃣ Build full question list (sync)
        finalQuestions.clear()
        finalQuestions.addAll(generalQuestions)        // Q1–Q5
        finalQuestions.add("Please choose a category") // Q6
        for (i in 7..17) finalQuestions.add("Question $i")
        finalQuestions.add(extraQuestion)              // Q18

        // 2️⃣ Load saved answers from DB
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url("${ApiClient.GET_PROJECT_BY_ID_URL}/$projectId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("RESUME", "Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val p = JSONObject(response.body!!.string())
                    .getJSONObject("pending_questions")

                answers.clear()

                for (i in 1..18) {
                    answers[i - 1] = p.optString("q$i", "")
                }

                selectedCategory = p.optString("category_id", null)

                // 3️⃣ Find last answered question
                var resumeIndex = 0
                for (i in 0..17) {
                    if (!answers[i].isNullOrBlank()) {
                        resumeIndex = i
                    }
                }

                runOnUiThread {
                    isRestoringDraft = true
                    currentIndex = resumeIndex
                    render()
                }
            }
        })
    }


}
