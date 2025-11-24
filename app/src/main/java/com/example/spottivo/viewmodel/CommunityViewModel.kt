package com.example.spottivo.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.CloudinaryService
import com.example.spottivo.data.CommunityRepository
import com.example.spottivo.model.Community
import com.example.spottivo.model.CommunityMember
import com.example.spottivo.model.GroupMessage
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
    private val cloudinaryService = CloudinaryService()
    
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

// ===== VIEWMODELS PARA COMUNIDADES =====

class CommunityListViewModel : ViewModel() {
    private val repository = CommunityRepository()
    
    private val _allCommunities = MutableStateFlow<List<Community>>(emptyList())
    val allCommunities: StateFlow<List<Community>> = _allCommunities.asStateFlow()
    
    private val _myCommunities = MutableStateFlow<List<Community>>(emptyList())
    val myCommunities: StateFlow<List<Community>> = _myCommunities.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadCommunities()
    }
    
    private fun loadCommunities() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar comunidades públicas
            repository.getAllPublicCommunities().collect { communities ->
                _allCommunities.value = communities
                _isLoading.value = false
            }
        }
        
        viewModelScope.launch {
            // Cargar mis comunidades
            repository.getMyCommunities().collect { communities ->
                _myCommunities.value = communities
            }
        }
    }
    
    fun createCommunity(
        nombre: String,
        descripcion: String,
        photoUri: Uri?,
        isPublic: Boolean = true,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            var photoUrl = ""
            
            // Si hay foto, subirla primero
            if (photoUri != null) {
                val tempCommunityId = "temp_${System.currentTimeMillis()}"
                val uploadResult = repository.uploadCommunityPhoto(tempCommunityId, photoUri)
                if (uploadResult.isSuccess) {
                    photoUrl = uploadResult.getOrNull() ?: ""
                }
            }
            
            val result = repository.createCommunity(nombre, descripcion, photoUrl, isPublic)
            
            _isLoading.value = false
            
            if (result.isSuccess) {
                val communityId = result.getOrNull() ?: ""
                onSuccess(communityId)
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun joinCommunity(communityId: String) {
        viewModelScope.launch {
            val result = repository.joinCommunity(communityId)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class CommunityDetailViewModel : ViewModel() {
    private val repository = CommunityRepository()
    private val cloudinaryService = CloudinaryService()
    
    private val _members = MutableStateFlow<List<CommunityMember>>(emptyList())
    val members: StateFlow<List<CommunityMember>> = _members.asStateFlow()
    
    private val _messages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val messages: StateFlow<List<GroupMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()
    
    private var currentCommunityId: String? = null
    
    companion object {
        private const val TAG = "CommunityDetailViewModel"
    }
    
    fun loadCommunity(communityId: String) {
        if (currentCommunityId == communityId) return
        
        currentCommunityId = communityId
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar miembros
            repository.getCommunityMembers(communityId).collect { membersList ->
                _members.value = membersList
                _isLoading.value = false
            }
        }
        
        viewModelScope.launch {
            // Cargar mensajes
            repository.getCommunityMessages(communityId).collect { messagesList ->
                _messages.value = messagesList
            }
        }
    }
    
    fun sendMessage(messageText: String) {
        val communityId = currentCommunityId ?: return
        
        viewModelScope.launch {
            _isSending.value = true
            repository.sendGroupMessage(communityId, messageText)
            _isSending.value = false
        }
    }
    
    fun joinCommunity(communityId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.joinCommunity(communityId)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }
    
    fun leaveCommunity(communityId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.leaveCommunity(communityId)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }
    
    fun updateCommunity(
        communityId: String,
        nombre: String,
        descripcion: String,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                var photoUrl: String? = null
                
                // Si hay una imagen nueva, subirla a Cloudinary
                if (imageUri != null) {
                    photoUrl = cloudinaryService.uploadImage(
                        imageUri = imageUri,
                        folder = "spottivo/communities",
                        publicId = "community_$communityId"
                    )
                }
                
                val result = repository.updateCommunity(communityId, nombre, descripcion, photoUrl)
                if (result.isSuccess) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando comunidad", e)
            }
        }
    }
}
