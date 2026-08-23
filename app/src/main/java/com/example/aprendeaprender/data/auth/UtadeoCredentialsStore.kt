package com.example.aprendeaprender.data.auth

import android.content.Context

data class UtadeoCredentials(
    val usuario: String,
    val contrasena: String
)

class UtadeoCredentialsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun guardar(usuario: String, contrasena: String) {
        prefs.edit()
            .putString(KEY_USUARIO, usuario.trim())
            .putString(KEY_CONTRASENA, contrasena)
            .apply()
    }

    fun obtener(): UtadeoCredentials? {
        val usuario = prefs.getString(KEY_USUARIO, null)?.trim().orEmpty()
        val contrasena = prefs.getString(KEY_CONTRASENA, null).orEmpty()

        if (usuario.isBlank() || contrasena.isBlank()) return null

        return UtadeoCredentials(
            usuario = usuario,
            contrasena = contrasena
        )
    }

    fun limpiar() {
        prefs.edit().clear().apply()
    }

    fun borrar() {
        limpiar()
    }

    fun hayCredenciales(): Boolean {
        return obtener() != null
    }

    companion object {
        private const val PREFS_NAME = "utadeo_credentials_store"
        private const val KEY_USUARIO = "usuario"
        private const val KEY_CONTRASENA = "contrasena"
    }
}