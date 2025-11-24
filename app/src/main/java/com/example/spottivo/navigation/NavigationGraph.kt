package com.example.spottivo.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.spottivo.ui.screens.*
import com.example.spottivo.viewmodel.ProfileViewModel
import com.example.spottivo.viewmodel.MapViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun NavigationGraph(navController: NavHostController, innerPadding: PaddingValues) {

    val profileViewModel: ProfileViewModel = viewModel()
    val mapViewModel: MapViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Search.route) { SearchScreen() }
        composable(Screen.Map.route) { FindMyMapScreen(viewModel = mapViewModel) }
        composable(Screen.Community.route) { 
            CommunityScreen(
                onNavigateToChat = { friendId, friendName, friendPhotoUrl, isOnline ->
                    val route = Screen.Chat.createRoute(friendId, friendName, friendPhotoUrl, isOnline)
                    android.util.Log.e("NavigationGraph", "🚨 NAVEGANDO AL CHAT")
                    android.util.Log.e("NavigationGraph", "   friendId: '$friendId'")
                    android.util.Log.e("NavigationGraph", "   friendName: '$friendName'")
                    android.util.Log.e("NavigationGraph", "   Ruta: '$route'")
                    navController.navigate(route)
                },
                onNavigateToCommunity = { communityId ->
                    navController.navigate(Screen.CommunityDetail.createRoute(communityId))
                }
            )
        }

        composable("profile") {
            ProfileScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, viewModel = profileViewModel)
        }
        
        // Ruta del chat
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("friendName") { type = NavType.StringType },
                navArgument("friendPhotoUrl") { type = NavType.StringType },
                navArgument("isOnline") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendName = URLDecoder.decode(
                backStackEntry.arguments?.getString("friendName") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val friendPhotoUrl = URLDecoder.decode(
                backStackEntry.arguments?.getString("friendPhotoUrl") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
            
            ChatScreen(
                friendId = friendId,
                friendName = friendName,
                friendPhotoUrl = friendPhotoUrl,
                isOnline = isOnline,
                onBack = { navController.popBackStack() }
            )
        }
        
        // Ruta de detalle de comunidad
        composable(
            route = Screen.CommunityDetail.route,
            arguments = listOf(
                navArgument("communityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
            
            // Obtener la comunidad del ViewModel
            val communityListViewModel: com.example.spottivo.viewmodel.CommunityListViewModel = viewModel()
            val allCommunities by communityListViewModel.allCommunities.collectAsState()
            val myCommunities by communityListViewModel.myCommunities.collectAsState()
            
            val community = (allCommunities + myCommunities).find { it.id == communityId }
            
            if (community != null) {
                CommunityDetailScreen(
                    community = community,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

