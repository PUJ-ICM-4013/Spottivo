package com.example.spottivo.model.social

import com.example.spottivo.model.core.Id

data class Comunidad(
    val id: Id,
    val nombre: String,
    val deporteRelacionadoId: Id,     // Deporte.id
    val miembros: List<Id>,           // Usuario.id (muchos a muchos)
    val chats: List<Id>,              // Chat.id (opcional)
    val eventos: List<Id>             // Evento.id
)
