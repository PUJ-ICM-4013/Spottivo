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

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val passwordResetSent: Boolean = false,
    val passwordResetMessage: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Validaciones básicas
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El email es requerido"
            )
            return
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "La contraseña es requerida"
            )
            return
        }

        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Formato de email inválido"
            )
            return
        }

        // Realizar login
        viewModelScope.launch {
            authRepository.loginWithEmail(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = result.user,
                            errorMessage = null,
                            isLoginSuccessful = true
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            isLoginSuccessful = false
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    fun clearPasswordResetState() {
        _uiState.value = _uiState.value.copy(passwordResetSent = false, passwordResetMessage = null)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = LoginUiState()
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El email es requerido")
            return
        }
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Formato de email inválido")
            return
        }
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    passwordResetSent = true,
                    passwordResetMessage = result.getOrNull(),
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    passwordResetSent = false,
                    passwordResetMessage = null,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}