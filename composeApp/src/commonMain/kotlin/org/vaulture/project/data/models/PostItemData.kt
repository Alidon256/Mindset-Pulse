package org.vaulture.project.data.models

/**
 * Represents a post item in the home feed, encapsulating all key attributes and features of a social media post.
 * This class models a single post with its content, metadata, and interaction statistics.
 *
 * @property id Unique identifier for the post, typically a UUID or platform-generated string.
 * @property userName Name of the user who posted the content.
 * @property userImage URL of the user's profile image.
 * @property content Text content of the post.
 * @property imageUrl Optional URL of an image associated with the post.
 * @property timestamp Time when the post was created (in milliseconds since epoch).
 * @property likes Total number of likes on the post.
 * @property comments Total number of comments on the post.
 */
data class PostItemData(
    val id: String,
    val userName: String,
    val userImage: String,
    val content: String,
    val imageUrl: String?,
    val timestamp: Long,
    val isLikedByCurrentUser: Boolean = false,
    val likes: Int,
    val comments: Int
)
