package com.example.spottivo.domain.repository

import com.example.spottivo.domain.model.place.EspacioDeportivo
import kotlinx.coroutines.flow.Flow

interface EspacioRepository {
    fun listenMine(uid: String): Flow<List<EspacioDeportivo>>
    suspend fun create(uid: String, model: EspacioDeportivo): String
    suspend fun update(uid: String, model: EspacioDeportivo)
    suspend fun delete(uid: String, id: String)
}
