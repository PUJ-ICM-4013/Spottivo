package com.example.spottivo.data.remote.firestore.dto

import com.example.spottivo.common.util.*
import com.example.spottivo.domain.model.booking.Reserva
import com.example.spottivo.domain.model.core.EstadoReserva
import com.example.spottivo.domain.model.core.Horario

data class ReservaDTO(
    val id: String = "",
    val usuarioId: String = "",
    val espacioId: String = "",
    val fechaCreacionMillis: Long = 0L,
    val precio: Double = 0.0,
    val estado: String = EstadoReserva.PENDIENTE.name,
    val fechaMillis: Long = 0L,
    val inicioMillis: Long = 0L,
    val finMillis: Long = 0L,
    val cupoDisponible: Int = 0
)

fun Reserva.toDto(): ReservaDTO = ReservaDTO(
    id.toFs(), usuarioId.toFs(), espacioId.toFs(),
    fechaCreacion.toEpochMillis(), precio, estado.name,
    horario.fecha.toEpochMillis(),
    horario.horaInicio.toEpochMillisOn(horario.fecha),
    horario.horaFin.toEpochMillisOn(horario.fecha),
    horario.cupoDisponible
)

fun ReservaDTO.toDomain(): Reserva = Reserva(
    id.toId(), usuarioId.toId(), espacioId.toId(),
    Horario(
        fechaMillis.toLocalDate(),
        inicioMillis.toLocalDateTime().toLocalTime(),
        finMillis.toLocalDateTime().toLocalTime(),
        cupoDisponible
    ),
    EstadoReserva.valueOf(estado),
    fechaCreacionMillis.toLocalDateTime(),
    precio
)
