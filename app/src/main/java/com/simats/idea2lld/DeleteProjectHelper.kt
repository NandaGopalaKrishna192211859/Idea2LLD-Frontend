package com.simats.idea2lld.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.simats.idea2lld.network.ApiClient
import okhttp3.*
import java.io.IOException

object DeleteProjectHelper {

    enum class ProjectType {
        DRAFT,
        SAVED
    }

    fun confirmAndDelete(
        context: Context,
        pid: Int,
        type: ProjectType,
        onSuccess: () -> Unit
    ) {

        val title = if (type == ProjectType.DRAFT)
            "Delete Draft?"
        else
            "Delete Saved Project?"

        val message = if (type == ProjectType.DRAFT)
            "This draft will be permanently deleted."
        else
            "This saved project will be permanently deleted."

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                deleteProject(context, pid, type, onSuccess)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProject(
        context: Context,
        pid: Int,
        type: ProjectType,
        onSuccess: () -> Unit
    ) {

        val token = SessionManager(context).getToken()

        val url = when (type) {
            ProjectType.DRAFT ->
                "${ApiClient.BASE_URL}/projects/delete-draft/$pid"
            ProjectType.SAVED ->
                "${ApiClient.BASE_URL}/projects/delete-saved/$pid"
        }

        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Delete failed. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    (context as? android.app.Activity)?.runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Project deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                "Delete failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }
}
