package com.simats.idea2lld

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CategorySpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, true)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, false)
    }

    private fun createView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        isSelected: Boolean
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.activity_category_spinner_adapter, parent, false)

        val textView = view.findViewById<TextView>(R.id.tvSpinnerText)
        textView.text = items[position]

        if (isSelected) {
            // ✅ Selected item
            textView.setBackgroundColor(0xFF81C784.toInt()) // light green
            textView.setTextColor(0xFFFFFFFF.toInt()) // white
        } else {
            // ✅ Dropdown items
            textView.setBackgroundColor(0xFFFFFFFF.toInt()) // white
            textView.setTextColor(0xFF000000.toInt()) // black
        }

        return view
    }
}
