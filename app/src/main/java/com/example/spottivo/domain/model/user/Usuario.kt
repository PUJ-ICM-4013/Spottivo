package com.example.spottivo.domain.model.user

import com.example.spottivo.domain.model.core.GeoLocation
import com.example.spottivo.domain.model.core.Id
import com.example.spottivo.domain.model.core.RolUsuario

/**
 * Usuario base (abstracto) con herencia sellada para roles.
 * Nota: Para persistencia (Room) conviene aplanar con "role" + tablas de perfil.
 */
sealed class Usuario(
    open val id: Id,
    open val nombre: String,
    open val email: String,
    open val passwordHash: String,   // no guardes password plano
    open val fotoPerfil: String?,    // Url opcional
    open val rol: RolUsuario
) {
    data class Deportista(
        override val id: Id,
        override val nombre: String,
        override val email: String,
        override val passwordHash: String,
        override val fotoPerfil: String?,
        val preferencias: List<Id>,       // lista de Deporte.id
        val esPremium: Boolean,
        val ubicacionActual: GeoLocation?
    ) : Usuario(id, nombre, email, passwordHash, fotoPerfil, RolUsuario.DEPORTISTA)

    data class Propietario(
        override val id: Id,
        override val nombre: String,
        override val email: String,
        override val passwordHash: String,
        override val fotoPerfil: String?,
        val esPremium: Boolean,
        val espaciosPublicados: List<Id>  // lista de EspacioDeportivo.id (carga diferida)
    ) : Usuario(id, nombre, email, passwordHash, fotoPerfil, RolUsuario.PROPIETARIO)

    data class Administrador(
        override val id: Id,
        override val nombre: String,
        override val email: String,
        override val passwordHash: String,
        override val fotoPerfil: String?
    ) : Usuario(id, nombre, email, passwordHash, fotoPerfil, RolUsuario.ADMINISTRADOR)
}
