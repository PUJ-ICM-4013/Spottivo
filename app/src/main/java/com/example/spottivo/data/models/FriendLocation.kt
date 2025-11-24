package com.example.spottivo.data.models

/**
 * Ubicación de un amigo en tiempo real (estilo FindMy)
 */
data class FriendLocation(
    val userId: String = "",
    val nombre: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isOnline: Boolean = false,
    val lastUpdate: Long = 0L, // timestamp en milisegundos
    val address: String = "" // dirección formateada (se obtiene con geocoding)
)

/**
 * Resultado del geocoding inverso (coordenadas → dirección)
 */
data class AddressResult(
    val formattedAddress: String = "",
    val street: String = "",
    val city: String = "",
    val country: String = ""
)
