package com.example.spottivo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.ChatRepository
import com.example.spottivo.data.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de chat individual
 */
class ChatViewModel : ViewModel() {
    
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()
    
    private var currentChatId: String? = null
    private var currentFriendId: String? = null
    private var listenerJob: Job? = null
    
    companion object {
        private const val TAG = "ChatViewModel"
    }
    
    /**
     * Inicia el chat con un amigo
     */
    fun startChat(friendId: String, friendName: String) {
        // Si ya hay un chat activo con este amigo, no reiniciar
        if (currentFriendId == friendId && listenerJob?.isActive == true) {
            Log.d(TAG, "⚠️ Chat ya iniciado con $friendName")
            return
        }
        
        // Cancelar listener anterior si existe
        listenerJob?.cancel()
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentFriendId = friendId
                
                Log.d(TAG, "🔄 Obteniendo o creando chat con $friendName (ID: $friendId)")
                
                // Obtener o crear chat ID
                val chatId = chatRepository.getOrCreateChatId(friendId)
                currentChatId = chatId
                
                Log.d(TAG, "✅ Chat iniciado: $chatId con $friendName")
                
                _isLoading.value = false
                
                // Escuchar mensajes en tiempo real en un Job separado
                listenerJob = viewModelScope.launch {
                    chatRepository.listenToMessages(chatId).collect { messagesList ->
                        Log.d(TAG, "📨 Mensajes recibidos: ${messagesList.size}")
                        messagesList.forEach { msg ->
                            Log.d(TAG, "  - ${msg.senderName}: ${msg.message}")
                        }
                        _messages.value = messagesList
                        
                        // Marcar mensajes como leídos
                        val currentUserId = auth.currentUser?.uid
                        if (currentUserId != null) {
                            chatRepository.markMessagesAsRead(chatId, currentUserId)
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error iniciando chat", e)
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return
        
        val chatId = currentChatId ?: run {
            Log.e(TAG, "❌ chatId es null")
            return
        }
        val friendId = currentFriendId ?: run {
            Log.e(TAG, "❌ friendId es null")
            return
        }
        
        viewModelScope.launch {
            try {
                _isSending.value = true
                Log.d(TAG, "📤 Enviando mensaje: '$messageText'")
                
                val result = chatRepository.sendMessage(chatId, friendId, messageText.trim())
                
                if (result.isSuccess) {
                    Log.d(TAG, "✅ Mensaje enviado exitosamente")
                } else {
                    Log.e(TAG, "❌ Error enviando mensaje: ${result.exceptionOrNull()?.message}")
                    result.exceptionOrNull()?.printStackTrace()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en sendMessage", e)
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        listenerJob?.cancel()
        Log.d(TAG, "🧹 ChatViewModel limpiado")
    }
}
