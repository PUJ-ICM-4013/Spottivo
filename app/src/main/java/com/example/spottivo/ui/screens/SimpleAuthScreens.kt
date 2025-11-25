package com.example.spottivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.ui.theme.TextPrimaryLight
import com.example.spottivo.ui.theme.GrayDark
import com.example.spottivo.ui.theme.AccentGreen
import com.example.spottivo.viewmodel.LoginViewModel
import com.example.spottivo.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Spottivo",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(60.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(
            onClick = {
                viewModel.clearError()
                viewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = "Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¿Olvidaste tu contraseña?",
            color = PrimaryPurple,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onForgotPassword() }
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "No tienes cuenta: ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Regístrate",
                color = PrimaryPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToBusiness: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegisterSuccessful) {
        if (uiState.isRegisterSuccessful) {
            onRegisterSuccess()
            viewModel.resetRegisterState()
        }
    }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    fun validateName(): Boolean {
        nameError = when {
            name.isBlank() -> "El nombre es requerido"
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> ""
        }; return nameError.isEmpty()
    }
    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "El email es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> ""
        }; return emailError.isEmpty()
    }
    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> ""
        }; return passwordError.isEmpty()
    }
    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> ""
        }; return confirmPasswordError.isEmpty()
    }
    fun isFormValid(): Boolean = validateName() && validateEmail() && validatePassword() && validateConfirmPassword()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "Spottivo", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; if (nameError.isNotEmpty()) validateName() },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError.isNotEmpty(),
            supportingText = if (nameError.isNotEmpty()) { { Text(nameError, color = MaterialTheme.colorScheme.error) } } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; if (emailError.isNotEmpty()) validateEmail() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError.isNotEmpty(),
            supportingText = if (emailError.isNotEmpty()) { { Text(emailError, color = MaterialTheme.colorScheme.error) } } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; if (passwordError.isNotEmpty()) validatePassword(); if (confirmPassword.isNotEmpty() && confirmPasswordError.isNotEmpty()) validateConfirmPassword() },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            supportingText = if (passwordError.isNotEmpty()) { { Text(passwordError, color = MaterialTheme.colorScheme.error) } } else null,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; if (confirmPasswordError.isNotEmpty()) validateConfirmPassword() },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = if (confirmPasswordError.isNotEmpty()) { { Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) } } else null,
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { if (isFormValid()) { viewModel.register(name, email, password); viewModel.clearError() } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = "Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) { Text(text = uiState.errorMessage, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "¿Tienes un negocio deportivo? ", color = Color.Gray, fontSize = 14.sp)
            Text(text = "Regístrate aquí", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onNavigateToBusiness() })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Ya tienes cuenta: ", color = Color.Gray, fontSize = 14.sp)
            Text(text = "Inicia sesión", color = PrimaryPurple, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onNavigateToLogin() })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
