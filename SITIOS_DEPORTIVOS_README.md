# Sistema de Sitios Deportivos - Spottivo

## 📋 Descripción

Sistema completo para mostrar, buscar y filtrar sitios deportivos en Bogotá. Los usuarios pueden descubrir gimnasios, canchas, estudios de yoga, piscinas y más, con información detallada de cada lugar.

## ✨ Características Implementadas

### 🏢 Modelo de Datos (SportPlace)
- **Información básica**: Nombre, descripción, dirección
- **Ubicación**: Latitud, longitud para mostrar en mapas
- **Clasificación**: Tags/etiquetas por tipo de deporte
- **Multimedia**: Múltiples fotos del lugar
- **Contacto**: Teléfono, email, WhatsApp
- **Propietario**: Email y nombre del dueño del sitio
- **Precios**: Rango de precios (desde-hasta)
- **Calificaciones**: Rating y número de reseñas
- **Horarios**: Horario de atención

### 🏷️ Tags de Clasificación
20 categorías deportivas disponibles:
- Gimnasio, CrossFit, Yoga, Pilates
- Natación, Tenis, Fútbol, Baloncesto, Voleibol
- Artes Marciales, Boxeo
- Ciclismo, Spinning, Atletismo
- Escalada, Pádel, Squash
- Entrenamiento Funcional, Calistenia, Parkour

### 🔍 Funcionalidades de Búsqueda
1. **Búsqueda por texto**: Nombre, descripción, dirección
2. **Filtro por tags**: Selección múltiple de categorías deportivas
3. **Filtro por ubicación**: Sitios cercanos con radio configurable
4. **Filtro por propietario**: Ver todos los sitios de un dueño

### 📱 Pantalla de Búsqueda (SportPlacesSearchScreen)
- **Barra de búsqueda** con autocompletado
- **Chips de filtros** seleccionados visibles
- **Contador de resultados** en tiempo real
- **Cards con información**: Foto, nombre, rating, tags, dirección, precio
- **Bottom Sheet de filtros** con todos los tags disponibles
- **Diálogo de detalles** con galería de fotos y opciones de contacto

### 📞 Opciones de Contacto
- **Llamada telefónica**: Abre el marcador con el número
- **WhatsApp**: Abre chat directo con el propietario
- **Email**: Abre cliente de correo

### 💾 Arquitectura
- **Repository Pattern**: `SportPlaceRepository` maneja Firestore
- **MVVM**: `SportPlaceViewModel` gestiona estado UI
- **StateFlow**: Comunicación reactiva entre capas
- **Coroutines**: Operaciones asíncronas eficientes

## 🗄️ Estructura de Firestore

```
sportPlaces/
  {placeId}/
    - id: String
    - nombre: String
    - descripcion: String
    - tags: Array<String>
    - fotos: Array<String>
    - latitud: Double
    - longitud: Double
    - direccion: String
    - telefono: String
    - email: String
    - whatsapp: String
    - propietarioId: String
    - propietarioEmail: String
    - propietarioNombre: String
    - horarios: String
    - precioDesde: Number
    - precioHasta: Number
    - calificacion: Number
    - numeroCalificaciones: Number
    - activo: Boolean
    - fechaCreacion: Timestamp
```

## 🔐 Reglas de Seguridad Firestore

```javascript
match /sportPlaces/{placeId} {
  // Lectura pública para usuarios autenticados
  allow read: if request.auth != null;
  
  // Solo usuarios autenticados pueden crear
  allow create: if request.auth != null && 
    request.resource.data.propietarioEmail == request.auth.token.email;
  
  // Solo el propietario puede actualizar
  allow update: if request.auth != null && 
    resource.data.propietarioEmail == request.auth.token.email;
  
  // Solo el propietario puede eliminar
  allow delete: if request.auth != null && 
    resource.data.propietarioEmail == request.auth.token.email;
}
```

## 📊 Datos de Prueba

Se cargaron **15 sitios deportivos reales** alrededor de Bogotá:

1. **Body Tech Unicentro** - Gimnasio premium (Suba)
2. **Smart Fit Salitre Plaza** - Gimnasio 24/7 (Salitre)
3. **Club El Nogal** - Club deportivo exclusivo (Chapinero)
4. **CrossFit Fusión** - Box de CrossFit (Chicó)
5. **Yoga Espacio Consciente** - Centro de yoga (Usaquén)
6. **Piscina Olímpica Simón Bolívar** - Complejo acuático
7. **Arena Fútbol 5 La Candelaria** - Canchas sintéticas
8. **Fight Zone MMA & Boxing** - Artes marciales (Usaquén)
9. **Club Campestre Guaymaral** - Club campestre (Chía)
10. **Spinning & Cycling Studio** - Studio boutique
11. **Boulder Park** - Rocódromo indoor (Fontibón)
12. **Pádel Club Bogotá** - Canchas de pádel (Cedritos)
13. **Coliseo Cubierto El Campín** - Coliseo multifuncional
14. **Academia de Parkour Bogotá** - Parkour y freerunning
15. **Centro Deportivo Salitre Mágico** - Complejo familiar

