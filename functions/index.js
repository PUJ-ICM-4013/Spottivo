const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors");

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

// =====================================================
// CLOUD FUNCTION: ENVIAR NOTIFICACIÓN DE CHAT
// =====================================================
/**
 * Trigger cuando se crea un nuevo mensaje
 * Envía notificación push al destinatario
 */
exports.sendChatNotification = functions.firestore
    .document('chats/{chatId}/messages/{messageId}')
    .onCreate(async (snapshot, context) => {
        try {
            const message = snapshot.data();
            const chatId = context.params.chatId;
            
            // Obtener participantes del chat
            const chatDoc = await db.collection('chats').doc(chatId).get();
            if (!chatDoc.exists) {
                console.log('Chat no encontrado');
                return null;
            }
            
            const participants = chatDoc.data().participants || [];
            
            // Encontrar el destinatario (el que NO es el emisor)
            const recipientId = participants.find(id => id !== message.senderId);
            
            if (!recipientId) {
                console.log('Destinatario no encontrado');
                return null;
            }
            
            // Obtener FCM token del destinatario
            const recipientDoc = await db.collection('users').doc(recipientId).get();
            if (!recipientDoc.exists) {
                console.log('Usuario destinatario no encontrado');
                return null;
            }
            
            const recipientData = recipientDoc.data();
            const fcmToken = recipientData.fcmToken;
            
            if (!fcmToken) {
                console.log('Destinatario no tiene FCM token');
                return null;
            }
            
            // Crear payload de notificación
            const payload = {
                notification: {
                    title: message.senderName,
                    body: message.message,
                    sound: 'default'
                },
                data: {
                    type: 'chat_message',
                    senderId: message.senderId,
                    senderName: message.senderName,
                    message: message.message,
                    chatId: chatId
                },
                token: fcmToken
            };
            
            // Enviar notificación
            const response = await messaging.send(payload);
            console.log('✅ Notificación enviada:', response);
            
            return response;
            
        } catch (error) {
            console.error('❌ Error enviando notificación:', error);
            return null;
        }
    });

// =====================================================
// 1. API REST (Express) - SERVICIOS Y HARDWARE CHECK
// =====================================================
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

/**
 * HARDWARE CHECK: Verifica el estado del servidor
 * GET /api/hardware-check
 */
app.get("/hardware-check", async (req, res) => {
    const memory = process.memoryUsage();
    
    const status = {
        status: "OPERATIONAL",
        timestamp: new Date().toISOString(),
        server: {
            uptime: process.uptime(),
            memory_rss: `${Math.round(memory.rss / 1024 / 1024)} MB`,
            memory_heap_used: `${Math.round(memory.heapUsed / 1024 / 1024)} MB`,
            node_version: process.version,
            platform: process.platform
        },
        database: "Connected"
    };

    try {
        // Verificar conexión a Firestore
        await db.collection("users").limit(1).get();
        res.status(200).json(status);
    } catch (error) {
        status.status = "ERROR";
        status.database = "Disconnected";
        status.error = error.message;
        res.status(503).json(status);
    }
});

/**
 * OBTENER UBICACIÓN DE UN USUARIO (REST)
 * GET /api/locations/:userId
 */
