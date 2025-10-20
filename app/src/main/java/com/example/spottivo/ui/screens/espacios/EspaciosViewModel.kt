package com.example.spottivo.ui.screens.espacios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.common.di.RepositoryModule
import com.example.spottivo.domain.model.place.EspacioDeportivo
import com.example.spottivo.domain.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EspaciosViewModel(
    private val repo: EspacioRepository = RepositoryModule.espacioRepository(),
    private val uid: String = "demoUid"
) : ViewModel() {

    private val _items = MutableStateFlow<List<EspacioDeportivo>>(emptyList())
    val items: StateFlow<List<EspacioDeportivo>> = _items

    init {
        viewModelScope.launch {
            repo.listenMine(uid).collect { _items.value = it }
        }
    }

    fun crear(espacio: EspacioDeportivo) = viewModelScope.launch {
        repo.create(uid, espacio)
    }

    fun eliminar(id: String) = viewModelScope.launch {
        repo.delete(uid, id)
    }
}
