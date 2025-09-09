package com.example.spottivo.model.social

import com.example.spottivo.model.core.EstadoPublicacion
import com.example.spottivo.model.core.Id
import java.time.LocalDateTime

/**
 * Publicación base (sellada) para uniformar moderación/estado.
 * Pueden existir implementaciones: Evento, Comentario, Anuncio, etc.
 */
sealed class Publicacion(
    open val id: Id,
    open val creadoPor: Id,                 // Usuario.id
    open val fechaCreacion: LocalDateTime,
    open val estado: EstadoPublicacion
)

/**
 * Evento como Publicación (puede estar sujeto a aprobación/moderación).
 */
data class Evento(
    override val id: Id,
    override val creadoPor: Id,             // Organizador (Usuario o Propietario)
    override val fechaCreacion: LocalDateTime,
    override val estado: EstadoPublicacion,
    val nombre: String,
    val fechaEvento: LocalDateTime,
    val espacioId: Id,                      // EspacioDeportivo.id
    val participantes: List<Id>,            // Usuario.id
    val cupoMaximo: Int,
    val precioEntrada: Double?
) : Publicacion(id, creadoPor, fechaCreacion, estado)
