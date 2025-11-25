package com.example.spottivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.spottivo.ui.components.AppLogo
import com.example.spottivo.navigation.Screen
import kotlinx.coroutines.delay

/**
 * Pure Compose splash screen. Displays the PNG logo centered.
 * After [splashDurationMs] navigates to initial destination (Search screen).
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    splashDurationMs: Long = 1800L
) {
    val finished = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200), label = "logoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200), label = "logoAlpha"
    )
    LaunchedEffect(Unit) {
        delay(splashDurationMs)
        if (!finished.value) {
            finished.value = true
            navController.navigate("login_new") {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C031A),
                        Color(0xFF2A0B50),
                        Color(0xFF0C031A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing backdrop circle (subtle)
        val pulseScale by animateFloatAsState(targetValue = 1.15f, animationSpec = tween(1600), label = "pulse")
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
                .alpha(0.12f)
                .background(Color(0xFF6A3DFF).copy(alpha = 0.35f))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo(sizeDp = 160, modifier = Modifier.scale(scale).alpha(alpha))
            Spacer(Modifier.height(24.dp))
            val taglineAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(1400, delayMillis = 400), label = "tagline")
            Text(
                text = "Tu deporte, tu estilo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
