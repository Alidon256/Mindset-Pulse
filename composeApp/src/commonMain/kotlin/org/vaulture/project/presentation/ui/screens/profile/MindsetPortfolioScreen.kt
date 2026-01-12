package org.vaulture.project.presentation.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.vaulture.project.domain.model.User
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.StaggeredStoryItem
import org.vaulture.project.presentation.viewmodels.SpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindsetPortfolioScreen(
    userId: String,
    viewModel: SpaceViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val userProfile by viewModel.targetUserProfile.collectAsState()
    val stories by viewModel.userStories.collectAsState()
    val isLoading by viewModel.isLoadingProfileData.collectAsState()
    val followingMap by viewModel.isFollowing.collectAsState()
    val isFollowing = followingMap[userId] ?: false
    val currentUser by viewModel.auth.authStateChanged.collectAsState(initial = null)
    val currentUserId = currentUser?.uid ?: ""
    val isOwnProfile = userId == currentUserId

    LaunchedEffect(userId) {
        viewModel.loadPublicProfile(userId)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 920.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Mindset Portfolio", style = PoppinsTypography().titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (isLoading && userProfile == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Card(
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PortfolioHeader(
                                    user = userProfile,
                                    isFollowing = isFollowing,
                                    isOwnProfile = isOwnProfile,
                                    onConnectClick = { viewModel.toggleFollow(userId) },
                                    onEditClick = onEditClick
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(48.dp))

                        Column(modifier = Modifier.weight(0.65f)) {
                            Text(
                                "Public Reflections",
                                style = PoppinsTypography().headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp
                            ) {
                                items(stories) { story ->
                                    StaggeredStoryItem(
                                        story = story,
                                        currentUserId = currentUserId,
                                        onLikeClick = { viewModel.toggleLike(story) },
                                        onClick = { /* Detail logic */ }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PortfolioHeader(
                                user = userProfile,
                                isFollowing = isFollowing,
                                isOwnProfile = isOwnProfile,
                                onConnectClick = { viewModel.toggleFollow(userId) },
                                onEditClick = onEditClick
                            )
                        }

                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                "Public Reflections",
                                style = PoppinsTypography().titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            )
                        }

                        items(stories) { story ->
                            StaggeredStoryItem(
                                story = story,
                                currentUserId = currentUserId,
                                onLikeClick = { viewModel.toggleLike(story) },
                                onClick = { /* Navigate to detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioHeader(
    user: User?,
    isFollowing: Boolean,
    isOwnProfile: Boolean,
    onConnectClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = user?.photoUrl ?: "https://images.pexels.com/photos/1065084/pexels-photo-1065084.jpeg",
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(32.dp).border(2.dp, Color.White, CircleShape)
            ) {
                Icon(
                    Icons.Default.Verified,
                    null,
                    tint = Color.White,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (user == null) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            user.username?.let {
                Text(
                    text = it,
                    style = PoppinsTypography().headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Text(
            text = if (isOwnProfile) "Your Mindset Journey" else "Community Member",
            style = PoppinsTypography().bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        if (!isOwnProfile) {
            Button(
                onClick = onConnectClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isFollowing) "Connected" else "Connect")
            }
        } else {
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Identity")
            }
        }
    }
}
