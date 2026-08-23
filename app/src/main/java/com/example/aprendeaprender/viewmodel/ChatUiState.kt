package com.example.aprendeaprender.viewmodel

import com.example.aprendeaprender.data.model.UtadeoConversation
import com.example.aprendeaprender.data.model.UtadeoMessage

data class ChatInboxUiState(
    val cargando: Boolean = false,
    val conversaciones: List<UtadeoConversation> = emptyList(),
    val error: String? = null,
    val sinCredenciales: Boolean = false,
    val miUserId: Long = 0L
)

data class ChatConversationUiState(
    val cargando: Boolean = false,
    val conversacion: UtadeoConversation? = null,
    val mensajes: List<UtadeoMessage> = emptyList(),
    val error: String? = null,
    val miUserId: Long = 0L,
    val enviando: Boolean = false,
    val textoInput: String = ""
)