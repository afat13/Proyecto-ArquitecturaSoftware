package com.example.aprendeaprender.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.repository.AuthRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _forgotPasswordUiState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordUiState: StateFlow<ForgotPasswordUiState> = _forgotPasswordUiState.asStateFlow()

    private val _verifyEmailUiState = MutableStateFlow(VerifyEmailUiState())
    val verifyEmailUiState: StateFlow<VerifyEmailUiState> = _verifyEmailUiState.asStateFlow()

    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    fun verificarSesion() {
        viewModelScope.launch {
            if (!repository.hasActiveSession()) {
                _authEvents.emit(AuthEvent.NavigateToLogin)
                return@launch
            }

            try {
                repository.reloadCurrentUser()
                _authEvents.emit(AuthEvent.NavigateToHome)
            } catch (_: Exception) {
                repository.signOut()
                _authEvents.emit(AuthEvent.NavigateToLogin)
            }
        }
    }

    fun onLoginCorreoChange(value: String) {
        _loginUiState.update { it.copy(correo = value, mensajeErrorResId = null) }
    }

    fun onLoginContrasenaChange(value: String) {
        _loginUiState.update { it.copy(contrasena = value, mensajeErrorResId = null) }
    }

    fun onRegisterNombreChange(value: String) {
        _registerUiState.update { it.copy(nombre = value, mensajeErrorResId = null) }
    }

    fun onRegisterApellidoChange(value: String) {
        _registerUiState.update { it.copy(apellido = value, mensajeErrorResId = null) }
    }

    fun onRegisterEdadChange(value: String) {
        _registerUiState.update { it.copy(edad = value, mensajeErrorResId = null) }
    }

    fun onRegisterCarreraChange(value: String) {
        _registerUiState.update { it.copy(carrera = value, mensajeErrorResId = null) }
    }

    fun onRegisterCorreoChange(value: String) {
        _registerUiState.update { it.copy(correo = value, mensajeErrorResId = null) }
    }

    fun onRegisterContrasenaChange(value: String) {
        _registerUiState.update { it.copy(contrasena = value, mensajeErrorResId = null) }
    }

    fun onRegisterConfirmarContrasenaChange(value: String) {
        _registerUiState.update { it.copy(confirmarContrasena = value, mensajeErrorResId = null) }
    }

    fun onRegisterAceptaTerminosChange(value: Boolean) {
        _registerUiState.update { it.copy(aceptaTerminos = value, mensajeErrorResId = null) }
    }

    fun onForgotPasswordCorreoChange(value: String) {
        _forgotPasswordUiState.update {
            it.copy(correo = value, mensajeErrorResId = null, mensajeExitoResId = null)
        }
    }

    fun iniciarSesion() {
        val correo = _loginUiState.value.correo.trim()
        val contrasena = _loginUiState.value.contrasena

        when {
            !esCorreoValido(correo) -> {
                _loginUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_invalid_email) }
                return
            }
            contrasena.isBlank() -> {
                _loginUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_empty_password) }
                return
            }
        }

        _loginUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }
        viewModelScope.launch {
            try {
                repository.login(correo, contrasena)
                _authEvents.emit(AuthEvent.ShowSnackbar(R.string.auth_login_success))
                _authEvents.emit(AuthEvent.NavigateToHome)
            } catch (e: Exception) {
                _loginUiState.update { it.copy(mensajeErrorResId = mapLoginError(e)) }
            } finally {
                _loginUiState.update { it.copy(cargando = false) }
            }
        }
    }

    fun registrarUsuario() {
        val state = _registerUiState.value
        val correo = state.correo.trim()
        val contrasena = state.contrasena
        val confirmar = state.confirmarContrasena

        when {
            !esCorreoValido(correo) -> {
                _registerUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_invalid_email) }
                return
            }
            !esContrasenaValida(contrasena) -> {
                _registerUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_weak_password) }
                return
            }
            contrasena != confirmar -> {
                _registerUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_password_mismatch) }
                return
            }
        }

        _registerUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }
        viewModelScope.launch {
            try {
                repository.register(
                    email = correo,
                    password = contrasena,
                    nombre = state.nombre.trim(),
                    apellido = state.apellido.trim()
                )
                _authEvents.emit(AuthEvent.NavigateToHome)
            } catch (e: Exception) {
                _registerUiState.update { it.copy(mensajeErrorResId = mapRegisterError(e)) }
            } finally {
                _registerUiState.update { it.copy(cargando = false) }
            }
        }
    }

    fun enviarCorreoRecuperacion() {
        val correo = _forgotPasswordUiState.value.correo.trim()
        if (!esCorreoValido(correo)) {
            _forgotPasswordUiState.update { it.copy(mensajeErrorResId = R.string.auth_error_invalid_email) }
            return
        }
        _forgotPasswordUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }
        viewModelScope.launch {
            try {
                repository.sendPasswordResetEmail(correo)
                _authEvents.emit(AuthEvent.NavigateToResetPasswordEmailSent)
            } catch (_: UnsupportedOperationException) {
                _forgotPasswordUiState.update {
                    it.copy(cargando = false, mensajeErrorResId = R.string.auth_error_generic)
                }
            } catch (e: Exception) {
                _forgotPasswordUiState.update {
                    it.copy(cargando = false, mensajeErrorResId = mapNetworkOrGeneric(e))
                }
            }
        }
    }

    fun reenviarCorreoVerificacion() {
        _verifyEmailUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }
        viewModelScope.launch {
            try {
                repository.resendEmailVerification()
            } catch (_: Exception) {
                _verifyEmailUiState.update {
                    it.copy(cargando = false, mensajeErrorResId = R.string.auth_error_generic)
                }
            }
        }
    }

    fun revisarEstadoVerificacion() {
        viewModelScope.launch {
            if (repository.hasActiveSession()) {
                _authEvents.emit(AuthEvent.NavigateToHome)
            } else {
                _authEvents.emit(AuthEvent.NavigateToLogin)
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.signOut()
            _authEvents.emit(AuthEvent.NavigateToLogin)
        }
    }

    private fun esCorreoValido(correo: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    private fun esContrasenaValida(contrasena: String): Boolean = contrasena.length >= 8

    private fun mapLoginError(e: Exception): Int = when (e) {
        is IOException -> R.string.auth_error_network
        is HttpException -> if (e.code() in listOf(400, 401, 403)) {
            R.string.auth_error_invalid_credentials
        } else R.string.auth_error_generic
        else -> R.string.auth_error_generic
    }

    private fun mapRegisterError(e: Exception): Int = when (e) {
        is IOException -> R.string.auth_error_network
        is HttpException -> if (e.code() == 400 || e.code() == 409) {
            R.string.auth_error_email_already_in_use
        } else R.string.auth_error_generic
        else -> R.string.auth_error_generic
    }

    private fun mapNetworkOrGeneric(e: Exception): Int =
        if (e is IOException) R.string.auth_error_network else R.string.auth_error_generic
}
