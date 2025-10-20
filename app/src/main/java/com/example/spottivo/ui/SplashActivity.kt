package com.example.spottivo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.spottivo.R
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Inicializa Firebase y registra evento de apertura
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val params = Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, "splash") }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, params)

        // Hide status bar for full screen experience
        window.statusBarColor = getColor(R.color.background_dark)

        // Navigate to MainActivity after splash timeout
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashTimeOut)
    }
}