package com.example.spottivo.data.remote.firestore.dto

import com.google.firebase.firestore.GeoPoint
import com.example.spottivo.domain.model.core.Id
import com.example.spottivo.domain.model.core.GeoLocation

fun Id.toFs(): String = value
fun String.toId(): Id = Id(this)

fun GeoLocation.toFs(): GeoPoint = GeoPoint(latitud, longitud)
fun GeoPoint.toDomain(): GeoLocation = GeoLocation(latitude, longitude)
