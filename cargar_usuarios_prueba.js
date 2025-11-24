/**
 * Script para cargar usuarios de prueba en Firestore
 * 
 * INSTRUCCIONES:
 * 1. Ve a Firebase Console → Firestore Database
 * 2. Copia y pega estos comandos en la consola de Node.js
 * 3. O usa Firebase CLI: firebase functions:shell
 * 
 * Usuarios de prueba en Bogotá con ubicaciones reales
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // Descargar desde Firebase Console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const usuariosPrueba = [
  {
    userId: "user_test_001",
    nombre: "María Rodríguez",
    email: "maria.rodriguez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/1.jpg",
    isOnline: true,
    location: { latitude: 4.60971, longitude: -74.08175 }
  },
  {
    userId: "user_test_002",
    nombre: "Carlos Méndez",
    email: "carlos.mendez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/12.jpg",
    isOnline: true,
    location: { latitude: 4.67694, longitude: -74.04834 }
  },
  {
    userId: "user_test_003",
    nombre: "Laura Gómez",
    email: "laura.gomez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/5.jpg",
    isOnline: true,
    location: { latitude: 4.65894, longitude: -74.09378 }
  },
  {
    userId: "user_test_004",
    nombre: "Andrés Torres",
    email: "andres.torres@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/15.jpg",
    isOnline: true,
    location: { latitude: 4.62389, longitude: -74.06445 }
  },
  {
    userId: "user_test_005",
    nombre: "Valentina Castro",
    email: "valentina.castro@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/9.jpg",
    isOnline: true,
    location: { latitude: 4.71099, longitude: -74.03506 }
  },
  {
    userId: "user_test_006",
    nombre: "Diego Ramírez",
    email: "diego.ramirez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/13.jpg",
    isOnline: false,
    location: { latitude: 4.59847, longitude: -74.07672 }
  },
  {
    userId: "user_test_007",
    nombre: "Camila Vargas",
    email: "camila.vargas@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/10.jpg",
    isOnline: true,
    location: { latitude: 4.64778, longitude: -74.05854 }
  },
  {
    userId: "user_test_008",
    nombre: "Santiago Morales",
    email: "santiago.morales@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/14.jpg",
    isOnline: true,
    location: { latitude: 4.66283, longitude: -74.05493 }
  },
  {
    userId: "user_test_009",
    nombre: "Isabella Fernández",
    email: "isabella.fernandez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/16.jpg",
    isOnline: false,
    location: { latitude: 4.60389, longitude: -74.06556 }
  },
  {
    userId: "user_test_010",
    nombre: "Mateo Herrera",
    email: "mateo.herrera@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/11.jpg",
    isOnline: true,
    location: { latitude: 4.63890, longitude: -74.11023 }
  },
  {
    userId: "user_test_011",
    nombre: "Sofía Jiménez",
    email: "sofia.jimenez@test.com",
    photoUrl: "https://randomuser.me/api/portraits/women/20.jpg",
    isOnline: true,
    location: { latitude: 4.68445, longitude: -74.05667 }
  },
  {
    userId: "user_test_012",
    nombre: "Sebastián Ruiz",
    email: "sebastian.ruiz@test.com",
    photoUrl: "https://randomuser.me/api/portraits/men/17.jpg",
    isOnline: true,
    location: { latitude: 4.61278, longitude: -74.07056 }
  }
];

async function cargarUsuariosPrueba() {
  console.log("🔥 Iniciando carga de usuarios de prueba...");
  
  const batch = db.batch();
  
  for (const usuario of usuariosPrueba) {
    // 1. Crear documento de usuario
    const userRef = db.collection('users').doc(usuario.userId);
    batch.set(userRef, {
      nombre: usuario.nombre,
      email: usuario.email,
      photoUrl: usuario.photoUrl,
      isOnline: usuario.isOnline,
      role: "usuario",
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // 2. Crear ubicación
    const locationRef = db.collection('locations').doc(usuario.userId);
    batch.set(locationRef, {
      latitude: usuario.location.latitude,
      longitude: usuario.location.longitude,
      timestamp: Date.now()
    });
    
    console.log(`✅ ${usuario.nombre} agregado`);
  }
  
  await batch.commit();
  console.log("🎉 ¡Usuarios de prueba cargados exitosamente!");
}

async function agregarComoAmigos(tuUserId) {
  console.log(`🤝 Agregando usuarios de prueba como amigos de ${tuUserId}...`);
  
  const batch = db.batch();
  
  for (const usuario of usuariosPrueba) {
    // Agregar cada usuario de prueba como amigo tuyo
    const friendRef = db.collection('users').doc(tuUserId).collection('friends').doc(usuario.userId);
    batch.set(friendRef, {
      addedAt: admin.firestore.FieldValue.serverTimestamp()
    });
  }
  
  await batch.commit();
  console.log("✅ Todos los usuarios agregados como amigos!");
}

// EJECUTAR ESTAS FUNCIONES:

// 1. Cargar usuarios
cargarUsuariosPrueba().then(() => {
  console.log("✅ Proceso completado");
  
  // 2. Agregarlos como amigos
  agregarComoAmigos('XamI92OLV1NYOsh4q5ce8EJdphA2').then(() => {
    console.log("✅ Amigos agregados");
    process.exit(0);
  });
}).catch(error => {
  console.error("❌ Error:", error);
  process.exit(1);
});
