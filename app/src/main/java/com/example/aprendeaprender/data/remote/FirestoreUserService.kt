package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.UserProfile
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirestoreUserService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun userRef(uid: String) =
        database.getReference("usuarios").child(uid)

    suspend fun getUserProfile(uid: String, fallbackEmail: String): UserProfile {
        val snapshot = userRef(uid).get().await()

        if (!snapshot.exists()) {
            return UserProfile(
                uid = uid,
                email = fallbackEmail,
                nombre = "",
                apellido = "",
                telefono = ""
            )
        }

        return UserProfile(
            uid = uid,
            email = snapshot.child("correo").getValue(String::class.java).orEmpty().ifBlank { fallbackEmail },
            nombre = snapshot.child("nombres").getValue(String::class.java).orEmpty(),
            apellido = snapshot.child("apellidos").getValue(String::class.java).orEmpty(),
            telefono = snapshot.child("telefono").getValue(String::class.java).orEmpty()
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val data = mapOf(
            "correo" to profile.email,
            "nombres" to profile.nombre,
            "apellidos" to profile.apellido,
            "telefono" to profile.telefono
        )
        userRef(profile.uid).updateChildren(data).await()
    }

    suspend fun createUserProfile(profile: UserProfile) {
        val data = mapOf(
            "correo" to profile.email,
            "nombres" to profile.nombre,
            "apellidos" to profile.apellido,
            "telefono" to profile.telefono
        )
        userRef(profile.uid).updateChildren(data).await()
    }
}