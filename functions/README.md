# Spottivo Backend - Firebase Functions

Backend completo para la aplicación Spottivo con:
- ✅ Seguimiento de ubicación en tiempo real (FindMy)
- ✅ Notificaciones push cuando amigos se conectan
- ✅ API REST para gestión de usuarios y ubicaciones
- ✅ Hardware Check para monitoreo del servidor

## 📦 Instalación

1. Instala Node.js (v18 o superior)
2. Instala Firebase CLI:
```bash
npm install -g firebase-tools
```

3. Inicializa Firebase en tu proyecto (si aún no lo has hecho):
```bash
firebase login
firebase init functions
```

4. Instala las dependencias:
```bash
cd functions
npm install
```

## 🚀 Despliegue

Para desplegar todas las funciones a Firebase:
```bash
firebase deploy --only functions
```

Para desplegar solo una función específica:
```bash
firebase deploy --only functions:api
```

## 🧪 Pruebas Locales

Ejecuta el emulador local:
```bash
npm run serve
```

Las funciones estarán disponibles en:
- API REST: `http://localhost:5001/YOUR_PROJECT_ID/us-central1/api`
- Triggers: Se ejecutarán automáticamente al modificar Firestore localmente

## 📡 Endpoints de la API

### Hardware Check
```
GET /api/hardware-check
```
Devuelve el estado del servidor, memoria, CPU y conexión a base de datos.

### Obtener ubicación de un usuario
```
GET /api/locations/:userId
```

### Obtener ubicaciones de todos los amigos
```
GET /api/friends/:userId/locations
```

### Agregar un amigo
```
POST /api/friends
Body: {
  "userId": "user123",
  "friendEmail": "amigo@example.com"
}
```

## 🔔 Funciones Automáticas (Triggers)

### onUserStatusChange
Se activa cuando un usuario cambia de offline a online. Envía notificaciones push a todos sus amigos.

### cleanOldLocations
Se ejecuta cada hora. Elimina ubicaciones con más de 24 horas de antigüedad.

### markInactiveUsersOffline
Se ejecuta cada 5 minutos. Marca como offline a usuarios que no han actualizado su ubicación en 15 minutos.

## 🔧 Configuración en Android

En tu archivo `ApiClient.kt`, actualiza la URL con tu Project ID:
```kotlin
private const val BASE_URL = "https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/api/"
```

## 📊 Monitoreo

Ver logs en tiempo real:
```bash
firebase functions:log
```

Ver logs de una función específica:
```bash
firebase functions:log --only api
```

## 🔐 Seguridad

- Todas las funciones usan Firebase Auth para validación
- Los tokens FCM se almacenan de forma segura en Firestore
- Las contraseñas NUNCA se guardan en la base de datos (usa Firebase Auth)

## 🆘 Troubleshooting

Si tienes problemas con los permisos:
```bash
firebase functions:config:set someservice.key="THE API KEY"
```

Para ver la configuración actual:
```bash
firebase functions:config:get
```
