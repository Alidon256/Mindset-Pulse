package org.vaulture.project.domain.model


import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
private fun isTimestampAfterNow(timestamp: Timestamp?): Boolean {
    if (timestamp == null) return false
    val now = Timestamp.now()
    // Direct comparison of seconds and nanoseconds is the most reliable cross-platform approach.
    return timestamp.seconds > now.seconds || (timestamp.seconds == now.seconds && timestamp.nanoseconds > now.nanoseconds)
}

@Serializable
data class Story @OptIn(ExperimentalSerializationApi::class) constructor(
    val storyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String? = null,
    val contentType: ContentType = ContentType.PHOTO,
    val contentUrl: String? = null,
    val thumbnailUrl: String? = null,
    val textContent: String? = null, // Used for captions or text-only posts
    val duration: Int = 5,

   val timestamp: Timestamp? = null,
    val expiryTimestamp: Timestamp? = null,

    val isViewed: Boolean = false,
    val viewCount: Int = 0,
    val locationTag: String? = null,
    val musicTrack: String? = null,
    val aspectRatio: Float = 1.0f,
    val privacySetting: PrivacySetting = PrivacySetting.PUBLIC,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList(),
    val linkUrl: String? = null,
    val isBusinessStory: Boolean = false,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val bookmarkedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val shareCount: Int = 0,
    val sharedByUserIds: List<String> = emptyList(),
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
