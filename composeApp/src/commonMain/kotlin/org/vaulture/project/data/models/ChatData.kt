package org.vaulture.project.data.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
@Serializable
data class ChatParticipant(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = ""
)

/**
 * Represents a single conversation.
 * This model is scalable and supports both 1-on-1 and group chats.
 */
@Immutable
@Serializable
data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participants: List<ChatParticipant> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val unreadCount: Int = 0 // Note: For a production app, this should be a map per user.
) {
    /**
     * A composable helper function to conveniently find the other participant in a 1-on-1 chat.
     */
    @Composable
    fun otherParticipant(): ChatParticipant? {
        val currentUid = Firebase.auth.currentUser?.uid
        return participants.find { it.id != currentUid }
    }
}

/**
 * Represents a single message within a chat.
 */
@Immutable
@Serializable
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val authorId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val authorAvatarUrl: String = "",
    @Transient val isFromMe: Boolean = false
)

/**
 * Helper to determine if a message was sent by the currently logged-in user.
 */
fun ChatMessage.isFromMe(currentUserId: String): Boolean {
    return authorId == currentUserId
}
