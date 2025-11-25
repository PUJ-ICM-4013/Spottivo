package com.example.spottivo.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos para sitios deportivos
 */
data class SportPlace(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tags: List<String> = emptyList(), // Ej: ["Gimnasio", "Crossfit", "Yoga"]
    val fotos: List<String> = emptyList(), // URLs de las fotos
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val propietarioId: String = "",
    val propietarioEmail: String = "",
    val propietarioNombre: String = "",
    val horarios: String = "", // Ej: "Lun-Vie: 6am-10pm, Sáb-Dom: 8am-6pm"
    val precioDesde: Double = 0.0, // Precio desde
    val precioHasta: Double = 0.0, // Precio hasta
    val calificacion: Double = 0.0, // Promedio de calificaciones
    val numeroCalificaciones: Int = 0,
    val activo: Boolean = true,
    @ServerTimestamp
    val fechaCreacion: Timestamp? = null
)

/**
 * Tags predefinidos para clasificar sitios deportivos
 */
object SportTags {
    const val GIMNASIO = "Gimnasio"
    const val CROSSFIT = "CrossFit"
    const val YOGA = "Yoga"
    const val PILATES = "Pilates"
    const val NATACION = "Natación"
    const val TENIS = "Tenis"
    const val FUTBOL = "Fútbol"
    const val BALONCESTO = "Baloncesto"
    const val VOLEIBOL = "Voleibol"
    const val ARTES_MARCIALES = "Artes Marciales"
    const val BOXEO = "Boxeo"
    const val CICLISMO = "Ciclismo"
    const val ATLETISMO = "Atletismo"
    const val ESCALADA = "Escalada"
    const val PADEL = "Pádel"
    const val SQUASH = "Squash"
    const val SPINNING = "Spinning"
    const val FUNCIONAL = "Entrenamiento Funcional"
    const val CALISTENIA = "Calistenia"
    const val PARKOUR = "Parkour"
    
    val ALL_TAGS = listOf(
        GIMNASIO, CROSSFIT, YOGA, PILATES, NATACION, TENIS, FUTBOL,
        BALONCESTO, VOLEIBOL, ARTES_MARCIALES, BOXEO, CICLISMO,
        ATLETISMO, ESCALADA, PADEL, SQUASH, SPINNING, FUNCIONAL,
        CALISTENIA, PARKOUR
    )
}
