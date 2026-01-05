package com.simats.idea2lld.utils

import android.content.Context
import android.content.Intent
import com.simats.idea2lld.ProjectPackageDisplayActivity

object ProjectRequestClickHandler {

    // single method – open project request package details
    fun open(context: Context, requestId: Int) {
        val intent = Intent(context, ProjectPackageDisplayActivity::class.java)
        intent.putExtra("REQUEST_ID", requestId)
        intent.putExtra("FROM_FOUNDER", true)
        context.startActivity(intent)
    }
}