app.get("/locations/:userId", async (req, res) => {
    try {
        const { userId } = req.params;
        const doc = await db.collection("locations").doc(userId).get();
        
        if (!doc.exists) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }
        
        res.json(doc.data());
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * OBTENER TODAS LAS UBICACIONES DE AMIGOS
 * GET /api/friends/:userId/locations
 */
app.get("/friends/:userId/locations", async (req, res) => {
    try {
        const { userId } = req.params;
        
        // 1. Obtener lista de amigos
        const friendsSnapshot = await db.collection("users")
            .doc(userId)
            .collection("friends")
            .get();

        if (friendsSnapshot.empty) {
            return res.json([]);
        }

        // 2. Obtener ubicaciones de cada amigo
        const locationPromises = friendsSnapshot.docs.map(async (friendDoc) => {
            const friendId = friendDoc.id;
            const locationDoc = await db.collection("locations").doc(friendId).get();
            const userDoc = await db.collection("users").doc(friendId).get();
            
            if (locationDoc.exists && userDoc.exists) {
                return {
                    userId: friendId,
                    userName: userDoc.data().nombre,
                    isOnline: userDoc.data().isOnline,
                    ...locationDoc.data()
                };
            }
            return null;
        });

        const locations = (await Promise.all(locationPromises)).filter(loc => loc !== null);
        res.json(locations);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * AGREGAR AMIGO (REST)
 * POST /api/friends
 * Body: { userId, friendEmail }
 */
app.post("/friends", async (req, res) => {
    try {
        const { userId, friendEmail } = req.body;

        if (!userId || !friendEmail) {
            return res.status(400).json({ error: "Faltan parámetros" });
        }

        // Buscar al amigo por email
        const usersSnapshot = await db.collection("users")
            .where("email", "==", friendEmail)
            .limit(1)
            .get();

        if (usersSnapshot.empty) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        const friendDoc = usersSnapshot.docs[0];
        const friendId = friendDoc.id;
        const friendData = friendDoc.data();

        // Crear relación de amistad
        const friendship = {
            userId: userId,
            friendId: friendId,
            friendName: friendData.nombre,
            status: "accepted",
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        };

        await db.collection("users")
            .doc(userId)
            .collection("friends")
            .doc(friendId)
            .set(friendship);

        res.json({ success: true, message: `Amigo agregado: ${friendData.nombre}` });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Exponer la app de Express como una Cloud Function
exports.api = functions.https.onRequest(app);

// =====================================================
// 2. NOTIFICACIONES (AMIGO CONECTADO)
// =====================================================

/**
 * Trigger que se activa cuando un usuario cambia su estado
 * Envía notificación a los amigos cuando alguien se conecta
 */
exports.onUserStatusChange = functions.firestore
    .document("users/{userId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();
        const userId = context.params.userId;

        // Detectar cambio de Offline a Online
        if (!oldData.isOnline && newData.isOnline) {
            const userName = newData.nombre || "Un amigo";
            console.log(`✅ ${userName} ahora está ONLINE`);

            try {
                // 1. Buscar amigos que tengan a este usuario
                const friendsQuery = await db.collectionGroup("friends")
                    .where("friendId", "==", userId)
                    .get();

                if (friendsQuery.empty) {
                    console.log("Este usuario no tiene amigos que lo hayan agregado");
                    return null;
                }

                // 2. Enviar notificación push a cada amigo
                const notificationPromises = friendsQuery.docs.map(async (friendDoc) => {
                    const friendUserId = friendDoc.ref.parent.parent.id;
                    
                    // Obtener el FCM token del amigo
                    const friendUserDoc = await db.collection("users").doc(friendUserId).get();
                    const fcmToken = friendUserDoc.data()?.fcmToken;

                    if (fcmToken) {
                        const message = {
                            notification: {
                                title: "¡Amigo Conectado!",
                                body: `${userName} acaba de conectarse a Spottivo.`,
                            },
                            data: {
                                type: "friend_online",
                                userId: userId,
                                userName: userName
                            },
                            token: fcmToken
                        };

                        try {
                            await messaging.send(message);
                            console.log(`Notificación enviada a ${friendUserId}`);
                        } catch (error) {
                            console.error(`Error enviando notificación a ${friendUserId}:`, error);
                        }
                    }

                    // También guardar notificación en Firestore
                    return db.collection("users")
                        .doc(friendUserId)
                        .collection("notifications")
                        .add({
                            title: "¡Amigo Conectado!",
                            body: `${userName} acaba de conectarse.`,
                            timestamp: admin.firestore.FieldValue.serverTimestamp(),
                            read: false,
                            type: "friend_online",
                            fromUserId: userId
                        });
                });

                await Promise.all(notificationPromises);
                console.log(`Notificaciones enviadas para ${userName}`);
                return null;

            } catch (error) {
                console.error("Error enviando notificaciones:", error);
                return null;
            }
        }
        return null;
    });

// =====================================================
// 3. LIMPIEZA DE UBICACIONES ANTIGUAS (Opcional)
// =====================================================

/**
 * Función programada que limpia ubicaciones con más de 24 horas
 * Se ejecuta cada hora
 */
exports.cleanOldLocations = functions.pubsub
    .schedule("every 1 hours")
    .onRun(async (context) => {
        const twentyFourHoursAgo = Date.now() - (24 * 60 * 60 * 1000);
        
        const locationsSnapshot = await db.collection("locations")
            .where("lastUpdate", "<", twentyFourHoursAgo)
            .get();

        const deletePromises = locationsSnapshot.docs.map(doc => doc.ref.delete());
        await Promise.all(deletePromises);

        console.log(`Limpiadas ${locationsSnapshot.size} ubicaciones antiguas`);
        return null;
    });

// =====================================================
// 4. TRIGGER PARA ACTUALIZAR USUARIOS OFFLINE
// =====================================================

/**
 * Si un usuario no actualiza su ubicación en 15 minutos, marcarlo como offline
 */
exports.markInactiveUsersOffline = functions.pubsub
    .schedule("every 5 minutes")
    .onRun(async (context) => {
        const fifteenMinutesAgo = Date.now() - (15 * 60 * 1000);
        
        const inactiveLocations = await db.collection("locations")
            .where("lastUpdate", "<", fifteenMinutesAgo)
            .get();

        const updatePromises = inactiveLocations.docs.map(async (doc) => {
            const userId = doc.id;
            const userDoc = await db.collection("users").doc(userId).get();
            
            if (userDoc.exists && userDoc.data().isOnline === true) {
                console.log(`Marcando a ${userId} como offline por inactividad`);
                return db.collection("users").doc(userId).update({
                    isOnline: false,
                    lastSeen: admin.firestore.FieldValue.serverTimestamp()
                });
            }
            return null;
        });

        await Promise.all(updatePromises);
        console.log(`Procesados ${inactiveLocations.size} usuarios inactivos`);
        return null;
    });
