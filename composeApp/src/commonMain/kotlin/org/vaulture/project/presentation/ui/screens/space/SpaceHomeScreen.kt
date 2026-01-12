package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.vaulture.project.data.remote.AuthService
import org.vaulture.project.domain.model.User
import org.vaulture.project.domain.model.WellnessType
import org.vaulture.project.presentation.navigation.Routes
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.AIPrivacyCard
import org.vaulture.project.presentation.ui.components.FilterTabBar
import org.vaulture.project.presentation.ui.components.PulseBadgeCard
import org.vaulture.project.presentation.ui.components.SearchBar
import org.vaulture.project.presentation.ui.components.SectionHeader
import org.vaulture.project.presentation.ui.components.StreakBanner
import org.vaulture.project.presentation.ui.components.WellnessActionItem
import org.vaulture.project.presentation.ui.components.WellnessStatsRow
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import org.vaulture.project.presentation.viewmodels.WellnessViewModel
import kotlin.time.Clock


@Composable
fun SpacesHomeScreen(
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
){
    val user by viewModel.userProfile.collectAsState()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()){
        val isExpanded = maxWidth > 920.dp

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())},
            label = "ResponsiveSpacesLayout"
        ){ expanded ->
            if (expanded){
                SpaceHomeScreenExpandable(
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
                    user = user,
                )
            }else {
                SpacesHomeScreenCompat(
                    viewModel = viewModel,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    onSpaceClick = onSpaceClick,
                    onCreateSpaceClick = onCreateSpaceClick,
                    onAddStoryClick = onAddStoryClick,
                    onCommentClick = onCommentClick,
                    navController = navController
                )
            }
        }
    }
}


@Composable
fun SpaceHomeScreenExpandable(
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
    user: User?,
){
    val activeId by viewModel.activeSpaceId.collectAsState()
    val statsState by wellnessViewModel.uiState.collectAsState()
    var selectedRailItem by remember { mutableStateOf("Spaces") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val isSearching by viewModel.isSearching.collectAsState()
    val wellnessState by wellnessViewModel.uiState.collectAsState()
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
                .weight(0.3f)
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
                        text = "${" "}${user?.displayName?.substringBefore(' ') ?: user?.username?.substringBefore(' ') ?: "..."}",
                        style = PoppinsTypography().bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
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
                            if (selectedRailItem == "Spaces")
                                Icons.Filled.Groups
                            else
                                Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Spaces",
                            style = PoppinsTypography().bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                Card(
                    onClick = { selectedRailItem = "Insights" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Insights")
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
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
                            style = PoppinsTypography().bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item{
                Card(
                    onClick = { selectedRailItem = "Settings" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRailItem == "Settings")
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
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
                            style = PoppinsTypography().bodyLarge,
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
                    Text(
                        "SignOut",
                        style = PoppinsTypography().bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
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
            modifier = Modifier.weight(0.4f)
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
                                },
                                activeSpaceId = activeId
                            )
                        }
                        SpaceFilter.Memories -> {
                            val memories by viewModel.filteredFeeds.collectAsState()
                            MemoriesScreen(
                                modifier = Modifier.fillMaxSize(),
                                stories = memories,
                                viewModel = viewModel,
                                onCommentClick = onCommentClick,
                                onBookmarkClick = { storyId -> viewModel.toggleBookmark(memories.find { it.storyId == storyId }!!) },
                                navController = navController
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
                .weight(0.3f)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Your Wellness Vault",
                style = PoppinsTypography().titleLarge,
                fontWeight = FontWeight.Bold
            )

            PulseBadgeCard(totalPoints = statsState.stats.resiliencePoints)

            WellnessStatsRow(stats = statsState.stats)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ShieldMoon,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Your spaces are end-to-end encrypted and AI-moderated for your safety.",
                        style = PoppinsTypography().bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            AIPrivacyCard()
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesHomeScreenCompat(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (String) -> Unit,
    navController: NavController
) {
    val activeId by viewModel.activeSpaceId.collectAsState()
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
                    val spaces by viewModel.filteredSpaces.collectAsState()
                    SpacesListContent(
                        spaces = spaces,
                        onSpaceClick = { spaceId ->
                            println("[UI] User clicked space: $spaceId. Triggering Join...")
                            viewModel.joinSpace(spaceId)
                            onSpaceClick(spaceId)
                        },
                        activeSpaceId = activeId
                    )
                }
                SpaceFilter.Memories -> {
                    val memories by viewModel.filteredFeeds.collectAsState()
                    MemoriesScreen(
                        modifier = Modifier.fillMaxSize(),
                        stories = memories,
                        viewModel = viewModel,
                        onCommentClick = onCommentClick,
                        onBookmarkClick = {},
                        navController = navController
                    )
                }
            }
        }
    }
}


