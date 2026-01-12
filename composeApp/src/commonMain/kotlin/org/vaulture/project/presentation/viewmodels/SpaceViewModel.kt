package org.vaulture.project.presentation.viewmodels

import androidx.collection.size
import androidx.compose.animation.core.copy
import androidx.compose.foundation.gestures.forEach
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.flow.update
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
enum class ProfileFilter {
    MY_POSTS,
    LIKED,
    BOOKMARKED
}

open class SpaceViewModel(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    val auth: FirebaseAuth
) : ViewModel() {

    private var feedsListenerJob: Job? = null
    private var spacesListenerJob: Job? = null
    private var postsListenerJob: Job? = null
    private var userProfileListenerJob: Job? = null

    private val _uiState = MutableStateFlow(SpaceUiState(isLoading = true))
    val uiState: StateFlow<SpaceUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _feeds = MutableStateFlow<List<Story>>(emptyList())
    val feeds: StateFlow<List<Story>> = _feeds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces.asStateFlow()
    private val _isLoadingSpaces = MutableStateFlow(true)
    val isLoadingSpaces: StateFlow<Boolean> = _isLoadingSpaces.asStateFlow()

    val spaceName = mutableStateOf("")
    val spaceDescription = mutableStateOf("")
    private val _isCreatingSpace = MutableStateFlow(false)
    val isCreatingSpace: StateFlow<Boolean> = _isCreatingSpace.asStateFlow()

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

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val userPhotoCache = mutableMapOf<String, String>()

    private val _activeSpaceId = MutableStateFlow<String?>(null)
    val activeSpaceId = _activeSpaceId.asStateFlow()

    private val _userStories = MutableStateFlow<List<Story>>(emptyList())
    val userStories: StateFlow<List<Story>> = _userStories.asStateFlow()

    private val _userLikedStories = MutableStateFlow<List<Story>>(emptyList())
    val userLikedStories: StateFlow<List<Story>> = _userLikedStories.asStateFlow()

    private val _userBookmarkedStories = MutableStateFlow<List<Story>>(emptyList())
    val userBookmarkedStories: StateFlow<List<Story>> = _userBookmarkedStories.asStateFlow()

    private val _isLoadingProfileData = MutableStateFlow(false)
    val isLoadingProfileData: StateFlow<Boolean> = _isLoadingProfileData.asStateFlow()

    private val _isFollowing = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isFollowing = _isFollowing.asStateFlow()

    private val _targetUserProfile = MutableStateFlow<User?>(null)
    val targetUserProfile = _targetUserProfile.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile = _isUpdatingProfile.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            auth.authStateChanged.collect { user ->
                if (user != null) {
                    listenToUserProfile(user.uid)
                    loadInitialData()
                    listenForFeeds()
                    fetchSpaces()
                } else {
                    feedsListenerJob?.cancel()
                    spacesListenerJob?.cancel()
                    userProfileListenerJob?.cancel()
                    _feeds.value = emptyList()
                    _spaces.value = emptyList()
                    _filteredFeeds.value = emptyList()
                    _filteredSpaces.value = emptyList()
                }
            }
        }
    }
    fun listenToUserProfile(uid: String) {
        userProfileListenerJob?.cancel()
        userProfileListenerJob = firestore.collection("users").document(uid)
            .snapshots
            .onEach { documentSnapshot ->
                _userProfile.value = documentSnapshot.data()
                println("[PROFILE_SYNC] Real-time user profile updated: ${documentSnapshot.data<User>().username}")
            }
            .catch { e ->
                println("Error listening to user profile: ${e.message}")
                _error.value = "Failed to load user profile."
            }
            .launchIn(viewModelScope)
    }
    fun loadProfileData(filter: ProfileFilter) {
        val uid = auth.currentUser?.uid ?: return
        _isLoadingProfileData.value = true

        viewModelScope.launch {
            try {
                when (filter) {
                    ProfileFilter.MY_POSTS -> fetchUserStories(uid)
                    ProfileFilter.LIKED -> fetchLikedStories(uid)
                    ProfileFilter.BOOKMARKED -> fetchBookmarkedStories(uid)
                }
            } catch (e: Exception) {
                println(" Error loading profile data for filter '$filter': ${e.message}")
            } finally {
                _isLoadingProfileData.value = false
            }
        }
    }
    private suspend fun fetchUserStories(uid: String) {
        val snapshot = firestore.collection("stories")
            .where { "userId" equalTo uid }
            .orderBy("timestamp", Direction.DESCENDING)
            .get()
        _userStories.value = snapshot.documents.mapNotNull { it.data<Story>() }
    }
    private suspend fun fetchLikedStories(uid: String) {
        val snapshot = firestore.collection("stories")
            .where { "likedBy" contains uid }
            .orderBy("timestamp", Direction.DESCENDING)
            .get()
        _userLikedStories.value = snapshot.documents.mapNotNull { it.data<Story>() }
    }
    private suspend fun fetchBookmarkedStories(uid: String) {
        val bookmarkDocs = firestore.collection("users").document(uid)
            .collection("bookmarks").get().documents

        val storyIds = bookmarkDocs.map { it.id }

        if (storyIds.isNotEmpty()) {
            val snapshot = firestore.collection("stories")
                .where { FieldPath.documentId inArray storyIds }
                .get()

            _userBookmarkedStories.value = snapshot.documents
                .mapNotNull { try { it.data<Story>() } catch (e: Exception) { null } }
                .sortedByDescending { it.timestamp?.seconds }
        } else {
            _userBookmarkedStories.value = emptyList()
        }
    }
    private fun fetchSpaces() {
        spacesListenerJob?.cancel()
        spacesListenerJob = viewModelScope.launch {
            firestore.collection("spaces").snapshots().collect { snapshot ->
                val spaceList = snapshot.documents.map { doc ->
                    val baseSpace = doc.data<Space>().copy(id = doc.id)

                    val photos = mutableListOf<String>()
                    baseSpace.memberIds.take(5).forEach { memberId ->
                        val cached = userPhotoCache[memberId]
                        if (cached != null) {
                            photos.add(cached)
                        } else {
                            try {
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
    fun addStory(
        mediaContent: ByteArray?,
        thumbnailContent: ByteArray?,
        textContent: String,
        contentType: Story.ContentType,
        isFeed: Boolean,
        aspectRatio: Float,
        visibility: Story.Visibility = Story.Visibility.PUBLIC,
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
                    visibility = visibility,
                    isFeed = isFeed,
                    timestamp = Timestamp.now()
                )

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

    private fun listenForFeeds() {
        val currentUserId = auth.currentUser?.uid ?: return
        feedsListenerJob?.cancel()

        feedsListenerJob = viewModelScope.launch {
            try {
                val userDocRef = firestore.collection("users").document(currentUserId)

                val followingList = try {
                    val snapshot = userDocRef.get()
                    if (snapshot.exists) {
                        snapshot.get<List<String>?>("following") ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    emptyList()
                }

                _error.value = null

                firestore.collection("stories")
                    .where { "isFeed" equalTo true }
                    .orderBy("timestamp", Direction.DESCENDING)
                    .snapshots
                    .collect { snapshot ->
                        val allStories = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.data<Story>().copy(storyId = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val authorizedStories = allStories.filter { story ->
                            when (story.visibility) {
                                Story.Visibility.PUBLIC -> true
                                Story.Visibility.CONNECTS_ONLY -> {
                                    story.userId == currentUserId || followingList.contains(story.userId)
                                }
                                Story.Visibility.PRIVATE -> story.userId == currentUserId
                            }
                        }

                        _feeds.value = authorizedStories
                        if (!_isSearching.value) _filteredFeeds.value = authorizedStories
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) return@launch

                _error.value = "Failed to load feed: ${e.message}"
                _isLoading.value = false
                println("SpaceVM: listenForFeeds Fatal - ${e.message}")
            }
        }
    }

    fun updatePortfolio(
        newDisplayName: String,
        newPhotoBytes: ByteArray?,onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated.")
            return
        }

        viewModelScope.launch {
            _isUpdatingProfile.value = true
            try {
                withContext(NonCancellable) {
                    var finalPhotoUrl = _userProfile.value?.photoUrl

                    if (newPhotoBytes != null) {
                        println("[PROFILE_SYNC] Step 1: Uploading new profile photo...")
                        val path = "users/$uid/profile_${Timestamp.now().seconds}.jpg"
                        finalPhotoUrl = uploadFileToStorage(newPhotoBytes, path)
                        println("[PROFILE_SYNC] Step 1 Complete: Photo URL is $finalPhotoUrl")
                    }

                    val contentUpdates = mapOf(
                        "userName" to newDisplayName,
                        "userProfileUrl" to finalPhotoUrl
                    )
                    val userProfileUpdates = mapOf(
                        "username" to newDisplayName,
                        "photoUrl" to finalPhotoUrl
                    )

                    println("[PROFILE_SYNC] Step 2: Preparing atomic batched write.")
                    val batch = firestore.batch()

                    val userDocRef = firestore.collection("users").document(uid)
                    batch.update(userDocRef, userProfileUpdates)
                    println("[PROFILE_SYNC] Queued update for users/$uid.")

                    val storiesQuery = firestore.collection("stories").where { "userId" equalTo uid }
                    val storiesSnapshot = storiesQuery.get()
                    storiesSnapshot.documents.forEach { doc -> batch.update(doc.reference, contentUpdates) }
                    println("[PROFILE_SYNC] Queued updates for ${storiesSnapshot.documents.size} stories.")

                    val allCommentsQuery = firestore.collectionGroup("comments").where { "userId" equalTo uid }
                    val allCommentsSnapshot = allCommentsQuery.get()
                    allCommentsSnapshot.documents.forEach { doc -> batch.update(doc.reference, contentUpdates) }
                    println("[PROFILE_SYNC] Queued updates for ${allCommentsSnapshot.documents.size} comments across all collections.")


                    val postsQuery =
                        firestore.collectionGroup("posts").where { "userId" equalTo uid }
                    val postsSnapshot = postsQuery.get()

                    for (postDoc in postsSnapshot.documents) {
                        batch.update(postDoc.reference, contentUpdates)

                        val postCommentsQuery = postDoc.reference.collection("comments").where { "userId" equalTo uid }
                        val postCommentsSnapshot = postCommentsQuery.get()
                        for (commentDoc in postCommentsSnapshot.documents) {
                            batch.update(commentDoc.reference, contentUpdates)
                        }
                    }
                    println("[PROFILE_SYNC] Queued updates for space posts and their nested comments.")

                    batch.commit()
                    println("[PROFILE_SYNC] Step 3 Complete: Batched write successful.")

                    withContext(Dispatchers.Main) {
                        _userProfile.update { currentUserProfile ->
                            currentUserProfile?.copy(
                                username = newDisplayName,
                                photoUrl = finalPhotoUrl
                            )
                        }
                        println("[PROFILE_SYNC] UI State Synced.")
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to update identity and related data."
                println("[PROFILE_SYNC] CRITICAL ERROR: $errorMsg")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isUpdatingProfile.value = false
                }
            }
        }
    }

        fun loadPublicProfile(userId: String) {

        _targetUserProfile.value = null
        _isLoadingProfileData.value = true

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get()

                if (userDoc.exists) {
                    val data = userDoc.data<User>()
                    _targetUserProfile.value = data
                    println("Profile Loaded: ${data.displayName}")
                } else {
                    _error.value = "Profile not found."
                }

                val storiesSnapshot = firestore.collection("stories")
                    .where { "userId" equalTo userId }
                    .where { "visibility" equalTo "PUBLIC" }
                    .orderBy("timestamp", Direction.DESCENDING)
                    .get()

                _userStories.value = storiesSnapshot.documents.mapNotNull { doc ->
                    try { doc.data<Story>().copy(storyId = doc.id) } catch (e: Exception) { null }
                }
            } catch (e: Exception) {
                println("Profile Error: ${e.message}")
                _error.value = "Connection to profile failed."
            } finally {
                _isLoadingProfileData.value = false
            }
        }
    }

    fun createSpace(coverImageBytes: ByteArray?, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
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
                    "coverImageUrl" to coverImageUrl,
                    "createdAt" to FieldValue.serverTimestamp,
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

    private suspend fun uploadFileToStorage(fileBytes: ByteArray, storagePath: String): String {
        println("[STORAGE] Uploading to: $storagePath")
        val storageRef = storage.reference(storagePath)
        storageRef.upload(fileBytes)
        val downloadUrl = storageRef.getDownloadUrl()
        println("[STORAGE] Upload complete. URL: $downloadUrl")
        return downloadUrl
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val lowercaseQuery = query.lowercase().trim()

            if (lowercaseQuery.isBlank()) {
                _filteredSpaces.value = _spaces.value
                _filteredFeeds.value = _feeds.value
            } else {

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

        isLoadingPosts.value = true
        val existingSpace = _filteredSpaces.value.find { it.id == spaceId }
        if (existingSpace != null) {
            _currentSpace.value = existingSpace
        }
        viewModelScope.launch {
            isLoadingPosts.value = true
            try {

                val doc = firestore.collection("spaces").document(spaceId).get()
                var spaceData = doc.data<Space>().copy(id = doc.id)
                _currentSpace.value = doc.data()

                val photos = mutableListOf<String>()
                spaceData.memberIds.take(5).forEach { memberId ->
                    val cached = userPhotoCache[memberId]
                    if (cached != null) {
                        photos.add(cached)
                    } else {
                        try {
                            val userDoc = firestore.collection("users").document(memberId).get()
                            val photo = userDoc.data<User>().photoUrl ?: ""
                            if (photo.isNotEmpty()) {
                                userPhotoCache[memberId] = photo
                                photos.add(photo)
                            }
                        } catch (e: Exception) { /* Handle hidden profiles */ }
                    }
                }

                spaceData = spaceData.copy(memberPhotoUrls = photos)

                loadSpacePosts(spaceId)
                listenForChatMessages(spaceId)

            } catch (e: Exception) {
                println("Error loading space details for $spaceId: ${e.message}")
                _currentSpace.value = null
            }
        }
    }
    fun loadSpacePosts(spaceId: String) {
        postsListenerJob?.cancel()

        postsListenerJob = firestore.collection("spaces").document(spaceId)
            .collection("posts")
            .orderBy("timestamp", Direction.DESCENDING)
            .snapshots()
            .onEach { snapshot ->
                val updatedPosts = snapshot.documents.mapNotNull { it.data<Story>() }
                _spacePosts.value = updatedPosts
                isLoadingPosts.value = false
            }
            .catch { e ->
                println("Error listening for space posts: ${e.message}")
                _spacePosts.value = emptyList()
                isLoadingPosts.value = false
            }
            .launchIn(viewModelScope)
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

            val newPost = Story(
                storyId = storyId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userProfileUrl = currentUser.photoURL,
                textContent = content,
                contentType = Story.ContentType.TEXT,
                contentUrl = null,
                thumbnailUrl = null,
                aspectRatio = 1.0f,
                isFeed = false,
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
    fun joinSpace(spaceId: String) {
        println("[SpaceVM] joinSpace CALLED for ID: $spaceId")

        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    val user = auth.currentUser

                    if (user == null) {
                        println("[SpaceVM] CRITICAL: Join attempted without Auth.")
                        return@withContext
                    }

                    println("[SpaceVM] DB_START: Attempting join for Space: $spaceId (User: ${user.uid})")

                    val spaceRef = firestore.collection("spaces").document(spaceId)

                    spaceRef.update("memberIds" to FieldValue.arrayUnion(user.uid))

                    println("[SpaceVM] DB_SUCCESS: User ${user.uid} is now a member of $spaceId")

                } catch (e: Exception) {
                    println("[SpaceVM] DB_FATAL: Join failed for $spaceId - ${e.message}")
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
                    storyRef.update(
                        "likeCount" to story.likeCount - 1,
                        "likedBy" to story.likedBy.filter { it != uid }
                    )
                } else {
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

    fun addComment(storyId: String, text: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            println("Comment Error: User not authenticated.")
            return
        }
        if (text.isBlank()) return

        val currentProfile = _userProfile.value
        if (currentProfile == null) {
            println("Comment Error: User profile data not loaded yet.")
        }

        viewModelScope.launch {
            try {
                val commentId = "comm_${authUser.uid}_${Timestamp.now().seconds}"

                val newComment = Story.Comment(
                    commentId = commentId,
                    storyId = storyId,
                    userId = authUser.uid,
                    userName = currentProfile?.username ?: authUser.displayName ?: "User",
                    userProfileUrl = currentProfile?.photoUrl ?: authUser.photoURL,
                    text = text,
                    timestamp = Timestamp.now()
                )

                val batch = firestore.batch()

                val commentRef = firestore.collection("stories").document(storyId)
                    .collection("comments").document(commentId)
                batch.set(commentRef, newComment)

                val storyRef = firestore.collection("stories").document(storyId)
                batch.update(storyRef, "commentCount" to FieldValue.increment(1))

                batch.commit()

            } catch (e: Exception) {
                println("Comment Error: ${e.message}")
                _error.value = "Failed to post comment."
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
                    postRef.update(
                        "likeCount" to story.likeCount - 1,
                        "likedBy" to story.likedBy.filter { it != uid }
                    )
                } else {
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
        val authUser = auth.currentUser
        if (authUser == null) {
            println("Comment Error: User not authenticated.")
            return
        }
        if (text.isBlank()) return

        val currentProfile = _userProfile.value
        if (currentProfile == null) {
            println("Comment Error: User profile data not loaded yet. Using fallback.")
        }

        viewModelScope.launch {
            try {
                val commentId = "comm_${authUser.uid}_${Timestamp.now().seconds}"

                val newComment = Story.Comment(
                    commentId = commentId,
                    storyId = postId,
                    userId = authUser.uid,
                    userName = currentProfile?.username ?: authUser.displayName ?: "User",
                    userProfileUrl = currentProfile?.photoUrl ?: authUser.photoURL,
                    text = text,
                    timestamp = Timestamp.now()
                )

                val batch = firestore.batch()

                val commentRef = firestore.collection("spaces").document(spaceId)
                    .collection("posts").document(postId)
                    .collection("comments").document(commentId)
                batch.set(commentRef, newComment)

                val postRef = firestore.collection("spaces").document(spaceId)
                    .collection("posts").document(postId)
                batch.update(postRef, "commentCount" to FieldValue.increment(1))

                batch.commit()

            } catch (e: Exception) {
                println("Space Comment Error: ${e.message}")
                _error.value = "Failed to post comment in space."
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
                    postRef.update("bookmarkedBy" to story.bookmarkedBy.filter { it != uid })
                    userRef.delete()
                } else {
                    postRef.update("bookmarkedBy" to story.bookmarkedBy + uid)
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
    fun toggleBookmark(story: Story) {
        val uid = auth.currentUser?.uid ?: return
        val storyRef = firestore.collection("stories").document(story.storyId)
        val userRef = firestore.collection("users").document(uid).collection("bookmarks").document(story.storyId)
        val isBookmarked = story.bookmarkedBy.contains(uid)

        viewModelScope.launch {
            try {
                if (isBookmarked) {
                    storyRef.update("bookmarkedBy" to story.bookmarkedBy.filter { it != uid })
                    userRef.delete()
                } else {
                    storyRef.update("bookmarkedBy" to story.bookmarkedBy + uid)
                    userRef.set(mapOf("savedAt" to Timestamp.now()))
                }
            } catch (e: Exception) {
                println("Bookmark Error: ${e.message}")
            }
        }
    }
    fun toggleFollow(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val targetUserRef = firestore.collection("users").document(targetUserId)

            val isCurrentlyFollowing = _isFollowing.value[targetUserId] ?: false

            try {
                if (isCurrentlyFollowing) {
                    currentUserRef.update("following" to FieldValue.arrayRemove(targetUserId))
                    targetUserRef.update("followers" to FieldValue.arrayRemove(currentUserId))
                } else {
                    currentUserRef.update("following" to FieldValue.arrayUnion(targetUserId))
                    targetUserRef.update("followers" to FieldValue.arrayUnion(currentUserId))
                }

                _isFollowing.value = _isFollowing.value + (targetUserId to !isCurrentlyFollowing)
            } catch (e: Exception) {
                _error.value = "Failed to update connection: ${e.message}"
            }
        }
    }

}
