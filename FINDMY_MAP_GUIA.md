# 🗺️ Guía: Sistema de Mapa FindMy

## ✅ Implementación Completada

Tu aplicación ahora tiene un **mapa estilo FindMy de Apple** que muestra la ubicación de tus amigos en tiempo real.

---

## 🎯 Características Implementadas

### 1. **Ubicaciones en Tiempo Real**
- ✅ Escucha cambios en Firestore automáticamente
- ✅ Se actualiza sin necesidad de refrescar manualmente
- ✅ Muestra solo amigos que has agregado
- ✅ Filtra usuarios que no son tus amigos

### 2. **Burbujas con Fotos de Perfil**
- ✅ Cada amigo aparece como una burbuja circular
- ✅ Foto de perfil descargada desde Cloudinary/Firebase Storage
- ✅ Borde verde si está **en línea** (isOnline: true)
- ✅ Borde gris si está **desconectado**
- ✅ Icono genérico si no tiene foto

### 3. **Rutas OSRM con Polylines** 🛣️
- ✅ **Long-press en el mapa** → Traza ruta desde tu ubicación al punto
- ✅ **Polyline azul** muestra el camino completo
- ✅ Calcula distancia (km) y tiempo estimado (minutos)
- ✅ Marker de destino con información
- ✅ Mantiene zoom actual al trazar ruta
- ✅ Usa servidor OSRM gratuito (Open Source Routing Machine)

### 4. **Detalles al Hacer Clic**
- ✅ Bottom sheet con información completa:
  - Foto de perfil grande
  - Nombre del amigo
  - Estado (En línea / Desconectado)
  - **Dirección completa** (geocoding inverso con Nominatim)
  - Coordenadas exactas (latitud, longitud)
  - Última actualización (hace X minutos)
- ✅ Botón "Ver en el mapa" para centrar en su ubicación

### 5. **Interfaz de Usuario**
- ✅ Contador de amigos en línea (parte superior)
- ✅ Botón de refrescar
- ✅ Botón para centrar en tu ubicación
- ✅ Mi ubicación mostrada con ícono especial
- ✅ Carga suave sin bloqueos

---

## 📂 Archivos Creados/Modificados

### **Nuevos Archivos:**

1. **`FriendLocation.kt`** - Modelo de datos
   ```
   app/src/main/java/com/example/spottivo/data/models/FriendLocation.kt
   ```

2. **`MapViewModel.kt`** - Lógica del mapa
   ```
   app/src/main/java/com/example/spottivo/viewmodel/MapViewModel.kt
   ```

3. **`FindMyMapScreen.kt`** - UI del mapa FindMy
   ```
   app/src/main/java/com/example/spottivo/ui/screens/FindMyMapScreen.kt
   ```

### **Archivos Modificados:**

- **`NavigationGraph.kt`** - Ahora usa `FindMyMapScreen` en lugar de `MapScreen`

---

## 🔧 Cómo Funciona

### **1. Listener de Firestore en Tiempo Real**

```kotlin
// MapViewModel.kt escucha cambios automáticamente
firestore.collection("locations")
    .addSnapshotListener { snapshot, error ->
        // Se ejecuta cada vez que cambia alguna ubicación
        // Filtra solo amigos
        // Actualiza el estado
    }
```

### **2. Markers Personalizados**

```kotlin
// Descarga foto de perfil con Coil
val bitmap = loadImageFromUrl(photoUrl)

// Crea bitmap circular con borde de color
val circularBitmap = createCircularBitmap(
    bitmap = bitmap,
    borderColor = if (isOnline) GREEN else GRAY
)

// Agrega marker al mapa
val marker = Marker(mapView).apply {
    position = GeoPoint(latitude, longitude)
    icon = BitmapDrawable(circularBitmap)
}
```

### **3. Geocoding Inverso (Coordenadas → Dirección)**

```kotlin
// Usa Nominatim de OpenStreetMap (gratis, sin API key)
val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon"

// Respuesta:
{
  "display_name": "Calle 72 #10-51, Bogotá, Colombia",
  "address": {
    "road": "Calle 72",
    "city": "Bogotá",
    "country": "Colombia"
  }
}
```

---

## 🧪 Cómo Probar

### **Paso 1: Agregar Usuarios de Prueba en Bogotá**

¡Ahora tienes 12 usuarios de prueba listos! Lee el archivo **`DATOS_PRUEBA_BOGOTA.md`** con instrucciones completas.

