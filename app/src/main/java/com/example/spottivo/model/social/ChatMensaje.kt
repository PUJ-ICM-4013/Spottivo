package com.example.spottivo.model.social

import com.example.spottivo.model.core.Id
import java.time.LocalDateTime

data class Chat(
    val id: Id,
    val usuarios: List<Id>,         // Usuario.id (2 o más si grupal)
    val esGrupal: Boolean
)

data class Mensaje(
    val id: Id,
    val chatId: Id,
    val emisorId: Id,               // Usuario.id
    val contenido: String,
    val fecha: LocalDateTime
)
