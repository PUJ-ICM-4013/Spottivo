package com.example.spottivo.common.seed

import android.content.Context
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseOneTimeSeed {

// Súbelo si necesitas re-ejecutar el seed
private const val SEED_VERSION = 2
private const val PREF_NAME = "seed_prefs"
private const val KEY_SEEDED_VERSION = "seed_version"

suspend fun runOnce(
context: Context,
db: FirebaseFirestore,
uid: String
) = withContext(Dispatchers.IO) {
val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
val already = prefs.getInt(KEY_SEEDED_VERSION, 0) >= SEED_VERSION
if (already) return@withContext

try {
seedCatalog(db)
seedUserData(db, uid)
prefs.edit().putInt(KEY_SEEDED_VERSION, SEED_VERSION).apply()
withContext(Dispatchers.Main) {
Toast.makeText(context, "Firestore seed OK", Toast.LENGTH_SHORT).show()
}
} catch (e: Exception) {
withContext(Dispatchers.Main) {
Toast.makeText(context, "Seed error: ${e.message}", Toast.LENGTH_LONG).show()
}
throw e
}
}

/** Catálogos globales en colecciones raíz válidas */
@VisibleForTesting
internal suspend fun seedCatalog(db: FirebaseFirestore) {
val deportes = listOf(
mapOf("id" to "futbol_5",  "nombre" to "Fútbol 5"),
mapOf("id" to "futbol_11", "nombre" to "Fútbol 11"),
mapOf("id" to "tenis",     "nombre" to "Tenis"),
mapOf("id" to "baloncesto","nombre" to "Baloncesto")
)
val servicios = listOf(
mapOf("id" to "parqueadero", "nombre" to "Parqueadero", "descripcion" to "Parqueadero cubierto"),
mapOf("id" to "duchas",      "nombre" to "Duchas",      "descripcion" to "Agua caliente"),
mapOf("id" to "cafeteria",   "nombre" to "Cafetería",   "descripcion" to "Snacks y bebidas")
)

val batch = db.batch()
val depCol = db.collection("catalog_deportes")    // <-- colección raíz (válida)
val srvCol = db.collection("catalog_servicios")   // <-- colección raíz (válida)

deportes.forEach { d -> batch.set(depCol.document(d["id"] as String), d) }
servicios.forEach { s -> batch.set(srvCol.document(s["id"] as String), s) }

batch.commit().await()
}

/** Usuario + 1 espacio + 1 reserva bajo /users/{uid} */
@VisibleForTesting
internal suspend fun seedUserData(db: FirebaseFirestore, uid: String) {
// Usuario
val user = mapOf(
"id" to uid,
"nombre" to "Usuario Demo",
"email" to "demo@spottivo.com",
"passwordHash" to "",
"fotoPerfil" to null,
"role" to "DEPORTISTA",
"preferencias" to listOf("futbol_5", "tenis"),
"esPremium" to false,
"ubicacionActual" to GeoPoint(4.65, -74.05)
)
db.collection("users").document(uid).set(user).await()

// Espacio
val espaciosCol = db.collection("users").document(uid).collection("espacios")
val espacioRef = espaciosCol.document() // Auto-ID
val espacio = mapOf(
"id" to espacioRef.id,
"nombre" to "Complejo Deportivo Central",
"direccion" to "Av. Siempre Viva 123",
"contacto" to "+57 312 000 0000",
"servicios" to listOf("parqueadero", "duchas"),
"deportes" to listOf("futbol_5", "tenis"),
"fotos" to emptyList<String>(),
"ubicacion" to GeoPoint(4.676, -74.048),
"esDestacado" to true,
"propietarioId" to uid
)
espacioRef.set(espacio).await()

// Reserva
val now = System.currentTimeMillis()
val dayMillis = 24L * 60 * 60 * 1000
val hoy00 = now - (now % dayMillis)
val inicio = hoy00 + 18L * 60 * 60 * 1000
val fin    = inicio + 60L * 60 * 1000

val reservasCol = db.collection("users").document(uid).collection("reservas")
val reservaRef = reservasCol.document()
val reserva = mapOf(
"id" to reservaRef.id,
"usuarioId" to uid,
"espacioId" to espacioRef.id,
"fechaCreacionMillis" to now,
"precio" to 90000.0,
"estado" to "PENDIENTE",
"fechaMillis" to hoy00,
"inicioMillis" to inicio,
"finMillis" to fin,
"cupoDisponible" to 10
)
reservaRef.set(reserva).await()
}
}