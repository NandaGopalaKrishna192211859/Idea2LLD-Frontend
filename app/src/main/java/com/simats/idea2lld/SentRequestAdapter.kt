package com.simats.idea2lld

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SentRequest(
    val id: Int,
    val title: String,
    val category: String,
    val status: String
)

class SentRequestAdapter(
    private val list: List<SentRequest>,
    private val onCancel: (Int) -> Unit
) : RecyclerView.Adapter<SentRequestAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val category: TextView = v.findViewById(R.id.tvCategory)
        val btnCancel: TextView = v.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_request, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = list[position]

        holder.title.text = r.title
        holder.category.text = r.category

        // Show cancel only for pending
        holder.btnCancel.visibility =
            if (r.status == "pending") View.VISIBLE else View.GONE

        holder.btnCancel.setOnClickListener {
            onCancel(r.id)
        }
    }

    override fun getItemCount(): Int = list.size
}
