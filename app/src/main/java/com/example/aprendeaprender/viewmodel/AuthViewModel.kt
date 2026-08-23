package com.example.aprendeaprender.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.repository.AuthRepository
import com.example.aprendeaprender.data.repository.RegisterResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            } catch (_: Exception) {
            }

            if (repository.isCurrentUserVerified()) {
                _authEvents.emit(AuthEvent.NavigateToHome)
            } else {
                _verifyEmailUiState.update {
                    it.copy(correo = repository.currentUserEmail())
                }
                _authEvents.emit(AuthEvent.NavigateToVerifyEmail)
            }
        }
    }

    fun onLoginCorreoChange(value: String) {
        _loginUiState.update {
            it.copy(
                correo = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onLoginContrasenaChange(value: String) {
        _loginUiState.update {
            it.copy(
                contrasena = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterNombreChange(value: String) {
        _registerUiState.update {
            it.copy(
                nombre = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterApellidoChange(value: String) {
        _registerUiState.update {
            it.copy(
                apellido = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterEdadChange(value: String) {
        _registerUiState.update {
            it.copy(
                edad = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterCarreraChange(value: String) {
        _registerUiState.update {
            it.copy(
                carrera = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterCorreoChange(value: String) {
        _registerUiState.update {
            it.copy(
                correo = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterContrasenaChange(value: String) {
        _registerUiState.update {
            it.copy(
                contrasena = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterConfirmarContrasenaChange(value: String) {
        _registerUiState.update {
            it.copy(
                confirmarContrasena = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onRegisterAceptaTerminosChange(value: Boolean) {
        _registerUiState.update {
            it.copy(
                aceptaTerminos = value,
                mensajeErrorResId = null
            )
        }
    }

    fun onForgotPasswordCorreoChange(value: String) {
        _forgotPasswordUiState.update {
            it.copy(
                correo = value,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }
    }

    fun iniciarSesion() {
        val correo = _loginUiState.value.correo.trim()
        val contrasena = _loginUiState.value.contrasena

        when {
            !esCorreoValido(correo) -> {
                _loginUiState.update {
                    it.copy(mensajeErrorResId = R.string.auth_error_invalid_email)
                }
                return
            }

            contrasena.isBlank() -> {
                _loginUiState.update {
                    it.copy(mensajeErrorResId = R.string.auth_error_empty_password)
                }
                return
            }
        }

        _loginUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null
            )
        }

        viewModelScope.launch {
            try {
                val emailVerificado = repository.login(correo, contrasena)

                if (emailVerificado) {
                    _authEvents.emit(AuthEvent.ShowSnackbar(R.string.auth_login_success))
                    _authEvents.emit(AuthEvent.NavigateToHome)
                } else {
                    _verifyEmailUiState.update {
                        it.copy(
                            correo = repository.currentUserEmail().ifBlank { correo },
                            mensajeErrorResId = null,
                            mensajeExitoResId = null
                        )
                    }
                    _authEvents.emit(AuthEvent.ShowSnackbar(R.string.auth_error_email_not_verified))
                    _authEvents.emit(AuthEvent.NavigateToVerifyEmail)
                }
            } catch (e: Exception) {
                _loginUiState.update {
                    it.copy(mensajeErrorResId = mapLoginError(e))
                }
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
                _registerUiState.update {
                    it.copy(mensajeErrorResId = R.string.auth_error_invalid_email)
                }
                return
            }

            !esContrasenaValida(contrasena) -> {
                _registerUiState.update {
                    it.copy(mensajeErrorResId = R.string.auth_error_weak_password)
                }
                return
            }

            contrasena != confirmar -> {
                _registerUiState.update {
                    it.copy(mensajeErrorResId = R.string.auth_error_password_mismatch)
                }
                return
            }
        }

        _registerUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null
            )
        }

        viewModelScope.launch {
            try {
                repository.register(
                    email = correo,
                    password = contrasena,
                    nombre = state.nombre.trim(),
                    apellido = state.apellido.trim()
                )

                _verifyEmailUiState.update {
                    it.copy(
                        correo = repository.currentUserEmail().ifBlank { correo },
                        cargando = false,
                        mensajeErrorResId = null,
                        mensajeExitoResId = R.string.verify_email_sent_success
                    )
                }

                _authEvents.emit(AuthEvent.ShowSnackbar(R.string.register_success_verify_email_sent))
                _authEvents.emit(AuthEvent.NavigateToVerifyEmail)

            } catch (e: Exception) {
                _registerUiState.update {
                    it.copy(mensajeErrorResId = mapRegisterError(e))
                }
            } finally {
                _registerUiState.update { it.copy(cargando = false) }
            }
        }
    }

    fun enviarCorreoRecuperacion() {
        val correo = _forgotPasswordUiState.value.correo.trim()

        if (!esCorreoValido(correo)) {
            _forgotPasswordUiState.update {
                it.copy(mensajeErrorResId = R.string.auth_error_invalid_email)
            }
            return
        }

        _forgotPasswordUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }

        viewModelScope.launch {
            try {
                repository.sendPasswordResetEmail(correo)
            } catch (e: FirebaseAuthInvalidUserException) {
                // Se ignora a para que no enumeren nuestros correos
            } catch (e: Exception) {
                _forgotPasswordUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = mapForgotPasswordError(e)
                    )
                }
                return@launch
            }

            _forgotPasswordUiState.update {
                it.copy(
                    cargando = false,
                    correoEnviadoA = correo,
                    mensajeErrorResId = null,
                    mensajeExitoResId = R.string.forgot_password_success_generic
                )
            }

            _authEvents.emit(AuthEvent.NavigateToResetPasswordEmailSent)
        }
    }

    fun reenviarCorreoVerificacion() {
        _verifyEmailUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }

        viewModelScope.launch {
            try {
                repository.resendEmailVerification()
                _verifyEmailUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeExitoResId = R.string.verify_email_resent_success
                    )
                }
            } catch (e: Exception) {
                _verifyEmailUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = mapVerifyEmailError(e)
                    )
                }
            }
        }
    }

    fun revisarEstadoVerificacion() {
        _verifyEmailUiState.update {
            it.copy(
                cargando = true,
                mensajeErrorResId = null,
                mensajeExitoResId = null
            )
        }

        viewModelScope.launch {
            try {
                repository.reloadCurrentUser()

                if (repository.isCurrentUserVerified()) {
                    _verifyEmailUiState.update {
                        it.copy(
                            cargando = false,
                            mensajeExitoResId = R.string.verify_email_confirmed_success
                        )
                    }
                    _authEvents.emit(AuthEvent.NavigateToHome)
                } else {
                    _verifyEmailUiState.update {
                        it.copy(
                            cargando = false,
                            mensajeErrorResId = R.string.verify_email_still_pending
                        )
                    }
                }
            } catch (e: Exception) {
                _verifyEmailUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = mapVerifyEmailError(e)
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        repository.signOut()
        viewModelScope.launch {
            _authEvents.emit(AuthEvent.NavigateToLogin)
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    private fun esContrasenaValida(contrasena: String): Boolean {
        return contrasena.length >= 8
    }

    private fun mapLoginError(e: Exception): Int {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_invalid_credentials
            is FirebaseAuthInvalidUserException -> R.string.auth_error_user_not_found
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }

    private fun mapRegisterError(e: Exception): Int {
        return when (e) {
            is FirebaseAuthWeakPasswordException -> R.string.auth_error_weak_password
            is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_invalid_email
            is FirebaseAuthUserCollisionException -> R.string.auth_error_email_already_in_use
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }

    private fun mapForgotPasswordError(e: Exception): Int {
        return when (e) {
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }

    private fun mapVerifyEmailError(e: Exception): Int {
        return when (e) {
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }
}