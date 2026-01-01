package org.vaulture.project.screens.space

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.components.FilterTabBar
import org.vaulture.project.components.PulseBadgeCard
import org.vaulture.project.components.SearchBar
import org.vaulture.project.components.WellnessStatsRow
import org.vaulture.project.data.models.Space
import org.vaulture.project.utils.buildHighlightedText
import org.vaulture.project.viewmodels.SpaceViewModel
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.mindset_pulse_logo
import vaulture.composeapp.generated.resources.mindset_pulse_nobg_logo


@Composable
fun SpacesHomeScreen(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (storyId: String) -> Unit
){
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
                    onCommentClick = onCommentClick
                )
            }else {
                SpacesHomeScreenCompat(
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
fun SpaceHomeScreenExpandable(
    viewModel: SpaceViewModel,
    selectedFilter: SpaceFilter,
    onFilterSelected: (SpaceFilter) -> Unit,
    onSpaceClick: (spaceId: String) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onCommentClick: (String) -> Unit
){
    var selectedRailItem by remember { mutableStateOf("Spaces") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val isSearching by viewModel.isSearching.collectAsState()

    Row(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ){
        NavigationRail(
            header = {
                Icon(
                    painter = painterResource(Res.drawable.mindset_pulse_nobg_logo),
                    contentDescription = "Mindset Pulse Logo",
                    modifier = Modifier.padding(top = 16.dp, bottom = 48.dp).size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxHeight()
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            NavigationRailItem(
                selected = selectedRailItem == "Spaces",
                onClick = { selectedRailItem = "Spaces" },
                icon = { Icon(if (selectedRailItem == "Spaces") Icons.Filled.Groups else Icons.Outlined.Groups, null) },
                label = { Text("Spaces") }
            )
            NavigationRailItem(
                selected = selectedRailItem == "Insights",
                onClick = { selectedRailItem = "Insights" },
                icon = { Icon(Icons.Default.Analytics, null) },
                label = { Text("Insights") }
            )
            NavigationRailItem(
                selected = selectedRailItem == "Settings",
                onClick = { selectedRailItem = "Settings" },
                icon = { Icon(Icons.Default.Settings, null) },
                label = { Text("Settings") }
            )
        }

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
                            // Use filteredSpaces instead of spaces
                            val spaces by viewModel.filteredSpaces.collectAsState()
                            SpacesListContent(spaces = spaces, onSpaceClick = onSpaceClick)
                        }
                        SpaceFilter.Memories -> {
                            // Use filteredFeeds instead of feeds
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

        // --- RIGHT "WELLNESS VAULT" PANEL ---
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Your Wellness Vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Current Streak Component
            PulseBadgeCard(streak = 12)

            // Dynamic Stats (Check-ins, Minutes, Consistency)
            WellnessStatsRow()

            // AI Responsible Disclaimer (Crucial for competition ethics)
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

            Spacer(Modifier.weight(1f))

            // Helpful Resources shortcut
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


// NOTE: You would apply the same fix to SpacesScreenCompat if it exists.
// I've added a placeholder here to show how.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesHomeScreenCompat(
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
                    SpacesListContent(spaces = spaces, onSpaceClick = onSpaceClick)
                }
                SpaceFilter.Memories -> {
                    // Use filteredFeeds instead of feeds
                    val memories by viewModel.filteredFeeds.collectAsState()
                    MemoriesScreen(
                        modifier = Modifier.fillMaxSize(),
                        stories = memories, // Pass filtered data
                        viewModel = viewModel,
                        onCommentClick = onCommentClick,
                        onBookmarkClick = {}
                    )
                }
            }
        }
    }
}


