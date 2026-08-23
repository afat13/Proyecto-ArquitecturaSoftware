package com.example.aprendeaprender.data.model

data class UtadeoMessage(
    val id: Long,
    val conversationId: Long,
    val userIdFrom: Long,
    val textPlain: String,        // sin HTML, listo para mostrar
    val textHtml: String,         // con HTML por si tiene links/formato
    val timeCreated: Long,        // millis
    val fromMe: Boolean = false   // se calcula en runtime
)