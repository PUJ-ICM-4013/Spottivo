package com.example.spottivo.common.di

import com.example.spottivo.data.repository.*
import com.example.spottivo.domain.repository.*

object RepositoryModule {
    fun userRepository(): UserRepository =
        UserRepositoryImpl(FirebaseModule.firestore)

    fun espacioRepository(): EspacioRepository =
        EspacioRepositoryImpl(FirebaseModule.firestore)

    fun reservaRepository(): ReservaRepository =
        ReservaRepositoryImpl(FirebaseModule.firestore)
}
