package com.example.spottivo.model.social

data class Friendship(
    val userId: String = "",
    val friendId: String = "",
    val friendName: String = "",
    val status: String = "accepted", // pending, accepted, blocked
    val createdAt: Long = System.currentTimeMillis()
)

data class FriendLocation(
    val userId: String = "",
    val userName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val lastUpdate: Long = 0,
    val isOnline: Boolean = false
)
