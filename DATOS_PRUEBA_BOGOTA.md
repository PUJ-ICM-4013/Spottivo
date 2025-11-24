# 🧪 Datos de Prueba - Usuarios en Bogotá

## 📍 12 Usuarios Activos en Ubicaciones Reales de Bogotá

### Forma Rápida (Firebase Console - Recomendada)

**No necesitas instalar nada, hazlo manualmente en Firebase Console:**

1. **Ve a Firebase Console** → Tu proyecto → Firestore Database

2. **Copia tu User ID actual:**
   - En Android Studio, abre Logcat
   - Busca: `FirebaseAuth` o ejecuta en tu app:
   ```kotlin
   Log.d("MI_USER_ID", FirebaseAuth.getInstance().currentUser?.uid ?: "")
   ```

3. **Para cada usuario de prueba, crea estos documentos:**

---

### 👤 Usuario 1: María Rodríguez (Centro Internacional)

```
Colección: users
Documento ID: user_test_001

Campos:
nombre: "María Rodríguez"
email: "maria.rodriguez@test.com"
photoUrl: "https://i.pravatar.cc/150?img=1"
isOnline: true
role: "usuario"
```

```
Colección: locations
Documento ID: user_test_001

Campos:
latitude: 4.60971
longitude: -74.08175
timestamp: [timestamp actual en milisegundos]
```

```
Colección: users/{TU_USER_ID}/friends
Documento ID: user_test_001

Campos:
addedAt: [timestamp]
```

---

### 👤 Usuario 2: Carlos Méndez (Usaquén)

```
users/user_test_002
{
  nombre: "Carlos Méndez",
  email: "carlos.mendez@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=12",
  isOnline: true,
  role: "usuario"
}

locations/user_test_002
{
  latitude: 4.67694,
  longitude: -74.04834,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_002
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 3: Laura Gómez (Zona T, Chapinero)

```
users/user_test_003
{
  nombre: "Laura Gómez",
  email: "laura.gomez@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=5",
  isOnline: true,
  role: "usuario"
}

