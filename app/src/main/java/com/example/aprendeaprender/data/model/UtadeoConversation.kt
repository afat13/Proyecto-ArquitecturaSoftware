package com.example.aprendeaprender.data.model

data class UtadeoConversation(
    val id: Long,
    val type: Int,                    // 1=1a1, 2=grupo privado, 3=self/notas, 4=grupo público
    val name: String,                 // nombre mostrable (otro usuario o grupo)
    val imageUrl: String? = null,
    val otherUserId: Long? = null,    // para 1a1, id del otro miembro
    val isRead: Boolean = true,
    val unreadCount: Int = 0,
    val isFavourite: Boolean = false,
    val lastMessagePreview: String = "",  // sin HTML
    val lastMessageTime: Long = 0L,       // millis
    val lastMessageFromMe: Boolean = false
)