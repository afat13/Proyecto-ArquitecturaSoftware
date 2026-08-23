package com.example.aprendeaprender.data.api

import android.content.Context

class SessionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "backend_session",
        Context.MODE_PRIVATE
    )

    fun token(): String? = preferences.getString(KEY_TOKEN, null)

    fun userEmail(): String = preferences.getString(KEY_EMAIL, "").orEmpty()

    fun userId(): String = preferences.getString(KEY_USER_ID, "").orEmpty()

    fun save(session: SessionResponse) {
        preferences.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_ID, session.user.id)
            .putString(KEY_EMAIL, session.user.email)
            .apply()
    }

    fun saveUser(user: ApiUser) {
        preferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    fun hasSession(): Boolean = !token().isNullOrBlank()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
    }
}
