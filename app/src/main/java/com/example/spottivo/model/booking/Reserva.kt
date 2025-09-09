package com.example.spottivo.model.booking

import com.example.spottivo.model.core.EstadoReserva
import com.example.spottivo.model.core.Horario
import com.example.spottivo.model.core.Id
import java.time.LocalDateTime

data class Reserva(
    val id: Id,
    val usuarioId: Id,             // Deportista.id
    val espacioId: Id,             // EspacioDeportivo.id
    val horario: Horario,
    val estado: EstadoReserva,
    val fechaCreacion: LocalDateTime,
    val precio: Double
)
