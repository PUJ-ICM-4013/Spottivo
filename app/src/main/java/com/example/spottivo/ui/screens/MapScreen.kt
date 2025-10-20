package com.example.spottivo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    initialPoint: GeoPoint = GeoPoint(4.60971, -74.08175), // Bogotá
    initialZoom: Double = 12.0
) {
    val context = LocalContext.current
    // osmdroid requiere un user-agent identificable para descargar tiles
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.applicationContext.packageName
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MapOSMContent(initialPoint = initialPoint, initialZoom = initialZoom)
    }
}

@Composable
private fun MapOSMContent(
    initialPoint: GeoPoint,
    initialZoom: Double
) {
    val context = LocalContext.current

    // --- Permisos de ubicación (opcionales para "mi ubicación") ---
    var fineGranted by remember { mutableStateOf(false) }
    var coarseGranted by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        fineGranted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true
        coarseGranted = res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // --- Fuentes de mapa (tile sources) ---
    var tileIndex by remember { mutableStateOf(0) }
    val tileSources: List<ITileSource> = remember {
        listOf(
            TileSourceFactory.MAPNIK, // OpenStreetMap estándar
            object : OnlineTileSourceBase(
                "OpenTopoMap",
                0, 19, 256, "", arrayOf("https://a.tile.opentopomap.org/")
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    val z = MapTileIndex.getZoom(pMapTileIndex)
                    val x = MapTileIndex.getX(pMapTileIndex)
                    val y = MapTileIndex.getY(pMapTileIndex)
                    return "https://a.tile.opentopomap.org/$z/$x/$y.png"
                }
            }
        )
    }

    // --- Estado del MapView y overlay de ubicación ---
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(tileSources[tileIndex])
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(initialZoom)
                    controller.setCenter(initialPoint)
                }.also { mv ->
                    mapView = mv
                    // Overlay "mi ubicación" si hay permisos
                    if (fineGranted || coarseGranted) {
                        val provider = GpsMyLocationProvider(ctx)
                        val overlay = MyLocationNewOverlay(provider, mv).apply {
                            enableMyLocation()
                            // Al primer fix: centrar en mi posición (convertimos a GeoPoint)
                            runOnFirstFix {
                                val loc = myLocation // android.location.Location?
                                val p: GeoPoint? = loc?.let { GeoPoint(it.latitude, it.longitude) }
                                if (p != null) {
                                    mv.post {
                                        mv.controller.setCenter(p)
                                        mv.controller.setZoom(16.0)
                                    }
                                }
                            }
                        }
                        mv.overlays.add(overlay)
                        myLocationOverlay = overlay
                    }
                }
            },
            update = { mv ->
                mv.setTileSource(tileSources[tileIndex])
                mv.invalidate()
            },
            onRelease = { mv ->
                myLocationOverlay?.disableMyLocation()
                myLocationOverlay = null
                mv.onDetach()
            }
        )

        // --- Controles flotantes ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = {
                tileIndex = (tileIndex + 1) % tileSources.size
            }) {
                Icon(Icons.Filled.Map, contentDescription = "Cambiar mapa")
            }

            FloatingActionButton(onClick = {
                mapView?.let { mv ->
                    val p: GeoPoint = myLocationOverlay?.myLocation
                        ?.let { GeoPoint(it.latitude, it.longitude) }
                        ?: initialPoint
                    mv.controller.animateTo(p)
                    mv.controller.setZoom(16.0)
                }
            }) {
                Icon(Icons.Filled.GpsFixed, contentDescription = "Mi ubicación")
            }
        }

        if (!(fineGranted || coarseGranted)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Text(
                    "Otorga permisos de ubicación para centrar el mapa en tu posición.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
