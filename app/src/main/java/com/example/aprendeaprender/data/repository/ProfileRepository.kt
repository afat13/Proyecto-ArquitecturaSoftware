package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.UserProfile
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreUserService

class ProfileRepository(
    private val authService: FirebaseAuthService,
    private val userService: FirestoreUserService = FirestoreUserService()
) {

    private fun currentUserId(): String {
        return authService.currentUser()?.uid
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

    suspend fun getProfile(): UserProfile {
        val uid = currentUserId()
        val email = authService.currentUser()?.email.orEmpty()
        return userService.getUserProfile(uid, email)
    }

    suspend fun updateProfile(
        nombre: String,
        apellido: String,
        telefono: String
    ) {
        val uid = currentUserId()
        val email = authService.currentUser()?.email.orEmpty()
        val profile = UserProfile(
            uid = uid,
            email = email,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono
        )
        userService.saveUserProfile(profile)
    }

    suspend fun createUserProfile(profile: UserProfile) {
        userService.createUserProfile(profile)
    }
}