package com.example.aprendeaprender.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.TaskListUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaskListScreen(
    uiState: TaskListUiState,
    onEstadoChange: (String, String, Task.Estado) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onAddTaskClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var materiaSeleccionada by remember { mutableStateOf<String?>(null) } // null = todas
    var incluirCompletadas by remember { mutableStateOf(false) }
    var dropdownMateriaOpen by remember { mutableStateOf(false) }

    val hayTareas = uiState.todasLasTareas.isNotEmpty()

    // Tareas de hoy (excluyendo completadas, salvo que el toggle esté activo)
    val tareasHoy = remember(uiState.tareasHoy, incluirCompletadas) {
        if (incluirCompletadas) uiState.tareasHoy
        else uiState.tareasHoy.filter { it.estado != Task.Estado.COMPLETADA.name }
    }

    // Vencidas (con toggle de completadas)
    val vencidas = remember(uiState.tareasVencidas, incluirCompletadas) {
        if (incluirCompletadas) uiState.tareasVencidas
        else uiState.tareasVencidas.filter { it.estado != Task.Estado.COMPLETADA.name }
    }

// El resto: ni hoy ni vencidas
    val resto = remember(uiState.todasLasTareas, uiState.tareasHoy, uiState.tareasVencidas) {
        val idsHoy = uiState.tareasHoy.map { it.id }.toSet()
        val idsVencidas = uiState.tareasVencidas.map { it.id }.toSet()
        uiState.todasLasTareas.filter { it.id !in idsHoy && it.id !in idsVencidas }
    }

    // Materias disponibles para el dropdown (sacadas de las tareas)
    val materias = remember(uiState.todasLasTareas) {
        uiState.todasLasTareas
            .map { it.subjectId to it.subjectName }
            .distinct()
            .filter { it.second.isNotBlank() }
            .sortedBy { it.second }
    }

    // Resto filtrado
    val restoFiltrado = remember(resto, materiaSeleccionada, incluirCompletadas, uiState.todasLasTareas) {
        val base = if (materiaSeleccionada == null) resto
        else uiState.todasLasTareas.filter { it.subjectId == materiaSeleccionada }
        base.filter { t ->
            incluirCompletadas || t.estado != Task.Estado.COMPLETADA.name
        }
    }

    val nombreMateriaSel = materias.firstOrNull { it.first == materiaSeleccionada }?.second
        ?: "Todas las materias"

    // ── Diálogo de borrar ──
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = DarkSurface,
            title = { Text(stringResource(R.string.task_delete_title), fontWeight = FontWeight.Bold, color = TextWhite) },
            text = { Text(stringResource(R.string.task_delete_message), color = TextGray, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { onDeleteClick(task.subjectId, task.id); taskToDelete = null }) {
                    Text(stringResource(R.string.subject_btn_delete), color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(stringResource(R.string.profile_btn_cancel), color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.task_list_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )

            if (hayTareas) {
                FilledTonalButton(
                    onClick = onAddTaskClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF0D1B2A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.task_add_button),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
            }
            return@Column
        }

        if (!hayTareas) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.task_no_tasks),
                        color = TextGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    AppButton(
                        text = stringResource(R.string.task_create_first),
                        onClick = onAddTaskClick
                    )
                }
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── HOY (siempre) ──
            item {
                Text(
                    text = "Hoy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
            if (tareasHoy.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎉 No tienes tareas para hoy",
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(tareasHoy, key = { "hoy-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onEstadoChange = { onEstadoChange(task.subjectId, task.id, it) },
                        onDeleteClick = { taskToDelete = task }
                    )
                }
            }

            // ── VENCIDAS ──
            if (vencidas.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vencidas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${vencidas.size}",
                                fontSize = 12.sp,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                items(vencidas, key = { "venc-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onEstadoChange = { onEstadoChange(task.subjectId, task.id, it) },
                        onDeleteClick = { taskToDelete = task }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── FILTROS ──
            item {
                Text(
                    text = "Por materia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Dropdown materia
            item {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TextGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { dropdownMateriaOpen = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nombreMateriaSel,
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownMateriaOpen,
                        onDismissRequest = { dropdownMateriaOpen = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las materias", color = TextWhite, fontSize = 13.sp) },
                            onClick = {
                                materiaSeleccionada = null
                                dropdownMateriaOpen = false
                            }
                        )
                        materias.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre, color = TextWhite, fontSize = 13.sp) },
                                onClick = {
                                    materiaSeleccionada = id
                                    dropdownMateriaOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Toggle completadas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrar completadas",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = incluirCompletadas,
                        onCheckedChange = { incluirCompletadas = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = CyanAccent.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = Color(0xFF1B2A3B)
                        )
                    )
                }
            }

            // Lista filtrada
            if (restoFiltrado.isEmpty()) {
                item {
                    Text(
                        text = if (materiaSeleccionada == null)
                            "No hay más tareas para mostrar."
                        else
                            "Esta materia no tiene tareas con los filtros actuales.",
                        color = TextGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(restoFiltrado, key = { "resto-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onEstadoChange = { onEstadoChange(task.subjectId, task.id, it) },
                        onDeleteClick = { taskToDelete = task }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onEstadoChange: (Task.Estado) -> Unit,
    onDeleteClick: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")).apply {
        timeZone = java.util.TimeZone.getTimeZone("America/Bogota")
    }
    val fechaTexto = if (task.fechaEntrega > 0) sdf.format(task.fechaEntrega) else "—"
    val prioridad = runCatching { Task.Prioridad.valueOf(task.prioridad) }.getOrDefault(Task.Prioridad.MEDIA)
    val estado = runCatching { Task.Estado.valueOf(task.estado) }.getOrDefault(Task.Estado.PENDIENTE)
    val completada = estado == Task.Estado.COMPLETADA
    var showEstadoDropdown by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completada) TextGray else TextWhite,
                        textDecoration = if (completada) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(text = task.subjectName, fontSize = 12.sp, color = CyanAccent)
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }

            if (task.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = task.descripcion, fontSize = 13.sp, color = TextGray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📅 $fechaTexto", fontSize = 12.sp, color = TextGray)
                Box(
                    modifier = Modifier
                        .border(1.dp, prioridadColor(prioridad), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = prioridadLabel(prioridad),
                        fontSize = 11.sp,
                        color = prioridadColor(prioridad),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, estadoColor(estado), RoundedCornerShape(8.dp))
                        .background(DarkBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = estadoLabel(estado),
                            color = estadoColor(estado),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showEstadoDropdown = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                DropdownMenu(
                    expanded = showEstadoDropdown,
                    onDismissRequest = { showEstadoDropdown = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    Task.Estado.values().forEach { e ->
                        DropdownMenuItem(
                            text = { Text(estadoLabel(e), color = estadoColor(e), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            onClick = { onEstadoChange(e); showEstadoDropdown = false }
                        )
                    }
                }
            }
        }
    }
}