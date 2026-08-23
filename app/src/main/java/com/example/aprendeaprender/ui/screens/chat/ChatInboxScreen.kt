package com.example.aprendeaprender.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.data.model.UtadeoConversation
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.ChatInboxUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatInboxScreen(
    uiState: ChatInboxUiState,
    onConversationClick: (Long) -> Unit,
    onSyncClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Chat",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
            }
            uiState.sinCredenciales -> EstadoVacio(
                titulo = "Necesitamos tus credenciales de Utadeo",
                detalle = "Ve a la pantalla de Sincronización (importar de Utadeo) y haz login una vez. Tus credenciales se guardan cifradas en el dispositivo.",
                botonTexto = "Ir a sincronizar",
                onClick = onSyncClick
            )
            uiState.error != null -> EstadoVacio(
                titulo = "Algo falló",
                detalle = uiState.error,
                botonTexto = "Reintentar",
                onClick = onSyncClick
            )
            uiState.conversaciones.isEmpty() -> EstadoVacio(
                titulo = "Sin conversaciones",
                detalle = "Aún no tienes mensajes en el chat de Utadeo.",
                botonTexto = "Recargar",
                onClick = onSyncClick
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.conversaciones, key = { it.id }) { conv ->
                    ConversationRow(conv) { onConversationClick(conv.id) }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ConversationRow(conv: UtadeoConversation, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp).clip(CircleShape).background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                val (icon, tint) = when (conv.type) {
                    3 -> Icons.Filled.PushPin to CyanAccent
                    1 -> Icons.Filled.Person to TextGray
                    else -> Icons.Filled.ChatBubble to TextGray
                }
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conv.name,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (conv.lastMessageTime > 0) {
                        Text(
                            text = formatoTiempoRelativo(conv.lastMessageTime),
                            color = if (conv.unreadCount > 0) CyanAccent else TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val prefijo = if (conv.lastMessageFromMe) "Tú: " else ""
                    Text(
                        text = "$prefijo${conv.lastMessagePreview.ifBlank { "(sin mensajes)" }}",
                        color = TextGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (conv.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(CyanAccent, CircleShape)
                                .padding(horizontal = 7.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${conv.unreadCount}",
                                color = Color(0xFF0D1B2A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(titulo: String, detalle: String, botonTexto: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(titulo, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(detalle, color = TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF0D1B2A))
            ) {
                Text(botonTexto, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatoTiempoRelativo(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val min = diff / 60_000
    val hr = diff / 3_600_000
    val dias = diff / 86_400_000
    return when {
        min < 1 -> "ahora"
        min < 60 -> "${min}m"
        hr < 24 -> "${hr}h"
        dias < 7 -> "${dias}d"
        else -> SimpleDateFormat("dd MMM", Locale("es")).apply {
            timeZone = java.util.TimeZone.getTimeZone("America/Bogota")
        }.format(Date(millis))
    }
}