package com.example.spottivo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhotoCamera
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
import com.example.spottivo.ui.theme.PrimaryPurple
import com.example.spottivo.ui.theme.TextPrimaryLight
import com.example.spottivo.ui.theme.GrayDark
import com.example.spottivo.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessRegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegular: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    // Personal info states
    var ownerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Business info states
    var siteName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var sportType by remember { mutableStateOf("") }
    var pricePerHour by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    // Sport types
    val sportTypes = listOf(
        "Fútbol",
        "Básquetbol",
        "Tenis",
        "Pádel",
        "Voleibol",
        "Fútbol americano",
        "Béisbol",
        "Otro"
    )
    
    // Validation states
    var ownerNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var siteNameError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var sportTypeError by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf("") }

    // Validation functions
    fun validateOwnerName(): Boolean {
        ownerNameError = when {
            ownerName.isBlank() -> "El nombre del propietario es requerido"
            ownerName.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> ""
        }
        return ownerNameError.isEmpty()
    }

    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "El email es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> ""
        }
        return emailError.isEmpty()
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> ""
        }
        return passwordError.isEmpty()
    }

    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> ""
        }
        return confirmPasswordError.isEmpty()
    }

    fun validateSiteName(): Boolean {
        siteNameError = when {
            siteName.isBlank() -> "El nombre del sitio es requerido"
            siteName.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> ""
        }
        return siteNameError.isEmpty()
    }

    fun validateAddress(): Boolean {
        addressError = when {
            address.isBlank() -> "La dirección es requerida"
            address.length < 10 -> "Proporciona una dirección completa"
            else -> ""
        }
        return addressError.isEmpty()
    }

    fun validateSportType(): Boolean {
        sportTypeError = when {
            sportType.isBlank() -> "Selecciona un tipo de deporte"
            else -> ""
        }
        return sportTypeError.isEmpty()
    }

    fun validatePrice(): Boolean {
        priceError = when {
            pricePerHour.isBlank() -> "El precio por hora es requerido"
            pricePerHour.toDoubleOrNull() == null -> "Ingresa un precio válido"
            pricePerHour.toDouble() <= 0 -> "El precio debe ser mayor a 0"
            else -> ""
        }
        return priceError.isEmpty()
    }

    fun isFormValid(): Boolean {
        return validateOwnerName() && validateEmail() && validatePassword() && 
               validateConfirmPassword() && validateSiteName() && validateAddress() && 
               validateSportType() && validatePrice()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo/Title
        Text(
            text = "Crear sitio deportivo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Owner Name Field
        OutlinedTextField(
            value = ownerName,
            onValueChange = { 
                ownerName = it
                if (ownerNameError.isNotEmpty()) validateOwnerName()
            },
            label = { Text("Nombre del propietario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = ownerNameError.isNotEmpty(),
            supportingText = if (ownerNameError.isNotEmpty()) {
                { Text(ownerNameError, color = MaterialTheme.colorScheme.error) }
            } else null,
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
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (emailError.isNotEmpty()) validateEmail()
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError.isNotEmpty(),
            supportingText = if (emailError.isNotEmpty()) {
                { Text(emailError, color = MaterialTheme.colorScheme.error) }
            } else null,
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
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (passwordError.isNotEmpty()) validatePassword()
                if (confirmPassword.isNotEmpty() && confirmPasswordError.isNotEmpty()) {
                    validateConfirmPassword()
                }
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            supportingText = if (passwordError.isNotEmpty()) {
                { Text(passwordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
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
        
        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (confirmPasswordError.isNotEmpty()) validateConfirmPassword()
            },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = if (confirmPasswordError.isNotEmpty()) {
                { Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            trailingIcon = {
                val image = if (confirmPasswordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Business Information Section
        Text(
            text = "Información del negocio",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryPurple,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Site Name Field
        OutlinedTextField(
            value = siteName,
            onValueChange = { 
                siteName = it
                if (siteNameError.isNotEmpty()) validateSiteName()
            },
            label = { Text("Nombre del sitio") },
            placeholder = { Text("Nombre del sitio", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = siteNameError.isNotEmpty(),
            supportingText = if (siteNameError.isNotEmpty()) {
                { Text(siteNameError, color = MaterialTheme.colorScheme.error) }
            } else null,
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
        
        // Address Field
        OutlinedTextField(
            value = address,
            onValueChange = { 
                address = it
                if (addressError.isNotEmpty()) validateAddress()
            },
            label = { Text("Dirección") },
            placeholder = { Text("Dirección", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = addressError.isNotEmpty(),
            supportingText = if (addressError.isNotEmpty()) {
                { Text(addressError, color = MaterialTheme.colorScheme.error) }
            } else null,
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
        
        // Sport Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = sportType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de deporte") },
                placeholder = { Text("Selecciona un deporte", color = Color.Gray) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = sportTypeError.isNotEmpty(),
                supportingText = if (sportTypeError.isNotEmpty()) {
                    { Text(sportTypeError, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    focusedLabelColor = PrimaryPurple,
                    cursorColor = PrimaryPurple,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = GrayDark
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sportTypes.forEach { sport ->
                    DropdownMenuItem(
                        text = { Text(sport) },
                        onClick = {
                            sportType = sport
                            expanded = false
                            if (sportTypeError.isNotEmpty()) validateSportType()
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Price per Hour Field
        OutlinedTextField(
            value = pricePerHour,
            onValueChange = { 
                pricePerHour = it
                if (priceError.isNotEmpty()) validatePrice()
            },
            label = { Text("Precio por hora") },
            placeholder = { Text("Precio por hora", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = priceError.isNotEmpty(),
            supportingText = if (priceError.isNotEmpty()) {
                { Text(priceError, color = MaterialTheme.colorScheme.error) }
            } else null,
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
        
        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            placeholder = { Text("Descripción", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
                focusedTextColor = TextPrimaryLight,
                unfocusedTextColor = GrayDark
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Photo Upload Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { /* TODO: Implement photo picker */ },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sube fotos del lugar",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Register Button
        Button(
            onClick = {
                if (isFormValid()) {
                    isLoading = true
                    // Simulate registration process
                    // In real app, this would call authentication service
                    onRegisterSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Crear sitio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Business Message Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F9FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Eres propietario de un negocio?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryPurple,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Únete a nuestra plataforma y conecta con deportistas de tu área. Gestiona reservas, horarios y maximiza el uso de tus instalaciones deportivas.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation Links
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Usuario regular? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Regístrate aquí",
                color = AccentGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onNavigateToRegular()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ya tienes cuenta: ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Inicia sesión",
                color = PrimaryPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}