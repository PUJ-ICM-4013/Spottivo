package com.example.spottivo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.spottivo.data.models.SportPlace
import com.example.spottivo.data.models.SportTags
import com.example.spottivo.data.CloudinaryService
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSportPlaceScreen(
    placeId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { com.example.spottivo.data.repository.SportPlaceRepository() }
    
    var place by remember { mutableStateOf<SportPlace?>(null) }
    var isLoadingPlace by remember { mutableStateOf(true) }
    
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var horarios by remember { mutableStateOf("") }
    var precioDesde by remember { mutableStateOf("") }
    var precioHasta by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var newSelectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Cargar datos del lugar
    LaunchedEffect(placeId) {
        scope.launch {
            val result = repository.getSportPlaceById(placeId)
            if (result.isSuccess) {
                val loadedPlace = result.getOrNull()
                if (loadedPlace != null) {
                    place = loadedPlace
                    nombre = loadedPlace.nombre
                    descripcion = loadedPlace.descripcion
                    direccion = loadedPlace.direccion
                    telefono = loadedPlace.telefono
                    email = loadedPlace.email
                    whatsapp = loadedPlace.whatsapp
                    horarios = loadedPlace.horarios
                    precioDesde = loadedPlace.precioDesde.toInt().toString()
                    precioHasta = loadedPlace.precioHasta.toInt().toString()
                    latitud = loadedPlace.latitud.toString()
                    longitud = loadedPlace.longitud.toString()
                    selectedTags = loadedPlace.tags
                    existingImageUrls = loadedPlace.fotos
                }
            } else {
                errorMessage = "Error al cargar el lugar"
            }
            isLoadingPlace = false
        }
    }
    
    if (isLoadingPlace) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (place == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: Lugar no encontrado")
        }
        return
    }
    
    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val totalImages = existingImageUrls.size + newSelectedImages.size + uris.size
            if (totalImages <= 5) {
                newSelectedImages = newSelectedImages + uris
            } else {
                val remaining = 5 - existingImageUrls.size - newSelectedImages.size
                if (remaining > 0) {
                    newSelectedImages = newSelectedImages + uris.take(remaining)
                }
                errorMessage = "Máximo 5 fotos en total"
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        imagePickerLauncher.launch("image/*")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Espacio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información básica
            item {
                Text(
                    text = "Información Básica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del lugar *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Place, null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    leadingIcon = { Icon(Icons.Default.Description, null) }
                )
            }
            
            // Categorías/Tags
            item {
                Text(
                    text = "Actividades Deportivas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showTagsDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Seleccionar actividades *")
                            if (selectedTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(selectedTags) { tag ->
                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
            
            // Fotos existentes y nuevas
            item {
                Text(
                    text = "Fotos del lugar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Fotos existentes
            if (existingImageUrls.isNotEmpty()) {
                item {
                    Text(
                        text = "Fotos actuales",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(existingImageUrls) { url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        existingImageUrls = existingImageUrls.filter { it != url }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Agregar nuevas fotos
            if (existingImageUrls.size + newSelectedImages.size < 5) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) -> {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                }
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Agregar más fotos")
                            Text(
                                text = "${existingImageUrls.size + newSelectedImages.size}/5 fotos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Nuevas fotos seleccionadas
            if (newSelectedImages.isNotEmpty()) {
                item {
                    Text(
                        text = "Nuevas fotos a agregar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(newSelectedImages) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        newSelectedImages = newSelectedImages.filter { it != uri }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Ubicación
            item {
                Text(
                    text = "Ubicación y Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitud,
                        onValueChange = { latitud = it },
                        label = { Text("Latitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = longitud,
                        onValueChange = { longitud = it },
                        label = { Text("Longitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
            
            item {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, null) }
                )
            }
            
            item {
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Message, null) }
                )
            }
            
            // Horarios y precios
            item {
                Text(
                    text = "Horarios y Precios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = horarios,
                    onValueChange = { horarios = it },
                    label = { Text("Horarios") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AccessTime, null) }
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = precioDesde,
                        onValueChange = { precioDesde = it.filter { char -> char.isDigit() } },
                        label = { Text("Precio desde") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("$") }
                    )
                    
                    OutlinedTextField(
                        value = precioHasta,
                        onValueChange = { precioHasta = it.filter { char -> char.isDigit() } },
                        label = { Text("Precio hasta") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("$") }
                    )
                }
            }
            
            // Mensaje de error
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Botón guardar
            item {
                Button(
                    onClick = {
                        scope.launch {
                            when {
                                nombre.isBlank() -> errorMessage = "El nombre es obligatorio"
                                descripcion.isBlank() -> errorMessage = "La descripción es obligatoria"
                                selectedTags.isEmpty() -> errorMessage = "Selecciona al menos una actividad"
                                direccion.isBlank() -> errorMessage = "La dirección es obligatoria"
                                email.isBlank() -> errorMessage = "El email es obligatorio"
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        // Subir nuevas imágenes a Cloudinary
                                        val cloudinaryService = CloudinaryService()
                                        val newImageUrls = mutableListOf<String>()
                                        newSelectedImages.forEach { uri ->
                                            val publicId = "${place!!.propietarioId}_${UUID.randomUUID()}"
                                            val imageUrl = cloudinaryService.uploadImage(
                                                imageUri = uri,
                                                folder = "spottivo/sport_places",
                                                publicId = publicId
                                            )
                                            newImageUrls.add(imageUrl)
                                        }
                                        
                                        // Combinar fotos existentes con nuevas
                                        val allPhotos = existingImageUrls + newImageUrls
                                        
                                        // Crear mapa de actualizaciones
                                        val updates = mapOf(
                                            "nombre" to nombre,
                                            "descripcion" to descripcion,
                                            "tags" to selectedTags,
                                            "fotos" to allPhotos,
                                            "latitud" to (latitud.toDoubleOrNull() ?: place!!.latitud),
                                            "longitud" to (longitud.toDoubleOrNull() ?: place!!.longitud),
                                            "direccion" to direccion,
                                            "telefono" to telefono,
                                            "email" to email,
                                            "whatsapp" to whatsapp,
                                            "horarios" to horarios,
                                            "precioDesde" to (precioDesde.toDoubleOrNull() ?: 0.0),
                                            "precioHasta" to (precioHasta.toDoubleOrNull() ?: 0.0)
                                        )
                                        
                                        // Actualizar en Firestore
                                        val result = repository.updateSportPlace(place!!.id, updates)
                                        
                                        if (result.isSuccess) {
                                            onSuccess()
                                        } else {
                                            errorMessage = "Error al actualizar: ${result.exceptionOrNull()?.message}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Diálogo de selección de tags
    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Seleccionar Actividades") },
            text = {
                LazyColumn {
                    items(SportTags.ALL_TAGS.chunked(2)) { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag),
                                    onClick = {
                                        selectedTags = if (selectedTags.contains(tag)) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    },
                                    label = { Text(tag) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTags.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
