package org.vaulture.project.domain.model


import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Story @OptIn(ExperimentalSerializationApi::class) constructor(
    val storyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String? = null,
    val contentType: ContentType = ContentType.PHOTO,
    val contentUrl: String? = null,
    val thumbnailUrl: String? = null,
    val textContent: String? = null,
    val duration: Int = 5,

   val timestamp: Timestamp? = null,
    val expiryTimestamp: Timestamp? = null,
    val isViewed: Boolean = false,
    val viewCount: Int = 0,
    val musicTrack: String? = null,
    val aspectRatio: Float = 1.0f,
    val privacySetting: PrivacySetting = PrivacySetting.PUBLIC,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList(),
    val linkUrl: String? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val bookmarkedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val isFeed: Boolean = false,
    val isLiked: Boolean = false
) {
    enum class ContentType {
        PHOTO, VIDEO, TEXT, AUDIO, GIF
    }

    enum class PrivacySetting {
        PUBLIC, FRIENDS, CUSTOM
    }

    @Serializable
    data class Comment @OptIn(ExperimentalSerializationApi::class) constructor(
        val commentId: String = "",
        val userId: String = "",
        val storyId: String = "",
        val userName: String = "",
        val userProfileUrl: String? = null,
        val text: String = "",
        val timestamp: Timestamp? = null
    )


}
