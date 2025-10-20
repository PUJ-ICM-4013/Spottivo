package com.example.spottivo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.spottivo.R
import androidx.lifecycle.lifecycleScope
import com.example.spottivo.common.seed.FirebaseOneTimeSeed
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            // 1) Sign-in anónimo
            val auth = Firebase.auth
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            val uid = auth.currentUser!!.uid

            // 2) Corre el seed usando **ese** uid
            FirebaseOneTimeSeed.runOnce(
                context = this@SplashActivity,
                db = FirebaseFirestore.getInstance(),
                uid = uid
            )
        }
    }
}