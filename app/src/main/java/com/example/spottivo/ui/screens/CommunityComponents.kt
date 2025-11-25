package com.example.spottivo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.spottivo.R
import com.example.spottivo.model.Community
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.viewmodel.CommunityListViewModel

/**
 * Card de comunidad mejorado
 */
@Composable
fun CommunityCard(
    community: Community,
    isMember: Boolean = false,
    showJoinButton: Boolean = true,
    onJoin: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Foto de la comunidad más grande
                AsyncImage(
                    model = community.photoUrl.ifEmpty { R.drawable.ic_community },
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                
                // Información de la comunidad
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = community.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = community.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información adicional y botón
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de miembros
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${community.memberCount} miembros",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    if (community.isPublic) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Público",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                // Botón de unirse (solo si no es miembro y showJoinButton es true)
                if (!isMember && showJoinButton) {
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Unirse", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog para crear una nueva comunidad
 */
@Composable
fun CreateCommunityDialog(
    onDismiss: () -> Unit,
    onCommunityCreated: (String) -> Unit,
    communityListViewModel: CommunityListViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var isCreating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Dialog(onDismissRequest = { if (!isCreating) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear Comunidad",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la comunidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isCreating
                )
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isCreating
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comunidad pública",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = !isCreating
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(Modifier.weight(1f))
                    
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isCreating
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (nombre.isNotBlank() && descripcion.isNotBlank()) {
                                isCreating = true
                                android.util.Log.d("CreateCommunityDialog", "🚀 Iniciando creación de comunidad: $nombre")
                                
                                communityListViewModel.createCommunity(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    photoUri = null,
                                    isPublic = isPublic,
                                    onSuccess = { communityId ->
                                        android.util.Log.d("CreateCommunityDialog", "✅ Comunidad creada con ID: $communityId")
                                        isCreating = false
                                        Toast.makeText(context, "Comunidad '$nombre' creada exitosamente", Toast.LENGTH_LONG).show()
                                        onCommunityCreated(communityId)
                                    },
                                    onError = { error ->
                                        android.util.Log.e("CreateCommunityDialog", "❌ Error: $error")
                                        isCreating = false
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        enabled = !isCreating && nombre.isNotBlank() && descripcion.isNotBlank()
                    ) {
                        if (isCreating) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text("Creando...")
                            }
                        } else {
                            Text("Crear")
                        }
                    }
                }
            }
        }
    }
}
