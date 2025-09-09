package com.example.spottivo.ui.models

data class PopularPlace(
    val id: String,
    val name: String,
    val description: String,
    val distance: String,
    val imageUrl: String,
    val isFavorite: Boolean = false
)