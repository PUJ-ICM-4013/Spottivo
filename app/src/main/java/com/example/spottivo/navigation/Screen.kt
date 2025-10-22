package com.example.spottivo.navigation

sealed class Screen(val route: String, val label: String) {
    object Search : Screen("search", "Buscar")
    object Map : Screen("map", "Mapa")
    object Community : Screen("community", "Comunidad")
    object Profile : Screen("profile", "Perfil")

    // Ruta de edición de perfil
    object ProfileEdit : Screen("profile/edit", "Editar perfil")
}