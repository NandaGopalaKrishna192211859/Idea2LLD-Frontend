package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.idea2lld.network.ApiClient
import com.simats.idea2lld.utils.DeleteProjectHelper
import com.simats.idea2lld.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DraftProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val draftList = mutableListOf<DraftProject>()
    private lateinit var adapter: DraftAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_projects)

        recyclerView = findViewById(R.id.recyclerDrafts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DraftAdapter(draftList) { draft ->
            val intent = Intent(this, CreateLLDActivity::class.java)
            intent.putExtra("RESUME_PID", draft.pid)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        loadDrafts()


    }


    private fun loadDrafts() {
        val token = SessionManager(this).getToken()

        val request = Request.Builder()
            .url(ApiClient.GET_DRAFTS_URL)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DRAFTS", "Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val arr = json.getJSONArray("drafts")

                runOnUiThread {
                    draftList.clear()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        draftList.add(
                            DraftProject(
                                pid = o.getInt("pid"),
                                version = o.optString("modification_version", "v1.0"),
                                title = o.optString("project_title").ifBlank { "Draft #${o.getInt("pid")}" },
                                updatedAt = o.optString("updated_at", "")
                            )
                        )


                    }
                    adapter.notifyDataSetChanged()
                }
            }

        })
    }

    data class DraftProject(
        val pid: Int,
        val title: String,
        val version: String,
        val updatedAt: String
    )

    fun showRenameDialog(pid: Int) {
        val intent = Intent(this, GeneratedLLDActivity::class.java)
        intent.putExtra("PID", pid)
        intent.putExtra("RENAME_ONLY", true)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadDrafts()
    }



    class DraftAdapter(
        private val items: List<DraftProject>,
        private val onClick: (DraftProject) -> Unit
    ) : RecyclerView.Adapter<DraftAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvDraftTitle)
            val date: TextView = v.findViewById(R.id.tvDraftDate)
            val btnRename: View = v.findViewById(R.id.btnRename)
            val btnContinue: View = v.findViewById(R.id.btnContinue)
            val btnDelete: View = v.findViewById(R.id.btnDelete)
            val version: TextView = v.findViewById(R.id.tvDraftVersion)
        }



        override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
            val view = LayoutInflater.from(p.context)
                .inflate(R.layout.item_draft_project, p, false)
            return VH(view)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val d = items[i]
            h.title.text = d.title
            h.date.text = d.updatedAt
            h.version.text = d.version
            // 🔹 Continue button → resume draft
            h.btnContinue.setOnClickListener {
                onClick(d)
            }

// 🔹 Card click → do nothing
            h.itemView.setOnClickListener { }

// 🔹 Rename button → rename only
            h.btnRename.setOnClickListener {
                val ctx = it.context
                if (ctx is DraftProjectsActivity) {
                    ctx.showRenameDialog(d.pid)
                }
            }

            h.btnDelete.setOnClickListener {
                val ctx = it.context
                if (ctx is DraftProjectsActivity) {
                    DeleteProjectHelper.confirmAndDelete(
                        context = ctx,
                        pid = d.pid,
                        type = DeleteProjectHelper.ProjectType.DRAFT
                    ) {
                        ctx.loadDrafts()   // 🔄 reload after delete
                    }
                }
            }



        }

        override fun getItemCount() = items.size
    }




}
