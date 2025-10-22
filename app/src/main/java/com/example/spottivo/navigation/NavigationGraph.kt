package com.example.spottivo.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spottivo.ui.screens.*
import com.example.spottivo.ui.viewmodels.ProfileViewModel

@Composable
fun NavigationGraph(navController: NavHostController, innerPadding: PaddingValues) {

    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Search.route) { SearchScreen() }
        composable(Screen.Map.route) { MapScreen() }
        composable(Screen.Community.route) { CommunityScreen() }

        composable("profile") {
            ProfileScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, viewModel = profileViewModel)
        }
    }
}
