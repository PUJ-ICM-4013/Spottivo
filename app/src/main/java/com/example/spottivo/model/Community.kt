package com.example.spottivo.model

data class Community(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var photoUrl: String = "",
    var creatorId: String = "",
    var creatorName: String = "",
    var createdAt: Long = 0,
    var memberCount: Int = 0,
    var isPublic: Boolean = true // Las comunidades pueden ser públicas o privadas
)

data class CommunityMember(
    var userId: String = "",
    var nombre: String = "",
    var email: String = "",
    var photoUrl: String = "",
    var joinedAt: Long = 0,
    var role: MemberRole = MemberRole.MEMBER
)

enum class MemberRole {
    CREATOR,    // Creador de la comunidad (todos los permisos)
    ADMIN,      // Administrador (puede moderar, añadir/quitar miembros)
    MEMBER      // Miembro regular (solo puede chatear)
}

data class GroupMessage(
    var id: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var senderPhotoUrl: String = "",
    var message: String = "",
    var timestamp: Long = 0,
    var readBy: List<String> = emptyList() // IDs de usuarios que leyeron el mensaje
)
