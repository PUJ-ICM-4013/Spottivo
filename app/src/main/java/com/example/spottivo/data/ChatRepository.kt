package com.example.spottivo.data

import android.util.Log
import com.example.spottivo.data.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Repositorio para gestionar chats y mensajes
 */
class ChatRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val httpClient = OkHttpClient()
    
    companion object {
        private const val TAG = "ChatRepository"
        private const val CLOUD_FUNCTION_URL = "YOUR_CLOUD_FUNCTION_URL" // Actualizar con tu URL
    }
    
    /**
     * Obtiene o crea un chat ID entre dos usuarios
     */
    suspend fun getOrCreateChatId(friendId: String): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        // El ID del chat es consistente: usuarios ordenados alfabéticamente
        val chatId = if (currentUserId < friendId) {
            "${currentUserId}_${friendId}"
        } else {
            "${friendId}_${currentUserId}"
        }
        
        // Verificar si el chat existe, si no, crearlo
        val chatDoc = firestore.collection("chats").document(chatId).get().await()
        
        if (!chatDoc.exists()) {
            // Crear nuevo chat
            val chatData = hashMapOf(
                "participants" to listOf(currentUserId, friendId),
                "createdAt" to System.currentTimeMillis(),
                "lastMessageTime" to 0L,
                "lastMessage" to ""
            )
            firestore.collection("chats").document(chatId).set(chatData).await()
            Log.d(TAG, "Chat creado: $chatId")
        }
        
        return chatId
    }
    
    /**
     * Envía un mensaje en el chat
     */
    suspend fun sendMessage(
        chatId: String,
        friendId: String,
        message: String
    ): Result<ChatMessage> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "📤 Enviando mensaje de $currentUserId a $friendId en chat $chatId")
            
            // Obtener datos del usuario actual
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val senderName = userDoc.getString("nombre") ?: "Usuario"
            val senderPhotoUrl = userDoc.getString("photoUrl") ?: ""
            
            Log.d(TAG, "👤 Datos del emisor: $senderName")
            
            val messageId = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document().id
            
            val timestamp = System.currentTimeMillis()
            
            // Crear mapa de datos explícito para evitar problemas de serialización
            val messageMap = hashMapOf(
                "id" to messageId,
                "chatId" to chatId,
                "senderId" to currentUserId,
                "senderName" to senderName,
                "senderPhotoUrl" to senderPhotoUrl,
                "message" to message,
                "timestamp" to timestamp,
                "isRead" to false
            )
            
            Log.d(TAG, "💾 Guardando mensaje en Firestore...")
            
            // Guardar mensaje
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageMap)
                .await()
            
            Log.d(TAG, "✅ Mensaje guardado: $messageId")
            
            // Actualizar último mensaje en el chat
            firestore.collection("chats")
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to message,
                        "lastMessageTime" to timestamp
                    )
                )
                .await()
            
            Log.d(TAG, "✅ Chat actualizado con último mensaje")
            
            // Enviar notificación push al destinatario
            sendPushNotification(friendId, senderName, message)
            
            val messageData = ChatMessage(
                id = messageId,
                chatId = chatId,
                senderId = currentUserId,
                senderName = senderName,
                senderPhotoUrl = senderPhotoUrl,
                message = message,
                timestamp = timestamp,
                isRead = false
            )
            
            Result.success(messageData)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando mensaje", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Escucha mensajes de un chat en tiempo real
     */
    fun listenToMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        Log.d(TAG, "👂 Iniciando listener de mensajes para chat: $chatId")
        
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error escuchando mensajes", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    Log.d(TAG, "📨 Snapshot recibido: ${snapshot.documents.size} documentos")
                    
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            Log.d(TAG, "  📄 Documento: ${doc.id}")
                            Log.d(TAG, "     Datos: ${doc.data}")
                            
                            ChatMessage(
                                id = doc.id,
                                chatId = doc.getString("chatId") ?: "",
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                senderPhotoUrl = doc.getString("senderPhotoUrl") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error parseando mensaje ${doc.id}", e)
                            null
                        }
                    }
                    
                    Log.d(TAG, "✅ Mensajes parseados: ${messages.size}")
                    trySend(messages)
                } else {
                    Log.w(TAG, "⚠️ Snapshot es null")
                }
            }
        
        awaitClose { 
            Log.d(TAG, "🛑 Cerrando listener de mensajes")
            listener.remove() 
        }
    }
    
    /**
     * Marca mensajes como leídos
     */
    suspend fun markMessagesAsRead(chatId: String, currentUserId: String) {
        try {
            val messages = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", currentUserId)
                .get()
                .await()
            
            val batch = firestore.batch()
            messages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            Log.d(TAG, "Mensajes marcados como leídos: ${messages.size()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error marcando mensajes como leídos", e)
        }
    }
    
    /**
     * Envía notificación push al usuario
     */
    private suspend fun sendPushNotification(userId: String, senderName: String, message: String) {
        try {
            // Obtener FCM token del destinatario
            val userDoc = firestore.collection("users").document(userId).get().await()
            val fcmToken = userDoc.getString("fcmToken") ?: return
            
            if (fcmToken.isEmpty()) {
                Log.w(TAG, "Usuario no tiene FCM token")
                return
            }
            
            // Crear payload de notificación
            val notification = JSONObject().apply {
                put("title", senderName)
                put("body", message)
            }
            
            val data = JSONObject().apply {
                put("type", "chat_message")
                put("senderId", auth.currentUser?.uid ?: "")
                put("senderName", senderName)
                put("message", message)
            }
            
            val payload = JSONObject().apply {
                put("to", fcmToken)
                put("notification", notification)
                put("data", data)
                put("priority", "high")
            }
            
            // Enviar usando Firebase Cloud Messaging API
            // Nota: En producción, esto debería hacerse desde Cloud Functions
            Log.d(TAG, "Notificación preparada para: $userId")
            Log.d(TAG, "Payload: ${payload.toString(2)}")
            
            // TODO: Implementar envío real via Cloud Functions
            
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando notificación push", e)
        }
    }
}
