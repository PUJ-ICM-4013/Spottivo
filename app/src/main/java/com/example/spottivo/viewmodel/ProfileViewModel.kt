package com.example.spottivo.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.AuthRepository
import com.example.spottivo.data.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUpdating: Boolean = false
)

// Enum para seleccionar el proveedor de almacenamiento
enum class StorageProvider {
    FIREBASE_STORAGE,
    CLOUDINARY
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val cloudinaryService = CloudinaryService()
    
    // ⚠️ CAMBIA ESTO PARA ELEGIR EL PROVEEDOR
    private val storageProvider = StorageProvider.CLOUDINARY // o FIREBASE_STORAGE
    
    var userName by mutableStateOf("")
        private set
    var userEmail by mutableStateOf("")
        private set
    var userPhotoUri by mutableStateOf<String?>(null)
        private set
    var userId by mutableStateOf("")
        private set
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "ProfileViewModel"
    }
    
    init {
        loadUserProfile()
    }
    
    /**
     * Cargar perfil del usuario desde Firestore
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No hay usuario autenticado"
                    )
                    return@launch
                }
                
                userId = currentUser.uid
                userEmail = currentUser.email ?: ""
                
                // Obtener datos adicionales de Firestore
                val userDoc = firestore.collection("users").document(userId).get().await()
                
                if (userDoc.exists()) {
                    userName = userDoc.getString("nombre") ?: "Usuario"
                    userPhotoUri = userDoc.getString("photoUrl")
                    
                    Log.d(TAG, "Perfil cargado: $userName, $userEmail")
                } else {
                    userName = "Usuario"
                    Log.w(TAG, "Documento de usuario no encontrado en Firestore")
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando perfil", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar perfil: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Actualizar nombre del usuario
     */
    fun updateUserName(newName: String) {
        if (newName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El nombre no puede estar vacío")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)
                
                val currentUser = auth.currentUser ?: throw Exception("No hay usuario autenticado")
                
                // Actualizar en Firestore
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("nombre", newName)
                    .await()
                
                userName = newName
                
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    successMessage = "Nombre actualizado correctamente"
                )
                
                Log.d(TAG, "Nombre actualizado a: $newName")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando nombre", e)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = "Error al actualizar: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Subir foto de perfil - Soporta Firebase Storage Y Cloudinary
     */
    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)
                
                val currentUser = auth.currentUser ?: throw Exception("No hay usuario autenticado")
                
                // Elegir proveedor según configuración
                val downloadUrl = when (storageProvider) {
                    StorageProvider.FIREBASE_STORAGE -> uploadToFirebaseStorage(imageUri, currentUser.uid)
                    StorageProvider.CLOUDINARY -> uploadToCloudinary(imageUri, currentUser.uid)
                }
                
                // Actualizar en Firestore
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("photoUrl", downloadUrl)
                    .await()
                
                userPhotoUri = downloadUrl
                
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    successMessage = "Foto actualizada correctamente"
                )
                
                Log.d(TAG, "Foto subida ($storageProvider): $downloadUrl")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo foto", e)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = "Error al subir foto: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Subir a Firebase Storage
     */
    private suspend fun uploadToFirebaseStorage(imageUri: Uri, userId: String): String {
        val storageRef = storage.reference
            .child("users")
            .child(userId)
            .child("profile_${System.currentTimeMillis()}.jpg")
        
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
    
    /**
     * Subir a Cloudinary
     */
    private suspend fun uploadToCloudinary(imageUri: Uri, userId: String): String {
        return cloudinaryService.uploadImage(
            imageUri = imageUri,
            folder = "spottivo/profiles",
            publicId = "user_$userId"
        )
    }
    
    /**
     * Actualizar foto localmente (temporal hasta que se guarde)
     */
    fun updatePhotoUri(uri: String?) {
        userPhotoUri = uri
    }
    
    /**
     * Cerrar sesión
     */
    fun logout() {
        authRepository.logout()
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}