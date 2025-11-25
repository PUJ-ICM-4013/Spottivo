package com.example.spottivo.data.repository

import android.util.Log
import com.example.spottivo.data.models.SportPlace
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar sitios deportivos en Firestore
 */
class SportPlaceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sportPlacesCollection = db.collection("sportPlaces")
    
    companion object {
        private const val TAG = "SportPlaceRepository"
    }
    
    /**
     * Obtener todos los sitios deportivos en tiempo real
     */
    fun getAllSportPlaces(): Flow<List<SportPlace>> = callbackFlow {
        val listener = sportPlacesCollection
            .whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al obtener sitios deportivos", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val places = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SportPlace::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear sitio: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "✅ Sitios deportivos obtenidos: ${places.size}")
                trySend(places)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Obtener sitios deportivos filtrados por tags
     */
    fun getSportPlacesByTags(tags: List<String>): Flow<List<SportPlace>> = callbackFlow {
        if (tags.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listener = sportPlacesCollection
            .whereEqualTo("activo", true)
            .whereArrayContainsAny("tags", tags)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al filtrar sitios por tags", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val places = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SportPlace::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear sitio: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "✅ Sitios filtrados por tags ${tags}: ${places.size}")
                trySend(places)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Obtener sitios deportivos por propietario
     */
    fun getSportPlacesByOwner(ownerEmail: String): Flow<List<SportPlace>> = callbackFlow {
        val listener = sportPlacesCollection
            .whereEqualTo("propietarioEmail", ownerEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al obtener sitios del propietario", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val places = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SportPlace::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear sitio: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "✅ Sitios del propietario $ownerEmail: ${places.size}")
                trySend(places)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Obtener sitios deportivos cercanos (dentro de un radio aproximado)
     */
    fun getSportPlacesNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Flow<List<SportPlace>> = callbackFlow {
        // Cálculo aproximado de grados por km (para Bogotá)
        val latDelta = radiusKm / 111.0
        val lonDelta = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(latitude)))
        
        val listener = sportPlacesCollection
            .whereEqualTo("activo", true)
            .whereGreaterThanOrEqualTo("latitud", latitude - latDelta)
            .whereLessThanOrEqualTo("latitud", latitude + latDelta)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al obtener sitios cercanos", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val places = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SportPlace::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear sitio: ${doc.id}", e)
                        null
                    }
                }?.filter {
                    // Filtro adicional por longitud
                    it.longitud >= longitude - lonDelta && it.longitud <= longitude + lonDelta
                } ?: emptyList()
                
                Log.d(TAG, "✅ Sitios cercanos a ($latitude, $longitude): ${places.size}")
                trySend(places)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Obtener un sitio deportivo por ID
     */
    suspend fun getSportPlaceById(placeId: String): Result<SportPlace> {
        return try {
            val doc = sportPlacesCollection.document(placeId).get().await()
            val place = doc.toObject(SportPlace::class.java)?.copy(id = doc.id)
            
            if (place != null) {
                Result.success(place)
            } else {
                Result.failure(Exception("Sitio no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener sitio por ID", e)
            Result.failure(e)
        }
    }
    
    /**
     * Crear un nuevo sitio deportivo
     */
    suspend fun createSportPlace(place: SportPlace): Result<String> {
        return try {
            val docRef = sportPlacesCollection.add(place).await()
            Log.d(TAG, "✅ Sitio creado: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear sitio", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar un sitio deportivo
     */
    suspend fun updateSportPlace(placeId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            sportPlacesCollection.document(placeId).update(updates).await()
            Log.d(TAG, "✅ Sitio actualizado: $placeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar sitio", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar un sitio deportivo (soft delete)
     */
    suspend fun deleteSportPlace(placeId: String): Result<Unit> {
        return try {
            sportPlacesCollection.document(placeId)
                .update("activo", false)
                .await()
            Log.d(TAG, "✅ Sitio eliminado: $placeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar sitio", e)
            Result.failure(e)
        }
    }
}
