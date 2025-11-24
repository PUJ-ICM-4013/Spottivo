package com.example.spottivo.data

import android.util.Log
import com.example.spottivo.data.models.FriendRequest
import com.example.spottivo.data.models.RequestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar solicitudes de amistad
 */
class FriendRequestRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "FriendRequestRepository"
    }
    
    /**
     * Buscar usuario por email
     */
    suspend fun searchUserByEmail(email: String): Result<Map<String, Any>?> {
        return try {
            Log.d(TAG, "🔍 Buscando usuario: $email")
            
            val query = firestore.collection("users")
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()
            
            if (query.isEmpty) {
                Log.d(TAG, "❌ Usuario no encontrado")
                Result.success(null)
            } else {
                val doc = query.documents[0]
                val userData = doc.data?.toMutableMap() ?: mutableMapOf()
                userData["userId"] = doc.id
                
                Log.d(TAG, "✅ Usuario encontrado: ${userData["nombre"]}")
                Result.success(userData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error buscando usuario", e)
            Result.failure(e)
        }
    }
    
    /**
     * Enviar solicitud de amistad
     */
    suspend fun sendFriendRequest(receiverId: String, receiverName: String, receiverEmail: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            val senderId = currentUser.uid
            
            // Verificar que no se envíe solicitud a sí mismo
            if (senderId == receiverId) {
                return Result.failure(Exception("No puedes enviarte una solicitud a ti mismo"))
            }
            
            // Verificar si ya son amigos
            val friendDoc = firestore.collection("users")
                .document(senderId)
                .collection("friends")
                .document(receiverId)
                .get()
                .await()
            
            if (friendDoc.exists()) {
                return Result.failure(Exception("Ya son amigos"))
            }
            
            // Obtener datos del usuario actual
            val senderDoc = firestore.collection("users").document(senderId).get().await()
            val senderData = senderDoc.data ?: return Result.failure(Exception("Error obteniendo datos del usuario"))
            
            // Usar el senderId como requestId para evitar duplicados
            // Si envías otra solicitud, simplemente actualiza la existente
            val requestId = "request_${senderId}"
            
            val request = hashMapOf(
                "requestId" to requestId,
                "senderId" to senderId,
                "senderName" to (senderData["nombre"] ?: ""),
                "senderEmail" to (senderData["email"] ?: ""),
                "senderPhotoUrl" to (senderData["photoUrl"] ?: ""),
                "receiverId" to receiverId,
                "status" to RequestStatus.PENDING.name,
                "timestamp" to System.currentTimeMillis()
            )
            
            // Guardar en la subcolección del receptor
            firestore.collection("users")
                .document(receiverId)
                .collection("friendRequests")
                .document(requestId)
                .set(request)
                .await()
            
            Log.d(TAG, "✅ Solicitud enviada a $receiverName")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando solicitud", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener solicitudes pendientes del usuario actual
     */
    suspend fun getPendingRequests(): Result<List<FriendRequest>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .get()
                .await()
            
            val requests = snapshot.documents.mapNotNull { doc ->
                try {
                    FriendRequest(
                        requestId = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        senderEmail = doc.getString("senderEmail") ?: "",
                        senderPhotoUrl = doc.getString("senderPhotoUrl") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        status = RequestStatus.valueOf(doc.getString("status") ?: "PENDING"),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parseando solicitud", e)
                    null
                }
            }
            
            Log.d(TAG, "✅ Solicitudes pendientes: ${requests.size}")
            Result.success(requests)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitudes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Aceptar solicitud de amistad
     */
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Actualizar estado de la solicitud
            firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(request.requestId)
                .update("status", RequestStatus.ACCEPTED.name)
                .await()
            
            // Agregar al remitente como amigo
            val friendData = hashMapOf(
                "userId" to request.senderId,
                "nombre" to request.senderName,
                "email" to request.senderEmail,
                "photoUrl" to request.senderPhotoUrl,
                "addedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(request.senderId)
                .set(friendData)
                .await()
            
            // Agregar al usuario actual como amigo del remitente (bidireccional)
            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            val currentUserData = currentUserDoc.data
            
            val myData = hashMapOf(
                "userId" to currentUserId,
                "nombre" to (currentUserData?.get("nombre") ?: ""),
                "email" to (currentUserData?.get("email") ?: ""),
                "photoUrl" to (currentUserData?.get("photoUrl") ?: ""),
                "addedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(request.senderId)
                .collection("friends")
                .document(currentUserId)
                .set(myData)
                .await()
            
            Log.d(TAG, "✅ Solicitud aceptada de ${request.senderName}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error aceptando solicitud", e)
            Result.failure(e)
        }
    }
    
    /**
     * Rechazar solicitud de amistad
     */
    suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(requestId)
                .update("status", RequestStatus.REJECTED.name)
                .await()
            
            Log.d(TAG, "✅ Solicitud rechazada")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error rechazando solicitud", e)
            Result.failure(e)
        }
    }
}
