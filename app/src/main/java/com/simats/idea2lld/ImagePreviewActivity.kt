package com.simats.idea2lld

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        val imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""

        val iv = findViewById<PhotoView>(R.id.ivFullImage)
        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            finish() // DONE → GO BACK
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_project)
            .error(R.drawable.placeholder_project)
            .into(iv)
    }
}
