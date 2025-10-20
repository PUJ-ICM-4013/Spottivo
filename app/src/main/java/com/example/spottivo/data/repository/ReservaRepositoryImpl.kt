package com.example.spottivo.data.repository

import com.example.spottivo.data.remote.firestore.FirestorePaths
import com.example.spottivo.data.remote.firestore.dto.ReservaDTO
import com.example.spottivo.data.remote.firestore.dto.toDomain
import com.example.spottivo.data.remote.firestore.dto.toDto
import com.example.spottivo.domain.model.booking.Reserva
import com.example.spottivo.domain.model.core.Id
import com.example.spottivo.domain.repository.ReservaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservaRepositoryImpl(private val db: FirebaseFirestore): ReservaRepository {
    private fun col(uid: String) =
        db.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.RESERVAS)

    override fun listenByUser(uid: String) = callbackFlow<List<Reserva>> {
        val reg = col(uid).orderBy("fechaCreacionMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject<ReservaDTO>()?.toDomain() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun create(uid: String, model: Reserva): String {
        val ref = col(uid).document()
        val data = model.copy(id = Id(ref.id)).toDto()
        ref.set(data).await()
        return ref.id
    }

    override suspend fun get(uid: String, id: String): Reserva? {
        val snap = col(uid).document(id).get().await()
        return snap.toObject<ReservaDTO>()?.toDomain()
    }

    override suspend fun update(uid: String, model: Reserva) {
        col(uid).document(model.id.value).set(model.toDto()).await()
    }

    override suspend fun delete(uid: String, id: String) {
        col(uid).document(id).delete().await()
    }
}
