package com.example.spottivo.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    initialPoint: GeoPoint = GeoPoint(4.60971, -74.08175), // Bogotá
    initialZoom: Double = 17.5
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.applicationContext.packageName
    }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        MapOSMWithOSRMAndSensors(initialPoint, initialZoom)
    }
}

@Composable
private fun MapOSMWithOSRMAndSensors(
    initialPoint: GeoPoint,
    initialZoom: Double
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---------- Permisos ubicación ----------
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

    // ---------- Velocidad (LocationManager) ----------
    val speedKmhState = remember { mutableStateOf(0.0) }
    DisposableEffect(fineGranted || coarseGranted) {
        if (!(fineGranted || coarseGranted)) {
            // Debe devolver un DisposableEffectResult siempre:
            return@DisposableEffect onDispose { /* no-op */ }
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : android.location.LocationListener {
            private var lastLoc: Location? = null
            override fun onLocationChanged(loc: Location) {
                val speedMps = if (loc.hasSpeed()) {
                    loc.speed.toDouble()
                } else {
                    val prev = lastLoc
                    if (prev != null) {
                        val d = haversineMeters(prev.latitude, prev.longitude, loc.latitude, loc.longitude)
                        val dt = (loc.time - prev.time) / 1000.0
                        if (dt > 0) d / dt else 0.0
                    } else 0.0
                }
                lastLoc = loc
                speedKmhState.value = (speedMps * 3.6).coerceAtLeast(0.0)
            }
        }
        try {
            val minTimeMs = 800L
            val minDistM = 0f
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, minTimeMs, minDistM, listener, Looper.getMainLooper()
                )
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, minTimeMs, minDistM, listener, Looper.getMainLooper()
                )
            }
        } catch (_: SecurityException) { }
        onDispose { lm.removeUpdates(listener) }
    }

    // ---------- Brújula (Rotation Vector + fallback) ----------
    val azimuthDegState = remember { mutableStateOf(0f) }
    CompassSensorEffect(azimuthDegState)

    // ---------- Tiles ----------
    var tileIndex by remember { mutableStateOf(0) }
    val tileSources: List<ITileSource> = remember {
        listOf(
            TileSourceFactory.MAPNIK,
            object : OnlineTileSourceBase(
                "OpenTopoMap", 0, 19, 256, "", arrayOf("https://a.tile.opentopomap.org/")
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

    // ---------- Estado mapa ----------
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    val httpClient = remember { OkHttpClient() }

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
                    overlays.add(RotationGestureOverlay(this).apply { isEnabled = true })
                }.also { mv ->
                    mapView = mv

                    if (fineGranted || coarseGranted) {
                        val provider = GpsMyLocationProvider(ctx)
                        val overlay = MyLocationNewOverlay(provider, mv).apply { enableMyLocation() }
                        mv.overlays.add(overlay)
                        myLocationOverlay = overlay
                    }

                    // Long-press → ruta OSRM manteniendo zoom actual
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            val dest = p ?: return false
                            val origin: GeoPoint = myLocationOverlay?.myLocation
                                ?.let { GeoPoint(it.latitude, it.longitude) }
                                ?: initialPoint
                            val currentZoom = mv.zoomLevelDouble

                            scope.launch {
                                val result = fetchOsrmRoute(httpClient, origin, dest)
                                if (result == null) {
                                    Toast.makeText(ctx, "No se pudo obtener la ruta", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                routePolyline?.let { mv.overlays.remove(it) }
                                val newPolyline = Polyline().apply {
                                    setPoints(result.points)
                                    outlinePaint.strokeWidth = 8f
                                }
                                mv.overlays.add(newPolyline)
                                routePolyline = newPolyline

                                destinationMarker?.let { mv.overlays.remove(it) }
                                val marker = Marker(mv).apply {
                                    position = dest
                                    title = "Destino — ${"%.1f".format(result.distanceKm)} km, ${result.durationMin.toInt()} min"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mv.overlays.add(marker)
                                destinationMarker = marker

                                val mid = GeoPoint(
                                    (origin.latitude + dest.latitude) / 2.0,
                                    (origin.longitude + dest.longitude) / 2.0
                                )
                                mv.post {
                                    mv.controller.animateTo(mid)
                                    mv.controller.setZoom(currentZoom)
                                    mv.invalidate()
                                }
                            }
                            return true
                        }
                    })
                    mv.overlays.add(eventsOverlay)
                }
            },
            update = { mv ->
                mv.setTileSource(tileSources[tileIndex])
                mv.invalidate()
            },
            onRelease = { mv ->
                myLocationOverlay?.disableMyLocation()
                myLocationOverlay = null
                routePolyline = null
                destinationMarker = null
                mv.onDetach()
            }
        )

        // ---------- HUD de sensores ----------
        Compass(
            azimuthDeg = azimuthDegState.value,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
        SpeedChip(
            speedKmh = speedKmhState.value,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )

        // ---------- Controles flotantes ----------
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = { tileIndex = (tileIndex + 1) % tileSources.size }) {
                Icon(Icons.Filled.Map, contentDescription = "Cambiar mapa")
            }
            FloatingActionButton(onClick = {
                mapView?.let { mv ->
                    val p: GeoPoint = myLocationOverlay?.myLocation
                        ?.let { GeoPoint(it.latitude, it.longitude) } ?: initialPoint
                    mv.controller.animateTo(p)
                    mv.controller.setZoom(18.5)
                }
            }) { Icon(Icons.Filled.GpsFixed, contentDescription = "Mi ubicación") }
            FloatingActionButton(onClick = {
                mapView?.let { mv ->
                    val next = (mv.zoomLevelDouble + 1.0).coerceAtMost(20.0)
                    mv.controller.setZoom(next)
                }
            }) { Icon(Icons.Filled.Add, contentDescription = "Acercar") }
            FloatingActionButton(onClick = {
                mapView?.let { mv ->
                    val next = (mv.zoomLevelDouble - 1.0).coerceAtLeast(3.0)
                    mv.controller.setZoom(next)
                }
            }) { Icon(Icons.Filled.Remove, contentDescription = "Alejar") }
        }

        if (!(fineGranted || coarseGranted)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
            ) {
                Text(
                    "Concede permisos para velocidad y ubicación. Long-press: ruta OSRM.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/* ---------------- Sensores: brújula ---------------- */

@Composable
private fun CompassSensorEffect(azimuthDegState: MutableState<Float>) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val R = FloatArray(9)
        val I = FloatArray(9)
        val orientation = FloatArray(3)
        val accVals = FloatArray(3)
        val magVals = FloatArray(3)
        var haveAcc = false
        var haveMag = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                when (e.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(R, e.values)
                        val az = SensorManager.getOrientation(R, orientation)[0]
                        azimuthDegState.value = ((Math.toDegrees(az.toDouble()) + 360) % 360).toFloat()
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(e.values, 0, accVals, 0, 3); haveAcc = true
                        if (haveMag && SensorManager.getRotationMatrix(R, I, accVals, magVals)) {
                            val az = SensorManager.getOrientation(R, orientation)[0]
                            azimuthDegState.value = ((Math.toDegrees(az.toDouble()) + 360) % 360).toFloat()
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(e.values, 0, magVals, 0, 3); haveMag = true
                        if (haveAcc && SensorManager.getRotationMatrix(R, I, accVals, magVals)) {
                            val az = SensorManager.getOrientation(R, orientation)[0]
                            azimuthDegState.value = ((Math.toDegrees(az.toDouble()) + 360) % 360).toFloat()
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rot != null) {
            sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accel?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            mag?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        }

        onDispose { sm.unregisterListener(listener) }
    }
}

@Composable
private fun Compass(
    azimuthDeg: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val animated = animateFloatAsState(targetValue = azimuthDeg, label = "azimuthAnim")
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.GpsFixed,
            contentDescription = "Brújula",
            modifier = Modifier
                .fillMaxSize(0.7f)
                .rotate(-animated.value),
            tint = Color(0xFFE53935)
        )
        Text(
            "N",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/* ---------------- HUD de velocidad ---------------- */

@Composable
private fun SpeedChip(
    speedKmh: Double,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        label = { Text(text = "${String.format("%.1f", speedKmh)} km/h") },
        leadingIcon = { Icon(Icons.Filled.GpsFixed, contentDescription = null) },
        modifier = modifier
    )
}

/* ---------------- OSRM helpers ---------------- */

private suspend fun fetchOsrmRoute(
    client: OkHttpClient,
    origin: GeoPoint,
    dest: GeoPoint
): OsrmRouteResult? = withContext(Dispatchers.IO) {
    try {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};${dest.longitude},${dest.latitude}" +
                "?overview=full&geometries=polyline&alternatives=false&steps=false"

        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext null
            val bodyStr = resp.body?.string() ?: return@withContext null
            val json = JSONObject(bodyStr)
            if (json.optString("code") != "Ok") return@withContext null

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null
            val r0 = routes.getJSONObject(0)
            val geometry = r0.getString("geometry")
            val distanceMeters = r0.getDouble("distance")
            val durationSeconds = r0.getDouble("duration")

            OsrmRouteResult(
                points = decodePolyline(geometry),
                distanceKm = distanceMeters / 1000.0,
                durationMin = durationSeconds / 60.0
            )
        }
    } catch (_: Exception) {
        null
    }
}

private fun decodePolyline(encoded: String): List<GeoPoint> {
    val len = encoded.length
    var index = 0
    var lat = 0
    var lng = 0
    val path = ArrayList<GeoPoint>()
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        path.add(GeoPoint(lat / 1E5, lng / 1E5))
    }
    return path
}

private data class OsrmRouteResult(
    val points: List<GeoPoint>,
    val distanceKm: Double,
    val durationMin: Double
)

/* ---------------- Utils ---------------- */

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2).pow(2.0) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
