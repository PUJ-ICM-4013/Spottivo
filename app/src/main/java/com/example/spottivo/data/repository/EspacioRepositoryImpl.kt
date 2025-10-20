package com.example.spottivo.data.repository

import com.example.spottivo.data.remote.firestore.FirestorePaths
import com.example.spottivo.data.remote.firestore.dto.EspacioDeportivoDTO
import com.example.spottivo.data.remote.firestore.dto.toDomain
import com.example.spottivo.data.remote.firestore.dto.toDto
import com.example.spottivo.domain.model.core.Id
import com.example.spottivo.domain.model.place.EspacioDeportivo
import com.example.spottivo.domain.repository.EspacioRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EspacioRepositoryImpl(private val db: FirebaseFirestore): EspacioRepository {
    private fun col(uid: String) =
        db.collection(FirestorePaths.USERS).document(uid).collection(FirestorePaths.ESPACIOS)

    override fun listenMine(uid: String) = callbackFlow<List<EspacioDeportivo>> {
        val reg = col(uid).orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject<EspacioDeportivoDTO>()?.toDomain() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun create(uid: String, model: EspacioDeportivo): String {
        val ref = col(uid).document()
        val data = model.copy(id = Id(ref.id)).toDto()
        ref.set(data).await()
        return ref.id
    }

    override suspend fun update(uid: String, model: EspacioDeportivo) {
        col(uid).document(model.id.value).set(model.toDto()).await()
    }

    override suspend fun delete(uid: String, id: String) {
        col(uid).document(id).delete().await()
    }
}
