package com.example.aprendeaprender.viewmodel

import androidx.annotation.StringRes
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.Task

data class CreateTaskUiState(
    // Campos del formulario
    val titulo: String = "",
    val descripcion: String = "",
    val fechaEntrega: Long = 0L,
    val fechaEntregaTexto: String = "",
    val prioridad: Task.Prioridad = Task.Prioridad.MEDIA,
    val estado: Task.Estado = Task.Estado.PENDIENTE,
    // Materia seleccionada
    val subjectId: String = "",
    val subjectName: String = "",
    // Lista de materias disponibles para elegir
    val materiasDisponibles: List<Subject> = emptyList(),
    val cargandoMaterias: Boolean = false,
    // Estado de la operación
    val cargando: Boolean = false,
    val tareaCreada: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)

data class TaskListUiState(
    val tareasHoy: List<Task> = emptyList(),
    val tareasVencidas: List<Task> = emptyList(),
    val todasLasTareas: List<Task> = emptyList(),
    val cargando: Boolean = false,
    @StringRes val mensajeErrorResId: Int? = null
)