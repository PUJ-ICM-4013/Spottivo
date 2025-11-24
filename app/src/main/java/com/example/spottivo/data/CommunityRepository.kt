package com.example.spottivo.data

import android.net.Uri
import android.util.Log
import com.example.spottivo.model.Community
import com.example.spottivo.model.CommunityMember
import com.example.spottivo.model.GroupMessage
import com.example.spottivo.model.MemberRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommunityRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    companion object {
        private const val TAG = "CommunityRepository"
        private const val COMMUNITIES_COLLECTION = "communities"
        private const val MEMBERS_COLLECTION = "members"
        private const val MESSAGES_COLLECTION = "messages"
    }
    
    /**
     * Crear una nueva comunidad
     */
    suspend fun createCommunity(
        nombre: String,
        descripcion: String,
        photoUrl: String = "",
        isPublic: Boolean = true
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Obtener datos del usuario actual
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val userName = userDoc.getString("nombre") ?: "Usuario"
            
            val communityId = firestore.collection(COMMUNITIES_COLLECTION).document().id
            
            val communityData = hashMapOf(
                "id" to communityId,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "photoUrl" to photoUrl,
                "creatorId" to currentUser.uid,
                "creatorName" to userName,
                "createdAt" to System.currentTimeMillis(),
                "memberCount" to 1,
                "isPublic" to isPublic
            )
            
            // Crear la comunidad
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .set(communityData)
                .await()
            
            // Agregar al creador como miembro
            val memberData = hashMapOf(
                "userId" to currentUser.uid,
                "nombre" to userName,
                "email" to (userDoc.getString("email") ?: ""),
                "photoUrl" to (userDoc.getString("photoUrl") ?: ""),
                "joinedAt" to System.currentTimeMillis(),
                "role" to MemberRole.CREATOR.name
            )
            
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .collection(MEMBERS_COLLECTION)
                .document(currentUser.uid)
                .set(memberData)
                .await()
            
            Log.d(TAG, "✅ Comunidad creada: $nombre")
            Result.success(communityId)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando comunidad", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todas las comunidades públicas
     */
    fun getAllPublicCommunities(): Flow<List<Community>> = callbackFlow {
        // Hacer una consulta única primero
        firestore.collection(COMMUNITIES_COLLECTION)
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val communities = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Community::class.java)?.copy(id = doc.id)
                }
                Log.d(TAG, "✅ ${communities.size} comunidades públicas encontradas (consulta única)")
                trySend(communities)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "❌ Error obteniendo comunidades", error)
                trySend(emptyList())
            }
        
        // Intentar también el listener (cuando el índice esté listo funcionará)
        val listener = firestore.collection(COMMUNITIES_COLLECTION)
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error escuchando comunidades (esperando índice)", error)
                    return@addSnapshotListener
                }
                
                val communities = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Community::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                Log.d(TAG, "✅ ${communities.size} comunidades públicas encontradas (listener)")
                trySend(communities)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Obtener comunidades del usuario actual
     */
    fun getMyCommunities(): Flow<List<Community>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collectionGroup(MEMBERS_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error escuchando mis comunidades", error)
                    // Enviar lista vacía en lugar de cerrar el Flow
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val communityIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.reference.parent.parent?.id
                } ?: emptyList()
                
                if (communityIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                // Obtener detalles de cada comunidad usando document IDs
                firestore.collection(COMMUNITIES_COLLECTION)
                    .whereIn(FieldPath.documentId(), communityIds)
                    .get()
                    .addOnSuccessListener { communitiesSnapshot ->
                        val communities = communitiesSnapshot.documents.mapNotNull { doc ->
                            doc.toObject(Community::class.java)?.copy(id = doc.id)
                        }
                        trySend(communities)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error obteniendo detalles de comunidades", e)
                        trySend(emptyList())
                    }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Unirse a una comunidad
     */
    suspend fun joinCommunity(communityId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Verificar si ya es miembro
            val memberDoc = firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .collection(MEMBERS_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await()
            
            if (memberDoc.exists()) {
                return Result.failure(Exception("Ya eres miembro de esta comunidad"))
            }
            
            // Obtener datos del usuario
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            
            val memberData = hashMapOf(
                "userId" to currentUser.uid,
                "nombre" to (userDoc.getString("nombre") ?: "Usuario"),
                "email" to (userDoc.getString("email") ?: ""),
                "photoUrl" to (userDoc.getString("photoUrl") ?: ""),
                "joinedAt" to System.currentTimeMillis(),
                "role" to MemberRole.MEMBER.name
            )
            
            // Agregar como miembro
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .collection(MEMBERS_COLLECTION)
                .document(currentUser.uid)
                .set(memberData)
                .await()
            
            // Incrementar contador de miembros
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .update("memberCount", FieldValue.increment(1))
                .await()
            
            Log.d(TAG, "✅ Usuario unido a comunidad")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uniéndose a comunidad", e)
            Result.failure(e)
        }
    }
    
    /**
     * Salir de una comunidad
     */
    suspend fun leaveCommunity(communityId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Verificar si es el creador
            val communityDoc = firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .get()
                .await()
            
            if (communityDoc.getString("creatorId") == currentUser.uid) {
                return Result.failure(Exception("El creador no puede salir de la comunidad. Debes eliminarla."))
            }
            
            // Eliminar de miembros
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .collection(MEMBERS_COLLECTION)
                .document(currentUser.uid)
                .delete()
                .await()
            
            // Decrementar contador de miembros
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .update("memberCount", FieldValue.increment(-1))
                .await()
            
            Log.d(TAG, "✅ Usuario salió de la comunidad")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saliendo de comunidad", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener miembros de una comunidad
     */
    fun getCommunityMembers(communityId: String): Flow<List<CommunityMember>> = callbackFlow {
        val listener = firestore.collection(COMMUNITIES_COLLECTION)
            .document(communityId)
            .collection(MEMBERS_COLLECTION)
            .orderBy("joinedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error escuchando miembros", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val members = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CommunityMember(
                            userId = doc.getString("userId") ?: "",
                            nombre = doc.getString("nombre") ?: "",
                            email = doc.getString("email") ?: "",
                            photoUrl = doc.getString("photoUrl") ?: "",
                            joinedAt = doc.getLong("joinedAt") ?: 0,
                            role = MemberRole.valueOf(doc.getString("role") ?: "MEMBER")
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(members)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Enviar mensaje en la comunidad
     */
    suspend fun sendGroupMessage(communityId: String, messageText: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Obtener datos del usuario
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            
            val messageData = hashMapOf(
                "senderId" to currentUser.uid,
                "senderName" to (userDoc.getString("nombre") ?: "Usuario"),
                "senderPhotoUrl" to (userDoc.getString("photoUrl") ?: ""),
                "message" to messageText,
                "timestamp" to System.currentTimeMillis(),
                "readBy" to listOf(currentUser.uid)
            )
            
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .collection(MESSAGES_COLLECTION)
                .add(messageData)
                .await()
            
            Log.d(TAG, "✅ Mensaje enviado a la comunidad")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando mensaje", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener mensajes de la comunidad
     */
    fun getCommunityMessages(communityId: String): Flow<List<GroupMessage>> = callbackFlow {
        val listener = firestore.collection(COMMUNITIES_COLLECTION)
            .document(communityId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error escuchando mensajes", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        GroupMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            senderPhotoUrl = doc.getString("senderPhotoUrl") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0,
                            readBy = (doc.get("readBy") as? List<String>) ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Subir foto de perfil de la comunidad
     */
    suspend fun uploadCommunityPhoto(communityId: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child("communities/$communityId/profile.jpg")
            
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            
            // Actualizar la comunidad con la nueva foto
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .update("photoUrl", downloadUrl.toString())
                .await()
            
            Log.d(TAG, "✅ Foto de comunidad subida")
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error subiendo foto", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar información de la comunidad (solo creador)
     */
    suspend fun updateCommunity(
        communityId: String,
        nombre: String,
        descripcion: String,
        photoUrl: String? = null
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
            
            // Verificar que sea el creador
            val communityDoc = firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .get()
                .await()
            
            if (communityDoc.getString("creatorId") != currentUser.uid) {
                return Result.failure(Exception("Solo el creador puede editar la comunidad"))
            }
            
            val updates = hashMapOf<String, Any>(
                "nombre" to nombre,
                "descripcion" to descripcion
            )
            
            // Agregar photoUrl si se proporcionó
            if (photoUrl != null) {
                updates["photoUrl"] = photoUrl
            }
            
            firestore.collection(COMMUNITIES_COLLECTION)
                .document(communityId)
                .update(updates)
                .await()
            
            Log.d(TAG, "✅ Comunidad actualizada")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando comunidad", e)
            Result.failure(e)
        }
    }
}
