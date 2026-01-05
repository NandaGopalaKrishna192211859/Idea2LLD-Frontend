package com.simats.idea2lld.utils

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.simats.idea2lld.R
import com.simats.idea2lld.network.ApiClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object ProfileImageLoader {
    /**
     * Loads investor profile image safely.
     * - If imagePath is null/empty → default icon
     * - If network fails → default icon
     */
    fun load(imageView: ImageView, imagePath: String?) {

        // 🔒 Safety: no image available
        if (imagePath.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_user)
            return
        }

        val fullUrl = "${ApiClient.IMAGE_BASE_URL}/uploads/$imagePath"

        val request = Request.Builder()
            .url(fullUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                imageView.post {
                    imageView.setImageResource(R.drawable.ic_user)
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (!response.isSuccessful || response.body == null) {
                    imageView.post {
                        imageView.setImageResource(R.drawable.ic_user)
                    }
                    return
                }

                val bytes = response.body!!.bytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            }
        })
    }
}
