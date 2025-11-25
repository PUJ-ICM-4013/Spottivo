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
import com.example.spottivo.data.models.SportPlace
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
        startDestination = "splash", // splash first
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("splash") { SplashScreen(navController = navController) }
        // New auth flow screens
        composable("login_new") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register_new") },
                onLoginSuccess = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo("login_new") { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("register_new") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login_new") },
                onRegisterSuccess = {
                    navController.navigate("login_new") {
                        popUpTo("register_new") { inclusive = true }
                    }
                },
                onNavigateToBusiness = { /* TODO: implementar ruta negocio */ }
            )
        }
        composable("forgot_password") { 
            ForgotPasswordScreen(navController = navController) 
        }
        // Legacy simple login screen for comparison
        composable("login_old") {
            LoginLegacyScreen(
                onNavigateToRegister = { navController.navigate("register_new") },
                onLoginSuccess = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo("login_old") { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable(Screen.Search.route) { SearchScreen(navController = navController) }
        composable("create_sport_place") {
            CreateSportPlaceScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable("my_sport_places") {
            MySportPlacesScreen(
                onBack = { navController.popBackStack() },
                onCreateNew = { navController.navigate("create_sport_place") },
                onEditPlace = { place ->
                    navController.navigate("edit_sport_place/${place.id}")
                }
            )
        }
        composable(
            route = "edit_sport_place/{placeId}",
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            EditSportPlaceScreen(
                placeId = placeId,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
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

