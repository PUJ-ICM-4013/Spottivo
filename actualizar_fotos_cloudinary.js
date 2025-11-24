/**
 * Script para subir fotos de avatares a Cloudinary y actualizar usuarios en Firestore
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');
const https = require('https');
const { Readable } = require('stream');

// Configuración de Cloudinary
const CLOUDINARY_CLOUD_NAME = 'dxwc5bviw';
const CLOUDINARY_UPLOAD_PRESET = 'spottivo_preset'; // Debes crear este preset en Cloudinary

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const usuariosConAvatares = [
  { userId: "user_test_001", nombre: "María Rodríguez", avatarNum: 1 },
  { userId: "user_test_002", nombre: "Carlos Méndez", avatarNum: 12 },
  { userId: "user_test_003", nombre: "Laura Gómez", avatarNum: 5 },
  { userId: "user_test_004", nombre: "Andrés Torres", avatarNum: 15 },
  { userId: "user_test_005", nombre: "Valentina Castro", avatarNum: 9 },
  { userId: "user_test_006", nombre: "Diego Ramírez", avatarNum: 13 },
  { userId: "user_test_007", nombre: "Camila Vargas", avatarNum: 10 },
  { userId: "user_test_008", nombre: "Santiago Morales", avatarNum: 14 },
  { userId: "user_test_009", nombre: "Isabella Fernández", avatarNum: 16 },
  { userId: "user_test_010", nombre: "Mateo Herrera", avatarNum: 11 },
  { userId: "user_test_011", nombre: "Sofía Jiménez", avatarNum: 20 },
  { userId: "user_test_012", nombre: "Sebastián Ruiz", avatarNum: 17 }
];

// Función para descargar imagen desde pravatar
function descargarImagen(url) {
  return new Promise((resolve, reject) => {
    https.get(url, (response) => {
      const chunks = [];
      response.on('data', (chunk) => chunks.push(chunk));
      response.on('end', () => resolve(Buffer.concat(chunks)));
      response.on('error', reject);
    }).on('error', reject);
  });
}

// Función para subir a Cloudinary usando API REST
async function subirACloudinary(imageBuffer, publicId) {
  return new Promise((resolve, reject) => {
    const FormData = require('form-data');
    const form = new FormData();
    
    form.append('file', imageBuffer, { filename: `${publicId}.jpg` });
    form.append('upload_preset', CLOUDINARY_UPLOAD_PRESET);
    form.append('public_id', publicId);
    form.append('folder', 'spottivo/avatares');
    
    form.submit(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`, (err, res) => {
      if (err) return reject(err);
      
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const result = JSON.parse(data);
          if (result.secure_url) {
            resolve(result.secure_url);
          } else {
            reject(new Error('No se obtuvo URL de Cloudinary'));
          }
        } catch (e) {
          reject(e);
        }
      });
    });
  });
}

async function actualizarFotosUsuarios() {
  console.log('🔄 Actualizando fotos de usuarios en Cloudinary...\n');
  
  for (const usuario of usuariosConAvatares) {
    try {
      console.log(`📷 Procesando ${usuario.nombre}...`);
      
      // 1. Descargar imagen de pravatar
      const pravaUavaUrl = `https://i.pravatar.cc/300?img=${usuario.avatarNum}`;
      console.log(`  Descargando desde ${pravaUavaUrl}`);
      const imageBuffer = await descargarImagen(pravaUavaUrl);
      console.log(`  ✅ Descargado: ${imageBuffer.length} bytes`);
      
      // 2. Subir a Cloudinary
      const publicId = `avatar_${usuario.userId}`;
      console.log(`  Subiendo a Cloudinary...`);
      const cloudinaryUrl = await subirACloudinary(imageBuffer, publicId);
      console.log(`  ✅ URL Cloudinary: ${cloudinaryUrl}`);
      
      // 3. Actualizar Firestore
      await db.collection('users').doc(usuario.userId).update({
        photoUrl: cloudinaryUrl
      });
      console.log(`  ✅ Firestore actualizado\n`);
      
    } catch (error) {
      console.error(`  ❌ Error con ${usuario.nombre}:`, error.message, '\n');
    }
  }
  
  console.log('🎉 ¡Proceso completado!');
  process.exit(0);
}

// Ejecutar
actualizarFotosUsuarios().catch(error => {
  console.error('❌ Error fatal:', error);
  process.exit(1);
});
