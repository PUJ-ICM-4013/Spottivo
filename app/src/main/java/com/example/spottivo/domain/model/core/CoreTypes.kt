package com.example.spottivo.model.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@JvmInline
value class Id(val value: String) {
    override fun toString() = value
}

data class GeoLocation(
    val latitud: Double,
    val longitud: Double
)

enum class RolUsuario { DEPORTISTA, PROPIETARIO, ADMINISTRADOR }

enum class EstadoPublicacion { PENDIENTE, APROBADO, RECHAZADO } // (corregido "APROVADO")

enum class EstadoReserva { PENDIENTE, CONFIRMADA, RECHAZADA, REPROGRAMADA, CADUCADA }

data class Horario(
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val cupoDisponible: Int
)

data class FiltroBusqueda(
    val deporteId: Id? = null,
    val fecha: LocalDate? = null,
    val precioMaximo: Double? = null,
    val ubicacion: GeoLocation? = null,
    val radioKm: Double? = null,          // opcional: búsqueda por radio
    val esDestacado: Boolean? = null
)

typealias Url = String
