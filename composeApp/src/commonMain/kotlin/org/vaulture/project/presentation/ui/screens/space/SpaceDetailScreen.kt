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
import org.vaulture.project.presentation.ui.components.PostItem
import org.vaulture.project.presentation.ui.screens.space.CommentSheetContentSpace
import org.vaulture.project.presentation.ui.screens.space.SpacesListContent
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
    onNavigateBack: () -> Unit,
    onSpaceSelected: (String) -> Unit,) {
    // --- STATE HOISTING ---
    val allSpaces by viewModel.spaces.collectAsState()
    val activeSpaceId by viewModel.activeSpaceId.collectAsState()
    val space by viewModel.currentSpace.collectAsState()
    var selectedTab by remember { mutableStateOf(SpaceDetailTab.POSTS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    // --- BOTTOM SHEET STATE ---
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedStoryId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // --- DATA LOADING ---
    LaunchedEffect(spaceId) {
        viewModel.loadSpaceDetails(spaceId)
        viewModel.listenForChatMessages(spaceId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSpaceDetails() }
    }

    // --- DIALOGS & SHEETS (kept at the top level) ---
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { content ->
                viewModel.createPost(spaceId, content)
                showCreatePostDialog = false
            }
        )
    }

    if (showCommentSheet && selectedStoryId != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CommentSheetContentSpace(
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

    // --- RESPONSIVE LAYOUT ---
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 920.dp

        if (!isWideScreen) {
            // --- MOBILE LAYOUT (Single Pane) ---
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
                        visible = selectedTab == SpaceDetailTab.POSTS,
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
                    SpaceDetailHeader(
                        space?.coverImageUrl,
                        space?.name,
                        isWide = isWideScreen
                    )
                    SpaceDetailTabs(
                        selectedTab,
                        { selectedTab = it })

                    TabContent(
                        tab = selectedTab,
                        viewModel = viewModel,
                        spaceId = spaceId,
                        onCommentClick = { storyId ->
                            selectedStoryId = storyId
                            showCommentSheet = true
                        }
                    )
                }
            }
        } else {
            // --- WEB/TABLET LAYOUT (Two Panes) ---
            Row(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            ) {
                // --- LEFT PANE (Master List) ---
                Box(modifier = Modifier.width(340.dp)) {
                    val spaces by viewModel.filteredSpaces.collectAsState()
                    // Assuming SpacesListContent is a LazyColumn showing a list of spaces
                    SpacesListContent(
                        spaces = spaces,
                        onSpaceClick = { newSpaceId ->
                            println("[UI] User clicked space: $newSpaceId. Triggering Join...")
                            viewModel.joinSpace(newSpaceId)
                            onSpaceSelected(newSpaceId)
                        }
                    )
                }

                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = space?.name ?: "Loading...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        // The back button is optional on web, you could hide it
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )

                    // The rest of the content scrolls within this column
                    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                        /*SpaceDetailHeader(
                            space?.coverImageUrl,
                            space?.name,
                            isWide = isWideScreen
                        )*/
                        SpaceDetailTabs(
                            selectedTab,
                            { selectedTab = it })

                        TabContent(
                            tab = selectedTab,
                            viewModel = viewModel,
                            spaceId = spaceId,
                            onCommentClick = { storyId ->
                                selectedStoryId = storyId
                                showCommentSheet = true
                            }
                        )
                    }
                }
            }
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
private fun SpaceDetailTabs(selectedTab: SpaceDetailTab, onTabSelected: (SpaceDetailTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SpaceDetailTab.entries.forEach { tab ->
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
    tab: SpaceDetailTab,
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
            SpaceDetailTab.POSTS -> SpacePostsContent(
                viewModel,
                onCommentClick,
                spaceId
            )
            SpaceDetailTab.CHAT -> SpaceChatContent(
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
                PostItem(
                    post = post,
                    currentUserId = currentUserId,
                    onLikeClick = { viewModel.toggleLikeSpace(post, spaceId) },
                    onCommentClick = { onCommentClick(post.storyId) },
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
   // var messageText by remember { mutableStateOf("") }
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
                    ChatMessageBubble(
                        message = message,
                        isFromMe = isFromMe
                    )
                }
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
@Composable
private fun SpacesListPanel(
    spaces: List<org.vaulture.project.domain.model.Space>,
    activeSpaceId: String?,
    onSpaceClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface)) {
        item {
            Text(
                "Your Spaces",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }
        items(spaces, key = { it.id }) { space ->
            val isSelected = space.id == activeSpaceId
            SpaceListItem(
                space = space,
                isSelected = isSelected,
                onClick = { onSpaceClick(space.id) }
            )
        }
    }
}

@Composable
private fun SpaceListItem(
    space: org.vaulture.project.domain.model.Space,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = space.coverImageUrl,
            contentDescription = "${space.name} cover image",
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = space.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun SpaceDetailContent(
    space: org.vaulture.project.domain.model.Space,
    selectedTab: SpaceDetailTab,
    onTabSelected: (SpaceDetailTab) -> Unit,viewModel: SpaceViewModel,
    onShowCommentSheet: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SpaceDetailHeader(space.coverImageUrl, space.name, isWide = true)
        SpaceDetailTabs(selectedTab, onTabSelected)
        TabContent(
            tab = selectedTab,
            viewModel = viewModel,
            spaceId = space.id,
            onCommentClick = onShowCommentSheet
        )
    }
}


/*package org.vaulture.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
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
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.vaulture.project.domain.model.Space
import org.vaulture.project.domain.model.SpaceMessage
import org.vaulture.project.presentation.ui.components.PostItem
import org.vaulture.project.presentation.ui.screens.space.CommentSheetContentSpace
import org.vaulture.project.presentation.utils.formatTimestamp2
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import kotlin.math.max

private enum class SpaceDetailTab(val title: String, val icon: ImageVector) {
    POSTS("Feed", Icons.Default.DynamicFeed),
    CHAT("Support Hub", Icons.Default.ChatBubble)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceDetailScreen(
    spaceId: String,
    viewModel: SpaceViewModel,
    onNavigateBack: () -> Unit,
    // This new lambda handles navigation when a *different* space is clicked in the master list
    onSpaceSelected: (String) -> Unit
) {
    // --- STATE MANAGEMENT ---
    val allSpaces by viewModel.spaces.collectAsState()
    val activeSpaceId by viewModel.activeSpaceId.collectAsState()
    val space by viewModel.currentSpace.collectAsState()
    var selectedTab by remember { mutableStateOf(SpaceDetailTab.POSTS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    // Internal state for the comment sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedStoryId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load initial data for the provided spaceId
    LaunchedEffect(spaceId) {
        viewModel.loadSpaceDetails(spaceId)
        viewModel.listenForChatMessages(spaceId)
    }

    // Cleanup listeners when the screen is disposed
    DisposableEffect(Unit) {
        onDispose { viewModel.clearSpaceDetails() }
    }

    // --- DIALOGS & SHEETS ---
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { content ->
                viewModel.createPost(spaceId, content)
                showCreatePostDialog = false
            }
        )
    }

    if (showCommentSheet && selectedStoryId != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CommentSheetContentSpace(
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

    // --- RESPONSIVE LAYOUT ---
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 800.dp

        if (isWideScreen) {
            // --- WEB/TABLET LAYOUT (MASTER-DETAIL) ---
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // Left Pane: List of all spaces
                Box(modifier = Modifier.width(340.dp)) {
                    SpacesListPanel(
                        spaces = allSpaces,
                        activeSpaceId = activeSpaceId, // Pass active ID for highlighting
                        onSpaceClick = { newSpaceId ->
                            // When a new space is clicked, trigger navigation
                            onSpaceSelected(newSpaceId)
                        }
                    )
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Right Pane: Details of the selected space
                Box(modifier = Modifier.weight(1f)) {
                    if (space != null) {
                        SpaceDetailContent(
                            space = space!!,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            viewModel = viewModel,
                            onShowCommentSheet = { storyId ->
                                selectedStoryId = storyId
                                showCommentSheet = true
                            }
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        } else {
            // --- MOBILE LAYOUT (SINGLE PANE WITH PARALLAX HEADER) ---
            Scaffold(
                topBar = {
                    // The top bar can be transparent as the parallax header will be behind it
                    TopAppBar(
                        title = { /* Title can be dynamic based on scroll */ },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = selectedTab == SpaceDetailTab.POSTS,
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
                CollapsingToolbarLayout(
                    modifier = Modifier.padding(paddingValues),
                    headerImageUrl = space?.coverImageUrl,
                    headerTitle = space?.name ?: "Loading...",
                    onTabSelected = { selectedTab = it },
                    selectedTab = selectedTab
                ) {
                    TabContent(
                        tab = selectedTab,
                        viewModel = viewModel,
                        spaceId = spaceId,
                        onCommentClick = { storyId ->
                            selectedStoryId = storyId
                            showCommentSheet = true
                        }
                    )
                }
            }
        }
    }
}

