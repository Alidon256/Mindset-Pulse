package org.vaulture.project.presentation.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.data.remote.AuthService
import org.vaulture.project.domain.model.Story
import org.vaulture.project.presentation.viewmodels.WellnessViewModel
import org.vaulture.project.domain.model.WellnessStats
import org.vaulture.project.domain.model.WellnessType
import org.vaulture.project.presentation.navigation.Routes
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.AIPrivacyCard
import org.vaulture.project.presentation.ui.components.PostItem
import org.vaulture.project.presentation.ui.components.ProfileAvatar
import org.vaulture.project.presentation.ui.components.PulseBadgeCard
import org.vaulture.project.presentation.ui.components.SectionHeader
import org.vaulture.project.presentation.ui.components.SignOutButton
import org.vaulture.project.presentation.ui.components.StaggeredStoryItem
import org.vaulture.project.presentation.ui.components.StatBox
import org.vaulture.project.presentation.ui.components.StreakBanner
import org.vaulture.project.presentation.ui.components.WellnessActionItem
import org.vaulture.project.presentation.ui.components.WellnessStatsRow
import org.vaulture.project.presentation.viewmodels.ProfileFilter
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import vaulture.composeapp.generated.resources.*
import kotlin.time.Clock

@Composable
fun ProfileScreen(
    authService: AuthService,
    wellnessViewModel: WellnessViewModel,
    onSignOut: () -> Unit,
    navController: NavController,
    spaceViewModel: SpaceViewModel,
    selectedFilter: ProfileFilter,
    onFilterSelected: (ProfileFilter) -> Unit,
    onCommentClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val statsState by wellnessViewModel.uiState.collectAsState()
    val user by authService.currentUser.collectAsState(null)
    val currentUserId = user?.uid ?: ""

    val myPosts by spaceViewModel.userStories.collectAsState()
    val likedPosts by spaceViewModel.userLikedStories.collectAsState()
    val bookmarkedPosts by spaceViewModel.userBookmarkedStories.collectAsState()
    val isLoading by spaceViewModel.isLoadingProfileData.collectAsState()

    val displayedStories = when (selectedFilter) {
        ProfileFilter.MY_POSTS -> myPosts
        ProfileFilter.LIKED -> likedPosts
        ProfileFilter.BOOKMARKED -> bookmarkedPosts
    }

    // Trigger data loading when the filter changes
    LaunchedEffect(selectedFilter) {
        spaceViewModel.loadProfileData(selectedFilter)
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth > 920.dp

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = { fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring()) },
            label = "ResponsiveProfileLayout"
        ) { expanded ->
            if (expanded) {
                ProfileScreenExpanded(
                    authService = authService,
                    stats = statsState.stats,
                    onSignOut = onSignOut,
                    wellnessViewModel = wellnessViewModel,
                    navController = navController,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    isLoading = isLoading,
                    displayedStories = displayedStories,
                    currentUserId = currentUserId,
                    onCommentClick = onCommentClick,
                    spaceViewModel = spaceViewModel
                )
            } else {
                ProfileScreenCompact(
                    authService = authService,
                    stats = statsState.stats,
                    spaceViewModel = spaceViewModel,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    isLoading = isLoading,
                    displayedStories = displayedStories,
                    currentUserId = currentUserId,
                    onCommentClick = onCommentClick,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenCompact(
    authService: AuthService,
    stats: WellnessStats,
    onNavigateToSettings: () -> Unit,
    spaceViewModel: SpaceViewModel,
    onFilterSelected: (ProfileFilter) -> Unit,
    onCommentClick: (String) -> Unit,
    selectedFilter: ProfileFilter,
    isLoading: Boolean,
    displayedStories: List<Story>,
    currentUserId: String
) {
    val user by authService.currentUser.collectAsState(null)
    val bannerHeight = 200.dp
    val avatarInitialSize = 110.dp

    val gridState = rememberLazyStaggeredGridState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(modifier = Modifier.fillMaxWidth().height(bannerHeight + avatarInitialSize / 2)) {
                    AsyncImage(
                        model = "https://images.pexels.com/photos/1231265/pexels-photo-1231265.jpeg",
                        contentDescription = "Wellness banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(bannerHeight)
                    )
                    Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent))))

                    val avatarY = (bannerHeight - (avatarInitialSize / 2))
                    Box(Modifier.padding(top = avatarY).size(110.dp).align(Alignment.TopCenter)) {
                        ProfileAvatar(
                            user?.photoUrl,
                            "Profile"
                        )
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                    Text(user?.displayName ?: "Guest User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                RealWellnessStatsRow(stats)

            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { onFilterSelected(filter) },
                                label = {
                                    Text(when(filter) {
                                        ProfileFilter.MY_POSTS -> "My Posts"
                                        ProfileFilter.LIKED -> "Liked"
                                        ProfileFilter.BOOKMARKED -> "Saved"
                                    })
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(filter) {
                                            ProfileFilter.MY_POSTS -> Icons.Default.Article
                                            ProfileFilter.LIKED -> Icons.Default.Favorite
                                            ProfileFilter.BOOKMARKED -> Icons.Default.Bookmark
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (isLoading) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (displayedStories.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("No posts found here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(displayedStories, key = { it.storyId }) { story ->
                    StaggeredStoryItem(
                        story = story,
                        currentUserId = currentUserId,
                        onLikeClick = { spaceViewModel.toggleLike(story) },
                        onClick = { onCommentClick(story.storyId) }
                    )
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PulseBadgeCard(totalPoints = stats.resiliencePoints)
                    AIPrivacyCard()
                    SettingsItem(onClick = onNavigateToSettings )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileScreenExpanded(
    authService: AuthService,
    stats: WellnessStats,
    onSignOut: () -> Unit,
    wellnessViewModel: WellnessViewModel,
    navController: NavController,
    selectedFilter: ProfileFilter,
    onFilterSelected: (ProfileFilter) -> Unit,
    isLoading: Boolean,
    displayedStories: List<Story>,
    currentUserId: String,
    onCommentClick: (String) -> Unit,
    spaceViewModel: SpaceViewModel
) {
    val user by authService.currentUser.collectAsState(null)
    val wellnessState by wellnessViewModel.uiState.collectAsState()
    var selectedRailItem by remember { mutableStateOf("Profile") }
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning 🌄,"
        in 12..17 -> "Good Afternoon 😎,"
        else -> "Good Evening 🌙,"
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp)
                .padding(vertical =8.dp, horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greeting,
                        style = PoppinsTypography().bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "${" "}${user?.displayName?.substringBefore(' ') ?: "..."}",
                        style = PoppinsTypography().bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StreakBanner(
                    wellnessState.stats.currentStreak
                )
            }
            item {
                SectionHeader(
                    title = "Mindful Actions",
                    icon = Icons.Default.SelfImprovement
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    WellnessActionItem(
                        "Breathing",
                        "5 Mins",
                        Icons.Default.Air,
                        bg = Color(0xFF03A9F4)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.BREATHING)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Yoga",
                        "10 Mins",
                        Icons.Default.SelfImprovement,
                        bg = Color(0xFF4CAF50)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.YOGA)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Meditation",
                        "15 Mins",
                        Icons.Default.Spa,
                        bg = Color(0xFF9C27B0)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }
                }
            }
            item {
                Card(
                    onClick = { selectedRailItem == "Profile"},
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Profile") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (selectedRailItem == "Profile") Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item{
                Card(
                    onClick = { navController.navigate(Routes.SETTINGS)},
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Settings") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("SignOut")
                }
            }
        }
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        val gridState = rememberLazyStaggeredGridState()

        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f).padding(start = 48.dp,end = 48.dp) ,
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        ProfileAvatar(
                            user?.photoUrl,
                            "User",
                            Modifier.size(120.dp)
                        )
                        Spacer(Modifier.width(32.dp))
                        Column {
                            Text(user?.displayName ?: "User", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                            Text(user?.email ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    RealWellnessStatsRow(stats)
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp,end = 16.dp,top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { onFilterSelected(filter) },
                                    label = {
                                        Text(when(filter) {
                                            ProfileFilter.MY_POSTS -> "My Posts"
                                            ProfileFilter.LIKED -> "Liked"
                                            ProfileFilter.BOOKMARKED -> "Saved"
                                        })
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when(filter) {
                                                ProfileFilter.MY_POSTS -> Icons.Default.Article
                                                ProfileFilter.LIKED -> Icons.Default.Favorite
                                                ProfileFilter.BOOKMARKED -> Icons.Default.Bookmark
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                if (isLoading) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (displayedStories.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            Text("No posts found here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(displayedStories, key = { it.storyId }) { story ->
                        StaggeredStoryItem(
                            story = story,
                            currentUserId = currentUserId,
                            onLikeClick = { spaceViewModel.toggleLike(story) },
                            onClick = { onCommentClick(story.storyId) }
                        )
                    }
                }

        }

        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Your Wellness Vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            PulseBadgeCard(totalPoints = stats.resiliencePoints)

            WellnessStatsRow(stats = stats)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShieldMoon, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "This is a safe space. Please be kind, supportive, and respectful to everyone in the Mindset Pulse community.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            AIPrivacyCard()
            Spacer(Modifier.padding(vertical = 60.dp))
        }
    }

}

@Composable
private fun SettingsItem(onClick: ()->Unit){
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Settings",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RealWellnessStatsRow(stats: WellnessStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "Daily Goal",
            value = "${stats.sessionsToday}/5",
            modifier = Modifier.weight(1f),
            icon = Icons.Default.BatteryChargingFull,
            bg = Color(0xFF03A9F4)
        )
        StatBox(
            label = "Streak",
            value = "${stats.currentStreak}d",
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            bg = Color(0xFF4CAF50)
        )
        StatBox(
            label = "Total Points",
            value = "${stats.resiliencePoints}",
            modifier = Modifier.weight(1f),
            icon = Icons.Default.EmojiEvents,
            bg = Color(0xFF9C27B0)
        )
    }
}


