package com.example.spottivo.data.repository

import com.example.spottivo.data.remote.firestore.FirestorePaths
import com.example.spottivo.data.remote.firestore.dto.UsuarioDTO
import com.example.spottivo.data.remote.firestore.dto.toDomain
import com.example.spottivo.data.remote.firestore.dto.toDto
import com.example.spottivo.domain.model.user.Usuario
import com.example.spottivo.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val db: FirebaseFirestore) : UserRepository {
    private fun doc(uid: String) = db.collection(FirestorePaths.USERS).document(uid)

    override suspend fun upsert(user: Usuario) {
        doc(user.id.value).set(user.toDto()).await()
    }

    override suspend fun get(uid: String): Usuario? {
        val snap = doc(uid).get().await()
        return snap.toObject<UsuarioDTO>()?.toDomain()
    }
}
