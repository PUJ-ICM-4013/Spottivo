package com.example.spottivo.data.remote.firestore.dto

import com.example.spottivo.domain.model.place.Deporte
import com.example.spottivo.domain.model.place.Servicio

data class DeporteDTO(val id: String = "", val nombre: String = "")
data class ServicioDTO(val id: String = "", val nombre: String = "", val descripcion: String? = null)

fun Deporte.toDto() = DeporteDTO(id.toFs(), nombre)
fun DeporteDTO.toDomain() = Deporte(id.toId(), nombre)

fun Servicio.toDto() = ServicioDTO(id.toFs(), nombre, descripcion)
fun ServicioDTO.toDomain() = Servicio(id.toId(), nombre, descripcion)
