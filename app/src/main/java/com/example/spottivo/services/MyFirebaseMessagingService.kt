package com.example.spottivo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.spottivo.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio para recibir notificaciones push de Firebase Cloud Messaging
 * Muestra notificaciones cuando amigos se conectan
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "friend_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Verificar si tiene notificación
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Spottivo", it.body ?: "")
        }

        // Verificar si tiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo token FCM generado: $token")
        // Aquí puedes actualizar el token en Firestore si es necesario
        // authRepository.updateFcmToken(token)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"] ?: "Spottivo"
        val body = data["body"] ?: ""
        val type = data["type"] ?: "general"

        when (type) {
            "chat_message" -> {
                val senderName = data["senderName"] ?: "Nuevo mensaje"
                val message = data["message"] ?: ""
                showChatNotification(senderName, message)
            }
            "friend_online" -> {
                showNotification(title, body)
            }
            "location_update" -> {
                // Manejar actualizaciones de ubicación si es necesario
                Log.d(TAG, "Actualización de ubicación recibida")
            }
            else -> {
                showNotification(title, body)
            }
        }
    }
    
    private fun showChatNotification(senderName: String, message: String) {
        val channelId = "chat_messages"
        
        // Crear canal de notificación para mensajes
        val channel = NotificationChannel(
            channelId,
            "Mensajes de Chat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de mensajes de chat"
            enableVibration(true)
            enableLights(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        // Crear notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(senderName)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_community)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "Notificación de chat mostrada: $senderName - $message")
    }

    private fun showNotification(title: String, message: String) {
        val channelId = CHANNEL_ID
        
        // Crear canal de notificación
        val channel = NotificationChannel(
            channelId,
            "Notificaciones de Amigos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones cuando tus amigos se conectan"
            enableVibration(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        // Crear notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_person)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "Notificación mostrada: $title - $message")
    }
}
