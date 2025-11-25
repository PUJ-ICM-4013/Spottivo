package com.example.spottivo.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spottivo.R
import com.example.spottivo.data.models.SportPlace
import com.example.spottivo.data.models.SportTags
import com.example.spottivo.ui.components.AppLogo
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.viewmodel.SportPlaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: androidx.navigation.NavHostController? = null,
    viewModel: SportPlaceViewModel = viewModel()
) {
    val context = LocalContext.current
    val filteredPlaces by viewModel.filteredPlaces.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header with Spottivo Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLogo(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Spottivo",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Empieza tu búsqueda") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sports Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SportsCategoryItem(
                iconRes = R.drawable.ic_sports,
                title = "Deportes",
                onClick = {
                    viewModel.clearTags()
                    // Deportes: Fútbol, Baloncesto, Voleibol, Tenis, Atletismo
                    viewModel.toggleTag(SportTags.FUTBOL)
                    viewModel.toggleTag(SportTags.BALONCESTO)
                    viewModel.toggleTag(SportTags.VOLEIBOL)
                    viewModel.toggleTag(SportTags.TENIS)
                    viewModel.toggleTag(SportTags.ATLETISMO)
                }
            )
            SportsCategoryItem(
                iconRes = R.drawable.ic_gym,
                title = "Gimnasio",
                onClick = {
                    viewModel.clearTags()
                    // Gimnasio: Gimnasio, CrossFit, Funcional, Spinning
                    viewModel.toggleTag(SportTags.GIMNASIO)
                    viewModel.toggleTag(SportTags.CROSSFIT)
                    viewModel.toggleTag(SportTags.FUNCIONAL)
                    viewModel.toggleTag(SportTags.SPINNING)
                }
            )
            SportsCategoryItem(
                iconRes = R.drawable.ic_competition,
                title = "Competencias",
                onClick = {
                    viewModel.clearTags()
                    // Todas las categorías para competencias
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Popular Places Section
        Text(
            text = "Lugares populares en Bogotá >",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Popular Places List (real data)
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading && filteredPlaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredPlaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron lugares",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPlaces) { place ->
                        PopularPlaceItem(
                            place = place,
                            onClick = { viewModel.selectPlace(place) }
                        )
                    }
                }
            }
        }
    }
    
    // Botón flotante para crear espacio deportivo
    if (navController != null) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("create_sport_place") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Crear espacio")
            }
        }
    }
    
    // Diálogo con detalles del sitio
    selectedPlace?.let { place ->
        SportPlaceDetailDialog(
            place = place,
            onDismiss = { viewModel.selectPlace(null) },
            onContact = { contactMethod ->
                when (contactMethod) {
                    "phone" -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${place.telefono}")
                        }
                        context.startActivity(intent)
                    }
                    "email" -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${place.email}")
                        }
                        context.startActivity(intent)
                    }
                    "whatsapp" -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/${place.whatsapp.replace("+", "")}")
                        }
                        context.startActivity(intent)
                    }
                }
            }
        )
    }
}

@Composable
fun SportsCategoryItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = PrimaryPurple
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PopularPlaceItem(
    place: SportPlace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del lugar
            if (place.fotos.isNotEmpty()) {
                AsyncImage(
                    model = place.fotos.first(),
                    contentDescription = place.nombre,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.direccion.split(",").first(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}