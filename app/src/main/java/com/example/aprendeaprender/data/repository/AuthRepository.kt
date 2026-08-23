package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.api.ApiService
import com.example.aprendeaprender.data.api.LoginRequest
import com.example.aprendeaprender.data.api.RegisterRequest
import com.example.aprendeaprender.data.api.SessionStore

sealed class RegisterResult {
    data object SuccessRegistered : RegisterResult()
}

class AuthRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    fun hasActiveSession(): Boolean = sessionStore.hasSession()

    // Se mantiene por compatibilidad con la interfaz actual. La verificación de correo
    // queda fuera del alcance de esta primera migración a PostgreSQL.
    fun isCurrentUserVerified(): Boolean = hasActiveSession()

    fun currentUserEmail(): String = sessionStore.userEmail()

    suspend fun login(email: String, password: String): Boolean {
        val session = api.login(LoginRequest(email.trim(), password))
        sessionStore.save(session)
        return true
    }

    suspend fun register(
        email: String,
        password: String,
        nombre: String = "",
        apellido: String = "",
        telefono: String = ""
    ): RegisterResult {
        val session = api.register(
            RegisterRequest(
                email = email.trim(),
                password = password,
                firstName = nombre.trim(),
                lastName = apellido.trim(),
                phone = telefono.trim().ifBlank { null }
            )
        )
        sessionStore.save(session)
        return RegisterResult.SuccessRegistered
    }

    suspend fun reloadCurrentUser() {
        val user = api.me()
        sessionStore.saveUser(user)
    }

    suspend fun signOut() {
        try {
            api.logout()
        } finally {
            sessionStore.clear()
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        throw UnsupportedOperationException("La recuperación de contraseña se implementará en una fase posterior")
    }

    suspend fun resendEmailVerification() {
        throw UnsupportedOperationException("La verificación de correo se implementará en una fase posterior")
    }
}
