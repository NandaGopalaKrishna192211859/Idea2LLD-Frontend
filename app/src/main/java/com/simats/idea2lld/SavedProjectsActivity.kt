package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.DeleteProjectHelper
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SavedProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val projectList = mutableListOf<SavedProject>()
    private lateinit var adapter: SavedProjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_projects)

        recyclerView = findViewById(R.id.recyclerSaved)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        adapter = SavedProjectsAdapter(projectList) { project ->

            if (project.imagePath.isBlank()) {
                Toast.makeText(
                    this,
                    "Diagram not available for this project",
                    Toast.LENGTH_SHORT
                ).show()
                return@SavedProjectsAdapter
            }

            val intent = Intent(this, GeneratedLLDActivity::class.java)
            intent.putExtra("IMAGE_URL", project.imagePath)
            intent.putExtra("PID", project.pid)
            startActivity(intent)
        }


        recyclerView.adapter = adapter

        loadSavedProjects()
    }

    private fun loadSavedProjects() {
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url(ApiClient.GET_SAVED_PROJECTS_URL)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("SAVED_PROJECTS", "Failed to load saved projects", e)
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: ""

                Log.d("SAVED_RESPONSE", body)

                runOnUiThread {
                    try {
                        val json = JSONObject(body)

                        if (!json.has("saved_projects")) {
                            findViewById<TextView>(R.id.tvEmptySaved).visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            return@runOnUiThread
                        }

                        val arr = json.getJSONArray("saved_projects")


                        projectList.clear()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)

                            projectList.add(
                                SavedProject(
                                    pid = obj.getInt("pid"),
                                    title = obj.optString("project_title", "Untitled Project"),
                                    version = obj.optString("modification_version", "v1.0"),
                                    imagePath = obj.optString("image_url", ""),
                                    createdAt = obj.optString("created_at", ""),
                                    category = obj.optString("category", "")
                                )
                            )
                        }

                        findViewById<TextView>(R.id.tvEmptySaved).visibility =
                            if (projectList.isEmpty()) View.VISIBLE else View.GONE

                        recyclerView.visibility =
                            if (projectList.isEmpty()) View.GONE else View.VISIBLE

                        adapter.notifyDataSetChanged()

                    } catch (e: Exception) {
                        Log.e("SAVED_PARSE", "Parsing failed", e)
                        Toast.makeText(
                            this@SavedProjectsActivity,
                            "Failed to load saved projects",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        })
    }

    fun showRenameDialog(pid: Int) {
        val intent = Intent(this, GeneratedLLDActivity::class.java)
        intent.putExtra("PID", pid)
        intent.putExtra("RENAME_ONLY", true)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            loadSavedProjects()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedProjects()
    }



    /* =====================================================
       INNER DATA CLASS
    ===================================================== */
    data class SavedProject(
        val pid: Int,
        val title: String,
        val version: String,
        val imagePath: String,
        val createdAt: String,
        val category: String
    )

    /* =====================================================
       INNER ADAPTER CLASS
    ===================================================== */
    class SavedProjectsAdapter(
        private val items: List<SavedProject>,
        private val onClick: (SavedProject) -> Unit
    ) : RecyclerView.Adapter<SavedProjectsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvProjectTitle)
            val date: TextView = view.findViewById(R.id.tvProjectDate)
            val version: TextView = view.findViewById(R.id.tvProjectVersion)
            val btnRename: View = view.findViewById(R.id.btnRename)
            val btnModify: View = view.findViewById(R.id.btnModify)
            val btnDelete: View = view.findViewById(R.id.btnDelete)
            val btnPublish: View = view.findViewById(R.id.btnPublish)

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_saved_project, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val project = items[position]
            holder.title.text = project.title
            holder.date.text = project.createdAt
            holder.version.text = project.version

            // 🔹 Card click → open image (existing behavior)
            holder.itemView.setOnClickListener {
                onClick(project)
            }

            // 🔹 Rename button → rename only
            holder.btnRename.setOnClickListener {
                val ctx = it.context
                if (ctx is SavedProjectsActivity) {
                    ctx.showRenameDialog(project.pid)
                }
            }

            holder.btnPublish.setOnClickListener {
                val ctx = it.context
                val intent = Intent(ctx, InvestorListActivity::class.java)
                intent.putExtra("CATEGORY", project.category)
                intent.putExtra("PID", project.pid)
                intent.putExtra("IMAGE_URL", project.imagePath)
                Log.d("PUBLISH_CATEGORY", project.category)

                ctx.startActivity(intent)
            }

            holder.btnModify.setOnClickListener {
                val ctx = it.context
                val intent = Intent(ctx, ModifyChatActivity::class.java)
                intent.putExtra("PID", project.pid)
                ctx.startActivity(intent)
            }

            holder.btnDelete.setOnClickListener {
                val ctx = it.context
                if (ctx is SavedProjectsActivity) {
                    DeleteProjectHelper.confirmAndDelete(
                        context = ctx,
                        pid = project.pid,
                        type = DeleteProjectHelper.ProjectType.SAVED
                    ) {
                        ctx.loadSavedProjects()   // 🔄 reload list after delete
                    }
                }
            }


        }


        override fun getItemCount(): Int = items.size


    }


}
