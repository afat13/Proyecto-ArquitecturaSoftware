package com.example.aprendeaprender.viewmodel

data class ProfileUiState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val mensajeErrorResId: Int? = null,
    val mensajeExitoResId: Int? = null
)