// --- MASTER-DETAIL COMPONENTS (FOR WIDE SCREENS) ---

@Composable
private fun SpacesListPanel(
    spaces: List<Space>,
    activeSpaceId: String?,
    onSpaceClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface)) {
        item {
            Text(
                "Your Spaces",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }
        items(spaces, key = { it.id }) { space ->
            val isSelected = space.id == activeSpaceId
            SpaceListItem(
                space = space,
                isSelected = isSelected,
                onClick = { onSpaceClick(space.id) }
            )
        }
    }
}

@Composable
private fun SpaceListItem(
    space: Space,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "SpaceListItemBg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "SpaceListItemContent"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = space.coverImageUrl,
            contentDescription = "${space.name} cover image",
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = space.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun SpaceDetailContent(
    space: Space,
    selectedTab: SpaceDetailTab,
    onTabSelected: (SpaceDetailTab) -> Unit,
    viewModel: SpaceViewModel,
    onShowCommentSheet: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SpaceDetailHeader(space.coverImageUrl, space.name, isWide = true)
        SpaceDetailTabs(selectedTab, onTabSelected)
        TabContent(
            tab = selectedTab,
            viewModel = viewModel,
            spaceId = space.id,
            onCommentClick = onShowCommentSheet
        )
    }
}

@Composable
private fun CollapsingToolbarLayout(
    modifier: Modifier = Modifier,
    headerImageUrl: String?,
    headerTitle: String,
    selectedTab: SpaceDetailTab,
    onTabSelected: (SpaceDetailTab) -> Unit,
    content: @Composable () -> Unit
) {
    val scrollState = rememberLazyListState()
    val headerHeightPx = with(LocalDensity.current) { 200.dp.toPx() }
    val tabsHeightPx = with(LocalDensity.current) { 72.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(200.dp + 72.dp))
            }
            item {
                content()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
                    val firstItemIndex = scrollState.firstVisibleItemIndex
                    if (firstItemIndex == 0) {
                        translationY = -scrollOffset * 0.5f
                        val scale = max(1f, 1f + (scrollOffset / headerHeightPx) * 0.2f)
                        scaleX = scale
                        scaleY = scale
                    }
                    alpha = 1f - (scrollOffset / headerHeightPx).coerceIn(0f, 1f)
                }
        ) {
            SpaceDetailHeaderContent(headerImageUrl, headerTitle)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
                    val firstItemIndex = scrollState.firstVisibleItemIndex
                    translationY = if (firstItemIndex == 0) -scrollOffset else -headerHeightPx
                }
                .offset(y = 200.dp),
            shadowElevation = if (scrollState.firstVisibleItemScrollOffset > 0) 4.dp else 0.dp
        ) {
            SpaceDetailTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
        }
    }
}

