/*package viewmodels

import auth.User
import base.BaseViewModel
import data.Chat
import data.ChatMessage
import data.ChatRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A single state object for the entire chat feature.
 * This is the modern and recommended approach, inspired by your SpaceViewModel.
 */
data class ChatFeatureUiState(
    val chats: List<Chat> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val currentChatMessages: List<ChatMessage> = emptyList(),
    val selectedChatId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    // A computed property to easily access the selected chat object from the UI.
    val selectedChat: Chat? get() = chats.find { it.id == selectedChatId }
}

class ChatViewModel(private val repository: ChatRepository) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ChatFeatureUiState())
    val uiState: StateFlow<ChatFeatureUiState> = _uiState.asStateFlow()

    private val currentUser = Firebase.auth.currentUser

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Launch concurrent jobs to fetch existing chats and new user suggestions.
            launch {
                repository.getChats()
                    .catch { e -> _uiState.update { it.copy(error = "Failed to load chats: ${e.message}") } }
                    .collect { chats -> _uiState.update { it.copy(chats = chats) } }
            }
            launch {
                try {
                    val suggestions = repository.getSuggestedUsers()
                    _uiState.update { it.copy(suggestedUsers = suggestions) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to load suggestions: ${e.message}") }
                }
            }
            // Mark loading as finished after the initial fetch attempts.
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Selects a chat and triggers loading its messages.
     * This is called from the UI when a user clicks a chat item.
     */
    fun selectChat(chatId: String) {
        // Immediately update the state to reflect the selection and clear old messages.
        _uiState.update { it.copy(selectedChatId = chatId, currentChatMessages = emptyList()) }
        loadMessagesForSelectedChat(chatId)
    }

    private fun loadMessagesForSelectedChat(chatId: String) {
        if (currentUser == null) return

        viewModelScope.launch {
            repository.getMessagesForChat(chatId)
                .catch { e -> _uiState.update { it.copy(error = "Failed to load messages: ${e.message}") } }
                .collect { messages ->
                    _uiState.update { state ->
                        // IMPROVEMENT: Determine 'isFromMe' here in the ViewModel.
                        val processedMessages = messages.map { msg ->
                            msg.copy(isFromMe = msg.authorId == currentUser.uid)
                        }
                        state.copy(currentChatMessages = processedMessages)
                    }
                }
        }
    }

    /**
     * Sends a message in the currently selected chat.
     */
    fun sendMessage(text: String) {
        val chatId = _uiState.value.selectedChatId
        if (chatId == null || text.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                repository.sendMessage(chatId, text.trim())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to send message: ${e.message}") }
            }
        }
    }

    /**
     * Starts a new chat with a suggested user and navigates to it.
     */
    fun startChatWithUser(user: User, onChatStarted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val chatId = repository.startChatWithUser(user)
                // Refresh data to move user from "suggestions" to "chats"
                loadInitialData()
                onChatStarted(chatId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error starting chat: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Clears the selected chat state when navigating back from the detail screen.
     */
    fun clearSelectedChat() {
        _uiState.update { it.copy(selectedChatId = null, currentChatMessages = emptyList()) }
    }
}
*/
package org.vaulture.project.viewmodels

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.vaulture.project.data.models.User
import org.vaulture.project.data.models.Chat
import org.vaulture.project.data.models.ChatMessage
import org.vaulture.project.data.repos.ChatRepository

data class ChatFeatureUiState(
    val chats: List<Chat> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val currentChatMessages: List<ChatMessage> = emptyList(),
    val selectedChatId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedChat: Chat? get() = chats.find { it.id == selectedChatId }
}

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatFeatureUiState())
    val uiState: StateFlow<ChatFeatureUiState> = _uiState.asStateFlow()

    // We get the current user ID dynamically to ensure it's up to date
    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Listen to Chats (Real-time)
            launch {
                repository.getChats()
                    .catch { e ->
                        println("Error loading chats: ${e.message}")
                        _uiState.update { it.copy(error = "Failed to load chats.") }
                    }
                    .collect { chats ->
                        _uiState.update { it.copy(chats = chats) }
                    }
            }

            // 2. Load Suggestions (One-time fetch)
            launch {
                try {
                    val suggestions = repository.getSuggestedUsers()
                    _uiState.update { it.copy(suggestedUsers = suggestions) }
                } catch (e: Exception) {
                    println("Error loading suggestions: ${e.message}")
                    // Don't show error to UI for suggestions, just log it
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectChat(chatId: String) {
        _uiState.update { it.copy(selectedChatId = chatId, currentChatMessages = emptyList()) }
        loadMessagesForSelectedChat(chatId)
    }

    private fun loadMessagesForSelectedChat(chatId: String) {
        val myUid = currentUserId ?: return

        viewModelScope.launch {
            repository.getMessagesForChat(chatId)
                .catch { e ->
                    println("Error loading messages: ${e.message}")
                    _uiState.update { it.copy(error = "Unable to load messages.") }
                }
                .collect { messages ->
                    _uiState.update { state ->
                        // Calculate 'isFromMe' here
                        val processedMessages = messages.map { msg ->
                            msg.copy(isFromMe = msg.authorId == myUid)
                        }
                        state.copy(currentChatMessages = processedMessages)
                    }
                }
        }
    }

    fun sendMessage(text: String) {
        val chatId = _uiState.value.selectedChatId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                repository.sendMessage(chatId, text.trim())
            } catch (e: Exception) {
                println("Error sending message: ${e.message}")
                _uiState.update { it.copy(error = "Failed to send message.") }
            }
        }
    }

    fun startChatWithUser(user: User, onChatStarted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val chatId = repository.startChatWithUser(user)
                // Select and notify
                selectChat(chatId)
                onChatStarted(chatId)
            } catch (e: Exception) {
                println("Start chat error: ${e.message}")
                _uiState.update { it.copy(error = "Could not start chat.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSelectedChat() {
        _uiState.update { it.copy(selectedChatId = null, currentChatMessages = emptyList()) }
    }
}
