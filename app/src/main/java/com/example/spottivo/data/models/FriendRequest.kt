package com.example.spottivo.data.models

/**
 * Solicitud de amistad
 */
data class FriendRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val senderPhotoUrl: String = "",
    val receiverId: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val timestamp: Long = 0L
)

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
