package com.example.spottivo.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.spottivo.R
import com.example.spottivo.data.AuthRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*

/**
 * Servicio en background para seguimiento de ubicación en tiempo real
 * Similar a "FindMy" de Apple
 */
class LocationTrackingService : Service() {
    private val authRepository = AuthRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_service_channel"
        private const val UPDATE_INTERVAL = 10000L // 10 segundos
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                Log.d(TAG, "Nueva ubicación: ${location.latitude}, ${location.longitude}")
                serviceScope.launch {
                    try {
                        authRepository.updateLocation(location.latitude, location.longitude)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error actualizando ubicación", e)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(UPDATE_INTERVAL / 2)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Actualizaciones de ubicación iniciadas")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permisos de ubicación no concedidos", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Seguimiento de Ubicación",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Compartiendo ubicación con tus amigos"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Spottivo")
            .setContentText("Compartiendo ubicación en tiempo real")
            .setSmallIcon(R.drawable.ic_location)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        Log.d(TAG, "Servicio de ubicación detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
