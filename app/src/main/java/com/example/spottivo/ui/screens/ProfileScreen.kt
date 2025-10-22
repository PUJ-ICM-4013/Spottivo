package com.example.spottivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spottivo.R
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController,
                  viewModel: ProfileViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_spottivo_logo),
                contentDescription = "Spottivo Logo",
                modifier = Modifier.size(32.dp),
                tint = PrimaryPurple
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
            ) {
                // Contenido principal (avatar, nombre, email) centrado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.userPhotoUri != null) {
                            AsyncImage(
                                model = viewModel.userPhotoUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = viewModel.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botón de editar en la esquina inferior derecha del card
                IconButton(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(28.dp)
                        .background(PrimaryPurple, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Editar perfil",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tu perfil",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Options
        LazyColumn {
            items(5) { index ->
                val options = listOf(
                    Pair("Configuración", R.drawable.ic_settings),
                    Pair("Favoritos", R.drawable.ic_favorite),
                    Pair("Ubicación", R.drawable.ic_location),
                    Pair("Ayuda", R.drawable.ic_help),
                    Pair("Cerrar sesión", R.drawable.ic_logout)
                )
                
                if (index < options.size) {
                    ProfileOptionItem(
                        title = options[index].first,
                        iconRes = options[index].second
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    title: String,
    iconRes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = PrimaryPurple
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Arrow",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}