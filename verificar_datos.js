const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

(async () => {
  console.log('=== Verificando estructura Firestore ===\n');
  
  // 1. Verificar amigos
  const friends = await db.collection('users')
    .doc('XamI92OLV1NYOsh4q5ce8EJdphA2')
    .collection('friends')
    .get();
  
  console.log(`✅ Amigos encontrados: ${friends.size}`);
  friends.forEach(doc => console.log('  -', doc.id));
  
  // 2. Verificar ubicaciones
  console.log('\n✅ Ubicaciones encontradas:');
  const locations = await db.collection('locations').get();
  console.log(`Total: ${locations.size}`);
  locations.forEach(doc => {
    const data = doc.data();
    console.log(`  - ${doc.id}: (${data.latitude}, ${data.longitude})`);
  });
  
  // 3. Verificar usuarios
  console.log('\n✅ Usuarios encontrados:');
  const users = await db.collection('users').get();
  console.log(`Total: ${users.size}`);
  users.forEach(doc => {
    const data = doc.data();
    console.log(`  - ${doc.id}: ${data.nombre} (${data.email})`);
  });
  
  process.exit(0);
})().catch(error => {
  console.error('❌ Error:', error);
  process.exit(1);
});
