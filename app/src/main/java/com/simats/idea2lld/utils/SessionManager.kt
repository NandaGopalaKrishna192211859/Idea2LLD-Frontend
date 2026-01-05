package com.simats.idea2lld.utils

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("idea2lld_session", Context.MODE_PRIVATE)

    fun saveLogin(
        uid: Int,
        token: String,
        fullName: String,
        role: String,
        preferredCategories: String
    ) {
        prefs.edit()
            .putBoolean("IS_LOGGED_IN", true)
            .putInt("uid", uid)
            .putString("TOKEN", token)
            .putString("FULL_NAME", fullName)
            .putString("ROLE", role)
            .putString("PREFERRED_CATEGORIES", preferredCategories)
            .apply()

        android.util.Log.d("SESSION_SAVE", "Saved uid=$uid")
    }


    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    fun getFullName(): String {
        return prefs.getString("FULL_NAME", "") ?: ""
    }

    fun getRole(): String {
        return prefs.getString("ROLE", "") ?: ""
    }

    fun getPreferredCategories(): String {
        return prefs.getString("PREFERRED_CATEGORIES", "") ?: ""
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }


    fun getUid(): Int {
        return prefs.getInt("uid", -1)
    }

    fun getToken(): String {
        return prefs.getString("TOKEN", "") ?: ""
    }




}
