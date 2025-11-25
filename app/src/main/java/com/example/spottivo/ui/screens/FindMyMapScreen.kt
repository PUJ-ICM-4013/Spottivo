package com.example.spottivo.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.spottivo.data.models.FriendLocation
import com.example.spottivo.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import android.widget.Toast
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Pantalla de mapa estilo FindMy de Apple
 * Muestra ubicaciones de amigos en tiempo real con sus fotos de perfil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindMyMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Inicializar OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.applicationContext.packageName
    }
    
    // Estado del ViewModel
    val friendsLocations by viewModel.friendsLocations.collectAsState()
    val selectedFriend by viewModel.selectedFriend.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val myLocation by viewModel.myLocation.collectAsState()
    val sportPlaces by viewModel.sportPlaces.collectAsState()
    
    // Cargar ubicación del usuario al iniciar
    LaunchedEffect(Unit) {
        viewModel.getCurrentUserLocation()
    }
    
    // Debug: Log cuando cambien las ubicaciones
    LaunchedEffect(friendsLocations.size) {
        android.util.Log.d("FindMyMapScreen", "📊 Estado actualizado: ${friendsLocations.size} amigos")
        friendsLocations.take(3).forEach {
            android.util.Log.d("FindMyMapScreen", "  - ${it.nombre}: (${it.latitude}, ${it.longitude}) foto: ${it.photoUrl.take(50)}")
        }
    }
    
    // Notificaciones de amigos en línea al abrir la app
    var hasNotified by remember { mutableStateOf(false) }
    LaunchedEffect(friendsLocations) {
        // Solo notificar una vez cuando hay amigos y no se ha notificado antes
        if (friendsLocations.isNotEmpty() && !hasNotified) {
            com.example.spottivo.services.NotificationHelper.notifyFriendsOnline(
                context,
                friendsLocations
            )
            hasNotified = true
            android.util.Log.d("FindMyMapScreen", "🔔 Notificaciones enviadas para ${friendsLocations.size} amigos")
        }
    }
    
    // Permisos de ubicación y notificaciones
    var fineGranted by remember { mutableStateOf(false) }
    var coarseGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
    }
    
    LaunchedEffect(Unit) {
        fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        notificationGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores a Android 13, no se requiere permiso
        }
        
        val permissionsToRequest = mutableListOf<String>()
        if (!fineGranted && !coarseGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!notificationGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    // Estado del mapa
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val friendMarkers = remember { mutableStateMapOf<String, Marker>() }
    val sportPlaceMarkers = remember { mutableStateMapOf<String, Marker>() }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    val httpClient = remember { OkHttpClient() }
    
    // Bottom sheet para mostrar detalles del amigo
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Bottom sheet para mostrar detalles del sitio deportivo
    val sportPlaceSheetState = rememberModalBottomSheetState()
    var showSportPlaceSheet by remember { mutableStateOf(false) }
    var selectedSportPlace by remember { mutableStateOf<com.example.spottivo.data.models.SportPlace?>(null) }
    
    // Actualizar markers cuando cambian las ubicaciones
    LaunchedEffect(friendsLocations) {
        android.util.Log.d("FindMyMapScreen", "🔄 LaunchedEffect triggered: ${friendsLocations.size} amigos")
        mapView?.let { mv ->
            updateFriendMarkers(
                context = context,
                mapView = mv,
                friends = friendsLocations,
                existingMarkers = friendMarkers,
                onMarkerClick = { friend ->
                    viewModel.selectFriend(friend)
                    showBottomSheet = true
                },
                scope = scope
            )
        }
    }
    
    // Actualizar markers de sitios deportivos cuando cambian
    LaunchedEffect(sportPlaces) {
        android.util.Log.d("FindMyMapScreen", "🏟️ LaunchedEffect sitios: ${sportPlaces.size} sitios")
        mapView?.let { mv ->
            updateSportPlaceMarkers(
                context = context,
                mapView = mv,
                sportPlaces = sportPlaces,
                existingMarkers = sportPlaceMarkers,
                onMarkerClick = { place ->
                    selectedSportPlace = place
                    showSportPlaceSheet = true
                }
            )
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Mapa
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    
                    // Configuración inicial (Bogotá)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(4.60971, -74.08175))
                    
                    // Overlay de mi ubicación
                    if (fineGranted || coarseGranted) {
                        val provider = GpsMyLocationProvider(ctx)
                        val overlay = MyLocationNewOverlay(provider, this).apply {
                            enableMyLocation()
                            enableFollowLocation()
                        }
                        overlays.add(overlay)
                        myLocationOverlay = overlay
                    }
                    
                    // Long-press para trazar ruta OSRM
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            val dest = p ?: return false
                            
                            // Intentar obtener ubicación en este orden:
                            // 1. GPS real (MyLocationOverlay)
                            // 2. Ubicación guardada en Firestore
                            // 3. Centro del mapa (fallback final)
                            val origin = when {
                                myLocationOverlay?.myLocation != null -> {
                                    val loc = myLocationOverlay!!.myLocation
                                    GeoPoint(loc.latitude, loc.longitude)
                                }
                                myLocation != null -> {
                                    Toast.makeText(ctx, "Usando ubicación guardada", Toast.LENGTH_SHORT).show()
                                    GeoPoint(myLocation!!.latitude, myLocation!!.longitude)
                                }
                                else -> {
                                    Toast.makeText(ctx, "Usando centro del mapa (configura tu ubicación)", Toast.LENGTH_SHORT).show()
                                    this@apply.mapCenter as GeoPoint
                                }
                            }
                            
                            scope.launch {
                                val result = fetchOsrmRoute(httpClient, origin, dest)
                                if (result == null) {
                                    Toast.makeText(ctx, "No se pudo obtener la ruta", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                
                                // Remover ruta anterior
                                routePolyline?.let { this@apply.overlays.remove(it) }
                                
                                // Agregar nueva ruta
                                val newPolyline = Polyline().apply {
                                    setPoints(result.points)
                                    outlinePaint.color = 0xFF2196F3.toInt()
                                    outlinePaint.strokeWidth = 12f
                                }
                                this@apply.overlays.add(newPolyline)
                                routePolyline = newPolyline
                                
                                // Marker de destino
                                destinationMarker?.let { this@apply.overlays.remove(it) }
                                val marker = Marker(this@apply).apply {
                                    position = dest
                                    title = "Destino — ${"%.1f".format(result.distanceKm)} km, ${result.durationMin.toInt()} min"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                this@apply.overlays.add(marker)
                                destinationMarker = marker
                                
                                this@apply.invalidate()
                                Toast.makeText(ctx, "Ruta: ${"%.1f".format(result.distanceKm)} km (${result.durationMin.toInt()} min)", Toast.LENGTH_LONG).show()
                            }
                            return true
                        }
                    })
                    overlays.add(eventsOverlay)
                }.also { mv ->
                    mapView = mv
                    android.util.Log.d("FindMyMapScreen", "✅ MapView creado, overlays: ${mv.overlays.size}")
                }
            },
            onRelease = { mv ->
                myLocationOverlay?.disableMyLocation()
                myLocationOverlay = null
                friendMarkers.clear()
                routePolyline = null
                destinationMarker = null
                mv.onDetach()
            }
        )
        
        // Controles flotantes (lado derecho)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón de refrescar
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refrescar ubicaciones"
                )
            }
            
            // Botón de centrar en mi ubicación
            FloatingActionButton(
                onClick = {
                    mapView?.let { mv ->
                        // Intentar GPS primero, luego ubicación guardada
                        val gpsLocation = myLocationOverlay?.myLocation
                        val savedLocation = myLocation
                        
                        when {
                            gpsLocation != null -> {
                                mv.controller.animateTo(
                                    GeoPoint(gpsLocation.latitude, gpsLocation.longitude)
                                )
                                mv.controller.setZoom(16.0)
                            }
                            savedLocation != null -> {
                                mv.controller.animateTo(
                                    GeoPoint(savedLocation.latitude, savedLocation.longitude)
                                )
                                mv.controller.setZoom(16.0)
                                Toast.makeText(context, "Ubicación guardada", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Filled.GpsFixed,
                    contentDescription = "Mi ubicación"
                )
            }
        }
        
        // Controles de zoom y borrar ruta (lado izquierdo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón para borrar ruta
            if (routePolyline != null || destinationMarker != null) {
                FloatingActionButton(
                    onClick = {
                        mapView?.let { mv ->
                            routePolyline?.let { mv.overlays.remove(it) }
                            destinationMarker?.let { mv.overlays.remove(it) }
                            routePolyline = null
                            destinationMarker = null
                            mv.invalidate()
                            Toast.makeText(context, "Ruta borrada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Borrar ruta",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Botón Zoom In (+)
            SmallFloatingActionButton(
                onClick = {
                    mapView?.let { mv ->
                        val currentZoom = mv.zoomLevelDouble
                        mv.controller.setZoom(currentZoom + 1.0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
            
            // Botón Zoom Out (-)
            SmallFloatingActionButton(
                onClick = {
                    mapView?.let { mv ->
                        val currentZoom = mv.zoomLevelDouble
                        mv.controller.setZoom(currentZoom - 1.0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text("−", style = MaterialTheme.typography.headlineMedium)
            }
        }
        
        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
        
        // Contador de amigos online y debug
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (friendsLocations.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray
                )
                if (friendsLocations.isNotEmpty()) {
                    val onlineCount = friendsLocations.count { it.isOnline }
                    Text(
                        text = "$onlineCount/${friendsLocations.size} amigos en línea",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = if (isLoading) "Cargando..." else "Sin amigos",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Bottom sheet con detalles del amigo
        if (showBottomSheet && selectedFriend != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.selectFriend(null)
                },
                sheetState = sheetState
            ) {
                FriendDetailsSheet(
                    friend = selectedFriend!!,
                    onClose = {
                        showBottomSheet = false
                        viewModel.selectFriend(null)
                    },
                    onNavigate = {
                        // Centrar mapa en la ubicación del amigo
                        mapView?.controller?.animateTo(
                            GeoPoint(selectedFriend!!.latitude, selectedFriend!!.longitude)
                        )
                        mapView?.controller?.setZoom(18.0)
                        showBottomSheet = false
                    }
                )
            }
        }
        
        // Bottom sheet con detalles del sitio deportivo
        if (showSportPlaceSheet && selectedSportPlace != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSportPlaceSheet = false
                    selectedSportPlace = null
                },
                sheetState = sportPlaceSheetState
            ) {
                SportPlaceDetailsSheet(
                    sportPlace = selectedSportPlace!!,
                    onClose = {
                        showSportPlaceSheet = false
                        selectedSportPlace = null
                    },
                    onNavigate = {
                        // Centrar mapa en la ubicación del sitio
                        mapView?.controller?.animateTo(
                            GeoPoint(selectedSportPlace!!.latitud, selectedSportPlace!!.longitud)
                        )
                        mapView?.controller?.setZoom(18.0)
                        showSportPlaceSheet = false
                    }
                )
            }
        }
    }
}

/**
 * Actualiza los markers de amigos en el mapa
 */
private suspend fun updateFriendMarkers(
    context: Context,
    mapView: MapView,
    friends: List<FriendLocation>,
    existingMarkers: MutableMap<String, Marker>,
    onMarkerClick: (FriendLocation) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) = withContext(Dispatchers.Main) {
    android.util.Log.d("FindMyMapScreen", "📍 updateFriendMarkers: ${friends.size} amigos")
    
    // Remover markers de amigos que ya no están en la lista
    val currentFriendIds = friends.map { it.userId }.toSet()
    val markersToRemove = existingMarkers.keys.filter { it !in currentFriendIds }
    
    markersToRemove.forEach { userId ->
        existingMarkers[userId]?.let { marker ->
            mapView.overlays.remove(marker)
        }
        existingMarkers.remove(userId)
    }
    
    // Agregar o actualizar markers de amigos
    friends.forEach { friend ->
        android.util.Log.d("FindMyMapScreen", "  👤 ${friend.nombre} (${friend.latitude}, ${friend.longitude})")
        val existingMarker = existingMarkers[friend.userId]
        
        if (existingMarker != null) {
            // Actualizar posición del marker existente
            android.util.Log.d("FindMyMapScreen", "    ↻ Actualizando marker existente")
            existingMarker.position = GeoPoint(friend.latitude, friend.longitude)
            existingMarker.title = friend.nombre
        } else {
            // Crear nuevo marker estilo FindMy
            android.util.Log.d("FindMyMapScreen", "    + Creando marker FindMy")
            try {
                // Determinar si es el usuario actual
                val isCurrentUser = friend.nombre == "Tú"
                
                // Crear marker con placeholder primero (círculo verde o azul)
                val placeholderIcon = withContext(Dispatchers.IO) {
                    createPlaceholderIcon(context, isCurrentUser)
                }
                
                val marker = Marker(mapView).apply {
                    position = GeoPoint(friend.latitude, friend.longitude)
                    title = friend.nombre
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    this.icon = placeholderIcon
                    
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(friend)
                        true
                    }
                }
                
                mapView.overlays.add(marker)
                existingMarkers[friend.userId] = marker
                android.util.Log.d("FindMyMapScreen", "    ✓ Marker placeholder agregado, total overlays: ${mapView.overlays.size}")
                
                // Cargar foto en background y actualizar
                scope.launch {
                    try {
                        val photoIcon = withContext(Dispatchers.IO) {
                            createCircularMarkerIcon(context, friend, isCurrentUser)
                        }
                        marker.icon = photoIcon
                        mapView.invalidate()
                        android.util.Log.d("FindMyMapScreen", "    ✓ Foto cargada para ${friend.nombre}")
                    } catch (e: Exception) {
                        android.util.Log.e("FindMyMapScreen", "    ✗ Error cargando foto", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FindMyMapScreen", "    ✗ Error creando marker", e)
            }
        }
    }
    
    android.util.Log.d("FindMyMapScreen", "✅ Total markers en mapa: ${existingMarkers.size}")
    mapView.invalidate()
}

/**
 * Crea un placeholder simple (círculo verde o azul) para mostrar inmediatamente
 */
private fun createPlaceholderIcon(context: Context, isCurrentUser: Boolean = false): Drawable {
    val size = 100
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
        // Azul para el usuario actual, verde para amigos
        color = if (isCurrentUser) 0xFF007AFF.toInt() else 0xFF34C759.toInt()
        style = Paint.Style.FILL
    }
    
    val center = size / 2f
    canvas.drawCircle(center, center, center, paint)
    
    // Borde blanco
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, center - 3f, paint)
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Crea un icono circular con la foto de perfil del amigo
 */
private suspend fun createCircularMarkerIcon(
    context: Context,
    friend: FriendLocation,
    isCurrentUser: Boolean = false
): Drawable = withContext(Dispatchers.IO) {
    android.util.Log.d("FindMyMapScreen", "      🖼️ Procesando foto para ${friend.nombre} (isCurrentUser: $isCurrentUser)")
    val size = 100 // Tamaño del marker en píxeles
    
    // Descargar la imagen del perfil
    val bitmap = if (friend.photoUrl.isNotEmpty()) {
        try {
            val downloaded = loadImageFromUrl(context, friend.photoUrl, size)
            if (downloaded != null) {
                android.util.Log.d("FindMyMapScreen", "      ✓ Bitmap obtenido")
                downloaded
            } else {
                android.util.Log.w("FindMyMapScreen", "      ⚠️ Bitmap null, usando placeholder")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FindMyMapScreen", "      ✗ Error cargando foto", e)
            null
        }
    } else {
        android.util.Log.d("FindMyMapScreen", "      ⚠️ Sin URL de foto")
        null
    }
    
    // Crear bitmap circular con borde azul para usuario actual, verde para amigos
    val borderColor = if (isCurrentUser) 0xFF007AFF.toInt() else 0xFF34C759.toInt()
    val circularBitmap = createCircularBitmap(
        bitmap = bitmap,
        size = size,
        borderColor = borderColor,
        borderWidth = 6f
    )
    
    android.util.Log.d("FindMyMapScreen", "      ✓ Icono circular creado: ${circularBitmap.width}x${circularBitmap.height}")
    BitmapDrawable(context.resources, circularBitmap)
}

/**
 * Descarga una imagen desde URL usando Coil
 */
private suspend fun loadImageFromUrl(
    context: Context,
    url: String,
    size: Int
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        android.util.Log.d("FindMyMapScreen", "        Descargando: $url")
        val loader = ImageLoader.Builder(context)
            .crossfade(true)
            .build()
        
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(size, size)
            .allowHardware(false) // Importante para poder manipular el bitmap
            .build()
        
        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            android.util.Log.d("FindMyMapScreen", "        ✓ Imagen descargada: ${bitmap?.width}x${bitmap?.height}")
            bitmap
        } else {
            android.util.Log.w("FindMyMapScreen", "        ✗ Error en resultado de Coil")
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("FindMyMapScreen", "        ✗ Excepción descargando imagen", e)
        null
    }
}

/**
 * Crea un bitmap circular con borde de color
 */
private fun createCircularBitmap(
    bitmap: Bitmap?,
    size: Int,
    borderColor: Int,
    borderWidth: Float
): Bitmap {
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    
    val paint = Paint().apply {
        isAntiAlias = true
    }
    
    val center = size / 2f
    val radius = center - borderWidth / 2
    
    if (bitmap != null) {
        // Escalar bitmap a tamaño correcto
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        
        // Dibujar círculo de máscara
        val maskPaint = Paint().apply {
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, radius, maskPaint)
        
        // Aplicar imagen con máscara circular
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, maskPaint)
        
        // Dibujar borde verde encima
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
        canvas.drawCircle(center, center, radius - borderWidth / 2, borderPaint)
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
    } else {
        // Sin foto: círculo blanco con borde verde
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(center, center, radius, paint)
        
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        canvas.drawCircle(center, center, radius - borderWidth / 2, paint)
        
        // Ícono genérico (círculo gris pequeño en el centro)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFBDBDBD.toInt()
        canvas.drawCircle(center, center, radius * 0.4f, paint)
    }
    
    return output
}

/**
 * Bottom sheet con detalles del amigo seleccionado
 */
@Composable
private fun FriendDetailsSheet(
    friend: FriendLocation,
    onClose: () -> Unit,
    onNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con foto y nombre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = if (friend.isOnline) Color(0xFF4CAF50) else Color.Gray,
                            shape = CircleShape
                        )
                ) {
                    coil.compose.AsyncImage(
                        model = friend.photoUrl.ifEmpty { null },
                        contentDescription = friend.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = null,
                        error = null
                    )
                }
                
                // Nombre y estado
                Column {
                    Text(
                        text = friend.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (friend.isOnline) Color(0xFF4CAF50) else Color.Gray)
                        )
                        Text(
                            text = if (friend.isOnline) "En línea" else "Desconectado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
            }
        }
        
        Divider()
        
        // Dirección
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (friend.address.isNotEmpty()) {
                Text(
                    text = friend.address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Cargando dirección...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
        
        // Coordenadas
        Text(
            text = "Lat: ${"%.6f".format(friend.latitude)}, Lon: ${"%.6f".format(friend.longitude)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Última actualización
        val timeAgo = getTimeAgo(friend.lastUpdate)
        Text(
            text = "Actualizado $timeAgo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Botón para navegar
        Button(
            onClick = onNavigate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.GpsFixed, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver en el mapa")
        }
        
        Spacer(Modifier.height(32.dp)) // Padding inferior para el bottom sheet
    }
}

/**
 * Calcula el tiempo transcurrido desde un timestamp
 */
private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "hace un momento"
        diff < 3600_000 -> "hace ${diff / 60_000} min"
        diff < 86400_000 -> "hace ${diff / 3600_000} h"
        else -> "hace ${diff / 86400_000} días"
    }
}

/**
 * Bottom sheet con detalles del sitio deportivo seleccionado
 */
@Composable
private fun SportPlaceDetailsSheet(
    sportPlace: com.example.spottivo.data.models.SportPlace,
    onClose: () -> Unit,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Galería de fotos
        if (sportPlace.fotos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sportPlace.fotos.take(2).forEach { photoUrl ->
                    coil.compose.AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con nombre y calificación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sportPlace.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (sportPlace.calificacion > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${"%.1f".format(sportPlace.calificacion)} (${sportPlace.numeroCalificaciones} reseñas)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }
            
            // Descripción
            if (sportPlace.descripcion.isNotEmpty()) {
                Text(
                    text = sportPlace.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Tags de actividades
            if (sportPlace.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Actividades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sportPlace.tags.take(3).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Información de contacto
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Información",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Dirección
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = sportPlace.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Horarios
                if (sportPlace.horarios.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = sportPlace.horarios,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Precio
                if (sportPlace.precioDesde > 0 || sportPlace.precioHasta > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        val precioText = if (sportPlace.precioHasta > sportPlace.precioDesde) {
                            "$${"%.0f".format(sportPlace.precioDesde)}k - $${"%.0f".format(sportPlace.precioHasta)}k"
                        } else {
                            "$${"%.0f".format(sportPlace.precioDesde)}k"
                        }
                        Text(
                            text = precioText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Botones de contacto
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Contactar al propietario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de llamar
                    if (sportPlace.telefono.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${sportPlace.telefono}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📞 Llamar")
                        }
                    }
                    
                    // Botón de WhatsApp
                    if (sportPlace.whatsapp.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://wa.me/${sportPlace.whatsapp.replace("+", "")}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF25D366)
                            )
                        ) {
                            Text("WhatsApp")
                        }
                    }
                }
                
                // Botón de email
                if (sportPlace.email.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:${sportPlace.email}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✉️ Enviar correo")
                    }
                }
            }
        }
    }
}

/* ---------------- OSRM Routing ---------------- */

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

/**
 * Actualiza los markers de sitios deportivos en el mapa
 */
private suspend fun updateSportPlaceMarkers(
    context: Context,
    mapView: MapView,
    sportPlaces: List<com.example.spottivo.data.models.SportPlace>,
    existingMarkers: MutableMap<String, Marker>,
    onMarkerClick: (com.example.spottivo.data.models.SportPlace) -> Unit
) = withContext(Dispatchers.Main) {
    android.util.Log.d("FindMyMapScreen", "🏟️ updateSportPlaceMarkers: ${sportPlaces.size} sitios")
    
    // Remover markers de sitios que ya no están en la lista
    val currentPlaceIds = sportPlaces.map { "place_${it.id}" }.toSet()
    val markersToRemove = existingMarkers.keys.filter { it.startsWith("place_") && it !in currentPlaceIds }
    
    markersToRemove.forEach { placeId ->
        existingMarkers[placeId]?.let { marker ->
            mapView.overlays.remove(marker)
        }
        existingMarkers.remove(placeId)
    }
    
    // Agregar o actualizar markers de sitios deportivos
    sportPlaces.forEach { place ->
        val markerId = "place_${place.id}"
        val existingMarker = existingMarkers[markerId]
        
        if (existingMarker != null) {
            // Actualizar posición del marker existente
            existingMarker.position = GeoPoint(place.latitud, place.longitud)
            existingMarker.title = place.nombre
            existingMarker.snippet = place.direccion
        } else {
            // Crear nuevo marker para sitio deportivo
            try {
                val marker = Marker(mapView).apply {
                    position = GeoPoint(place.latitud, place.longitud)
                    title = place.nombre
                    snippet = place.direccion
                    
                    // Crear ícono personalizado con emoji según el primer tag
                    icon = createSportPlaceMarkerIcon(context, place.tags.firstOrNull() ?: "")
                    
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    
                    setOnMarkerClickListener { clickedMarker, _ ->
                        onMarkerClick(place)
                        true
                    }
                }
                
                mapView.overlays.add(marker)
                existingMarkers[markerId] = marker
                android.util.Log.d("FindMyMapScreen", "  ✓ Marker agregado para: ${place.nombre} (${place.tags.firstOrNull()})")
                
            } catch (e: Exception) {
                android.util.Log.e("FindMyMapScreen", "  ✗ Error creando marker para ${place.nombre}", e)
            }
        }
    }
    
    mapView.invalidate()
    android.util.Log.d("FindMyMapScreen", "✅ Total markers en mapa: ${mapView.overlays.size}")
}

/**
 * Obtiene el recurso de ícono según el tag del deporte
 */
private fun getSportIconResource(tag: String): Int {
    return when (tag) {
        "Gimnasio" -> android.R.drawable.star_big_on
        "CrossFit" -> android.R.drawable.star_big_on
        "Yoga" -> android.R.drawable.star_big_on
        "Pilates" -> android.R.drawable.star_big_on
        "Natación" -> android.R.drawable.star_big_on
        "Tenis" -> android.R.drawable.star_big_on
        "Fútbol" -> android.R.drawable.star_big_on
        "Baloncesto" -> android.R.drawable.star_big_on
        "Voleibol" -> android.R.drawable.star_big_on
        "Artes Marciales" -> android.R.drawable.star_big_on
        "Boxeo" -> android.R.drawable.star_big_on
        "Ciclismo" -> android.R.drawable.star_big_on
        "Atletismo" -> android.R.drawable.star_big_on
        "Escalada" -> android.R.drawable.star_big_on
        "Pádel" -> android.R.drawable.star_big_on
        "Squash" -> android.R.drawable.star_big_on
        "Spinning" -> android.R.drawable.star_big_on
        "Entrenamiento Funcional" -> android.R.drawable.star_big_on
        "Calistenia" -> android.R.drawable.star_big_on
        "Parkour" -> android.R.drawable.star_big_on
        else -> android.R.drawable.ic_dialog_map
    }
}

/**
 * Crea un marcador personalizado con el emoji del deporte
 */
private fun createSportPlaceMarkerIcon(context: Context, tag: String): Drawable {
    // Emojis según el tipo de deporte
    val emoji = when (tag) {
        "Gimnasio" -> "💪"
        "CrossFit" -> "🏋️"
        "Yoga" -> "🧘"
        "Pilates" -> "🤸"
        "Natación" -> "🏊"
        "Tenis" -> "🎾"
        "Fútbol" -> "⚽"
        "Baloncesto" -> "🏀"
        "Voleibol" -> "🏐"
        "Artes Marciales" -> "🥋"
        "Boxeo" -> "🥊"
        "Ciclismo" -> "🚴"
        "Atletismo" -> "🏃"
        "Escalada" -> "🧗"
        "Pádel" -> "🎾"
        "Squash" -> "🎾"
        "Spinning" -> "🚴"
        "Entrenamiento Funcional" -> "💪"
        "Calistenia" -> "🤸"
        "Parkour" -> "🤸"
        else -> "📍"
    }
    
    // Color según el tipo de deporte
    val color = when (tag) {
        "Gimnasio", "CrossFit", "Entrenamiento Funcional" -> 0xFFFF5722.toInt() // Rojo-naranja
        "Yoga", "Pilates" -> 0xFF9C27B0.toInt() // Púrpura
        "Natación" -> 0xFF2196F3.toInt() // Azul
        "Tenis", "Pádel", "Squash" -> 0xFF4CAF50.toInt() // Verde
        "Fútbol" -> 0xFF8BC34A.toInt() // Verde claro
        "Baloncesto" -> 0xFFFF9800.toInt() // Naranja
        "Voleibol" -> 0xFFFFC107.toInt() // Amarillo
        "Artes Marciales", "Boxeo" -> 0xFFF44336.toInt() // Rojo
        "Ciclismo", "Spinning" -> 0xFF03A9F4.toInt() // Azul claro
        "Atletismo" -> 0xFF00BCD4.toInt() // Cyan
        "Escalada" -> 0xFF795548.toInt() // Marrón
        "Calistenia", "Parkour" -> 0xFF607D8B.toInt() // Gris azulado
        else -> 0xFF9E9E9E.toInt() // Gris
    }
    
    // Crear bitmap con el emoji y fondo circular
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Dibujar círculo de fondo
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    // Dibujar borde blanco
    paint.apply {
        this.color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    // Dibujar emoji
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0xFFFFFFFF.toInt()
        textSize = size / 2.2f
        textAlign = Paint.Align.CENTER
    }
    val textBounds = Rect()
    textPaint.getTextBounds(emoji, 0, emoji.length, textBounds)
    canvas.drawText(emoji, size / 2f, size / 2f - textBounds.exactCenterY(), textPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

 
