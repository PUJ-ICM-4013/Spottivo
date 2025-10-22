package com.example.spottivo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var userName by mutableStateOf("Usuario Spottivo")
    var userEmail by mutableStateOf("usuario@spottivo.com")
    var userPhotoUri by mutableStateOf<String?>(null)
}