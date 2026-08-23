package com.example.aprendeaprender.data.notifications

import android.content.Context

class SeenTasksStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("seen_tasks", Context.MODE_PRIVATE)

    fun yaVista(key: String): Boolean = prefs.getBoolean(key, false)

    fun marcarVista(key: String) {
        prefs.edit().putBoolean(key, true).apply()
    }

    /** Marca de "ya pasó la primera ejecución" para no notificar 50 tareas viejas la primera vez. */
    fun estaInicializado(): Boolean = prefs.getBoolean("__initialized", false)
    fun marcarInicializado() {
        prefs.edit().putBoolean("__initialized", true).apply()
    }
}