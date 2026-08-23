package com.example.aprendeaprender.ui.screens.tasks

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.components.AppTextField
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.CreateTaskUiState
import java.util.Calendar

@Composable
fun CreateTaskScreen(
    uiState: CreateTaskUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaEntregaChange: (Long) -> Unit,
    onPrioridadChange: (Task.Prioridad) -> Unit,
    onEstadoChange: (Task.Estado) -> Unit,
    onSubjectChange: (String, String) -> Unit,
    onCrearClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showPrioridadDropdown by remember { mutableStateOf(false) }
    var showEstadoDropdown by remember { mutableStateOf(false) }

    Scaffold(containerColor = DarkBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Header
            IconButton(onClick = onBackClick) {
                Text("‹", fontSize = 32.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
            }

            Text(
                text = stringResource(R.string.task_new_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Materia ──
            Text(
                text = stringResource(R.string.task_subject_label),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (uiState.cargandoMaterias) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(24.dp))
                }
            } else if (uiState.materiasDisponibles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ErrorRed, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.task_no_subjects),
                        color = ErrorRed,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (uiState.subjectId.isBlank()) TextGray else CyanAccent,
                                RoundedCornerShape(10.dp)
                            )
                            .background(DarkSurface, RoundedCornerShape(10.dp))
                            .clickable { showSubjectDropdown = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.subjectName.isBlank())
                                    stringResource(R.string.task_select_subject)
                                else uiState.subjectName,
                                color = if (uiState.subjectName.isBlank()) TextGray else TextWhite,
                                fontSize = 14.sp
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextGray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        uiState.materiasDisponibles.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Text(subject.asignatura, color = TextWhite, fontSize = 14.sp)
                                },
                                onClick = {
                                    onSubjectChange(subject.id, subject.asignatura)
                                    showSubjectDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Título ──
            AppTextField(
                value = uiState.titulo,
                label = stringResource(R.string.task_title_label),
                onValueChange = onTituloChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Descripción ──
            AppTextField(
                value = uiState.descripcion,
                label = stringResource(R.string.task_description_label),
                onValueChange = onDescripcionChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Fecha de entrega ──
            Text(
                text = stringResource(R.string.task_due_date_label),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (uiState.fechaEntrega == 0L) TextGray else CyanAccent,
                        RoundedCornerShape(10.dp)
                    )
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, day, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onFechaEntregaChange(selected.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.fechaEntregaTexto.isBlank())
                            stringResource(R.string.task_select_date)
                        else uiState.fechaEntregaTexto,
                        color = if (uiState.fechaEntregaTexto.isBlank()) TextGray else TextWhite,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Prioridad ──
            Text(
                text = stringResource(R.string.task_priority_label),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyanAccent, RoundedCornerShape(10.dp))
                        .background(DarkSurface, RoundedCornerShape(10.dp))
                        .clickable { showPrioridadDropdown = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prioridadLabel(uiState.prioridad),
                            color = prioridadColor(uiState.prioridad),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray)
                    }
                }

                DropdownMenu(
                    expanded = showPrioridadDropdown,
                    onDismissRequest = { showPrioridadDropdown = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    Task.Prioridad.values().forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    prioridadLabel(p),
                                    color = prioridadColor(p),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onPrioridadChange(p)
                                showPrioridadDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Estado ──
            Text(
                text = stringResource(R.string.task_status_label),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyanAccent, RoundedCornerShape(10.dp))
                        .background(DarkSurface, RoundedCornerShape(10.dp))
                        .clickable { showEstadoDropdown = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = estadoLabel(uiState.estado),
                            color = estadoColor(uiState.estado),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray)
                    }
                }

                DropdownMenu(
                    expanded = showEstadoDropdown,
                    onDismissRequest = { showEstadoDropdown = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    Task.Estado.values().forEach { e ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    estadoLabel(e),
                                    color = estadoColor(e),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onEstadoChange(e)
                                showEstadoDropdown = false
                            }
                        )
                    }
                }
            }

            // Error
            uiState.mensajeErrorResId?.let { resId ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(resId), color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = if (uiState.cargando) stringResource(R.string.common_loading)
                else stringResource(R.string.task_create_button),
                onClick = onCrearClick,
                enabled = !uiState.cargando && uiState.materiasDisponibles.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun prioridadLabel(p: Task.Prioridad): String = when (p) {
    Task.Prioridad.ALTA -> "🔴 Alta"
    Task.Prioridad.MEDIA -> "🟡 Media"
    Task.Prioridad.BAJA -> "🟢 Baja"
}

fun prioridadColor(p: Task.Prioridad): Color = when (p) {
    Task.Prioridad.ALTA -> Color(0xFFFF5252)
    Task.Prioridad.MEDIA -> Color(0xFFFFD740)
    Task.Prioridad.BAJA -> Color(0xFF69F0AE)
}

fun estadoLabel(e: Task.Estado): String = when (e) {
    Task.Estado.PENDIENTE -> "⏳ Pendiente"
    Task.Estado.EN_PROGRESO -> "🔄 En progreso"
    Task.Estado.COMPLETADA -> "✅ Completada"
}

fun estadoColor(e: Task.Estado): Color = when (e) {
    Task.Estado.PENDIENTE -> Color(0xFFFFD740)
    Task.Estado.EN_PROGRESO -> Color(0xFF40C4FF)
    Task.Estado.COMPLETADA -> Color(0xFF69F0AE)
}