package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.data.model.UtadeoCourse
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.data.repository.UtadeoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UtadeoUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val sincronizando: Boolean = false,
    val cursos: List<UtadeoCourse> = emptyList(),
    val error: String? = null,
    val cargado: Boolean = false,
    val materiasSincronizadas: Int = 0,
    val tareasSincronizadas: Int = 0
)

class UtadeoViewModel(
    private val utadeoRepository: UtadeoRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val credentialsStore: com.example.aprendeaprender.data.auth.UtadeoCredentialsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UtadeoUiState())
    val uiState: StateFlow<UtadeoUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(value: String)   = _uiState.update { it.copy(usuario = value, error = null) }
    fun onContrasenaChange(value: String) = _uiState.update { it.copy(contrasena = value, error = null) }

    fun obtenerCursos() {
        val state = _uiState.value
        if (state.usuario.isBlank())   { _uiState.update { it.copy(error = "Ingresa tu usuario") }; return }
        if (state.contrasena.isBlank()){ _uiState.update { it.copy(error = "Ingresa tu contraseña") }; return }

        _uiState.update { it.copy(cargando = true, error = null, cursos = emptyList()) }

        viewModelScope.launch {
            try {
                android.util.Log.d("UTADEO_VM", "Iniciando scraping completo...")
                val resultado = utadeoRepository.sincronizarTodo(state.usuario, state.contrasena)
                android.util.Log.d("UTADEO_VM", "Cursos: ${resultado.cursos.size}, tareas: ${resultado.tareas.size}")
                // Persistir creds para que chat pueda usarlas sin volver a pedirlas
                credentialsStore.guardar(state.usuario, state.contrasena)

                _uiState.update {
                    it.copy(cargando = false, sincronizando = true, cursos = resultado.cursos)
                }

                // 1) Materias
                subjectRepository.sincronizarDesdeUtadeo(resultado.cursos, resultado.participantesPorCurso)
                // 2) Tareas (necesita cursos para mapear subjectName)
                taskRepository.sincronizarTareasUtadeo(resultado.cursos, resultado.tareas)

                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        cargado = true,
                        materiasSincronizadas = resultado.cursos.size,
                        tareasSincronizadas = resultado.tareas.size
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("UTADEO_VM", "Error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        sincronizando = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun resetear() { _uiState.value = UtadeoUiState() }
}