**Forma rápida:** Ve a Firebase Console → Firestore y copia los datos manualmente (5 minutos)

Ubicaciones incluidas:
- 🏢 Centro Internacional (María)
- 🏛️ Plaza de Bolívar (Diego - offline)
- 🎭 Museo del Oro (Isabella - offline)
- 🌳 Parque de la 93 (Andrés)
- 🛍️ Zona T (Laura)
- 🏪 Unicentro (Valentina)
- Y 6 más distribuidos por Bogotá

### **Paso 2: Agregar Amigos**

Para que aparezcan ubicaciones en el mapa, necesitas:

1. **Tener amigos agregados en Firestore:**
   ```
   users/{tuUserId}/friends/{amigoId}
   ```

2. **Que tus amigos tengan ubicaciones activas:**
   ```
   locations/{amigoId} {
     latitude: 4.65894,
     longitude: -74.09378,
     timestamp: 1700756432000
   }
   ```

3. **Que tus amigos estén registrados:**
   ```
   users/{amigoId} {
     nombre: "Juan Pérez",
     email: "juan@example.com",
     photoUrl: "https://...",
     isOnline: true
   }
   ```

### **Paso 2: Datos de Prueba**

Si quieres probar rápido, agrega manualmente en Firestore:

```javascript
// En Firebase Console → Firestore
// 1. Agregar amigo
users/TU_USER_ID/friends/AMIGO_ID
{ addedAt: [timestamp] }

// 2. Agregar ubicación del amigo
locations/AMIGO_ID
{
  latitude: 4.60971,
  longitude: -74.08175,
  timestamp: [timestamp actual]
}

// 3. Datos del usuario amigo
users/AMIGO_ID
{
  nombre: "Carlos Test",
  email: "carlos@test.com",
  photoUrl: "https://cloudinary.com/...",
  isOnline: true
}
```

### **Paso 3: Ver en el Mapa**

1. Abre la app
2. Ve a la pestaña **"Mapa"** (segundo ícono)
3. Deberías ver:
   - Tu ubicación (punto azul)
   - Burbujas de tus amigos en línea
   - Contador "10/12 amigos en línea" (si agregaste los usuarios de prueba)

4. **Haz clic en una burbuja** → aparece bottom sheet con detalles

5. **Long-press en cualquier parte del mapa** → traza ruta OSRM desde tu ubicación
   - Aparece polyline azul con el camino
   - Marker de destino con distancia y tiempo
   - Toast: "Ruta: X.X km"

---

## 🛣️ Uso de Rutas OSRM

### **Trazar Ruta:**
1. Mantén presionado (long-press) en cualquier punto del mapa
2. Se calculará la ruta desde tu ubicación actual
3. Verás:
   - **Polyline azul** → Camino completo
   - **Marker de destino** → Con distancia y tiempo
   - **Toast** → Confirmación rápida

### **Cambiar Ruta:**
- Simplemente haz long-press en otro punto
- La ruta anterior se borra automáticamente
- Se dibuja la nueva ruta

### **Ruta a un Amigo:**
1. Haz clic en la burbuja de un amigo
2. En el bottom sheet, toca "Ver en el mapa"
3. El mapa se centra en su ubicación
4. Haz long-press sobre su burbuja para trazar ruta hacia él

---

## 🎨 Personalización

### **Cambiar Tamaño de las Burbujas**

```kotlin
// FindMyMapScreen.kt, función createCircularMarkerIcon
val size = 120 // Cambiar a 80 para burbujas más pequeñas
```

### **Cambiar Color del Borde**

```kotlin
// FindMyMapScreen.kt, función createCircularMarkerIcon
borderColor = if (friend.isOnline) 
    0xFF4CAF50.toInt() // Verde
else 
    0xFFFF5722.toInt() // Cambia a naranja si está desconectado
```

### **Cambiar Zoom Inicial**

```kotlin
// FindMyMapScreen.kt, línea ~87
controller.setZoom(12.0) // Cambiar a 15.0 para más acercamiento
```

### **Cambiar Centro Inicial**

```kotlin
// FindMyMapScreen.kt, línea ~88
controller.setCenter(GeoPoint(4.60971, -74.08175)) // Bogotá
// Cambiar a tu ciudad
```

---

## 🔥 Integración con LocationTrackingService

