package com.example.spottivo.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ProfileViewModel : ViewModel() {
    var userName by mutableStateOf("Usuario Spottivo")
    var userEmail by mutableStateOf("usuario@spottivo.com")
    var userPhotoUri by mutableStateOf<String?>(null)
}