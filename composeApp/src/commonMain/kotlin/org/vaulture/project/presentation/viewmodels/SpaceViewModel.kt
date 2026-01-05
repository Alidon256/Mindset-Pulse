package org.vaulture.project.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vaulture.project.domain.model.User
import org.vaulture.project.presentation.utils.upload
import org.vaulture.project.domain.model.Space
import org.vaulture.project.domain.model.SpaceMessage
import org.vaulture.project.domain.model.Story
import kotlin.text.lowercase

data class SpaceUiState(
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

open class SpaceViewModel(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    val auth: FirebaseAuth
) : ViewModel() {

    private var feedsListenerJob: Job? = null
    private var spacesListenerJob: Job? = null
    private var postsListenerJob: Job? = null

    private val _uiState = MutableStateFlow(SpaceUiState(isLoading = true))
    val uiState: StateFlow<SpaceUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    // --- State for Memories Feed ---
    private val _feeds = MutableStateFlow<List<Story>>(emptyList())
    val feeds: StateFlow<List<Story>> = _feeds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- State Properties for the "Spaces" Feature ---
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces.asStateFlow()
    private val _isLoadingSpaces = MutableStateFlow(true)
    val isLoadingSpaces: StateFlow<Boolean> = _isLoadingSpaces.asStateFlow()

    val spaceName = mutableStateOf("")
    val spaceDescription = mutableStateOf("")
    private val _isCreatingSpace = MutableStateFlow(false)
    val isCreatingSpace: StateFlow<Boolean> = _isCreatingSpace.asStateFlow()
    val initialPulse = mutableStateOf("")
    val selectedAtmosphere = mutableStateOf("Rain")

    private val _currentSpace = MutableStateFlow<Space?>(null)
    val currentSpace: StateFlow<Space?> = _currentSpace.asStateFlow()

    private val _spacePosts = MutableStateFlow<List<Story>>(emptyList())
    val spacePosts: StateFlow<List<Story>> = _spacePosts.asStateFlow()

    private val _spaceMessages = MutableStateFlow<List<SpaceMessage>>(emptyList())
    val spaceMessages: StateFlow<List<SpaceMessage>> = _spaceMessages.asStateFlow()
    val newMessageText = mutableStateOf("")

    val isLoadingPosts = MutableStateFlow(false)
    private var chatListenerJob: Job? = null

    private val _filteredSpaces = MutableStateFlow<List<Space>>(emptyList())
    val filteredSpaces: StateFlow<List<Space>> = _filteredSpaces.asStateFlow()

    private val _filteredFeeds = MutableStateFlow<List<Story>>(emptyList())
    val filteredFeeds: StateFlow<List<Story>> = _filteredFeeds.asStateFlow()

    // 1. Add a searching state to the ViewModel
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val userPhotoCache = mutableMapOf<String, String>()

    // This will hold the ID of the space currently being viewed in the detail pane.
    private val _activeSpaceId = MutableStateFlow<String?>(null)
    val activeSpaceId = _activeSpaceId.asStateFlow()

    init {
        viewModelScope.launch {
            auth.authStateChanged.collect { user ->
                if (user != null) {
                    loadInitialData()
                    listenForFeeds()
                    fetchSpaces()
                } else {
                    // CRITICAL: Stop everything immediately on sign-out
                    feedsListenerJob?.cancel()
                    spacesListenerJob?.cancel()
                    _feeds.value = emptyList()
                    _spaces.value = emptyList()
                    _filteredFeeds.value = emptyList()
                    _filteredSpaces.value = emptyList()
                }
            }
        }
    }

/**
 * HYDRATION ENGINE:
 * Converts raw Member IDs into actual Profile Photo URLs.
 */
    private suspend fun hydrateSpacePhotos(memberIds: List<String>): List<String> {
        val topMemberIds = memberIds.take(5)
        val photos = mutableListOf<String>()

        topMemberIds.forEach { uid ->
            if (userPhotoCache.containsKey(uid)) {
                userPhotoCache[uid]?.let { photos.add(it) }
            } else {
                try {
                    // Fetch the user document to get their photoUrl
                    val userDoc = firestore.collection("users").document(uid).get()
                    val photoUrl = userDoc.data<User>().photoUrl ?: ""
                    if (photoUrl.isNotEmpty()) {
                        userPhotoCache[uid] = photoUrl
                        photos.add(photoUrl)
                    }
                } catch (e: Exception) {
                    // Fallback for failed fetches
                }
            }
        }
        return photos
   }

    private fun fetchSpaces() {
        spacesListenerJob?.cancel()
        spacesListenerJob = viewModelScope.launch {
            firestore.collection("spaces").snapshots().collect { snapshot ->
                val spaceList = snapshot.documents.map { doc ->
                    val baseSpace = doc.data<Space>().copy(id = doc.id)

                    // --- HYDRATION STEP ---
                    // We fetch the photos for the first 5 members
                    val photos = mutableListOf<String>()
                    baseSpace.memberIds.take(5).forEach { memberId ->
                        val cached = userPhotoCache[memberId]
                        if (cached != null) {
                            photos.add(cached)
                        } else {
                            try {
                                // Look up the member's photo in the users collection
                                val userDoc = firestore.collection("users").document(memberId).get()
                                val photo = userDoc.data<User>().photoUrl ?: ""
                                if (photo.isNotEmpty()) {
                                    userPhotoCache[memberId] = photo
                                    photos.add(photo)
                                }
                            } catch (e: Exception) { /* Handle hidden profiles */ }
                        }
                    }

                    baseSpace.copy(memberPhotoUrls = photos)
                }
                _filteredSpaces.value = spaceList
                _isLoadingSpaces.value = false
            }
        }
    }

    /**
     * Subscribes to the 'stories' collection.
     * Includes extensive error logging to identify why parsing fails in the UI.
     */
    private fun listenForFeeds() {
        feedsListenerJob?.cancel() // Clear existing
        feedsListenerJob = viewModelScope.launch {
            try {
                firestore.collection("stories")
                    .where { "isFeed" equalTo true }
                    .orderBy("timestamp", Direction.DESCENDING)
                    .snapshots
                    .collect { snapshot ->
                        val storiesList = snapshot.documents.mapNotNull { doc ->
                            try { doc.data<Story>().copy(storyId = doc.id) } catch (e: Exception) { null }
                        }
                        _feeds.value = storiesList
                        if (!_isSearching.value) _filteredFeeds.value = storiesList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                println("🛑 SpaceVM: Feeds Listener Blocked (Safe during sign-out)")
            }
        }
    }


    fun addStory(
        mediaContent: ByteArray?,
        thumbnailContent: ByteArray?,
        textContent: String,
        contentType: Story.ContentType,
        isFeed: Boolean,
        aspectRatio: Float,
        onProgress: (String) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("User not authenticated.")
                return@launch
            }

            onProgress("Preparing content...")
            // Ensure unique ID prevents collisions
            val storyId =
                "story_${currentUser.uid}_${Timestamp.now().seconds}_${(0..1000).random()}"
            var finalContentUrl: String? = null
            var finalThumbnailUrl: String? = null

            try {
                if (mediaContent != null) {
                    val mediaFileName =
                        "${contentType.name.lowercase()}_${Timestamp.now().nanoseconds}"
                    val mediaStoragePath = "stories/${currentUser.uid}/$storyId/$mediaFileName"
                    onProgress("Uploading ${contentType.name.lowercase()}...")
                    finalContentUrl = uploadFileToStorage(mediaContent, mediaStoragePath)
                }

                if (thumbnailContent != null) {
                    onProgress("Uploading thumbnail...")
                    val thumbnailFileName = "thumbnail_${Timestamp.now().nanoseconds}"
                    val thumbnailStoragePath =
                        "stories/${currentUser.uid}/$storyId/thumbnails/$thumbnailFileName"
                    finalThumbnailUrl = uploadFileToStorage(thumbnailContent, thumbnailStoragePath)
                }

                onProgress("Saving details...")

                // Using current server timestamp
                val newStory = Story(
                    storyId = storyId,
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Anonymous",
                    userProfileUrl = currentUser.photoURL,
                    contentType = contentType,
                    contentUrl = finalContentUrl,
                    thumbnailUrl = finalThumbnailUrl,
                    textContent = textContent,
                    aspectRatio = aspectRatio,
                    isFeed = isFeed,
                    timestamp = Timestamp.now()
                )

                // Using Story.toMap if your Story class has a companion object helper,
                // otherwise passing the object directly usually works with the gitlive library
                // assuming @Serializable is present on the data class.
                firestore.collection("stories").document(storyId).set(newStory)

                onProgress("Post created successfully!")
                onSuccess()

            } catch (e: Exception) {
                println("Error adding story: ${e.message}")
                e.printStackTrace()
                onError(e.message ?: "An unknown error occurred while adding the story.")
            }
        }
    }

    fun createSpace(
        coverImageBytes: ByteArray?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingSpace.value = true
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("User not authenticated.")
                _isCreatingSpace.value = false
                return@launch
            }

            try {
                val spaceId = "space_${currentUser.uid}_${Timestamp.now().seconds}"
                var coverImageUrl = ""
                if (coverImageBytes != null) {
                    val storagePath = "space_covers/$spaceId/cover.jpg"
                    coverImageUrl = uploadFileToStorage(coverImageBytes, storagePath)
                }

                val spaceData = mapOf(
                    "id" to spaceId,
                    "name" to spaceName.value,
                    "description" to spaceDescription.value,
                    "ownerId" to currentUser.uid,
                    "memberIds" to listOf(currentUser.uid),
                    "atmosphere" to selectedAtmosphere.value,
                    "coverImageUrl" to coverImageUrl,
                    "createdAt" to FieldValue.serverTimestamp,
                    "initialPulse" to initialPulse.value
                )

                firestore.collection("spaces").document(spaceId).set(spaceData)
                onSuccess(spaceId)

            } catch (e: Exception) {
                onError("Failed to create space: ${e.message}")
            } finally {
                _isCreatingSpace.value = false
                spaceName.value = ""
                spaceDescription.value = ""
            }
        }
    }

    private suspend fun uploadFileToStorage(
        fileBytes: ByteArray,
        storagePath: String
    ): String {
        val storageRef = storage.reference(storagePath)
        storageRef.upload(fileBytes) // Attempting generic putData
        return storageRef.getDownloadUrl()
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val lowercaseQuery = query.lowercase().trim()

            if (lowercaseQuery.isBlank()) {
                // Reset to full lists immediately
                _filteredSpaces.value = _spaces.value
                _filteredFeeds.value = _feeds.value
            } else {
                // Filter locally for instant response
                _filteredFeeds.value = _feeds.value.filter {
                    it.textContent?.lowercase()?.contains(lowercaseQuery) == true ||
                            it.userName.lowercase().contains(lowercaseQuery)
                }

                _filteredSpaces.value = _spaces.value.filter {
                    it.name.lowercase().contains(lowercaseQuery) ||
                            it.description.lowercase().contains(lowercaseQuery)
                }
            }
            _isSearching.value = false
        }
    }


    fun loadInitialData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                userName = currentUser.displayName ?: "User",
                userAvatarUrl = currentUser.photoURL,
                isLoading = false
            )
        }
    }

    fun loadSpaceDetails(spaceId: String) {
        _activeSpaceId.value = spaceId
        viewModelScope.launch {
            try {
                val doc = firestore.collection("spaces").document(spaceId).get()
                _currentSpace.value = doc.data()
                loadSpacePosts(spaceId)
                listenForChatMessages(spaceId)
            } catch (e: Exception) {
                _currentSpace.value = null
            }
        }
    }

    /*private fun loadSpacePosts(spaceId: String) {
        viewModelScope.launch {
            isLoadingPosts.value = true
            try {
                val snapshot = firestore.collection("spaces").document(spaceId)
                    .collection("posts")
                    .orderBy("timestamp", Direction.DESCENDING)
                    .get()
                _spacePosts.value = snapshot.documents.mapNotNull { it.data<Story>() }
            } catch (e: Exception) {
                println("Error loading posts: ${e.message}")
                _spacePosts.value = emptyList()
            } finally {
                isLoadingPosts.value = false
            }
        }
    }*/

    private fun loadSpacePosts(spaceId: String) {
        // Cancel any previous listener to avoid multiple streams on the same screen.
        postsListenerJob?.cancel()

        // Use .snapshots() to get a real-time Flow of updates.
        postsListenerJob = firestore.collection("spaces").document(spaceId)
            .collection("posts")
            .orderBy("timestamp", Direction.DESCENDING)
            .snapshots() // This returns a Flow that emits new data whenever the collection changes
            .onEach { snapshot ->
                // This block runs every time a post is liked, commented on, or created.
                val updatedPosts = snapshot.documents.mapNotNull { it.data<Story>() }
                _spacePosts.value = updatedPosts
                isLoadingPosts.value = false // Set loading to false on the first successful update
            }
            .catch { e ->
                // If the listener fails (e.g., permissions), log the error.
                println("Error listening for space posts: ${e.message}")
                _spacePosts.value = emptyList()
                isLoadingPosts.value = false
            }
            .launchIn(viewModelScope) // Launch the listener in the ViewModel's scope.
    }

    fun listenForChatMessages(spaceId: String) {
        chatListenerJob?.cancel()
        chatListenerJob = firestore.collection("spaces").document(spaceId)
            .collection("messages")
            .orderBy("timestamp", Direction.ASCENDING)
            .snapshots
            .onEach { snapshot ->
                _spaceMessages.value = snapshot.documents.mapNotNull { it.data<SpaceMessage>() }
            }
            .catch { e -> println("Error listening for chat messages: ${e.message}") }
            .launchIn(viewModelScope)
    }


    fun clearSpaceDetails() {
        chatListenerJob?.cancel()
        postsListenerJob?.cancel()
        _activeSpaceId.value = null
        _currentSpace.value = null
        _spacePosts.value = emptyList()
        _spaceMessages.value = emptyList()
    }

    fun createPost(spaceId: String, content: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val storyId = "post_${currentUser.uid}_${Timestamp.now().seconds}"

            // Reconstruct the Story object properly for a text-only post in a space
            val newPost = Story(
                storyId = storyId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userProfileUrl = currentUser.photoURL,
                textContent = content,
                contentType = Story.ContentType.TEXT, // Assuming TEXT enum exists
                contentUrl = null,
                thumbnailUrl = null,
                aspectRatio = 1.0f,
                isFeed = false, // Space posts might not be in the main feed
                timestamp = Timestamp.now()
            )

            try {
                firestore.collection("spaces").document(spaceId)
                    .collection("posts").document(storyId)
                    .set(newPost)

                loadSpacePosts(spaceId)

            } catch (e: Exception) {
                println("Error creating post: ${e.message}")
            }
        }
    }

    fun sendChatMessage(spaceId: String) {
        val text = newMessageText.value
        if (text.isBlank()) return

        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val messageId = "msg_${currentUser.uid}_${Timestamp.now().seconds}"

            val message = SpaceMessage(
                id = messageId,
                spaceId = spaceId,
                authorId = currentUser.uid,
                authorName = currentUser.displayName ?: "User",
                authorAvatarUrl = currentUser.photoURL ?: "",
                text = text,
                timestamp = Timestamp.now()
            )
            try {
                firestore.collection("spaces").document(spaceId)
                    .collection("messages").document(messageId)
                    .set(message)
                newMessageText.value = ""
            } catch (e: Exception) {
                println("Error sending message: ${e.message}")
            }
        }
    }

    /**
     * MEMBERSHIP ENGINE:
     * Production-grade logic to join a safe space.
     * Uses NonCancellable to ensure the database write completes even if
     * the user navigates to the chat screen immediately.
     */
    fun joinSpace(spaceId: String) {
        // Log at the very first entry point to verify the UI call
        println("🖱️ [SpaceVM] joinSpace CALLED for ID: $spaceId")

        viewModelScope.launch {
            // 1. Force the logic to run even if the UI scope is destroyed by navigation
            withContext(NonCancellable) {
                try {
                    // 2. Refresh Auth state inside the background thread
                    val user = auth.currentUser

                    if (user == null) {
                        println("🛑 [SpaceVM] CRITICAL: Join attempted without Auth.")
                        return@withContext
                    }

                    println("📡 [SpaceVM] DB_START: Attempting join for Space: $spaceId (User: ${user.uid})")

                    val spaceRef = firestore.collection("spaces").document(spaceId)

                    // 3. ATOMIC ARRAY UNION: Standard production way to manage members
                    // This prevents duplicates and ensures data integrity.
                    spaceRef.update("memberIds" to FieldValue.arrayUnion(user.uid))

                    println("✅ [SpaceVM] DB_SUCCESS: User ${user.uid} is now a member of $spaceId")

                } catch (e: Exception) {
                    println("❌ [SpaceVM] DB_FATAL: Join failed for $spaceId - ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }


    fun getCommentsForStory(storyId: String): Flow<List<Story.Comment>> {
        return firestore.collection("stories").document(storyId)
            .collection("comments")
            .orderBy("timestamp", Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<Story.Comment>() }
            }
    }

    fun toggleLike(story: Story) {
        val uid = auth.currentUser?.uid ?: return;
        val storyRef = firestore.collection("stories").document(story.storyId)
        val isLiked = story.likedBy.contains(uid)

        viewModelScope.launch {
            try {
                if (isLiked) {
                    // Remove like
                    storyRef.update(
                        "likeCount" to story.likeCount - 1,
                        "likedBy" to story.likedBy.filter { it != uid }
                    )
                } else {
                    // Add like
                    storyRef.update(
                        "likeCount" to story.likeCount + 1,
                        "likedBy" to story.likedBy + uid
                    )
                }
            } catch (e: Exception) {
                println("Like Error: ${e.message}")
            }
        }
    }

    /**
     * ADD COMMENT LOGIC
     */
    fun addComment(storyId: String, text: String) {
        val user = auth.currentUser ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val commentId = "comm_${user.uid}_${Timestamp.now().seconds}"
                val newComment = Story.Comment(
                    commentId = commentId,
                    storyId = storyId,
                    userId = user.uid,
                    userName = user.displayName ?: "User",
                    userProfileUrl = user.photoURL,
                    text = text,
                    timestamp = Timestamp.now()
                )

                // 1. Add the comment to a sub-collection
                firestore.collection("stories").document(storyId)
                    .collection("comments").document(commentId).set(newComment)

                // 2. Increment comment count on the main story
                firestore.collection("stories").document(storyId).update(
                    "commentCount" to FieldValue.increment(1)
                )
            } catch (e: Exception) {
                println("Comment Error: ${e.message}")
            }
        }
    }
    fun toggleLikeSpace(story: Story, spaceId: String) {
        val uid = auth.currentUser?.uid ?: return
        val postRef = firestore.collection("spaces").document(spaceId).collection("posts").document(story.storyId)
        val isLiked = story.likedBy.contains(uid)

        viewModelScope.launch {
            try {
                if (isLiked) {
                    // Remove like
                    postRef.update(
                        "likeCount" to story.likeCount - 1,
                        "likedBy" to story.likedBy.filter { it != uid }
                    )
                } else {
                    // Add like
                    postRef.update(
                        "likeCount" to story.likeCount + 1,
                        "likedBy" to story.likedBy + uid
                    )
                }
            } catch (e: Exception) {
                println("Like Error: ${e.message}")
            }
        }
    }

    fun addCommentSpace(spaceId: String, postId: String, text: String) {
        val user = auth.currentUser ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val commentId = "comm_${user.uid}_${Timestamp.now().seconds}"
                val newComment = Story.Comment(
                    commentId = commentId,
                    storyId = postId,
                    userId = user.uid,
                    userName = user.displayName ?: "User",
                    userProfileUrl = user.photoURL,
                    text = text,
                    timestamp = Timestamp.now()
                )

                // 1. Add the comment to a sub-collection
                firestore.collection("spaces").document(spaceId)
                    .collection("posts").document(postId)
                    .collection("comments").document(commentId).set(newComment)

                // 2. Increment comment count on the main post
                firestore.collection("spaces").document(spaceId)
                    .collection("posts").document(postId)
                    .update("commentCount" to FieldValue.increment(1))
            } catch (e: Exception) {
                println("Comment Error: ${e.message}")
            }
        }
    }

    fun toggleBookmarkSpace(story: Story, spaceId: String) {
        val uid = auth.currentUser?.uid ?: return
        val postRef = firestore.collection("spaces").document(spaceId).collection("posts").document(story.storyId)
        val userRef = firestore.collection("users").document(uid).collection("bookmarks").document(story.storyId)
        val isBookmarked = story.bookmarkedBy.contains(uid)

        viewModelScope.launch {
            try {
                if (isBookmarked) {
                    // 1. Remove from global post list
                    postRef.update("bookmarkedBy" to story.bookmarkedBy.filter { it != uid })
                    // 2. Remove from user's private collection
                    userRef.delete()
                } else {
                    // 1. Add to global post list
                    postRef.update("bookmarkedBy" to story.bookmarkedBy + uid)
                    // 2. Add to user's private collection for easy "My Saved" filtering
                    userRef.set(mapOf("savedAt" to Timestamp.now()))
                }
            } catch (e: Exception) {
                println("Bookmark Error: ${e.message}")
            }
        }
    }

    fun getCommentsForPost(spaceId: String, postId: String): Flow<List<Story.Comment>> {
        return firestore.collection("spaces").document(spaceId)
            .collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data<Story.Comment>() }
            }
    }
    /**
     * TOGGLE BOOKMARK LOGIC
     * Saves the story ID to the user's private collection and updates the story document.
     */
    fun toggleBookmark(story: Story) {
        val uid = auth.currentUser?.uid ?: return
        val storyRef = firestore.collection("stories").document(story.storyId)
        val userRef = firestore.collection("users").document(uid).collection("bookmarks").document(story.storyId)
        val isBookmarked = story.bookmarkedBy.contains(uid)

        viewModelScope.launch {
            try {
                if (isBookmarked) {
                    // 1. Remove from global story list
                    storyRef.update("bookmarkedBy" to story.bookmarkedBy.filter { it != uid })
                    // 2. Remove from user's private collection
                    userRef.delete()
                } else {
                    // 1. Add to global story list
                    storyRef.update("bookmarkedBy" to story.bookmarkedBy + uid)
                    // 2. Add to user's private collection for easy "My Saved" filtering
                    userRef.set(mapOf("savedAt" to Timestamp.now()))
                }
            } catch (e: Exception) {
                println("Bookmark Error: ${e.message}")
            }
        }
    }

}
