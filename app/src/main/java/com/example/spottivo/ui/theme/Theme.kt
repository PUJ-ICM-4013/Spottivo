package com.example.spottivo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Importar colores desde Color.kt
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.ui.theme.AccentGreen
import com.example.spottivo.ui.theme.AccentPink
import com.example.spottivo.ui.theme.BackgroundDark
import com.example.spottivo.ui.theme.BackgroundLight
import com.example.spottivo.ui.theme.SurfaceDark
import com.example.spottivo.ui.theme.SurfaceLight
import com.example.spottivo.ui.theme.White
import com.example.spottivo.ui.theme.TextPrimaryDark
import com.example.spottivo.ui.theme.TextPrimaryLight

// Importar Typography desde Type.kt
import com.example.spottivo.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = AccentGreen,
    tertiary = AccentPink,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = AccentGreen,
    tertiary = AccentPink,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
)

@Composable
fun SpottivoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) 
{
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

