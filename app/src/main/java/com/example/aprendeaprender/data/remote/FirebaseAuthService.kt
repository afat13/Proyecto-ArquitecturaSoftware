package com.example.aprendeaprender.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    init {
        auth.useAppLanguage()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
            ?: throw IllegalStateException("No se pudo obtener el usuario autenticado.")
    }

    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user
            ?: throw IllegalStateException("No se pudo obtener el usuario registrado.")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendEmailVerification(user: FirebaseUser) {
        try {
            user.sendEmailVerification().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando correo de verificación", e)
            throw e
        }
    }

    suspend fun reloadCurrentUser() {
        auth.currentUser?.reload()?.await()
    }

    fun signOut() {
        auth.signOut()
    }
}