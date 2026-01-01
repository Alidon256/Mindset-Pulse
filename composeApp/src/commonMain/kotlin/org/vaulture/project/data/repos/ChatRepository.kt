package org.vaulture.project.data.repos

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.vaulture.project.data.models.User // Ensure this points to your User data class
import org.vaulture.project.data.models.Chat
import org.vaulture.project.data.models.ChatMessage
import org.vaulture.project.data.models.ChatParticipant
import kotlin.time.Clock

class ChatRepository {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    /**
     * Fetches chats for the current user.
     * Includes robust error handling to prevent "Something wrong happened" crashes.
     */
    fun getChats(): Flow<List<Chat>> {
        val currentUser = auth.currentUser
        if (currentUser == null) return flow { emit(emptyList()) }

        return firestore.collection("chats")
            .where("participantIds", arrayContains = currentUser.uid)
            .orderBy("lastMessageTimestamp", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        // Safely parse the chat. If one chat is malformed, we skip it instead of crashing.
                        doc.data<Chat>().copy(id = doc.id)
                    } catch (e: Exception) {
                        println("⚠️ Error parsing chat document ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
    }

    /**
     * Fetches all messages for a given chat.
     */
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<ChatMessage>().copy(id = doc.id)
                    } catch (e: Exception) {
                        println("⚠️ Error parsing message in chat $chatId: ${e.message}")
                        null
                    }
                }
            }
    }

    /**
     * Sends a message and updates the chat metadata (last message/timestamp).
     */
    suspend fun sendMessage(chatId: String, text: String) {
        val user = auth.currentUser ?: throw Exception("User must be logged in")
        val timestamp = Clock.System.now().toEpochMilliseconds()

        val message = ChatMessage(
            chatId = chatId,
            authorId = user.uid,
            text = text,
            timestamp = timestamp,
            authorAvatarUrl = user.photoURL ?: ""
        )

        // 1. Add message to subcollection
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(message)

        // 2. Update the parent chat document with the latest snippet
        firestore.collection("chats").document(chatId).update(
            "lastMessage" to text,
            "lastMessageTimestamp" to timestamp
        )
    }

    /**
     * Fetches users for suggestions.
     * Reads from the "users" collection which must be created upon SignUp.
     */
    suspend fun getSuggestedUsers(): List<User> {
        val currentUser = auth.currentUser ?: return emptyList()

        try {
            // Get all users
            // Note: In a large app, you should paginate this or use algolia search.
            val snapshot = firestore.collection("users").get()

            val allUsers = snapshot.documents.mapNotNull {
                try {
                    it.data<User>()
                } catch (e: Exception) {
                    println("Error parsing user ${it.id}: ${e.message}")
                    null
                }
            }

            // Filter out current user
            return allUsers.filter { it.uid != currentUser.uid }

        } catch (e: Exception) {
            println("Error fetching suggested users: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Starts a chat with a user. Reuse existing if found, else create new.
     */
    suspend fun startChatWithUser(otherUser: User): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Not logged in")

        // Consistent ordering ensures we can find the chat regardless of who started it
        val participantIds = listOf(currentUser.uid, otherUser.uid).sorted()

        // Check if chat exists
        val existingQuery = firestore.collection("chats")
            .where("participantIds", equalTo = participantIds)
            .get()

        if (existingQuery.documents.isNotEmpty()) {
            return existingQuery.documents.first().id
        }

        // Create new chat
        val newChat = Chat(
            participantIds = participantIds,
            participants = listOf(
                ChatParticipant(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "Me",
                    avatarUrl = currentUser.photoURL ?: ""
                ),
                ChatParticipant(
                    id = otherUser.uid,
                    name = otherUser.displayName ?: "User",
                    avatarUrl = otherUser.photoUrl ?: ""
                )
            ),
            lastMessageTimestamp = Clock.System.now().toEpochMilliseconds(),
            lastMessage = ""
        )

        val docRef = firestore.collection("chats").add(newChat)
        // Store the ID inside the document for easier mapping later
        docRef.update("id" to docRef.id)

        return docRef.id
    }
}
