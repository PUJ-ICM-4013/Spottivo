package com.example.spottivo.model.commerce

import com.example.spottivo.model.core.Id

enum class TipoPlan { USUARIO, PROPIETARIO }

data class PlanPremium(
    val id: Id,
    val nombre: String,
    val precio: Double,
    val beneficios: List<String>,
    val tipo: TipoPlan
)

data class Publicidad(
    val id: Id,
    val marca: String,
    val contenido: String,
    val segmento: String,   // criterio simple; luego puedes usar targeting detallado
    val activo: Boolean
)
