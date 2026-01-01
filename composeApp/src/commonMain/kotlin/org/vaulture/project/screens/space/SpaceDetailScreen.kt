package org.vaulture.project.ui.screens


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.vaulture.project.components.PostItem
import org.vaulture.project.data.models.SpaceMessage
import org.vaulture.project.utils.formatTimestamp2
import org.vaulture.project.viewmodels.SpaceViewModel

// Enum to manage tabs
private enum class SpaceDetailTab(val title: String, val icon: ImageVector) {
    POSTS("Posts", Icons.Default.DynamicFeed),
    CHAT("Chat", Icons.Default.ChatBubble)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceDetailScreen(
    spaceId: String,
    viewModel: SpaceViewModel,
    onNavigateBack: () -> Unit
) {
    val space by viewModel.currentSpace.collectAsState()
    var selectedTab by remember { mutableStateOf(SpaceDetailTab.POSTS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    LaunchedEffect(spaceId) {
        viewModel.loadSpaceDetails(spaceId) // This will also trigger post loading
        viewModel.listenForChatMessages(spaceId)
    }

    // Clean up listeners when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSpaceDetails()
        }
    }

    // Dialog for creating a new post
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { content ->
                viewModel.createPost(spaceId, content)
                showCreatePostDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(space?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            // Show FAB only on the Posts tab with animation
            AnimatedVisibility(
                visible = selectedTab == SpaceDetailTab.POSTS,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(onClick = { showCreatePostDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            SpaceDetailHeader(space?.coverImageUrl, space?.name)
            SpaceDetailTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // Animated content switcher for tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally { width -> direction * width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    SpaceDetailTab.POSTS -> SpacePostsContent(viewModel)
                    SpaceDetailTab.CHAT -> SpaceChatContent(viewModel, spaceId)
                }
            }
        }
    }
}

@Composable
private fun SpaceDetailHeader(coverImageUrl: String?, spaceName: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (coverImageUrl != null) {
            AsyncImage(
                model = coverImageUrl,
                contentDescription = "$spaceName cover image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Add a scrim for better text visibility
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
    }
}

@Composable
private fun SpaceDetailTabs(selectedTab: SpaceDetailTab, onTabSelected: (SpaceDetailTab) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        SpaceDetailTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.title) },
                icon = { Icon(tab.icon, contentDescription = tab.title) }
            )
        }
    }
}

@Composable
fun SpacePostsContent(viewModel: SpaceViewModel) {
    val posts by viewModel.spacePosts.collectAsState()
    val isLoading by viewModel.isLoadingPosts.collectAsState()
    val currentUser by viewModel.auth.authStateChanged.collectAsState(initial = null)
    val currentUserId = currentUser?.uid ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && posts.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else if (posts.isEmpty()) {
            Text(
                "No posts yet. Be the first to share something!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for the FAB
            ) {
                items(posts, key = { it.storyId }) { post ->
                    PostItem(post,currentUserId, {viewModel.toggleLike(post) }, {viewModel.addComment(post.storyId, it)}, {}, {}, {})
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
fun SpaceChatContent(viewModel: SpaceViewModel, spaceId: String) {
    val messages by viewModel.spaceMessages.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                val isFromMe = message.authorId == viewModel.auth.currentUser?.uid
                ChatMessageBubble(message = message, isFromMe = isFromMe)
            }
        }

        MessageInputRow(
            value = viewModel.newMessageText.value,
            onValueChange = { viewModel.newMessageText.value = it },
            onSend = { viewModel.sendChatMessage(spaceId) }
        )
    }
}

@Composable
fun ChatMessageBubble(message: SpaceMessage, isFromMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe) {
            AsyncImage(
                model = message.authorAvatarUrl,
                contentDescription = message.authorName,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            // Use modern Material 3 colors for better distinction
            color = if (isFromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isFromMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isFromMe) {
                    Text(
                        message.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // This Box wrapper is key for the WhatsApp-style timestamp alignment
                Box(modifier = Modifier.align(if (isFromMe) Alignment.End else Alignment.Start)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            // Use weight to ensure text wraps correctly and pushes timestamp
                            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                        )
                        Text(
                            // Format the Firestore Timestamp to a readable string
                            text = message.timestamp?.let { formatTimestamp2(it, "h:mm a") } ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSend, enabled = value.isNotBlank()) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var postContent by remember { mutableStateOf("") }
    val isSubmitEnabled = postContent.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a New Post") },
        text = {
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5,
                placeholder = { Text("Share something with the space...")}
            )
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(postContent) },
                enabled = isSubmitEnabled
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
