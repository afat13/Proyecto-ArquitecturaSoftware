package com.example.aprendeaprender.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.data.model.UtadeoMessage
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.ChatConversationUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatConversationScreen(
    uiState: ChatConversationUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.conversacion?.name ?: "Chat",
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (uiState.conversacion?.otherUserId != null && uiState.conversacion.type != 3) {
                    Text("Utadeo", color = TextGray, fontSize = 11.sp)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1B2A3B), thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanAccent)
                }
                uiState.error != null && uiState.mensajes.isEmpty() -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error, color = ErrorRed, fontSize = 14.sp, modifier = Modifier.padding(24.dp))
                }
                uiState.mensajes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay mensajes en esta conversación.", color = TextGray, fontSize = 14.sp)
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.mensajes, key = { it.id }) { msg ->
                        MessageBubble(msg)
                    }
                }
            }
        }

        // Banner de error si hubo problema enviando pero sí hay mensajes cargados
        if (uiState.error != null && uiState.mensajes.isNotEmpty()) {
            Surface(color = ErrorRed.copy(alpha = 0.2f)) {
                Text(
                    text = uiState.error,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Input de mensaje
        Surface(color = Color(0xFF1B2A3B)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.textoInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Mensaje", color = TextGray, fontSize = 14.sp) },
                    enabled = !uiState.enviando,
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.4f),
                        cursorColor = CyanAccent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = onSendClick,
                    enabled = !uiState.enviando && uiState.textoInput.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF0D1B2A),
                        disabledContainerColor = TextGray.copy(alpha = 0.3f)
                    )
                ) {
                    if (uiState.enviando) {
                        CircularProgressIndicator(
                            color = Color(0xFF0D1B2A),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: UtadeoMessage) {
    val sdf = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("America/Bogota")
        }
    }
    val alignment = if (msg.fromMe) Alignment.End else Alignment.Start
    val bgColor = if (msg.fromMe) CyanAccent else Color(0xFF1B2A3B)
    val textColor = if (msg.fromMe) Color(0xFF0D1B2A) else TextWhite
    val shape = if (msg.fromMe)
        RoundedCornerShape(14.dp, 14.dp, 2.dp, 14.dp)
    else
        RoundedCornerShape(14.dp, 14.dp, 14.dp, 2.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = msg.textPlain.ifBlank { "(mensaje vacío)" },
                    color = textColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sdf.format(Date(msg.timeCreated)),
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}