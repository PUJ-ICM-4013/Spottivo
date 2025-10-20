package com.example.spottivo.domain.model.place

import com.example.spottivo.domain.model.core.GeoLocation
import com.example.spottivo.domain.model.core.Id

data class EspacioDeportivo(
    val id: Id,
    val nombre: String,
    val direccion: String,
    val contacto: String,                // teléfono/correo
    val servicios: List<Id>,             // Servicio.id
    val deportes: List<Id>,              // Deporte.id
    val fotos: List<String>,             // Urls
    val ubicacion: GeoLocation,
    val esDestacado: Boolean,
    val propietarioId: Id                // Usuario.Propietario.id
) {
    // Receptor lógico de reservas, validaciones de negocio podrían vivir en un use-case.
}
