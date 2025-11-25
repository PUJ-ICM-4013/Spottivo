package com.example.spottivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.spottivo.ui.components.AppLogo
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.ui.theme.TextPrimaryLight
import com.example.spottivo.ui.theme.GrayDark
import com.example.spottivo.ui.theme.AccentGreen
import com.example.spottivo.viewmodel.LoginViewModel

// Local translucent glass modifier needed for this dark screen
private fun glassModifier(): Modifier = Modifier
    .clip(RoundedCornerShape(28.dp))
    .background(Color.White.copy(alpha = 0.08f))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF13052A), Color(0xFF2F0A58), Color(0xFF13052A))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Recuperar contraseña", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(40.dp))
            AppLogo(sizeDp = 80)
            Spacer(Modifier.height(32.dp))
            Column(modifier = glassModifier().fillMaxWidth().padding(24.dp)) {
                Text(
                    text = "Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; if (uiState.passwordResetSent) viewModel.clearPasswordResetState() },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                        cursorColor = PrimaryPurple,
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = GrayDark
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.sendPasswordReset(email) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Enviar enlace", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                if (uiState.passwordResetSent) {
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.passwordResetMessage ?: "", color = AccentGreen, fontSize = 13.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Volver a iniciar sesión",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.navigate("login_new") }
                )
            }
        }
    }
}
