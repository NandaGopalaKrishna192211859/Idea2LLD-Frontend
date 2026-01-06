package com.simats.idea2lld.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simats.idea2lld.NotificationsActivity
import com.simats.idea2lld.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.pm.PackageManager


object NotificationHelper {

    private const val CHANNEL_ID = "idea2lld_channel"
    private const val PREF_NAME = "idea2lld_notifications"
    private const val KEY_LIST = "notifications"

    // ------------------ PUBLIC API ------------------
    fun push(context: Context, title: String, message: String) {
        createChannel(context)
        saveNotification(context, title, message)
        showSystemNotification(context, title, message)
    }

    fun getAll(context: Context): List<JSONObject> {
        val pref = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val jsonStr = pref.getString(KEY_LIST, "[]") ?: "[]"
        val arr = JSONArray(jsonStr)

        val list = mutableListOf<JSONObject>()
        for (i in arr.length() - 1 downTo 0) {
            list.add(arr.getJSONObject(i))
        }
        return list
    }

    // ------------------ INTERNAL ------------------
    private fun saveNotification(context: Context, title: String, message: String) {
        val pref = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val arr = JSONArray(pref.getString(KEY_LIST, "[]"))

        val obj = JSONObject()
        obj.put("title", title)
        obj.put("message", message)
        obj.put("time", getIndianTime())

        arr.put(obj)
        pref.edit().putString(KEY_LIST, arr.toString()).apply()
    }

    private fun showSystemNotification(context: Context, title: String, message: String) {

        val intent = Intent(context, NotificationsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // 🔔 app logo
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }

    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Idea2LLD Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getIndianTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("en", "IN"))
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        return sdf.format(Date())
    }
}