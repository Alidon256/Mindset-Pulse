package org.vaulture.project.domain.model

import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.TimestampSerializer
import kotlinx.serialization.Serializable

data class Member(
    val id: String,
    val name: String,
    val avatarUrl: String
)
@Serializable
data class Space(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val atmosphere: String = "Neutral",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp? = null,
    val memberPhotoUrls: List<String> = emptyList(),
    val unreadCount: Int = 0,
    val aiModerationEnabled: Boolean = true, // Pillar 2: Safety
    val initialPulse: String = "", // Pillar 4: Community Vitality
)

data class Message(
    val id: String,
    val author: Member,
    val content: String,
    val timestamp: String,
    val isFromCurrentUser: Boolean // Simplifies UI alignment
)

@Serializable
data class SpaceMessage(
    val id: String = "",
    val spaceId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",

    // --- GOOD PRACTICE: Apply the same fix here ---
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Timestamp? = null
)