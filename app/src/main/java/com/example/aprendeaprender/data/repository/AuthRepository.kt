package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.UserProfile
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreUserService

sealed class RegisterResult {
    data object SuccessEmailSent : RegisterResult()
}

class AuthRepository(
    private val authService: FirebaseAuthService,
    private val userService: FirestoreUserService
) {

    fun hasActiveSession(): Boolean {
        return authService.currentUser() != null
    }

    fun isCurrentUserVerified(): Boolean {
        return authService.currentUser()?.isEmailVerified == true
    }

    fun currentUserEmail(): String {
        return authService.currentUser()?.email.orEmpty()
    }

    suspend fun login(email: String, password: String): Boolean {
        authService.signIn(email.trim(), password)
        authService.reloadCurrentUser()
        return authService.currentUser()?.isEmailVerified == true
    }

    suspend fun register(
        email: String,
        password: String,
        nombre: String = "",
        apellido: String = "",
        telefono: String = ""
    ): RegisterResult {
        val user = authService.register(email.trim(), password)

        authService.sendEmailVerification(user)

        userService.createUserProfile(
            UserProfile(
                uid = user.uid,
                email = user.email.orEmpty(),
                nombre = nombre,
                apellido = apellido,
                telefono = telefono
            )
        )

        return RegisterResult.SuccessEmailSent
    }

    suspend fun sendPasswordResetEmail(email: String) {
        authService.sendPasswordResetEmail(email.trim())
    }

    suspend fun resendEmailVerification() {
        val user = authService.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado.")
        authService.sendEmailVerification(user)
    }

    suspend fun reloadCurrentUser() {
        authService.reloadCurrentUser()
    }

    fun signOut() {
        authService.signOut()
    }
}