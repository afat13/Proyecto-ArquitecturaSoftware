package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.data.model.Participante
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectDetailUiState(
    val materia: Subject? = null,
    val participantes: List<Participante> = emptyList(),
    val tareas: List<Task> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class SubjectDetailViewModel(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    fun cargar(subjectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val materia = subjectRepository.getMySubjects().firstOrNull { it.id == subjectId }
                val participantes = subjectRepository.getParticipantes(subjectId)
                val tareas = taskRepository.getTasksBySubject(subjectId)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        materia = materia,
                        participantes = participantes,
                        tareas = tareas
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SUBJECT_DETAIL", "Error cargando detalle", e)
                _uiState.update { it.copy(cargando = false, error = e.message ?: "Error") }
            }
        }
    }
}