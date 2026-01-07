package org.vaulture.project.domain.model

import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.TimestampSerializer
import kotlinx.serialization.Serializable

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
    val aiModerationEnabled: Boolean = true,
    val initialPulse: String = "",
)

@Serializable
data class SpaceMessage(
    val id: String = "",
    val spaceId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Timestamp? = null
)