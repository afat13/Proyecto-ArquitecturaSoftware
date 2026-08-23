package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.ui.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargarDatosHome() {
        viewModelScope.launch {
            val nombreUsuario = obtenerNombreUsuario()
            val tareas = obtenerTareas()

            val tz = java.util.TimeZone.getTimeZone("America/Bogota")

            val inicioHoy = Calendar.getInstance(tz).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val inicioManana = Calendar.getInstance(tz).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis

            val completadas = tareas.count { it.estado == Task.Estado.COMPLETADA.name }
            val enCurso = tareas.count { it.estado == Task.Estado.EN_PROGRESO.name }
            val hoy = tareas.count {
                it.fechaEntrega in inicioHoy until inicioManana &&
                        it.estado != Task.Estado.COMPLETADA.name
            }
            val vencidas = tareas.count {
                it.fechaEntrega in 1 until inicioHoy &&
                        it.estado != Task.Estado.COMPLETADA.name
            }

            val progreso = if (tareas.isEmpty()) 0f
            else completadas.toFloat() / tareas.size.toFloat()

            android.util.Log.d("HOME_VM", "tareas=${tareas.size} hoy=$hoy vencidas=$vencidas completadas=$completadas")

            _uiState.update {
                it.copy(
                    userName = nombreUsuario,
                    completedTasks = completadas,
                    inProgressTasks = enCurso,
                    todayTasks = hoy,
                    overdueTasks = vencidas,
                    progress = progreso
                )
            }
        }
    }
    private suspend fun obtenerNombreUsuario(): String {
        return try {
            val profile = profileRepository.getProfile()
            profile.nombre.ifBlank { profile.email }
        } catch (_: Exception) {
            _uiState.value.userName
        }
    }

    private suspend fun obtenerTareas(): List<Task> {
        return try {
            taskRepository.getMyTasks()
        } catch (_: Exception) {
            emptyList()
        }
    }
}