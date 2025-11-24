package com.example.spottivo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spottivo.services.LocationTrackingService
import com.example.spottivo.ui.screens.LoginScreen
import com.example.spottivo.ui.screens.RegisterScreen
import com.example.spottivo.ui.screens.BusinessRegisterScreen
import com.example.spottivo.ui.theme.SpottivoTheme

class AuthActivity : ComponentActivity() {
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineGranted || coarseGranted) {
            startLocationService()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpottivoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthNavigation(
                        onAuthSuccess = {
                            // Solicitar permisos e iniciar servicio de ubicación
                            requestLocationPermissionsAndStartService()
                            
                            // Navigate to main app
                            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    private fun requestLocationPermissionsAndStartService() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Agregar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Verificar si ya tenemos los permisos
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasLocationPermission) {
            startLocationService()
        } else {
            locationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToBusiness = {
                    navController.navigate("business_register")
                },
                onRegisterSuccess = onAuthSuccess
            )
        }
        
        composable("business_register") {
            BusinessRegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegular = {
                    navController.popBackStack()
                },
                onRegisterSuccess = onAuthSuccess
            )
        }
    }
}