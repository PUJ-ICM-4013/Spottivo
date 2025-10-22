package com.example.spottivo.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class User(
    val email: String = "",
    val nombre: String = "",
    val role: String = ""
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private var currentUser: User? = null

    fun loginWithEmail(email: String, password: String): Flow<AuthResult> = flow {
        try {
            Log.d("AuthRepository", "Iniciando login para email: $email")
            emit(AuthResult.Loading)
            
            // Verificar si el usuario existe en Firestore
            Log.d("AuthRepository", "Consultando Firestore para email: $email")
            val userQuery = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            Log.d("AuthRepository", "Resultado de consulta: ${userQuery.size()} documentos encontrados")

            if (userQuery.isEmpty) {
                Log.w("AuthRepository", "Usuario no encontrado para email: $email")
                emit(AuthResult.Error("Usuario no encontrado"))
                return@flow
            }

            val userDocument = userQuery.documents.first()
            val userData = userDocument.data
            Log.d("AuthRepository", "Datos del usuario obtenidos: $userData")

            if (userData == null) {
                Log.e("AuthRepository", "userData es null")
                emit(AuthResult.Error("Error al obtener datos del usuario"))
                return@flow
            }

            val storedPassword = userData["password"] as? String
            Log.d("AuthRepository", "Contraseña almacenada obtenida: ${storedPassword != null}")

            // Verificar contraseña
            if (storedPassword != password) {
                Log.w("AuthRepository", "Contraseña incorrecta para email: $email")
                emit(AuthResult.Error("Contraseña incorrecta"))
                return@flow
            }

            // Crear objeto User con los datos de Firestore
            val user = User(
                email = userData["email"] as? String ?: "",
                nombre = userData["nombre"] as? String ?: "",
                role = userData["role"] as? String ?: ""
            )

            Log.d("AuthRepository", "Usuario creado exitosamente: ${user.email}")

            // Guardar usuario actual en memoria
            currentUser = user

            emit(AuthResult.Success(user))

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login: ${e.message}", e)
            emit(AuthResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun logout() {
        currentUser = null
    }

    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    fun registerUser(name: String, email: String, password: String): Flow<AuthResult> = flow {
        try {
            Log.d("AuthRepository", "Iniciando registro para email: $email")
            emit(AuthResult.Loading)
            
            // Verificar si el usuario ya existe
            Log.d("AuthRepository", "Verificando si el usuario ya existe: $email")
            val existingUserQuery = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!existingUserQuery.isEmpty) {
                Log.w("AuthRepository", "Usuario ya existe para email: $email")
                emit(AuthResult.Error("El usuario ya existe"))
                return@flow
            }

            // Generar el siguiente ID secuencial
            Log.d("AuthRepository", "Generando ID secuencial para nuevo usuario")
            val nextUserId = generateNextUserId()
            Log.d("AuthRepository", "ID generado: $nextUserId")

            // Crear nuevo usuario en Firestore con ID secuencial
            val userData = hashMapOf(
                "email" to email,
                "nombre" to name,
                "password" to password,
                "role" to "user"
            )

            Log.d("AuthRepository", "Creando usuario en Firestore con ID: $nextUserId")
            firestore.collection("users")
                .document(nextUserId)
                .set(userData)
                .await()

            Log.d("AuthRepository", "Usuario creado exitosamente en Firestore: $nextUserId")

            // Crear objeto User
            val user = User(
                email = email,
                nombre = name,
                role = "user"
            )

            // Guardar usuario actual en memoria
            currentUser = user

            emit(AuthResult.Success(user))

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registro: ${e.message}", e)
            emit(AuthResult.Error("Error al crear usuario: ${e.message}"))
        }
    }

    private suspend fun generateNextUserId(): String {
        return try {
            // Obtener todos los documentos de usuarios
            val usersSnapshot = firestore.collection("users").get().await()
            
            // Encontrar el número más alto
            var maxNumber = 0
            for (document in usersSnapshot.documents) {
                val documentId = document.id
                if (documentId.startsWith("user_")) {
                    val numberPart = documentId.substring(5) // Quitar "user_"
                    try {
                        val number = numberPart.toInt()
                        if (number > maxNumber) {
                            maxNumber = number
                        }
                    } catch (e: NumberFormatException) {
                        // Ignorar documentos que no sigan el patrón user_XXX
                        Log.w("AuthRepository", "Documento con ID no numérico encontrado: $documentId")
                    }
                }
            }
            
            // Generar el siguiente ID
            val nextNumber = maxNumber + 1
            val nextId = "user_${String.format("%03d", nextNumber)}"
            Log.d("AuthRepository", "Siguiente ID generado: $nextId (basado en max: $maxNumber)")
            
            nextId
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error generando ID secuencial: ${e.message}", e)
            // Fallback: usar timestamp si hay error
            "user_${System.currentTimeMillis()}"
        }
    }
}