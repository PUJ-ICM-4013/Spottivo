package com.example.spottivo.domain.repository

import com.example.spottivo.domain.model.booking.Reserva
import kotlinx.coroutines.flow.Flow

interface ReservaRepository {
    fun listenByUser(uid: String): Flow<List<Reserva>>
    suspend fun create(uid: String, model: Reserva): String
    suspend fun get(uid: String, id: String): Reserva?
    suspend fun update(uid: String, model: Reserva)
    suspend fun delete(uid: String, id: String)
}
