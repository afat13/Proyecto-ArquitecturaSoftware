package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.UtadeoConversation
import com.example.aprendeaprender.data.model.UtadeoMessage
import com.example.aprendeaprender.data.remote.UtadeoService

class ChatRepository(
    private val service: UtadeoService = UtadeoService()
) {
    data class BandejaResult(val miUserId: Long, val conversaciones: List<UtadeoConversation>)
    data class MensajesResult(val miUserId: Long, val mensajes: List<UtadeoMessage>)

    suspend fun cargarBandeja(usuario: String, contrasena: String): BandejaResult {
        if (usuario.isBlank()) throw Exception("El usuario no puede estar vacío")
        if (contrasena.isBlank()) throw Exception("La contraseña no puede estar vacía")
        val snap = service.cargarBandejaChat(usuario, contrasena)
        return BandejaResult(snap.miUserId, snap.conversaciones)
    }

    suspend fun cargarMensajes(
        usuario: String,
        contrasena: String,
        conversationId: Long
    ): MensajesResult {
        val (uid, msgs) = service.cargarMensajesConversacion(usuario, contrasena, conversationId)
        return MensajesResult(uid, msgs)
    }
    suspend fun enviarMensaje(
        usuario: String,
        contrasena: String,
        conversationId: Long,
        texto: String
    ): com.example.aprendeaprender.data.model.UtadeoMessage? {
        if (texto.isBlank()) throw Exception("El mensaje está vacío")
        return service.enviarMensajeConSesion(usuario, contrasena, conversationId, texto)
    }
}