**Propietaria de todos los sitios**: Gabriela (gabrielaa@gmail.com)

## 🚀 Cómo Ejecutar

### 1. Cargar Datos de Prueba
```bash
node cargar_sitios_deportivos.js
```

### 2. Compilar y Ejecutar App
```bash
./gradlew assembleDebug
```

### 3. Integrar en la Navegación
Agregar la pantalla al `NavigationGraph`:

```kotlin
composable(Screen.SportPlaces.route) {
    SportPlacesSearchScreen()
}
```

## 🎨 UI/UX Implementada

### Pantalla Principal
- **Top Bar**: Título + botón de filtros con badge
- **Search Bar**: Búsqueda en tiempo real
- **Chips**: Tags seleccionados (removibles)
- **Lista**: Cards con scroll infinito
- **Loading**: Indicador de carga

### Card de Sitio
- Imagen destacada (180dp altura)
- Nombre + Rating con estrellas
- Descripción (2 líneas máximo)
- Tags principales (máximo 3)
- Dirección + rango de precios

### Bottom Sheet de Filtros
- Grid 2 columnas de FilterChips
- Botón "Limpiar" para resetear
- Botón "Aplicar filtros"

### Diálogo de Detalles
- Galería horizontal de fotos
- Descripción completa
- Todos los tags
- Información de contacto
- Horarios y precios
- Botones de contacto (Llamar, WhatsApp, Email)

## 🔄 Flujo de Datos

```
Usuario interactúa
    ↓
SportPlacesSearchScreen (UI)
    ↓
SportPlaceViewModel (Estado)
    ↓
SportPlaceRepository (Datos)
    ↓
Firestore (Base de datos)
    ↓
Flow actualiza UI en tiempo real
```

## 📝 Casos de Uso

### 1. Buscar gimnasios cerca
1. Usuario escribe "gimnasio" en búsqueda
2. ViewModel filtra en tiempo real
3. Se muestran todos los sitios con tag "Gimnasio"

### 2. Filtrar por múltiples deportes
1. Usuario abre bottom sheet de filtros
2. Selecciona "Yoga" y "Pilates"
3. Se muestran solo sitios que tengan alguno de esos tags

### 3. Ver detalles y contactar
1. Usuario toca un card
2. Se abre diálogo con toda la información
3. Usuario toca "WhatsApp"
4. Se abre WhatsApp con el número del propietario

### 4. Ver sitios de un propietario
```kotlin
viewModel.loadPlacesByOwner("gabrielaa@gmail.com")
```

## 🔧 Configuración Adicional

### Permisos en AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
```

### Dependencias Requeridas
- Firebase Firestore
- Coil para imágenes
- Material3 para UI
- Coroutines + Flow

## 🎯 Próximas Mejoras Sugeridas

1. **Integración con mapa**: Mostrar sitios en mapa con marcadores
2. **Sistema de reseñas**: Permitir a usuarios calificar sitios
3. **Favoritos**: Guardar sitios favoritos del usuario
4. **Reservas**: Sistema de reserva de espacios/clases
5. **Notificaciones**: Alertas de nuevos sitios cercanos
6. **Comparador**: Comparar precios y servicios
7. **Ruta**: Calcular ruta desde ubicación actual
8. **Filtros avanzados**: Por precio, rating, distancia

## 📸 Assets Utilizados

Todas las fotos provienen de **Unsplash** (libres de derechos):
- Imágenes de alta calidad
- URLs directas sin necesidad de descarga
- Diferentes ángulos y estilos por sitio

## ✅ Estado Actual

- ✅ Modelo de datos completo
- ✅ Repository con operaciones CRUD
- ✅ ViewModel con lógica de negocio
- ✅ Pantalla UI completa y funcional
- ✅ Filtros múltiples (texto + tags)
- ✅ 15 sitios de prueba cargados
- ✅ Reglas Firestore configuradas
- ✅ Compilación exitosa
- ✅ Sistema de contacto implementado

---

**Desarrollado para Spottivo** 🏃‍♂️
