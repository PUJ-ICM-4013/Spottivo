package com.example.spottivo.data.remote.firestore.dto

import com.example.spottivo.domain.model.place.EspacioDeportivo

data class EspacioDeportivoDTO(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val contacto: String = "",
    val servicios: List<String> = emptyList(),
    val deportes: List<String> = emptyList(),
    val fotos: List<String> = emptyList(),
    val ubicacion: com.google.firebase.firestore.GeoPoint? = null,
    val esDestacado: Boolean = false,
    val propietarioId: String = ""
)

fun EspacioDeportivo.toDto() = EspacioDeportivoDTO(
    id.toFs(), nombre, direccion, contacto,
    servicios.map { it.toFs() }, deportes.map { it.toFs() }, fotos,
    ubicacion.toFs(), esDestacado, propietarioId.toFs()
)

fun EspacioDeportivoDTO.toDomain() = EspacioDeportivo(
    id.toId(), nombre, direccion, contacto,
    servicios.map { it.toId() }, deportes.map { it.toId() }, fotos,
    (ubicacion ?: com.google.firebase.firestore.GeoPoint(0.0,0.0)).toDomain(),
    esDestacado, propietarioId.toId()
)