@Composable
private fun SpaceDetailHeaderContent(coverImageUrl: String?, spaceName: String) {
    // This is the actual visual content of the header, separated for reuse.
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(spaceName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Safe Space", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primaryContainer)
        }
    }
}


// --- REST OF THE FILE (TAB CONTENT, POSTS, CHAT, DIALOGS) ---
// Note: SpaceDetailHeader has been replaced by SpaceDetailHeaderContent, the rest are mostly unchanged

@Composable
private fun SpaceDetailTabs(selectedTab: SpaceDetailTab, onTabSelected: (SpaceDetailTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SpaceDetailTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val background by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, spring(stiffness = Spring.StiffnessLow), label = "")
            val contentColor by animateColorAsState(if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "")

            Box(
                modifier = Modifier.weight(1f).height(40.dp).clip(CircleShape).background(background).clickable { onTabSelected(tab) },
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
    tab: SpaceDetailTab,
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
            SpaceDetailTab.POSTS -> SpacePostsContent(
                viewModel,
                onCommentClick,
                spaceId
            )
            SpaceDetailTab.CHAT -> SpaceChatContent(
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

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading && posts.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (posts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.DynamicFeed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No posts yet. Be the first to share!", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            }
        } else {
            posts.forEach { post ->
                PostItem(
                    post = post,
                    currentUserId = currentUserId,
                    onLikeClick = { viewModel.toggleLikeSpace(post, spaceId) },
                    onCommentClick = { onCommentClick(post.storyId) },
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
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                val offsetY by infiniteTransition.animateFloat(0f, -
                12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),repeatMode = RepeatMode.Reverse
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
                    ChatMessageBubble(message = message, isFromMe = isFromMe)
                }
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
private fun MessageInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send")
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
*/