Tu servicio de ubicación en segundo plano (`LocationTrackingService.kt`) **ya está actualizado** y guarda ubicaciones en Firestore:

```kotlin
// LocationTrackingService.kt actualiza automáticamente
firestore.collection("locations")
    .document(currentUserId)
    .set(mapOf(
        "latitude" to location.latitude,
        "longitude" to location.longitude,
        "timestamp" to System.currentTimeMillis()
    ))
```

**Esto significa:**
- ✅ Cuando corres la app, tu ubicación se guarda cada 10 segundos
- ✅ Tus amigos ven tu ubicación actualizada en su mapa
- ✅ Tú ves sus ubicaciones si ellos tienen el servicio activo

---

## 📊 Estructura de Datos en Firestore

```
Firestore
├── users
│   ├── usuario1_id
│   │   ├── nombre: "Ana García"
│   │   ├── email: "ana@example.com"
│   │   ├── photoUrl: "https://cloudinary.com/..."
│   │   ├── isOnline: true
│   │   └── friends (subcollection)
│   │       └── usuario2_id
│   │           └── addedAt: [timestamp]
│   └── usuario2_id
│       └── ...
│
└── locations
    ├── usuario1_id
    │   ├── latitude: 4.65894
    │   ├── longitude: -74.09378
    │   └── timestamp: 1700756432000
    └── usuario2_id
        └── ...
```

---

## 🚀 Próximos Pasos

### **Funcionalidades Opcionales que Puedes Agregar:**

1. **Filtrar solo amigos en línea**
   ```kotlin
   val onlineFriends = friendsLocations.filter { it.isOnline }
   ```

2. **Ruta entre tu ubicación y un amigo**
   - Ya tienes OSRM implementado en el MapScreen original
   - Puedes integrar `fetchOsrmRoute()` en FindMyMapScreen

3. **Notificaciones cuando un amigo llega cerca**
   - Usar Geofencing de Google Play Services
   - Detectar cuando la distancia < 500m

4. **Historial de ubicaciones**
   - Guardar trazas en Firestore
   - Mostrar ruta recorrida del día

5. **Compartir ubicación temporal**
   - Campo `sharingUntil` en Firestore
   - Dejar de mostrar ubicación después de X horas

---

## ❓ Solución de Problemas

### **No aparecen amigos en el mapa**

1. Verifica que tengas amigos en:
   ```
   users/{tuUserId}/friends/{amigoId}
   ```

2. Verifica que tus amigos tengan ubicaciones:
   ```
   locations/{amigoId}
   ```

3. Revisa Logcat:
   ```
   adb logcat | grep MapViewModel
   ```

### **Las fotos no se cargan**

1. Verifica que `photoUrl` tenga una URL válida
2. Verifica que Coil esté instalado:
   ```kotlin
   implementation("io.coil-kt:coil-compose:2.2.2")
   ```
3. Si la URL es HTTPS, no debería haber problemas de seguridad

### **El geocoding no funciona**

1. Nominatim requiere User-Agent válido (ya configurado)
2. Si falla, verifica conectividad a internet
3. Alternativa: usar Google Maps Geocoding API (requiere API key de pago)

### **Permisos de ubicación**

Si no ves tu ubicación:
1. Ve a Configuración → Apps → Spottivo → Permisos
2. Activa "Ubicación" → "Permitir todo el tiempo"

---

## 📝 Notas Técnicas

### **Listener de Firestore**
- Se crea en `init {}` del ViewModel
- Se destruye automáticamente en `onCleared()`
- No consume recursos cuando la app está cerrada

### **Descarga de Imágenes**
- Usa Coil (librería optimizada)
- Cache automático en disco y memoria
- Solo descarga una vez por sesión

### **Geocoding Inverso**
- API gratuita de OpenStreetMap
- Límite: ~1 request/segundo (suficiente para uso normal)
- No requiere API key ni registro

### **Performance**
- Actualización eficiente con `StateFlow`
- Solo re-renderiza markers que cambiaron
- Bitmaps circulares creados en background thread

---

## 🎉 ¡Listo!

Tu mapa estilo FindMy está completamente funcional. Solo necesitas:

1. **Sincronizar Gradle** (para actualizar imports)
2. **Agregar amigos en Firestore** (o pedirles que se registren)
3. **Correr la app** con LocationTrackingService activo

**Disfruta rastreando a tus amigos en tiempo real! 📍**
