const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

(async () => {
  console.log('=== DIAGNÓSTICO COMPLETO ===\n');
  
  const userId = 'XamI92OLV1NYOsh4q5ce8EJdphA2';
  
  // 1. Verificar que el usuario existe en Auth
  try {
    const userAuth = await auth.getUser(userId);
    console.log('✅ Usuario en Authentication:');
    console.log(`   Email: ${userAuth.email}`);
    console.log(`   UID: ${userAuth.uid}`);
    console.log(`   Display Name: ${userAuth.displayName || 'N/A'}`);
  } catch (e) {
    console.log('❌ Usuario NO existe en Firebase Authentication');
    console.log('   Necesitas iniciar sesión en la app con este usuario\n');
  }
  
  // 2. Verificar documento de usuario
  const userDoc = await db.collection('users').doc(userId).get();
  if (userDoc.exists) {
    console.log('\n✅ Usuario en Firestore:');
    const data = userDoc.data();
    console.log(`   Nombre: ${data.nombre}`);
    console.log(`   Email: ${data.email}`);
    console.log(`   PhotoURL: ${data.photoUrl || 'N/A'}`);
  } else {
    console.log('\n❌ Usuario NO existe en Firestore');
  }
  
  // 3. Verificar amigos
  const friends = await db.collection('users').doc(userId).collection('friends').get();
  console.log(`\n✅ Amigos: ${friends.size}`);
  if (friends.size === 0) {
    console.log('   ⚠️ No tienes amigos agregados');
  } else {
    friends.docs.slice(0, 3).forEach(doc => console.log(`   - ${doc.id}`));
    if (friends.size > 3) console.log(`   ... y ${friends.size - 3} más`);
  }
  
  // 4. Verificar ubicaciones de amigos
  const friendIds = friends.docs.map(doc => doc.id);
  console.log(`\n✅ Ubicaciones de amigos:`);
  
  let count = 0;
  for (const friendId of friendIds.slice(0, 3)) {
    const locationDoc = await db.collection('locations').doc(friendId).get();
    if (locationDoc.exists) {
      const loc = locationDoc.data();
      const userDoc = await db.collection('users').doc(friendId).get();
      const userData = userDoc.data();
      console.log(`   ✓ ${userData?.nombre}: (${loc.latitude}, ${loc.longitude})`);
      console.log(`     Foto: ${userData?.photoUrl}`);
      count++;
    }
  }
  if (friendIds.length > 3) {
    console.log(`   ... y ${friendIds.length - 3} ubicaciones más`);
  }
  
  // 5. Simular lo que hace MapViewModel
  console.log('\n=== SIMULACIÓN MapViewModel ===');
  console.log(`1. Usuario actual: ${userId}`);
  console.log(`2. Buscando amigos en: users/${userId}/friends`);
  console.log(`3. Encontrados: ${friends.size} amigos`);
  console.log(`4. Buscando ubicaciones en: locations/{userId}`);
  console.log(`5. Ubicaciones válidas: ${count}`);
  
  if (count === 0) {
    console.log('\n❌ PROBLEMA: No se encontraron ubicaciones válidas');
    console.log('   Solución: Verifica que la colección "locations" tenga datos');
  } else {
    console.log(`\n✅ TODO CORRECTO - Deberían verse ${count} usuarios en el mapa`);
    console.log('\nPosibles causas si no aparecen:');
    console.log('1. No iniciaste sesión con el usuario correcto en la app');
    console.log('2. Estás viendo MapScreen en vez de FindMyMapScreen');
    console.log('3. Problemas de permisos en Firestore Rules');
    console.log('4. La app no tiene permisos de ubicación');
  }
  
  process.exit(0);
})().catch(error => {
  console.error('❌ Error:', error);
  process.exit(1);
});
