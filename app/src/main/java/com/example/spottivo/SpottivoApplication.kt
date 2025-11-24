package com.example.spottivo

import android.app.Application
import com.example.spottivo.data.CloudinaryService
import com.example.spottivo.services.NotificationHelper
import com.example.spottivo.services.ExerciseReminderWorker
import com.google.firebase.FirebaseApp

/**
 * Clase Application de Spottivo
 * Inicializa servicios globales
 */
class SpottivoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Inicializar Cloudinary
        CloudinaryService.initialize(this)
        
        // Inicializar canales de notificación
        NotificationHelper.createNotificationChannels(this)
        
        // Programar notificación diaria de ejercicio a las 12:00 PM
        ExerciseReminderWorker.schedule(this)
    }
}
