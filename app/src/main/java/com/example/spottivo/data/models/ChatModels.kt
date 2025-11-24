package com.example.spottivo.data.models

/**
 * Modelo de mensaje de chat
 */
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

/**
 * Modelo de chat/conversación
 */
data class ChatConversation(
    val chatId: String = "",
    val participantId: String = "",
    val participantName: String = "",
    val participantPhotoUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)
