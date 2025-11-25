package com.example.spottivo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.models.SportPlace
import com.example.spottivo.data.repository.SportPlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar sitios deportivos
 */
class SportPlaceViewModel : ViewModel() {
    private val repository = SportPlaceRepository()
    
    private val _sportPlaces = MutableStateFlow<List<SportPlace>>(emptyList())
    val sportPlaces: StateFlow<List<SportPlace>> = _sportPlaces.asStateFlow()
    
    private val _filteredPlaces = MutableStateFlow<List<SportPlace>>(emptyList())
    val filteredPlaces: StateFlow<List<SportPlace>> = _filteredPlaces.asStateFlow()
    
    private val _selectedPlace = MutableStateFlow<SportPlace?>(null)
    val selectedPlace: StateFlow<SportPlace?> = _selectedPlace.asStateFlow()
    
    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    companion object {
        private const val TAG = "SportPlaceViewModel"
    }
    
    init {
        loadAllSportPlaces()
    }
    
    /**
     * Cargar todos los sitios deportivos
     */
    fun loadAllSportPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllSportPlaces().collect { places ->
                    _sportPlaces.value = places
                    applyFilters()
                    _isLoading.value = false
                    Log.d(TAG, "📍 Sitios deportivos cargados: ${places.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar sitios deportivos", e)
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filtrar por tags seleccionados
     */
    fun toggleTag(tag: String) {
        val currentTags = _selectedTags.value.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _selectedTags.value = currentTags
        applyFilters()
        Log.d(TAG, "🏷️ Tags seleccionados: $currentTags")
    }
    
    /**
     * Limpiar filtros de tags
     */
    fun clearTags() {
        _selectedTags.value = emptyList()
        applyFilters()
    }
    
    /**
     * Actualizar búsqueda por texto
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    /**
     * Aplicar filtros combinados (tags + búsqueda)
     */
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val tags = _selectedTags.value
        
        var filtered = _sportPlaces.value
        
        // Filtrar por tags
        if (tags.isNotEmpty()) {
            filtered = filtered.filter { place ->
                place.tags.any { tag -> tags.contains(tag) }
            }
        }
        
        // Filtrar por búsqueda de texto
        if (query.isNotEmpty()) {
            filtered = filtered.filter { place ->
                place.nombre.lowercase().contains(query) ||
                place.descripcion.lowercase().contains(query) ||
                place.tags.any { it.lowercase().contains(query) } ||
                place.direccion.lowercase().contains(query)
            }
        }
        
        _filteredPlaces.value = filtered
        Log.d(TAG, "🔍 Filtros aplicados: ${filtered.size} resultados")
    }
    
    /**
     * Seleccionar un sitio para ver detalles
     */
    fun selectPlace(place: SportPlace?) {
        _selectedPlace.value = place
        Log.d(TAG, "📌 Sitio seleccionado: ${place?.nombre ?: "ninguno"}")
    }
    
    /**
     * Cargar sitios cercanos a una ubicación
     */
    fun loadNearbyPlaces(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getSportPlacesNearby(latitude, longitude, radiusKm).collect { places ->
                    _sportPlaces.value = places
                    applyFilters()
                    Log.d(TAG, "📍 Sitios cercanos: ${places.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar sitios cercanos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar sitios de un propietario específico
     */
    fun loadPlacesByOwner(ownerEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getSportPlacesByOwner(ownerEmail).collect { places ->
                    _sportPlaces.value = places
                    applyFilters()
                    Log.d(TAG, "📍 Sitios del propietario: ${places.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar sitios del propietario", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crear un nuevo sitio deportivo
     */
    suspend fun createSportPlace(place: SportPlace): Result<String> {
        return repository.createSportPlace(place)
    }
}
