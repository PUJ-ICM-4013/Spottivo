package com.example.spottivo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.spottivo.R
import com.example.spottivo.data.FriendRequestRepository
import com.example.spottivo.data.models.FriendRequest
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.viewmodel.CommunityViewModel
import com.example.spottivo.viewmodel.CommunityListViewModel
import com.example.spottivo.viewmodel.Friend
import com.example.spottivo.model.Community
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = viewModel(),
    communityListViewModel: CommunityListViewModel = viewModel(),
    onNavigateToChat: (String, String, String, Boolean) -> Unit = { _, _, _, _ -> },
    onNavigateToCommunity: (String) -> Unit = { }
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Mensajes", "Solicitudes", "Grupos")
    
    val friends by viewModel.friends.collectAsState()
    val allCommunities by communityListViewModel.allCommunities.collectAsState()
    val myCommunities by communityListViewModel.myCommunities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCreateCommunityDialog by remember { mutableStateOf(false) }
    var pendingRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FriendRequestRepository() }
    
    // Cargar solicitudes pendientes
    LaunchedEffect(Unit) {
        val result = repository.getPendingRequests()
        if (result.isSuccess) {
            pendingRequests = result.getOrNull() ?: emptyList()
        }
    }
    
    Scaffold(
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> {
                    FloatingActionButton(
                        onClick = { showAddFriendDialog = true },
                        containerColor = PrimaryPurple
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir amigo",
                            tint = Color.White
                        )
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { showCreateCommunityDialog = true },
                        containerColor = PrimaryPurple
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear comunidad",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .padding(paddingValues)
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comunidad",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Botón de refrescar
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refrescar",
                    tint = PrimaryPurple
                )
            }
            
            Icon(
                painter = painterResource(id = R.drawable.ic_spottivo_logo),
                contentDescription = "Spottivo Logo",
                modifier = Modifier.size(32.dp),
                tint = PrimaryPurple
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Layout
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryPurple
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title)
                            if (index == 1 && pendingRequests.isNotEmpty()) {
                                Badge {
                                    Text("${pendingRequests.size}")
                                }
                            }
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido según la pestaña seleccionada
        when (selectedTabIndex) {
            0 -> {
                // Pestaña de Mensajes (Amigos)
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No tienes amigos agregados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Agrega amigos para chatear con ellos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            FriendItem(
                                friend = friend,
                                onClick = {
                                    android.util.Log.e("CommunityScreen", "🚨 CLICK en amigo:")
                                    android.util.Log.e("CommunityScreen", "   userId: '${friend.userId}'")
                                    android.util.Log.e("CommunityScreen", "   nombre: '${friend.nombre}'")
                                    android.util.Log.e("CommunityScreen", "   photoUrl: '${friend.photoUrl}'")
                                    
                                    onNavigateToChat(
                                        friend.userId,
                                        java.net.URLEncoder.encode(friend.nombre, "UTF-8"),
                                        java.net.URLEncoder.encode(friend.photoUrl, "UTF-8"),
                                        friend.isOnline
                                    )
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Pestaña de Solicitudes
                if (pendingRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No tienes solicitudes pendientes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pendingRequests) { request ->
                            FriendRequestItem(
                                request = request,
                                onAccept = {
                                    scope.launch {
                                        val result = repository.acceptFriendRequest(request)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
                                            pendingRequests = pendingRequests.filter { it.requestId != request.requestId }
                                            viewModel.refresh()
                                        } else {
                                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        val result = repository.rejectFriendRequest(request.requestId)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                                            pendingRequests = pendingRequests.filter { it.requestId != request.requestId }
                                        } else {
                                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            2 -> {
                // Pestaña de Grupos
                var showGroupSubTabs by remember { mutableStateOf(0) }
                val groupTabs = listOf("Mis Grupos", "Explorar")
                
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sub-pestañas
                    TabRow(selectedTabIndex = showGroupSubTabs) {
                        groupTabs.forEachIndexed { index, title ->
                            Tab(
                                selected = showGroupSubTabs == index,
                                onClick = { showGroupSubTabs = index },
                                text = { Text(title) }
                            )
                        }
                    }
                    
                    when (showGroupSubTabs) {
                        0 -> {
                            // Mis Grupos
                            if (myCommunities.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_community),
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No estás en ningún grupo",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Explora o crea uno nuevo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(myCommunities) { community ->
                                        CommunityCard(
                                            community = community,
                                            isMember = true,
                                            showJoinButton = false,
                                            onClick = { onNavigateToCommunity(community.id) }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Explorar Comunidades
                            if (allCommunities.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text(
                                            text = "Cargando comunidades...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(allCommunities) { community ->
                                        val isMember = myCommunities.any { it.id == community.id }
                                        CommunityCard(
                                            community = community,
                                            isMember = isMember,
                                            showJoinButton = true,
                                            onJoin = {
                                                communityListViewModel.joinCommunity(community.id)
                                                Toast.makeText(context, "Te uniste a ${community.nombre}", Toast.LENGTH_SHORT).show()
                                            },
                                            onClick = { onNavigateToCommunity(community.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        
        // Dialog para añadir amigos
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onFriendAdded = {
                    viewModel.refresh()
                    showAddFriendDialog = false
                }
            )
        }
        
        // Dialog para crear comunidad
        if (showCreateCommunityDialog) {
            CreateCommunityDialog(
                onDismiss = { showCreateCommunityDialog = false },
                onCommunityCreated = { communityId ->
                    showCreateCommunityDialog = false
                },
                communityListViewModel = communityListViewModel
            )
        }
    }
}

/**
 * Item de amigo en la lista de mensajes
 */
@Composable
fun FriendItem(
    friend: Friend,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil con indicador de estado
            Box {
                if (friend.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = friend.photoUrl,
                        contentDescription = "Foto de ${friend.nombre}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (friend.isOnline) Color(0xFF34C759) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 2.dp,
                                color = if (friend.isOnline) Color(0xFF34C759) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Indicador de estado online (punto verde)
                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del amigo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de estado
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (friend.isOnline) Color(0xFF34C759) else Color.Gray
                            )
                    )
                    
                    Text(
                        text = if (friend.isOnline) {
                            "En línea"
                        } else {
                            getLastSeenText(friend.lastSeen)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (friend.isOnline) {
                            Color(0xFF34C759)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Convierte el timestamp de última vez visto a texto legible
 */
fun getLastSeenText(lastSeen: Long): String {
    if (lastSeen == 0L) return "Offline"
    
    val now = System.currentTimeMillis()
    val diff = now - lastSeen
    
    val minutes = diff / 60000
    val hours = diff / 3600000
    val days = diff / 86400000
    
    return when {
        minutes < 1 -> "Hace un momento"
        minutes < 60 -> "Hace ${minutes}m"
        hours < 24 -> "Hace ${hours}h"
        days < 7 -> "Hace ${days}d"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(lastSeen))
        }
    }
}

@Composable
fun CommunityMemberItem(
    name: String,
    status: String
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dialog para añadir amigos por email
 */
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onFriendAdded: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FriendRequestRepository() }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Añadir Amigo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
                
                Text(
                    text = "Busca a tu amigo por su correo electrónico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Campo de email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                        searchResult = null
                    },
                    label = { Text("Email del amigo") },
                    placeholder = { Text("ejemplo@email.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSearching
                )
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Resultado de búsqueda
                if (searchResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Foto de perfil
                            val photoUrl = searchResult?.get("photoUrl") as? String ?: ""
                            if (photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_person),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = searchResult?.get("nombre") as? String ?: "Usuario",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = searchResult?.get("email") as? String ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSearching
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (searchResult != null) {
                                // Enviar solicitud
                                scope.launch {
                                    isSearching = true
                                    val userId = searchResult?.get("userId") as? String ?: ""
                                    val userName = searchResult?.get("nombre") as? String ?: ""
                                    val userEmail = searchResult?.get("email") as? String ?: ""
                                    
                                    val result = repository.sendFriendRequest(userId, userName, userEmail)
                                    isSearching = false
                                    
                                    if (result.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            "Solicitud enviada a $userName",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onFriendAdded()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                                    }
                                }
                            } else {
                                // Buscar usuario
                                if (email.isBlank()) {
                                    errorMessage = "Ingresa un email"
                                    return@Button
                                }
                                
                                scope.launch {
                                    isSearching = true
                                    val result = repository.searchUserByEmail(email)
                                    isSearching = false
                                    
                                    if (result.isSuccess) {
                                        val user = result.getOrNull()
                                        if (user != null) {
                                            searchResult = user
                                        } else {
                                            errorMessage = "No se encontró un usuario con ese email"
                                        }
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Error buscando usuario"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSearching,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        )
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (searchResult != null) "Enviar Solicitud" else "Buscar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item de solicitud de amistad
 */
@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto de perfil
            Box {
                if (request.senderPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = request.senderPhotoUrl,
                        contentDescription = request.senderName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.senderEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Quiere ser tu amigo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botones de acción
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onAccept,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF34C759)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aceptar",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rechazar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}