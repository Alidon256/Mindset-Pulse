package org.vaulture.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.vaulture.project.domain.model.SpaceMessage
import org.vaulture.project.presentation.utils.formatTimestamp2
import org.vaulture.project.presentation.viewmodels.SpaceViewModel

private enum class SpaceDetailTab(val title: String, val icon: ImageVector) {
    POSTS("Feed", Icons.Default.DynamicFeed),
    CHAT("Support Hub", Icons.Default.ChatBubble)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceDetailScreen(
    spaceId: String,
    viewModel: SpaceViewModel,
    onNavigateBack: () -> Unit
    // Note: onCommentClick is now handled internally
) {
    val space by viewModel.currentSpace.collectAsState()
    var selectedTab by remember { mutableStateOf(_root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.POSTS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedStoryId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var isWideScreen by remember { mutableStateOf(false) }
    BoxWithConstraints {
        isWideScreen = maxWidth > 600.dp
    }

    LaunchedEffect(spaceId) {
        viewModel.loadSpaceDetails(spaceId)
        viewModel.listenForChatMessages(spaceId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSpaceDetails() }
    }

    if (showCreatePostDialog) {
        _root_ide_package_.org.vaulture.project.ui.screens.CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { content ->
                viewModel.createPost(spaceId, content)
                showCreatePostDialog = false
            }
        )
    }

    // --- NEW: Modal Bottom Sheet is now defined here ---
    if (showCommentSheet && selectedStoryId != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.CommentSheetContentSpace(
                spaceId = spaceId,
                postId = selectedStoryId!!,
                viewModel = viewModel,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showCommentSheet = false
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = space?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.POSTS,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Share Reflection") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailHeader(
                space?.coverImageUrl,
                space?.name,
                isWide = isWideScreen
            )
            _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTabs(
                selectedTab,
                { selectedTab = it })

            _root_ide_package_.org.vaulture.project.ui.screens.TabContent(
                tab = selectedTab,
                viewModel = viewModel,
                spaceId = spaceId,
                // The onCommentClick lambda now triggers the internal state change
                onCommentClick = { storyId ->
                    selectedStoryId = storyId
                    showCommentSheet = true
                }
            )
        }
    }
}


@Composable
private fun SpaceDetailHeader(coverImageUrl: String?, spaceName: String?, isWide: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isWide) 280.dp else 200.dp)
            .clip(if (isWide) RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp) else RoundedCornerShape(0.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = coverImageUrl ?: "https://images.pexels.com/photos/1231265/pexels-photo-1231265.jpeg",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = spaceName ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Safe Space",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
private fun SpaceDetailTabs(selectedTab: org.vaulture.project.ui.screens.SpaceDetailTab, onTabSelected: (org.vaulture.project.ui.screens.SpaceDetailTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val background by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            val contentColor by animateColorAsState(
                if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(background)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(tab.icon, null, modifier = Modifier.size(18.dp), tint = contentColor)
                    Spacer(Modifier.width(8.dp))
                    Text(tab.title, color = contentColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: org.vaulture.project.ui.screens.SpaceDetailTab,
    viewModel: SpaceViewModel,
    spaceId: String,
    onCommentClick: (storyId: String) -> Unit // The internal trigger
) {
    AnimatedContent(
        targetState = tab,
        transitionSpec = {
            fadeIn(tween(300)) + slideInHorizontally { if (targetState == _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.CHAT) it else -it } togetherWith
                    fadeOut(tween(300)) + slideOutHorizontally { if (targetState == _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.CHAT) -it else it }
        },
        label = "TabContentTransition"
    ) { currentTab ->
        when (currentTab) {
            _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.POSTS -> _root_ide_package_.org.vaulture.project.ui.screens.SpacePostsContent(
                viewModel,
                onCommentClick,
                spaceId
            )
            _root_ide_package_.org.vaulture.project.ui.screens.SpaceDetailTab.CHAT -> _root_ide_package_.org.vaulture.project.ui.screens.SpaceChatContent(
                viewModel,
                spaceId
            )
        }
    }
}

@Composable
fun SpacePostsContent(viewModel: SpaceViewModel, onCommentClick: (String) -> Unit,spaceId: String) {
    val posts by viewModel.spacePosts.collectAsState()
    val isLoading by viewModel.isLoadingPosts.collectAsState()
    val currentUserId = viewModel.auth.currentUser?.uid ?: ""

    if (isLoading && posts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (posts.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DynamicFeed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "No posts yet. Be the first to share something!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for the FAB
        ) {
            items(posts, key = { it.storyId }) { post ->
                _root_ide_package_.org.vaulture.project.presentation.ui.components.PostItem(
                    post = post,
                    currentUserId = currentUserId,
                    onLikeClick = { viewModel.toggleLikeSpace(post, spaceId) },
                    onCommentClick = { onCommentClick(post.storyId) }, // Trigger the internal state change
                    onBookmarkClick = { viewModel.toggleBookmarkSpace(post, spaceId) },
                    onProfileClick = {},
                    onOptionClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun SpaceChatContent(viewModel: SpaceViewModel, spaceId: String) {
    val messages by viewModel.spaceMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounceAnim"
                )
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { translationY = offsetY }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "No messages yet. Start the conversation!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isFromMe = message.authorId == viewModel.auth.currentUser?.uid
                    _root_ide_package_.org.vaulture.project.ui.screens.ChatMessageBubble(
                        message = message,
                        isFromMe = isFromMe
                    )
                }
            }
        }

        _root_ide_package_.org.vaulture.project.ui.screens.MessageInputRow(
            value = viewModel.newMessageText.value,
            onValueChange = { viewModel.newMessageText.value = it },
            onSend = { viewModel.sendChatMessage(spaceId) }
        )
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
                modifier = Modifier.fillMaxWidth().heightIn(150.dp),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Share something with the space...") }
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
                Box(modifier = Modifier.align(if (isFromMe) Alignment.End else Alignment.Start)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                        )
                        Text(
                            text = message.timestamp?.let { formatTimestamp2(it, "h:mm a") } ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = LocalContentColor.current.copy(alpha = 0.7f)
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
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
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
