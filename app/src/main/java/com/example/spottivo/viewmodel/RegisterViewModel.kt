package com.example.spottivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottivo.data.AuthRepository
import com.example.spottivo.data.AuthResult
import com.example.spottivo.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isRegisterSuccessful: Boolean = false,
    val errorMessage: String = "",
    val user: User? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            authRepository.registerUser(name, email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = "",
                            isRegisterSuccessful = false
                        )
                    }
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegisterSuccessful = true,
                            errorMessage = "",
                            user = result.user
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegisterSuccessful = false,
                            errorMessage = result.message,
                            user = null
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    fun resetRegisterState() {
        _uiState.value = RegisterUiState()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun logout() {
        authRepository.logout()
        resetRegisterState()
    }
}