package com.example.aprendeaprender.data.notifications

import android.content.Context

class SeenMessagesStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("chat_seen_messages", Context.MODE_PRIVATE)

    fun yaVisto(messageId: Long): Boolean =
        prefs.getBoolean("m_$messageId", false)

    fun marcarVisto(messageId: Long) {
        prefs.edit().putBoolean("m_$messageId", true).apply()
    }

    /** Limpia IDs viejos (mantén solo los recientes para no llenar prefs). */
    fun limpiarSiPasaDe(maxEntradas: Int = 500) {
        val all = prefs.all
        if (all.size > maxEntradas) {
            prefs.edit().clear().apply()
        }
    }
}