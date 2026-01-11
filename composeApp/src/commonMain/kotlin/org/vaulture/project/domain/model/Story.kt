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

    val timestamp: Timestamp? = null,
    val viewCount: Int = 0,
    val aspectRatio: Float = 1.0f,
    val visibility: Visibility = Visibility.PUBLIC,
    val authorizedViewers: List<String> = emptyList(),
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList(),
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

    enum class Visibility { PUBLIC, CONNECTS_ONLY, PRIVATE }

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
