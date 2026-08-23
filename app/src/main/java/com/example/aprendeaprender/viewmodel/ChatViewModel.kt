package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.data.auth.UtadeoCredentialsStore
import com.example.aprendeaprender.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val credentialsStore: UtadeoCredentialsStore
) : ViewModel() {

    private val _inboxState = MutableStateFlow(ChatInboxUiState())
    val inboxState: StateFlow<ChatInboxUiState> = _inboxState.asStateFlow()

    private val _conversationState = MutableStateFlow(ChatConversationUiState())
    val conversationState: StateFlow<ChatConversationUiState> = _conversationState.asStateFlow()

    fun cargarBandeja() {
        val creds = credentialsStore.obtener()
        if (creds == null) {
            _inboxState.update { it.copy(sinCredenciales = true, cargando = false) }
            return
        }

        _inboxState.update { it.copy(cargando = true, error = null, sinCredenciales = false) }

        viewModelScope.launch {
            try {
                android.util.Log.d("CHAT_VM", "Cargando bandeja para ${creds.usuario}")
                val resultado = chatRepository.cargarBandeja(creds.usuario, creds.contrasena)
                _inboxState.update {
                    it.copy(
                        cargando = false,
                        conversaciones = resultado.conversaciones,
                        miUserId = resultado.miUserId
                    )
                }
                android.util.Log.d("CHAT_VM", "✓ ${resultado.conversaciones.size} conversaciones")
            } catch (e: Exception) {
                android.util.Log.e("CHAT_VM", "Error cargando bandeja: ${e.message}", e)
                _inboxState.update {
                    it.copy(cargando = false, error = e.message ?: "Error al cargar el chat")
                }
            }
        }
    }

    fun abrirConversacion(conversationId: Long) {
        val creds = credentialsStore.obtener() ?: return
        val convMeta = _inboxState.value.conversaciones.firstOrNull { it.id == conversationId }

        _conversationState.update {
            ChatConversationUiState(cargando = true, conversacion = convMeta)
        }

        viewModelScope.launch {
            try {
                val resultado = chatRepository.cargarMensajes(creds.usuario, creds.contrasena, conversationId)
                _conversationState.update {
                    it.copy(
                        cargando = false,
                        mensajes = resultado.mensajes,
                        miUserId = resultado.miUserId
                    )
                }
                android.util.Log.d("CHAT_VM", "✓ ${resultado.mensajes.size} mensajes")
            } catch (e: Exception) {
                android.util.Log.e("CHAT_VM", "Error mensajes: ${e.message}", e)
                _conversationState.update {
                    it.copy(cargando = false, error = e.message ?: "Error al cargar los mensajes")
                }
            }
        }
    }

    fun onInputChange(texto: String) {
        _conversationState.update { it.copy(textoInput = texto) }
    }

    fun enviarMensaje() {
        val state = _conversationState.value
        val texto = state.textoInput.trim()
        val convId = state.conversacion?.id ?: return
        if (texto.isBlank()) return
        val creds = credentialsStore.obtener() ?: return

        _conversationState.update { it.copy(enviando = true, error = null) }

        viewModelScope.launch {
            try {
                val msg = chatRepository.enviarMensaje(creds.usuario, creds.contrasena, convId, texto)
                if (msg != null) {
                    _conversationState.update {
                        it.copy(
                            enviando = false,
                            textoInput = "",
                            mensajes = it.mensajes + msg.copy(fromMe = true)
                        )
                    }
                    android.util.Log.d("CHAT_VM", "✓ Mensaje enviado: ${msg.id}")
                } else {
                    _conversationState.update {
                        it.copy(enviando = false, error = "El servidor no confirmó el envío")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CHAT_VM", "Error enviando: ${e.message}", e)
                _conversationState.update { it.copy(enviando = false, error = e.message) }
            }
        }
    }

    fun limpiarError() {
        _inboxState.update { it.copy(error = null) }
        _conversationState.update { it.copy(error = null) }
    }
}