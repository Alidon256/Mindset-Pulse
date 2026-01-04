package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.data.remote.AuthService
import org.vaulture.project.domain.model.Space
import org.vaulture.project.domain.model.WellnessType
import org.vaulture.project.presentation.navigation.Routes
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.AIPrivacyCard
import org.vaulture.project.presentation.ui.components.ProfileAvatar
import org.vaulture.project.presentation.ui.components.PulseBadgeCard
import org.vaulture.project.presentation.ui.components.SearchBar
import org.vaulture.project.presentation.ui.components.SectionHeader
import org.vaulture.project.presentation.ui.components.StreakBanner
import org.vaulture.project.presentation.ui.components.WellnessActionItem
import org.vaulture.project.presentation.ui.components.WellnessStatsRow
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import org.vaulture.project.presentation.viewmodels.WellnessViewModel
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.mindset_pulse_nobg_logo
import kotlin.time.Clock


enum class SpaceFilter {
    Spaces, Memories
}

@Composable
fun SpacesScreen(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (storyId: String) -> Unit,
    wellnessViewModel: WellnessViewModel,
    navController: NavController,
    onSignOut: () -> Unit,
    authService: AuthService

){
    BoxWithConstraints(modifier = Modifier.fillMaxSize()){
        val isExpanded = maxWidth > 920.dp

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())},
            label = "ResponsiveSpacesLayout"
        ){ expanded ->
            if (expanded){
                SpaceScreenExpandable(
                    viewModel = viewModel,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    onSpaceClick = onSpaceClick,
                    onCreateSpaceClick = onCreateSpaceClick,
                    onAddStoryClick = onAddStoryClick,
                    onCommentClick = onCommentClick,
                    wellnessViewModel = wellnessViewModel,
                    navController = navController,
                    onSignOut = onSignOut,
                    authService = authService
                )
            }else {
                SpacesScreenCompat(
                    viewModel = viewModel,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    onSpaceClick = onSpaceClick,
                    onCreateSpaceClick = onCreateSpaceClick,
                    onAddStoryClick = onAddStoryClick,
                    onCommentClick = onCommentClick
                )
            }
        }
    }
}

