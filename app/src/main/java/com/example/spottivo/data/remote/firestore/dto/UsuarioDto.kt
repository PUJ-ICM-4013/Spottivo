package com.example.spottivo.data.remote.firestore.dto

import com.example.spottivo.domain.model.core.RolUsuario
import com.example.spottivo.domain.model.user.Usuario

data class UsuarioDTO(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val fotoPerfil: String? = null,
    val role: String = RolUsuario.DEPORTISTA.name,
    val preferencias: List<String>? = null,
    val esPremium: Boolean? = null,
    val ubicacionActual: com.google.firebase.firestore.GeoPoint? = null,
    val espaciosPublicados: List<String>? = null
)

fun Usuario.toDto(): UsuarioDTO = when (this) {
    is Usuario.Deportista -> UsuarioDTO(
        id.toFs(), nombre, email, passwordHash, fotoPerfil,
        RolUsuario.DEPORTISTA.name, preferencias.map { it.toFs() }, esPremium,
        ubicacionActual?.toFs(), null
    )
    is Usuario.Propietario -> UsuarioDTO(
        id.toFs(), nombre, email, passwordHash, fotoPerfil,
        RolUsuario.PROPIETARIO.name, null, esPremium, null,
        espaciosPublicados.map { it.toFs() }
    )
    is Usuario.Administrador -> UsuarioDTO(
        id.toFs(), nombre, email, passwordHash, fotoPerfil, RolUsuario.ADMINISTRADOR.name
    )
}

fun UsuarioDTO.toDomain(): Usuario = when (role) {
    RolUsuario.PROPIETARIO.name -> Usuario.Propietario(
        id.toId(), nombre, email, passwordHash, fotoPerfil,
        esPremium = esPremium ?: false,
        espaciosPublicados = (espaciosPublicados ?: emptyList()).map { it.toId() }
    )
    RolUsuario.ADMINISTRADOR.name -> Usuario.Administrador(
        id.toId(), nombre, email, passwordHash, fotoPerfil
    )
    else -> Usuario.Deportista(
        id.toId(), nombre, email, passwordHash, fotoPerfil,
        preferencias = (preferencias ?: emptyList()).map { it.toId() },
        esPremium = esPremium ?: false,
        ubicacionActual = ubicacionActual?.toDomain()
    )
}
