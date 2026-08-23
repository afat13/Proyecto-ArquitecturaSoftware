package com.example.aprendeaprender.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.data.model.Participante
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.SubjectDetailUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubjectDetailScreen(
    uiState: SubjectDetailUiState,
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = uiState.materia?.asignatura ?: "Materia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            }
            uiState.error != null -> {
                Text(uiState.error, color = ErrorRed, fontSize = 13.sp)
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Info de la materia
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Profesor", fontSize = 12.sp, color = TextGray)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = uiState.materia?.instructor.orEmpty()
                                        .ifBlank { "Sin información" },
                                    fontSize = 15.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Participantes
                    item {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Participantes (${uiState.participantes.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    if (uiState.participantes.isEmpty()) {
                        item {
                            Text("No hay participantes guardados.", color = TextGray, fontSize = 13.sp)
                        }
                    } else {
                        items(uiState.participantes) { p -> ParticipanteRow(p) }
                    }

                    // Tareas
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tareas (${uiState.tareas.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    if (uiState.tareas.isEmpty()) {
                        item {
                            Text("No hay tareas para esta materia.", color = TextGray, fontSize = 13.sp)
                        }
                    } else {
                        items(uiState.tareas) { t -> TareaRow(t) }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ParticipanteRow(p: Participante) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(p.nombre, color = TextWhite, fontSize = 14.sp, modifier = Modifier.weight(1f))
            val rolColor = if (p.rol == "Profesor") CyanAccent else TextGray
            Box(
                modifier = Modifier
                    .border(1.dp, rolColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = p.rol,
                    color = rolColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TareaRow(t: Task) {
    val fechaFmt = if (t.fechaEntrega > 0) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es")).format(Date(t.fechaEntrega))
    } else "Sin fecha"

    val estadoColor = when (t.estado) {
        Task.Estado.COMPLETADA.name -> Color(0xFF69F0AE)
        Task.Estado.EN_PROGRESO.name -> Color(0xFFFFD54F)
        else -> Color(0xFFFF8A65)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = t.titulo,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = t.estado,
                    color = estadoColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(text = fechaFmt, color = TextGray, fontSize = 12.sp)
        }
    }
}