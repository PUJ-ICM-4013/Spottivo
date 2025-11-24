package com.example.spottivo.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class HardwareStatus(
    val status: String,
    val timestamp: String,
    val server: ServerInfo,
    val database: String
)

data class ServerInfo(
    val uptime: Double,
    val memory_rss: String,
    val node_version: String
)

data class LocationData(
    val userId: String,
    val lat: Double,
    val lng: Double,
    val lastUpdate: Long
)

interface ApiService {
    @GET("hardware-check")
    suspend fun getHardwareStatus(): HardwareStatus

    @GET("locations/{userId}")
    suspend fun getUserLocation(@Path("userId") userId: String): LocationData
    
    @GET("friends/{userId}/locations")
    suspend fun getFriendsLocations(@Path("userId") userId: String): List<LocationData>
}

object ApiClient {
    // Cambia esto por la URL de tu proyecto Firebase
    private const val BASE_URL = "https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/api/"
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
