package com.example.spottivo.ui.models

data class CommunityMember(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val profileImageUrl: String,
    val isOnline: Boolean = false
)