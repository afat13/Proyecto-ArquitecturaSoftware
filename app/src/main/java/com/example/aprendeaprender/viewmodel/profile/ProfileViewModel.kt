package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        _uiState.update {
            it.copy(
                nombre = value,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }
    }

    fun onApellidoChange(value: String) {
        _uiState.update {
            it.copy(
                apellido = value,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }
    }

    fun onTelefonoChange(value: String) {
        _uiState.update {
            it.copy(
                telefono = value,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    cargando = true,
                    mensajeErrorResId = null,
                    mensajeExitoResId = null
                )
            }

            try {
                val profile = repository.getProfile()
                android.util.Log.d("PERFIL", "uid=${profile.uid}, email=${profile.email}, nombre=${profile.nombre}, apellido=${profile.apellido}")

                _uiState.update {
                    it.copy(
                        cargando = false,
                        nombre = profile.nombre,
                        apellido = profile.apellido,
                        correo = profile.email,
                        telefono = profile.telefono
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("PERFIL", "Error cargando perfil", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = R.string.profile_load_error
                    )
                }
            }
        }
    }

    fun guardarPerfil() {
        val state = _uiState.value
        val nombre = state.nombre.trim()
        val apellido = state.apellido.trim()
        val telefono = state.telefono.trim()

        if (nombre.isEmpty()) {
            _uiState.update {
                it.copy(mensajeErrorResId = R.string.profile_error_empty_name)
            }
            return
        }

        if (apellido.isEmpty()) {
            _uiState.update {
                it.copy(mensajeErrorResId = R.string.profile_error_empty_last_name)
            }
            return
        }

        if (telefono.isNotEmpty() && !esTelefonoValido(telefono)) {
            _uiState.update {
                it.copy(mensajeErrorResId = R.string.profile_error_invalid_phone)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    guardando = true,
                    mensajeErrorResId = null,
                    mensajeExitoResId = null
                )
            }

            try {
                repository.updateProfile(
                    nombre = nombre,
                    apellido = apellido,
                    telefono = telefono
                )

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeExitoResId = R.string.profile_save_success
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeErrorResId = R.string.profile_save_error
                    )
                }
            }
        }
    }

    private fun esTelefonoValido(value: String): Boolean {
        return Regex("^[0-9+\\- ]{7,20}$").matches(value)
    }
}