@Composable
fun SpaceScreenExpandable(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (String) -> Unit,
    wellnessViewModel: WellnessViewModel,
    navController: NavController,
    onSignOut: () -> Unit,
    authService: AuthService
){
    var selectedRailItem by remember { mutableStateOf("Spaces") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val isSearching by viewModel.isSearching.collectAsState()
    val wellnessState by wellnessViewModel.uiState.collectAsState()
    val user by authService.currentUser.collectAsState(null)
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning 🌄,"
        in 12..17 -> "Good Afternoon 😎,"
        else -> "Good Evening 🌙,"
    }

    Row(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ){
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
                        // 1. Select the activity first
                        wellnessViewModel.selectActivity(WellnessType.BREATHING)
                        // 2. Start the timer with the duration
                        //wellnessViewModel.startTimer(5)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Yoga",
                        "10 Mins",
                        Icons.Default.SelfImprovement,
                        bg = Color(0xFF4CAF50)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.YOGA)
                        //wellnessViewModel.startTimer(10)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }

                    WellnessActionItem(
                        "Meditation",
                        "15 Mins",
                        Icons.Default.Spa,
                        bg = Color(0xFF9C27B0)
                    ) {
                        wellnessViewModel.selectActivity(WellnessType.MEDITATION)
                        //wellnessViewModel.startTimer(15)
                        navController.navigate(Routes.WELLNESS_TIMER)
                    }
                }
            }
            item {
               Card(
                   onClick = { selectedRailItem = "Spaces" },
                   colors = CardDefaults.cardColors(
                       containerColor = if (selectedRailItem == "Spaces") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                   ),
                   shape = RoundedCornerShape(16.dp),
                   modifier = Modifier.fillMaxWidth()
               ) {
                   Row(
                       modifier = Modifier.padding(16.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Icon(
                           if (selectedRailItem == "Spaces") Icons.Filled.Groups else Icons.Outlined.Groups,
                           contentDescription = null,
                           tint = MaterialTheme.colorScheme.primary
                       )
                       Spacer(Modifier.width(12.dp))
                       Text(
                           "Spaces",
                           style = MaterialTheme.typography.bodyLarge,
                           color = MaterialTheme.colorScheme.onSurface
                       )
                   }
               }
           }
            item {
                Card(
                    onClick = { selectedRailItem = "Insights" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Insights") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Insights",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item{
                Card(
                    onClick = { selectedRailItem = "Settings" },
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


        Scaffold(
            topBar = {
                if (isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        onSearch = {},
                        isExpanded = isSearchExpanded,
                        onToggleExpanded = { isSearchExpanded = !isSearchExpanded },
                        placeholderText = "Search safe spaces...",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    if (!isSearchExpanded) {
                        FilterTabBar(
                            selectedFilter = selectedFilter,
                            onFilterSelected = onFilterSelected
                        )
                    }
                }
            },
            floatingActionButton = {
                if (!isSearchExpanded) {
                    val icon = if (selectedFilter == SpaceFilter.Spaces) Icons.Default.Add else Icons.Default.Edit
                    val label = if (selectedFilter == SpaceFilter.Spaces) "Create Space" else "Add Reflection"

                    ExtendedFloatingActionButton(
                        text = { Text(label) },
                        icon = { Icon(icon, null) },
                        onClick = if (selectedFilter == SpaceFilter.Spaces) onCreateSpaceClick else onAddStoryClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            modifier = Modifier.weight(1f)
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedFilter,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ContentTransition"
                ) { filter ->
                    when (filter) {
                        SpaceFilter.Spaces -> {
                            val spaces by viewModel.filteredSpaces.collectAsState()
                            SpacesListContent(
                                spaces = spaces,
                                onSpaceClick = { spaceId ->
                                    println("[UI] User clicked space: $spaceId. Triggering Join...")

                                    viewModel.joinSpace(spaceId)
                                    onSpaceClick(spaceId)
                                }
                            )
                        }
                        SpaceFilter.Memories -> {
                            val memories by viewModel.filteredFeeds.collectAsState()
                            MemoriesScreen(
                                modifier = Modifier.fillMaxSize(),
                                stories = memories, // Pass filtered data
                                viewModel = viewModel,
                                onCommentClick = onCommentClick,
                                onBookmarkClick = { storyId -> viewModel.toggleBookmark(memories.find { it.storyId == storyId }!!) }
                            )
                        }
                    }
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

            PulseBadgeCard(streak = 12)

            WellnessStatsRow()

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
                        "Your spaces are end-to-end encrypted and AI-moderated for your safety.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            AIPrivacyCard()

            Spacer(Modifier.weight(1f))

            // Helpful Resources shortcut
            OutlinedButton(
                onClick = { /* Navigate to Security Rules Detail */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Shield, null)
                Spacer(Modifier.width(8.dp))
                Text("Data Security Settings")
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Nightlife, null)
                Spacer(Modifier.width(8.dp))
                Text("Emergency Resources")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesScreenCompat(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val isSearching by viewModel.isSearching.collectAsState()

    Scaffold(
        topBar = {
            if (isSearching) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }

            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    onSearch = {},
                    isExpanded = isSearchExpanded,
                    onToggleExpanded = { isSearchExpanded = !isSearchExpanded },
                    placeholderText = "Search safe spaces...",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                if (!isSearchExpanded) {
                    FilterTabBar(
                        selectedFilter = selectedFilter,
                        onFilterSelected = onFilterSelected
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedFilter) {
                SpaceFilter.Spaces -> FloatingActionButton(
                    onClick = onCreateSpaceClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clip(CircleShape)
                ) { Icon(
                    Icons.Default.Add,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary
                ) }
                SpaceFilter.Memories -> FloatingActionButton(
                    onClick = onAddStoryClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.clip(CircleShape)
                    ) { Icon(
                    Icons.Default.Edit,
                    null,
                        tint = MaterialTheme.colorScheme.onPrimary
                ) }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = selectedFilter,
            modifier = Modifier.padding(paddingValues),
            label = "ScreenContentCompat"
        ) { filter ->
            when (filter) {
                SpaceFilter.Spaces -> {
                    // Use filteredSpaces instead of spaces
                    val spaces by viewModel.filteredSpaces.collectAsState()
                    SpacesListContent(
                        spaces = spaces,
                        onSpaceClick = { spaceId ->
                            println("🖱️ [UI] User clicked space: $spaceId. Triggering Join...")
                            viewModel.joinSpace(spaceId)
                            onSpaceClick(spaceId)
                        }
                    )
                }
                SpaceFilter.Memories -> {
                    val memories by viewModel.filteredFeeds.collectAsState()
                    MemoriesScreen(
                        modifier = Modifier.fillMaxSize(),
                        stories = memories,
                        viewModel = viewModel,
                        onCommentClick = onCommentClick,
                        onBookmarkClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTabBar(
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spaces Tab
            val isSpaces = selectedFilter == SpaceFilter.Spaces
            val spacesBg = if (isSpaces) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val spacesContent = if (isSpaces) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(spacesBg)
                    .clickable { onFilterSelected(SpaceFilter.Spaces) }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.People, contentDescription = null, tint = spacesContent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spaces",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSpaces) FontWeight.SemiBold else FontWeight.Medium,
                        color = spacesContent
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Memories Tab
            val isMemories = selectedFilter == SpaceFilter.Memories
            val memoriesBg = if (isMemories) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val memoriesContent = if (isMemories) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(memoriesBg)
                    .clickable { onFilterSelected(SpaceFilter.Memories) }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Stream, contentDescription = null, tint = memoriesContent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memories",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isMemories) FontWeight.SemiBold else FontWeight.Medium,
                        color = memoriesContent
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

