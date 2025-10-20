package com.example.spottivo.domain.model.place

import com.example.spottivo.domain.model.core.Id

data class Deporte(
    val id: Id,
    val nombre: String
)

data class Servicio(
    val id: Id,
    val nombre: String,
    val descripcion: String? = null
)
