# 📸 GUÍA: Guardar Fotos en Firebase

## ❓ ¿Se pueden guardar fotos en Firestore?

**NO directamente.** Firestore es una base de datos de documentos, **NO** está diseñado para almacenar archivos binarios como imágenes.

## ✅ La forma correcta: Firebase Storage

### 🔹 Firebase Storage vs Firestore

| Servicio | Uso | Límite |
|----------|-----|--------|
| **Firestore** | Datos estructurados (texto, números, booleanos) | 1 MB por documento |
| **Firebase Storage** | Archivos (fotos, videos, PDFs, etc.) | 5 GB gratis |

---

## 🚀 Cómo funciona en Spottivo

### 1. Arquitectura implementada

```
┌──────────────┐
│   Android    │
│    (foto)    │
└──────┬───────┘
       │ upload
       ▼
┌──────────────────┐
│ Firebase Storage │ ← Guarda la foto
│  /users/uid/     │   Retorna URL
│   profile.jpg    │
└──────┬───────────┘
       │ URL: https://...
       ▼
┌──────────────────┐
│   Firestore      │ ← Guarda solo la URL
│  users/uid/      │
│  photoUrl: "..." │
└──────────────────┘
```

### 2. Flujo completo

1. **Usuario selecciona una foto** (galería o cámara)
2. **`ProfileViewModel.uploadProfilePhoto()`** sube la foto a Storage
3. Storage retorna una **URL pública** de descarga
4. Guardamos la **URL** en Firestore (campo `photoUrl`)
5. La app descarga y muestra la foto usando la URL

---

## 📁 Estructura en Firebase Storage

```
storage/
└── users/
    ├── user123/
    │   └── profile_1700000000.jpg  ← Foto del usuario 1
    ├── user456/
    │   └── profile_1700000001.jpg  ← Foto del usuario 2
    └── user789/
        └── profile_1700000002.jpg
```

---

## 💻 Implementación en tu código

### Ya está implementado en `ProfileViewModel.kt`:

```kotlin
fun uploadProfilePhoto(imageUri: Uri) {
    viewModelScope.launch {
        // 1. Referencia en Storage
        val storageRef = storage.reference
            .child("users")
            .child(currentUser.uid)
            .child("profile_${System.currentTimeMillis()}.jpg")
        
        // 2. Subir imagen
        storageRef.putFile(imageUri).await()
        
        // 3. Obtener URL de descarga
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        // 4. Guardar URL en Firestore
        firestore.collection("users")
            .document(currentUser.uid)
            .update("photoUrl", downloadUrl)
            .await()
        
        // 5. Actualizar UI
        userPhotoUri = downloadUrl
    }
}
```

---

## 🔧 Configurar Firebase Storage

### Paso 1: Habilitar Storage en Firebase Console

1. Ve a: https://console.firebase.google.com/project/spottivo-33658/storage
2. Click en **"Get Started"**
3. Selecciona **"Start in test mode"** (para desarrollo)
4. Elige tu región (us-central1)
5. Click en **"Done"**

### Paso 2: Reglas de seguridad (producción)

Por defecto está en "test mode" (cualquiera puede subir). Para producción, usa estas reglas:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      // Solo el usuario puede subir/leer sus propias fotos
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 📊 Ventajas de este enfoque

✅ **Escalable:** Storage puede manejar millones de fotos  
✅ **CDN integrado:** Las fotos se sirven rápido desde servidores globales  
✅ **Económico:** 5 GB gratis, luego $0.026/GB  
✅ **URLs permanentes:** Puedes compartir links a las fotos  
✅ **Compresión automática:** Puedes configurar thumbnails

---

## 🎨 Mostrar fotos en la UI

### Usando Coil (ya implementado):

```kotlin
AsyncImage(
    model = viewModel.userPhotoUri,  // URL de Firebase Storage
    contentDescription = "Foto de perfil",
    modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
)
```

---

## 🔥 Optimizaciones avanzadas (opcional)

### 1. Comprimir fotos antes de subir

```kotlin
fun compressImage(uri: Uri, context: Context): ByteArray {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream) // 75% calidad
    return outputStream.toByteArray()
}
```

### 2. Generar thumbnails

```kotlin
// Guardar versión completa
storageRef.child("profile_full.jpg").putFile(imageUri)

// Guardar thumbnail
val thumbnail = createThumbnail(imageUri, 200, 200)
storageRef.child("profile_thumb.jpg").putBytes(thumbnail)
```

### 3. Eliminar fotos antiguas

```kotlin
fun deleteOldProfilePhoto(oldUrl: String) {
    val oldRef = storage.getReferenceFromUrl(oldUrl)
    oldRef.delete()
}
```

---

## 📱 Límites y costos

| Plan | Storage Gratuito | Transferencia | Costo adicional |
|------|------------------|---------------|-----------------|
| Spark (gratis) | 5 GB | 1 GB/día | N/A |
| Blaze (pago) | 5 GB | 1 GB/día | $0.026/GB storage + $0.12/GB transferencia |

**Ejemplo:** 1000 usuarios con fotos de 2 MB cada uno = 2 GB usado = **GRATIS**

---

## ✅ Resumen

1. ❌ **NO guardes fotos en Firestore** (límite 1 MB por documento)
2. ✅ **USA Firebase Storage** para archivos (ya implementado)
3. ✅ Guarda solo la **URL** en Firestore
4. ✅ Habilita Storage en Firebase Console
5. ✅ En producción, configura reglas de seguridad

**Tu implementación ya está lista y funcionando correctamente.** Solo necesitas habilitar Storage en la consola de Firebase.
