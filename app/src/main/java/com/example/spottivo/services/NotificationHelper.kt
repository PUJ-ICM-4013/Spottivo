package com.example.spottivo.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spottivo.R
import com.example.spottivo.data.models.FriendLocation
import com.example.spottivo.ui.MainActivity

/**
 * Helper para gestionar notificaciones push
 */
object NotificationHelper {
    
    private const val CHANNEL_FRIENDS_ID = "friends_online"
    private const val CHANNEL_EXERCISE_ID = "exercise_reminder"
    private const val NOTIFICATION_FRIENDS_BASE_ID = 1000
    private const val NOTIFICATION_EXERCISE_ID = 2000
    
    /**
     * Crea los canales de notificación necesarios
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para amigos en línea
            val friendsChannel = NotificationChannel(
                CHANNEL_FRIENDS_ID,
                "Amigos en línea",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando tus amigos están en línea"
                enableVibration(true)
            }
            
            // Canal para recordatorio de ejercicio
            val exerciseChannel = NotificationChannel(
                CHANNEL_EXERCISE_ID,
                "Recordatorio de ejercicio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios diarios para hacer ejercicio"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(friendsChannel)
            notificationManager.createNotificationChannel(exerciseChannel)
        }
    }
    
    /**
     * Envía notificación por cada amigo en línea
     */
    fun notifyFriendsOnline(context: Context, friends: List<FriendLocation>) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Enviar una notificación por cada amigo online
        friends.forEachIndexed { index, friend ->
            val notification = NotificationCompat.Builder(context, CHANNEL_FRIENDS_ID)
                .setSmallIcon(R.drawable.ic_map) // Usa tu icono
                .setContentTitle("${friend.nombre} está en línea")
                .setContentText("Tu amigo está disponible en Spottivo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(NOTIFICATION_FRIENDS_BASE_ID + index, notification)
        }
    }
    
    /**
     * Envía notificación de recordatorio de ejercicio
     */
    fun notifyExerciseReminder(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_EXERCISE_ID)
            .setSmallIcon(R.drawable.ic_map)
            .setContentTitle("¡Hora de hacer ejercicio! 💪")
            .setContentText("Es mediodía, el momento perfecto para moverte y mantenerte activo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_EXERCISE_ID, notification)
    }
    
    /**
     * Cancela todas las notificaciones de amigos
     */
    fun cancelFriendsNotifications(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        // Cancelar hasta 100 notificaciones de amigos
        for (i in 0 until 100) {
            notificationManager.cancel(NOTIFICATION_FRIENDS_BASE_ID + i)
        }
    }
}
