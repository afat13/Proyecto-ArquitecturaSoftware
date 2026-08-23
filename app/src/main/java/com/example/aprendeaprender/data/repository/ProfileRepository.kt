package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.api.ApiService
import com.example.aprendeaprender.data.api.SessionStore
import com.example.aprendeaprender.data.api.UpdateProfileRequest
import com.example.aprendeaprender.data.model.UserProfile

class ProfileRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun getProfile(): UserProfile {
        val user = api.getProfile()
        sessionStore.saveUser(user)
        return UserProfile(
            uid = user.id,
            email = user.email,
            nombre = user.firstName,
            apellido = user.lastName,
            telefono = user.phone.orEmpty()
        )
    }

    suspend fun updateProfile(
        nombre: String,
        apellido: String,
        telefono: String
    ) {
        val user = api.updateProfile(
            UpdateProfileRequest(
                firstName = nombre.trim(),
                lastName = apellido.trim(),
                phone = telefono.trim()
            )
        )
        sessionStore.saveUser(user)
    }

    suspend fun createUserProfile(profile: UserProfile) {
        updateProfile(profile.nombre, profile.apellido, profile.telefono)
    }
}
