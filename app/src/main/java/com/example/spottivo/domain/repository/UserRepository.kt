package com.example.spottivo.domain.repository

import com.example.spottivo.domain.model.user.Usuario

interface UserRepository {
    suspend fun upsert(user: Usuario)
    suspend fun get(uid: String): Usuario?
}
