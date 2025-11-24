package com.example.spottivo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Modelo de datos para un amigo en la lista de mensajes
 */
data class Friend(
    val userId: String = "",
    val nombre: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

/**
 * ViewModel para la pantalla de Comunidad
 */
class CommunityViewModel : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var friendsListener: ListenerRegistration? = null
    
    companion object {
        private const val TAG = "CommunityViewModel"
    }
    
    init {
        loadFriends()
    }
    
    /**
     * Carga la lista de amigos del usuario actual
     */
    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid
        
        if (currentUserId == null) {
            Log.e(TAG, "❌ Usuario no autenticado")
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Obtener lista de amigos
                val friendsSnapshot = firestore
                    .collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .get()
                    .await()
                
                val friendIds = friendsSnapshot.documents.map { it.id }
                
                Log.d(TAG, "👥 Amigos encontrados: ${friendIds.size}")
                
                if (friendIds.isEmpty()) {
                    _friends.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                // Escuchar cambios en los datos de los amigos
                startListeningToFriends(friendIds)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando amigos", e)
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Escucha en tiempo real los cambios de estado de los amigos
     */
    private fun startListeningToFriends(friendIds: List<String>) {
        friendsListener?.remove()
        
        // Escuchar la colección users para obtener el estado online
        friendsListener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error escuchando amigos", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                viewModelScope.launch {
                    val friendsList = mutableListOf<Friend>()
                    
                    for (doc in snapshot.documents) {
                        if (!friendIds.contains(doc.id)) continue
                        
                        try {
                            val nombre = doc.getString("nombre") ?: "Usuario"
                            val email = doc.getString("email") ?: ""
                            val photoUrl = doc.getString("photoUrl") ?: ""
                            val isOnline = doc.getBoolean("isOnline") ?: false
                            val lastSeen = doc.getLong("lastSeen") ?: 0L
                            
                            friendsList.add(
                                Friend(
                                    userId = doc.id,
                                    nombre = nombre,
                                    email = email,
                                    photoUrl = photoUrl,
                                    isOnline = isOnline,
                                    lastSeen = lastSeen
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error procesando amigo ${doc.id}", e)
                        }
                    }
                    
                    // Ordenar: primero los que están online, luego por nombre
                    val sorted = friendsList.sortedWith(
                        compareByDescending<Friend> { it.isOnline }
                            .thenBy { it.nombre }
                    )
                    
                    _friends.value = sorted
                    _isLoading.value = false
                    
                    Log.d(TAG, "✅ Amigos actualizados: ${friendsList.size}")
                    Log.d(TAG, "   Online: ${friendsList.count { it.isOnline }}")
                    Log.d(TAG, "   Offline: ${friendsList.count { !it.isOnline }}")
                }
            }
    }
    
    /**
     * Refrescar manualmente la lista de amigos
     */
    fun refresh() {
        loadFriends()
    }
    
    override fun onCleared() {
        super.onCleared()
        friendsListener?.remove()
        Log.d(TAG, "Listener de amigos detenido")
    }
}
