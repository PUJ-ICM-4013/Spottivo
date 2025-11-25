/**
 * Script para cargar sitios deportivos de prueba en Firestore
 * Ejecutar con: node cargar_sitios_deportivos.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Propietaria de todos los sitios
const OWNER = {
  email: 'gabrielaa@gmail.com',
  nombre: 'Gabriela',
  id: 'XamI92OLV1NYOsh4q5ce8EJdphA2'
};

// Sitios deportivos de prueba alrededor de Bogotá
const sitiosDeportivos = [
  {
    nombre: "Body Tech Unicentro",
    descripcion: "Gimnasio de alta gama con equipos de última tecnología, clases grupales, zona de crossfit y entrenadores certificados. Incluye sauna, turco y piscina.",
    tags: ["Gimnasio", "CrossFit", "Spinning", "Yoga"],
    fotos: [
      "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800",
      "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800",
      "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800"
    ],
    latitud: 4.6618,
    longitud: -74.0551,
    direccion: "Av. Suba # 104C-50, Bogotá",
    telefono: "+57 1 7431234",
    email: "unicentro@bodytech.com.co",
    whatsapp: "+573001234567",
    horarios: "Lun-Vie: 5:00am-11:00pm, Sáb-Dom: 7:00am-9:00pm",
    precioDesde: 180000,
    precioHasta: 350000,
    calificacion: 4.7,
    numeroCalificaciones: 156
  },
  {
    nombre: "Smart Fit Salitre Plaza",
    descripcion: "Gimnasio inteligente 24/7 con acceso mediante app. Equipamiento completo para fuerza y cardio. Ambiente motivador y precios accesibles.",
    tags: ["Gimnasio", "Entrenamiento Funcional", "Calistenia"],
    fotos: [
      "https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800",
      "https://images.unsplash.com/photo-1558611848-73f7eb4001a1?w=800"
    ],
    latitud: 4.6537,
    longitud: -74.1087,
    direccion: "Calle 26 # 68B-70, Salitre Plaza",
    telefono: "+57 1 7432222",
    email: "salitre@smartfit.com.co",
    whatsapp: "+573002345678",
    horarios: "Abierto 24 horas",
    precioDesde: 59900,
    precioHasta: 89900,
    calificacion: 4.3,
    numeroCalificaciones: 342
  },
  {
    nombre: "Club El Nogal",
    descripcion: "Club deportivo exclusivo con canchas de tenis, squash, piscinas olímpicas, gimnasio, spa y restaurante. Instalaciones de primer nivel.",
    tags: ["Tenis", "Squash", "Natación", "Gimnasio"],
    fotos: [
      "https://images.unsplash.com/photo-1622163642998-1ea32b0bbc67?w=800",
      "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800"
    ],
    latitud: 4.6689,
    longitud: -74.0544,
    direccion: "Cra. 7 # 74-56, Bogotá",
    telefono: "+57 1 3267777",
    email: "info@clubelnogal.com",
    whatsapp: "+573003456789",
    horarios: "Lun-Dom: 6:00am-10:00pm",
    precioDesde: 450000,
    precioHasta: 850000,
    calificacion: 4.9,
    numeroCalificaciones: 89
  },
  {
    nombre: "CrossFit Fusión",
    descripcion: "Box de CrossFit con coaches certificados nivel 1 y 2. WODs diarios, gimnasia, levantamiento olímpico y entrenamiento funcional de alta intensidad.",
    tags: ["CrossFit", "Entrenamiento Funcional", "Calistenia"],
    fotos: [
      "https://images.unsplash.com/photo-1599058917212-d750089bc07e?w=800",
      "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800"
    ],
    latitud: 4.6767,
    longitud: -74.0481,
    direccion: "Calle 94A # 11A-62, Chicó",
    telefono: "+57 1 7434444",
    email: "info@crossfitfusion.co",
    whatsapp: "+573004567890",
    horarios: "Lun-Vie: 5:30am-9:00pm, Sáb: 8:00am-12:00pm",
    precioDesde: 280000,
    precioHasta: 380000,
    calificacion: 4.8,
    numeroCalificaciones: 127
  },
  {
    nombre: "Yoga Espacio Consciente",
    descripcion: "Centro de yoga y meditación. Clases de Hatha, Vinyasa, Ashtanga y Yin Yoga. Ambiente tranquilo con instructores certificados internacionalmente.",
    tags: ["Yoga", "Pilates"],
    fotos: [
      "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800",
      "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800"
    ],
    latitud: 4.6528,
    longitud: -74.0602,
    direccion: "Cra. 13 # 85-24, Bogotá",
    telefono: "+57 1 7435555",
    email: "namaste@espacioconsciente.co",
    whatsapp: "+573005678901",
    horarios: "Lun-Vie: 6:00am-8:00pm, Sáb: 8:00am-2:00pm",
    precioDesde: 120000,
    precioHasta: 220000,
    calificacion: 4.9,
    numeroCalificaciones: 203
  },
  {
    nombre: "Piscina Olímpica Simón Bolívar",
    descripcion: "Complejo acuático con piscinas olímpica y semi-olímpica. Clases de natación para todas las edades, aquaerobics y entrenamiento de alto rendimiento.",
    tags: ["Natación", "Atletismo"],
    fotos: [
      "https://images.unsplash.com/photo-1576610616656-d3aa5d1f4534?w=800",
      "https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=800"
    ],
    latitud: 4.6577,
    longitud: -74.0936,
    direccion: "Av. Calle 63 # 48-81, Parque Simón Bolívar",
    telefono: "+57 1 3157777",
    email: "info@piscinasimonbolivar.gov.co",
    whatsapp: "+573006789012",
    horarios: "Lun-Dom: 6:00am-7:00pm",
    precioDesde: 35000,
    precioHasta: 150000,
    calificacion: 4.4,
    numeroCalificaciones: 278
  },
  {
    nombre: "Arena Fútbol 5 La Candelaria",
    descripcion: "Canchas sintéticas de fútbol 5 y 7 con iluminación LED. Alquiler por horas, torneos interempresas y ligas recreativas. Incluye vestiers y cafetería.",
    tags: ["Fútbol"],
    fotos: [
      "https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800",
      "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800"
    ],
    latitud: 4.5981,
    longitud: -74.0758,
    direccion: "Calle 10 # 4-32, La Candelaria",
    telefono: "+57 1 7436666",
    email: "reservas@arenafutbol5.co",
    whatsapp: "+573007890123",
    horarios: "Lun-Dom: 6:00am-11:00pm",
    precioDesde: 80000,
    precioHasta: 150000,
    calificacion: 4.5,
    numeroCalificaciones: 421
  },
  {
    nombre: "Fight Zone MMA & Boxing",
    descripcion: "Gimnasio especializado en artes marciales mixtas, boxeo, muay thai y jiu-jitsu brasileño. Entrenadores con experiencia en competencia profesional.",
    tags: ["Boxeo", "Artes Marciales"],
    fotos: [
      "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=800",
      "https://images.unsplash.com/photo-1555597673-b21d5c935865?w=800"
    ],
    latitud: 4.6872,
    longitud: -74.0449,
    direccion: "Calle 127 # 17-23, Usaquén",
    telefono: "+57 1 7437777",
    email: "info@fightzone.co",
    whatsapp: "+573008901234",
    horarios: "Lun-Vie: 6:00am-10:00pm, Sáb: 8:00am-6:00pm",
    precioDesde: 180000,
    precioHasta: 280000,
    calificacion: 4.7,
    numeroCalificaciones: 167
  },
  {
    nombre: "Club Campestre Guaymaral",
    descripcion: "Club campestre con campos de golf 18 hoyos, canchas de tenis, squash, piscinas, gimnasio y zona ecuestre. Ideal para toda la familia.",
    tags: ["Tenis", "Natación", "Gimnasio", "Ciclismo"],
    fotos: [
      "https://images.unsplash.com/photo-1587174486073-ae5e5cff23aa?w=800",
      "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?w=800"
    ],
    latitud: 4.7956,
    longitud: -74.0489,
    direccion: "Km 2 Vía Guaymaral, Chía",
    telefono: "+57 1 8627777",
    email: "info@clubguaymaral.com",
    whatsapp: "+573009012345",
    horarios: "Lun-Dom: 6:00am-9:00pm",
    precioDesde: 650000,
    precioHasta: 1200000,
    calificacion: 4.8,
    numeroCalificaciones: 94
  },
  {
    nombre: "Spinning & Cycling Studio",
    descripcion: "Studio boutique especializado en spinning con clases coreografiadas, música en vivo y sistema de métricas en tiempo real. Ambiente energético y motivador.",
    tags: ["Spinning", "Ciclismo"],
    fotos: [
      "https://images.unsplash.com/photo-1521805103424-d8f8430e8933?w=800",
      "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800"
    ],
    latitud: 4.6712,
    longitud: -74.0577,
    direccion: "Cra. 11 # 82-14, Bogotá",
    telefono: "+57 1 7438888",
    email: "info@spinningcyclingstudio.co",
    whatsapp: "+573000123456",
    horarios: "Lun-Vie: 5:30am-8:30pm, Sáb-Dom: 8:00am-2:00pm",
    precioDesde: 150000,
    precioHasta: 250000,
    calificacion: 4.6,
    numeroCalificaciones: 189
  },
  {
    nombre: "Boulder Park - Escalada",
    descripcion: "Rocódromo indoor con más de 500m² de pared de escalada. Rutas para principiantes y avanzados, clases con instructores certificados y tienda especializada.",
    tags: ["Escalada", "Calistenia"],
    fotos: [
      "https://images.unsplash.com/photo-1522163182402-834f871fd851?w=800",
      "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=800"
    ],
    latitud: 4.6456,
    longitud: -74.1123,
    direccion: "Calle 26 # 82-32, Fontibón",
    telefono: "+57 1 7439999",
    email: "info@boulderpark.co",
    whatsapp: "+573001234567",
    horarios: "Lun-Vie: 10:00am-10:00pm, Sáb-Dom: 9:00am-8:00pm",
    precioDesde: 25000,
    precioHasta: 180000,
    calificacion: 4.7,
    numeroCalificaciones: 312
  },
  {
    nombre: "Pádel Club Bogotá",
    descripcion: "Complejo con 8 canchas de pádel profesionales, iluminación LED, tienda pro shop, cafetería y zona lounge. Clases con profesores nacionales e internacionales.",
    tags: ["Pádel", "Tenis"],
    fotos: [
      "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?w=800",
      "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800"
    ],
    latitud: 4.6923,
    longitud: -74.0412,
    direccion: "Calle 140 # 10-35, Cedritos",
    telefono: "+57 1 7430000",
    email: "reservas@padelclubbogota.co",
    whatsapp: "+573002345678",
    horarios: "Lun-Dom: 6:00am-10:00pm",
    precioDesde: 60000,
    precioHasta: 200000,
    calificacion: 4.8,
    numeroCalificaciones: 145
  },
  {
    nombre: "Coliseo Cubierto El Campín",
    descripcion: "Coliseo multifuncional para práctica de baloncesto, voleibol, fútbol sala y eventos deportivos. Alquiler de canchas y programas deportivos comunitarios.",
    tags: ["Baloncesto", "Voleibol", "Fútbol"],
    fotos: [
      "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800",
      "https://images.unsplash.com/photo-1611296350298-6e5eae7ab09f?w=800"
    ],
    latitud: 4.6480,
    longitud: -74.0752,
    direccion: "Av. NQS # 56-15, Chapinero",
    telefono: "+57 1 2227777",
    email: "info@coliseocampin.gov.co",
    whatsapp: "+573003456789",
    horarios: "Lun-Vie: 6:00am-9:00pm, Sáb-Dom: 8:00am-6:00pm",
    precioDesde: 50000,
    precioHasta: 300000,
    calificacion: 4.2,
    numeroCalificaciones: 267
  },
  {
    nombre: "Academia de Parkour Bogotá",
    descripcion: "Primera academia de parkour y freerunning en Bogotá. Entrenamientos estructurados desde nivel básico hasta avanzado. Instalaciones seguras con colchonetas y obstáculos.",
    tags: ["Parkour", "Calistenia"],
    fotos: [
      "https://images.unsplash.com/photo-1571188654248-7a89213915f7?w=800",
      "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?w=800"
    ],
    latitud: 4.6234,
    longitud: -74.0644,
    direccion: "Calle 27 Sur # 26-45, San Cristóbal",
    telefono: "+57 1 7431111",
    email: "info@parkourbo.co",
    whatsapp: "+573004567890",
    horarios: "Lun-Vie: 3:00pm-9:00pm, Sáb: 9:00am-5:00pm",
    precioDesde: 120000,
    precioHasta: 200000,
    calificacion: 4.9,
    numeroCalificaciones: 156
  },
  {
    nombre: "Centro Deportivo Salitre Mágico",
    descripcion: "Complejo deportivo con canchas de baloncesto, voleibol, tenis, piscinas y gimnasio. Ubicado dentro del parque Salitre Mágico. Ideal para grupos y familias.",
    tags: ["Baloncesto", "Voleibol", "Natación", "Tenis"],
    fotos: [
      "https://images.unsplash.com/photo-1577223625816-7546f13df25d?w=800",
      "https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=800"
    ],
    latitud: 4.6556,
    longitud: -74.1065,
    direccion: "Calle 63 # 60-80, Salitre",
    telefono: "+57 1 3157788",
    email: "deportes@salitremagico.gov.co",
    whatsapp: "+573005678901",
    horarios: "Lun-Dom: 7:00am-7:00pm",
    precioDesde: 30000,
    precioHasta: 120000,
    calificacion: 4.3,
    numeroCalificaciones: 398
  }
];

async function cargarSitiosDeportivos() {
  console.log('🏃 Iniciando carga de sitios deportivos...\n');
  
  const batch = db.batch();
  let contador = 0;
  
  for (const sitio of sitiosDeportivos) {
    const sitioCompleto = {
      ...sitio,
      propietarioId: OWNER.id,
      propietarioEmail: OWNER.email,
      propietarioNombre: OWNER.nombre,
      activo: true,
      fechaCreacion: admin.firestore.FieldValue.serverTimestamp()
    };
    
    const docRef = db.collection('sportPlaces').doc();
    batch.set(docRef, sitioCompleto);
    
    contador++;
    console.log(`✅ ${contador}. ${sitio.nombre}`);
    console.log(`   📍 ${sitio.direccion}`);
    console.log(`   🏷️  ${sitio.tags.join(', ')}`);
    console.log(`   💰 $${sitio.precioDesde.toLocaleString()} - $${sitio.precioHasta.toLocaleString()}`);
    console.log(`   ⭐ ${sitio.calificacion} (${sitio.numeroCalificaciones} calificaciones)`);
    console.log('');
  }
  
  await batch.commit();
  
  console.log(`\n🎉 ¡Carga completada exitosamente!`);
  console.log(`📊 Total de sitios deportivos: ${contador}`);
  console.log(`👤 Propietaria: ${OWNER.nombre} (${OWNER.email})`);
  console.log(`📍 Ubicación: Bogotá y alrededores`);
  console.log(`\n💡 Puedes filtrar por tags:`);
  console.log(`   - Gimnasio, CrossFit, Yoga, Pilates`);
  console.log(`   - Natación, Tenis, Fútbol, Baloncesto`);
  console.log(`   - Artes Marciales, Boxeo, Escalada`);
  console.log(`   - Spinning, Ciclismo, Parkour, etc.`);
}

cargarSitiosDeportivos()
  .then(() => {
    console.log('\n✅ Proceso finalizado');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Error:', error);
    process.exit(1);
  });
