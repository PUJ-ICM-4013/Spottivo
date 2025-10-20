package com.example.spottivo.ui.screens.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.common.di.RepositoryModule
import com.example.spottivo.domain.model.booking.Reserva
import com.example.spottivo.domain.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservaViewModel(
    private val repo: ReservaRepository = RepositoryModule.reservaRepository(),
    private val uid: String = "demoUid"
) : ViewModel() {

    private val _items = MutableStateFlow<List<Reserva>>(emptyList())
    val items: StateFlow<List<Reserva>> = _items

    init {
        viewModelScope.launch {
            repo.listenByUser(uid).collect { _items.value = it }
        }
    }

    fun crear(reserva: Reserva) = viewModelScope.launch {
        repo.create(uid, reserva)
    }
}
