package com.example.spottivo.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class User(
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val role: String = "",
    val isOnline: Boolean = false,
    val fcmToken: String = "" // Para notificaciones push
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    private var currentUser: User? = null

    companion object {
        private const val TAG = "AuthRepository"
    }

    // ==========================================
    // LOGIN SEGURO CON FIREBASE AUTH
    // ==========================================
    fun loginWithEmail(email: String, password: String): Flow<AuthResult> = flow {
        try {
            Log.d(TAG, "Iniciando login para: $email")
            emit(AuthResult.Loading)
            
            // 1. Autenticar con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID no encontrado")

            // 2. Obtener token FCM para notificaciones
            val fcmToken = try {
                messaging.token.await()
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo obtener FCM token", e)
                ""
            }

            // 3. Obtener datos del usuario desde Firestore
            val docSnapshot = firestore.collection("users").document(uid).get().await()
            
            val user = if (docSnapshot.exists()) {
                User(
                    id = uid,
                    email = docSnapshot.getString("email") ?: email,
                    nombre = docSnapshot.getString("nombre") ?: "Usuario",
                    role = docSnapshot.getString("role") ?: "user",
                    isOnline = true,
                    fcmToken = fcmToken
                )
            } else {
                User(id = uid, email = email, isOnline = true, fcmToken = fcmToken)
            }

            // 4. Actualizar estado ONLINE y token FCM
            updateUserStatus(uid, true, fcmToken)

            currentUser = user
            emit(AuthResult.Success(user))

        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            emit(AuthResult.Error(e.message ?: "Error al iniciar sesión"))
        }
    }

    // ==========================================
    // REGISTRO SEGURO
    // ==========================================
    fun registerUser(name: String, email: String, password: String): Flow<AuthResult> = flow {
        try {
            Log.d(TAG, "Registrando usuario: $email")
            emit(AuthResult.Loading)

            // 1. Crear usuario en Firebase Auth (Maneja la contraseña de forma segura)
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("No se pudo crear usuario")

            // 2. Obtener token FCM
            val fcmToken = try {
                messaging.token.await()
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo obtener FCM token", e)
                ""
            }

            // 3. Crear documento en Firestore (SIN CONTRASEÑA)
            val userData = hashMapOf(
                "id" to uid,
                "email" to email,
                "nombre" to name,
                "role" to "user",
                "isOnline" to true,
                "fcmToken" to fcmToken,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(userData).await()

            val user = User(
                id = uid,
                email = email,
                nombre = name,
                role = "user",
                isOnline = true,
                fcmToken = fcmToken
            )

            currentUser = user
            emit(AuthResult.Success(user))

        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            emit(AuthResult.Error(e.message ?: "Error al registrar"))
        }
    }

    // ==========================================
    // FINDMY: ACTUALIZAR UBICACIÓN EN TIEMPO REAL
    // ==========================================
    suspend fun updateLocation(lat: Double, lng: Double) {
        val uid = auth.currentUser?.uid ?: return
        
        val locationData = hashMapOf(
            "userId" to uid,
            "lat" to lat,
            "lng" to lng,
            "lastUpdate" to System.currentTimeMillis()
        )

        try {
            firestore.collection("locations").document(uid)
                .set(locationData, SetOptions.merge())
            Log.d(TAG, "Ubicación actualizada: ($lat, $lng)")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando ubicación", e)
        }
    }

    // ==========================================
    // GESTIÓN DE AMIGOS
    // ==========================================
    suspend fun addFriend(friendEmail: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
            
            // Buscar al amigo por email
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", friendEmail)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val friendDoc = querySnapshot.documents.first()
            val friendId = friendDoc.id
            val friendName = friendDoc.getString("nombre") ?: "Amigo"

            // Crear relación de amistad
            val friendshipData = hashMapOf(
                "userId" to uid,
                "friendId" to friendId,
                "friendName" to friendName,
                "status" to "accepted",
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid)
                .collection("friends").document(friendId)
                .set(friendshipData)
                .await()

            Result.success("Amigo agregado: $friendName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    fun logout() {
        currentUser?.let { updateUserStatus(it.id, false, "") }
        auth.signOut()
        currentUser = null
    }

    fun getCurrentUser(): User? = currentUser
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // ==========================================
    // UTILIDADES PRIVADAS
    // ==========================================
    private fun updateUserStatus(userId: String, isOnline: Boolean, fcmToken: String) {
        val updateData = mutableMapOf<String, Any>(
            "isOnline" to isOnline,
            "lastSeen" to System.currentTimeMillis()
        )
        
        if (fcmToken.isNotEmpty()) {
            updateData["fcmToken"] = fcmToken
        }

        firestore.collection("users").document(userId)
            .set(updateData, SetOptions.merge())
    }
}