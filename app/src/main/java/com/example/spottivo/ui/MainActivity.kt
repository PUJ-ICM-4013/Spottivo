package com.example.spottivo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.spottivo.ui.screens.MainScreen
import com.example.spottivo.ui.theme.SpottivoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpottivoTheme {
                MainScreen()
            }
        }
    }
}

