package com.example.spottivo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.models.AddressResult
import com.example.spottivo.data.models.FriendLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * ViewModel para el mapa estilo FindMy
 * Escucha ubicaciones en tiempo real de amigos
 */
class MapViewModel : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val httpClient = OkHttpClient()
    
    private val _friendsLocations = MutableStateFlow<List<FriendLocation>>(emptyList())
    val friendsLocations: StateFlow<List<FriendLocation>> = _friendsLocations.asStateFlow()
    
    private val _selectedFriend = MutableStateFlow<FriendLocation?>(null)
    val selectedFriend: StateFlow<FriendLocation?> = _selectedFriend.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _myLocation = MutableStateFlow<FriendLocation?>(null)
    val myLocation: StateFlow<FriendLocation?> = _myLocation.asStateFlow()
    
    private var locationsListener: ListenerRegistration? = null
    
    companion object {
        private const val TAG = "MapViewModel"
    }
    
    init {
        startListeningToFriendsLocations()
    }
    
    /**
     * Escucha en tiempo real las ubicaciones de todos los amigos
     */
    private fun startListeningToFriendsLocations() {
        val currentUserId = auth.currentUser?.uid
        
        if (currentUserId == null) {
            Log.e(TAG, "❌ Usuario no autenticado")
            return
        }
        
        Log.d(TAG, "🔍 Usuario actual: $currentUserId")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 1. Obtener lista de amigos
                val friendsSnapshot = firestore
                    .collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .get()
                    .await()
                
                val friendIds = friendsSnapshot.documents.map { it.id }
                
                Log.d(TAG, "👥 Amigos encontrados: ${friendIds.size}")
                friendIds.forEach { Log.d(TAG, "  - $it") }
                
                if (friendIds.isEmpty()) {
                    _friendsLocations.value = emptyList()
                    _isLoading.value = false
                    Log.w(TAG, "⚠️ No tienes amigos agregados")
                    return@launch
                }
                
                Log.d(TAG, "📍 Escuchando ubicaciones de ${friendIds.size} amigos")
                
                // 2. Escuchar cambios en la colección "locations"
                locationsListener = firestore.collection("locations")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error escuchando ubicaciones", error)
                            return@addSnapshotListener
                        }
                        
                        if (snapshot == null) return@addSnapshotListener
                        
                        viewModelScope.launch {
                            val locations = mutableListOf<FriendLocation>()
                            
                            for (doc in snapshot.documents) {
                                val userId = doc.id
                                
                                // Incluir amigos Y al usuario actual
                                if (!friendIds.contains(userId) && userId != currentUserId) continue
                                
                                try {
                                    // Obtener datos de ubicación
                                    val latitude = doc.getDouble("latitude") ?: continue
                                    val longitude = doc.getDouble("longitude") ?: continue
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    
                                    // Obtener datos del usuario
                                    val userDoc = firestore.collection("users")
                                        .document(userId)
                                        .get()
                                        .await()
                                    
                                    val nombre = userDoc.getString("nombre") ?: "Usuario"
                                    val email = userDoc.getString("email") ?: ""
                                    val photoUrl = userDoc.getString("photoUrl") ?: ""
                                    val isOnline = userDoc.getBoolean("isOnline") ?: false
                                    
                                    // Solo agregar si está en línea
                                    if (isOnline) {
                                        locations.add(
                                            FriendLocation(
                                                userId = userId,
                                                nombre = if (userId == currentUserId) "Tú" else nombre,
                                                email = email,
                                                photoUrl = photoUrl,
                                                latitude = latitude,
                                                longitude = longitude,
                                                isOnline = isOnline,
                                                lastUpdate = timestamp
                                            )
                                        )
                                        Log.d(TAG, "📍 Agregado: ${if (userId == currentUserId) "Tú" else nombre} - foto: ${photoUrl.take(30)}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error procesando ubicación de $userId", e)
                                }
                            }
                            
                            _friendsLocations.value = locations
                            _isLoading.value = false
                            Log.d(TAG, "✅ Actualizadas ${locations.size} ubicaciones de amigos")
                            if (locations.isEmpty()) {
                                Log.w(TAG, "⚠️ No se encontraron ubicaciones para mostrar")
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error iniciando listener de ubicaciones", e)
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Selecciona un amigo para mostrar sus detalles
     */
    fun selectFriend(friend: FriendLocation?) {
        _selectedFriend.value = friend
        
        // Si se selecciona un amigo, obtener su dirección
        if (friend != null) {
            viewModelScope.launch {
                val address = getAddressFromCoordinates(friend.latitude, friend.longitude)
                _selectedFriend.value = friend.copy(address = address.formattedAddress)
            }
        }
    }
    
    /**
     * Obtiene la dirección formateada a partir de coordenadas (geocoding inverso)
     * Usa Nominatim de OpenStreetMap (gratis, sin API key)
     */
    private suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): AddressResult = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?" +
                    "format=json&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Spottivo-Android")
                .get()
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext AddressResult(formattedAddress = "Ubicación no disponible")
                }
                
                val json = JSONObject(response.body?.string() ?: "{}")
                val displayName = json.optString("display_name", "Dirección no disponible")
                
                val address = json.optJSONObject("address")
                val street = address?.optString("road") ?: ""
                val city = address?.optString("city") ?: address?.optString("town") ?: ""
                val country = address?.optString("country") ?: ""
                
                AddressResult(
                    formattedAddress = displayName,
                    street = street,
                    city = city,
                    country = country
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo dirección", e)
            AddressResult(formattedAddress = "Error obteniendo dirección")
        }
    }
    
    /**
     * Obtiene la ubicación del usuario actual desde Firestore
     */
    suspend fun getCurrentUserLocation(): FriendLocation? = withContext(Dispatchers.IO) {
        val currentUserId = auth.currentUser?.uid ?: return@withContext null
        
        try {
            // Obtener ubicación guardada
            val locationDoc = firestore.collection("locations")
                .document(currentUserId)
                .get()
                .await()
            
            if (!locationDoc.exists()) {
                Log.w(TAG, "⚠️ Usuario no tiene ubicación guardada")
                return@withContext null
            }
            
            val latitude = locationDoc.getDouble("latitude") ?: return@withContext null
            val longitude = locationDoc.getDouble("longitude") ?: return@withContext null
            val timestamp = locationDoc.getLong("timestamp") ?: System.currentTimeMillis()
            
            // Obtener datos del usuario
            val userDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
            
            val nombre = userDoc.getString("nombre") ?: "Tú"
            val email = userDoc.getString("email") ?: ""
            val photoUrl = userDoc.getString("photoUrl") ?: ""
            
            val location = FriendLocation(
                userId = currentUserId,
                nombre = "Tú",
                email = email,
                photoUrl = photoUrl,
                latitude = latitude,
                longitude = longitude,
                isOnline = true,
                lastUpdate = timestamp
            )
            
            _myLocation.value = location
            Log.d(TAG, "📍 Mi ubicación obtenida: ($latitude, $longitude)")
            
            location
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo mi ubicación", e)
            null
        }
    }
    
    /**
     * Refrescar manualmente las ubicaciones
     */
    fun refresh() {
        // El listener ya actualiza automáticamente, pero por si acaso
        viewModelScope.launch {
            _isLoading.value = true
            getCurrentUserLocation()
            // Esperar un momento para dar feedback visual
            kotlinx.coroutines.delay(500)
            _isLoading.value = false
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        locationsListener?.remove()
        Log.d(TAG, "Listener de ubicaciones detenido")
    }
}
