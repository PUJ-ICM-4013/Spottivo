package com.example.spottivo.ui.screens

import org.osmdroid.util.GeoPoint

/**
 * Resultado simplificado de una ruta retornada por OSRM.
 */
data class OsrmRouteResult(
    val points: List<GeoPoint>,
    val distanceKm: Double,
    val durationMin: Double
)
