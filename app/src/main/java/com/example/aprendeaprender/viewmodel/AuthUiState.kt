package com.example.aprendeaprender.viewmodel

import androidx.annotation.StringRes

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)

data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val edad: String = "",
    val carrera: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaTerminos: Boolean = false,
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)

data class ForgotPasswordUiState(
    val correo: String = "",
    val correoEnviadoA: String = "",
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null,
    @StringRes val mensajeExitoResId: Int? = null
)

data class VerifyEmailUiState(
    val correo: String = "",
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null,
    @StringRes val mensajeExitoResId: Int? = null
)