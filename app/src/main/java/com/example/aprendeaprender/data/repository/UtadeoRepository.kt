package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.remote.UtadeoService

class UtadeoRepository(
    private val service: UtadeoService = UtadeoService()
) {
    suspend fun sincronizarTodo(usuario: String, contrasena: String): UtadeoService.ResultadoSync {
        if (usuario.isBlank()) throw Exception("El usuario no puede estar vacío")
        if (contrasena.isBlank()) throw Exception("La contraseña no puede estar vacía")
        return service.sincronizarTodo(usuario, contrasena)
    }
}