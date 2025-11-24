package com.example.spottivo.services

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker para enviar notificación de ejercicio al mediodía
 */
class ExerciseReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        // Enviar notificación
        NotificationHelper.notifyExerciseReminder(applicationContext)
        return Result.success()
    }
    
    companion object {
        private const val WORK_NAME = "exercise_reminder"
        
        /**
         * Programa la notificación diaria a las 12:00 PM
         */
        fun schedule(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // Si ya pasó el mediodía hoy, programar para mañana
                if (before(currentDate)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()
            
            val dailyWorkRequest = PeriodicWorkRequestBuilder<ExerciseReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
        
        /**
         * Cancela las notificaciones programadas
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