locations/user_test_003
{
  latitude: 4.65894,
  longitude: -74.09378,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_003
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 4: Andrés Torres (Parque de la 93)

```
users/user_test_004
{
  nombre: "Andrés Torres",
  email: "andres.torres@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=15",
  isOnline: true,
  role: "usuario"
}

locations/user_test_004
{
  latitude: 4.62389,
  longitude: -74.06445,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_004
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 5: Valentina Castro (Unicentro)

```
users/user_test_005
{
  nombre: "Valentina Castro",
  email: "valentina.castro@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=9",
  isOnline: true,
  role: "usuario"
}

locations/user_test_005
{
  latitude: 4.71099,
  longitude: -74.03506,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_005
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 6: Diego Ramírez (Plaza de Bolívar) - DESCONECTADO

```
users/user_test_006
{
  nombre: "Diego Ramírez",
  email: "diego.ramirez@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=13",
  isOnline: false,
  role: "usuario"
}

locations/user_test_006
{
  latitude: 4.59847,
  longitude: -74.07672,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_006
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 7: Camila Vargas (Parque El Virrey)

```
users/user_test_007
{
  nombre: "Camila Vargas",
  email: "camila.vargas@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=10",
  isOnline: true,
  role: "usuario"
}

locations/user_test_007
{
  latitude: 4.64778,
  longitude: -74.05854,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_007
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 8: Santiago Morales (Andino Shopping)

```
users/user_test_008
{
  nombre: "Santiago Morales",
  email: "santiago.morales@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=14",
  isOnline: true,
  role: "usuario"
}

locations/user_test_008
{
  latitude: 4.66283,
  longitude: -74.05493,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_008
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 9: Isabella Fernández (Museo del Oro) - DESCONECTADA

```
users/user_test_009
{
  nombre: "Isabella Fernández",
  email: "isabella.fernandez@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=16",
  isOnline: false,
  role: "usuario"
}

locations/user_test_009
{
  latitude: 4.60389,
  longitude: -74.06556,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_009
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 10: Mateo Herrera (Salitre Mágico)

```
users/user_test_010
{
  nombre: "Mateo Herrera",
  email: "mateo.herrera@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=11",
  isOnline: true,
  role: "usuario"
}

locations/user_test_010
{
  latitude: 4.63890,
  longitude: -74.11023,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_010
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 11: Sofía Jiménez (Hacienda Santa Bárbara)

```
users/user_test_011
{
  nombre: "Sofía Jiménez",
  email: "sofia.jimenez@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=20",
  isOnline: true,
  role: "usuario"
}

locations/user_test_011
{
  latitude: 4.68445,
  longitude: -74.05667,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_011
{ addedAt: [timestamp] }
```

---

### 👤 Usuario 12: Sebastián Ruiz (Av. Jiménez con Séptima)

```
users/user_test_012
{
  nombre: "Sebastián Ruiz",
  email: "sebastian.ruiz@test.com",
  photoUrl: "https://i.pravatar.cc/150?img=17",
  isOnline: true,
  role: "usuario"
}

locations/user_test_012
{
  latitude: 4.61278,
  longitude: -74.07056,
  timestamp: [ahora]
}

users/{TU_USER_ID}/friends/user_test_012
{ addedAt: [timestamp] }
```

---

## 🗺️ Mapa de Ubicaciones

```
📍 Bogotá - Distribución de Usuarios:

Centro/Candelaria:
- María (Centro Internacional)
- Diego (Plaza de Bolívar) [OFFLINE]
- Isabella (Museo del Oro) [OFFLINE]
- Sebastián (Av. Jiménez)

Norte (Chapinero/Usaquén):
- Carlos (Usaquén)
- Laura (Zona T)
- Andrés (Parque 93)
- Camila (Parque El Virrey)
- Santiago (Andino)
- Sofía (Hacienda Santa Bárbara)
- Valentina (Unicentro)

Occidente:
- Mateo (Salitre Mágico)

Usuarios en línea: 10/12
Usuarios offline: 2/12 (Diego, Isabella)
```

---

## 🚀 Método Rápido con Script (Opcional)

Si prefieres usar el script automático:

### 1. Descargar Service Account Key

1. Firebase Console → Configuración del Proyecto (⚙️)
2. Cuentas de servicio
3. **Generar nueva clave privada** → Descargar JSON
4. Guardar como `serviceAccountKey.json` en la carpeta del proyecto

### 2. Ejecutar Script

```bash
cd c:\Users\OsoGa\StudioProjects\Spottivo\Spottivo-1

# Instalar firebase-admin
npm install firebase-admin

# Editar el script y poner tu User ID
# Línea 95: agregarComoAmigos('TU_USER_ID_AQUI')

# Ejecutar
node cargar_usuarios_prueba.js
```

---

## ✅ Resultado Esperado

Después de agregar los datos, verás en tu app:

1. **Mapa con 12 burbujas** distribuidas por Bogotá
2. **10 burbujas verdes** (usuarios en línea)
3. **2 burbujas grises** (Diego e Isabella offline)
4. **Contador:** "10/12 amigos en línea"
5. **Al hacer clic:** Nombres, fotos y direcciones reales de Bogotá

---

## 🧹 Limpiar Datos de Prueba

Para borrar todos los usuarios de prueba:

```javascript
// En Firebase Console → Firestore
// Eliminar colecciones:
locations/user_test_001 hasta user_test_012
users/user_test_001 hasta user_test_012
users/{TU_USER_ID}/friends/user_test_001 hasta user_test_012
```

O con script:

```javascript
async function limpiarDatosPrueba() {
  const batch = db.batch();
  for (let i = 1; i <= 12; i++) {
    const userId = `user_test_${String(i).padStart(3, '0')}`;
    batch.delete(db.collection('users').doc(userId));
    batch.delete(db.collection('locations').doc(userId));
  }
  await batch.commit();
  console.log("✅ Datos de prueba eliminados");
}
```

---

## 📸 Fotos de Perfil

Las fotos usan **Pravatar** (servicio gratuito de avatares aleatorios):
- URL: `https://i.pravatar.cc/150?img=X`
- Donde X es un número del 1 al 70
- No requiere configuración adicional
- Se cargan automáticamente con Coil

---

## 🎯 Próximas Pruebas

1. **Abrir la app** → Ver mapa con 12 usuarios
2. **Hacer clic en María** → Ver su ubicación en Centro Internacional
3. **Long-press en el mapa** → Trazar ruta OSRM a cualquier punto
4. **Hacer clic en Carlos** → Ver bottom sheet con dirección en Usaquén

¡Disfruta probando tu app con datos reales de Bogotá! 🇨🇴
