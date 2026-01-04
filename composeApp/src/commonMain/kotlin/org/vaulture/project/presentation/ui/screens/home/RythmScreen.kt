package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.vaulture.project.data.repos.SearchContext
import org.vaulture.project.data.repos.SearchableItem
import org.vaulture.project.presentation.viewmodels.RhythmViewModel
import kotlin.math.roundToInt
import kotlin.text.contains
import kotlin.text.lowercase

// Categories for classification
data class RhythmCategory(val name: String, val tags: List<String>)

@Composable
fun RhythmHomeScreen(
    navController: NavHostController,
    viewModel: RhythmViewModel, // Injected from AppNavigation
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val rawSuggestions by viewModel.searchSuggestions.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    val recentSearches = remember { mutableStateListOf<String>() }

    // Multiplatform alternative to LocalConfiguration
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isTablet = screenWidth >= 600.dp
        val padding = if (isTablet) 32.dp else 16.dp
        val searchBarWidthFraction = if (isTablet) 0.7f else 1f

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // Header: Search & Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isSearchExpanded) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                _root_ide_package_.org.vaulture.project.presentation.ui.components.SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { newQuery ->
                        viewModel.updateSearchQuery(newQuery, SearchContext.RHYTHM)
                    },
                    onSearch = { query ->
                        if (query.isNotBlank() && !recentSearches.contains(query)) {
                            recentSearches.add(0, query)
                        }
                        isSearchExpanded = false
                    },
                    isExpanded = isSearchExpanded,
                    onToggleExpanded = { isSearchExpanded = !isSearchExpanded },
                    suggestions = rawSuggestions.map {
                        SearchableItem.RhythmItem(it)
                    },
                    onSuggestionClick = { suggestion ->
                        viewModel.updateSearchQuery(suggestion.title, SearchContext.RHYTHM)
                        isSearchExpanded = false
                    },
                    recentSearches = recentSearches.toList(),
                    placeholderText = "Search Rhythm Beats...",
                    modifier = Modifier.weight(1f).fillMaxWidth(searchBarWidthFraction)
                )
            }

            _root_ide_package_.org.vaulture.project.presentation.ui.screens.home.RhythmHomeContent(
                searchQuery = uiState.searchQuery,
                navController = navController,
                viewModel = viewModel,
                padding = padding,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun RhythmHomeContent(
    searchQuery: String,
    navController: NavHostController,
    viewModel: RhythmViewModel,
    padding: Dp,
    screenWidth: Dp
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filter tracks based on the search query
    val filteredBySearch = remember(uiState.tracks, searchQuery) {
        if (searchQuery.isBlank()) uiState.tracks
        else uiState.tracks.filter { track ->
            track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true) ||
                    track.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val categories = remember {
        listOf(
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.home.RhythmCategory(
                "Focus",
                listOf("productivity", "concentration", "work")
            ),
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.home.RhythmCategory(
                "Sleep",
                listOf("calm", "sleep", "ambient")
            ),
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.home.RhythmCategory(
                "Relax",
                listOf("chill", "peaceful", "meditation")
            ),
            _root_ide_package_.org.vaulture.project.presentation.ui.screens.home.RhythmCategory(
                "Energy",
                listOf("upbeat", "motivation", "workout")
            )
        )
    }

    var selectedCategoryName by remember { mutableStateOf<String?>(null) }

    // Apply category filter on top of search results
    val finalTracks = remember(filteredBySearch, selectedCategoryName) {
        if (selectedCategoryName == null) filteredBySearch
        else filteredBySearch.filter { track ->
            track.tags.any { tag ->
                categories.find { it.name == selectedCategoryName }?.tags?.any { catTag ->
                    tag.equals(catTag, ignoreCase = true)
                } == true
            }
        }
    }

    val columnCount = (screenWidth.value / 180).roundToInt().coerceAtLeast(2)

    if (selectedCategoryName == null && searchQuery.isBlank()) {
        // --- Category Overview Mode ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                _root_ide_package_.org.vaulture.project.presentation.ui.components.FilterChips(
                    categories = categories.map { it.name },
                    selectedCategory = selectedCategoryName,
                    onFilterSelected = { selected ->
                        selectedCategoryName =
                            if (selectedCategoryName == selected) null else selected
                    },
                    modifier = Modifier.padding(horizontal = padding)
                )
            }

            items(categories) { category ->
                val categoryTracks = uiState.tracks.filter { track ->
                    track.tags.any { tag -> category.tags.contains(tag.lowercase()) }
                }.take(8)

                if (categoryTracks.isNotEmpty()) {
                    Column {
                        Text(
                            text = category.name,
                            style = _root_ide_package_.org.vaulture.project.presentation.theme.PoppinsTypography().headlineMedium.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = padding, vertical = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = padding),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categoryTracks) { track ->
                                _root_ide_package_.org.vaulture.project.presentation.ui.components.RhythmItem(
                                    track = track,
                                    onClick = {
                                        viewModel.playTrack(track)
                                        navController.navigate("rhythmPlayer/${track.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- Search / Grid Mode ---
        Column(modifier = Modifier.fillMaxSize()) {
            _root_ide_package_.org.vaulture.project.presentation.ui.components.FilterChips(
                categories = categories.map { it.name },
                selectedCategory = selectedCategoryName,
                onFilterSelected = { selected ->
                    selectedCategoryName = if (selectedCategoryName == selected) null else selected
                },
                modifier = Modifier.padding(horizontal = padding, vertical = 8.dp)
            )

            AnimatedContent(targetState = finalTracks, label = "GridTransition") { tracksToShow ->
                if (tracksToShow.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matches for '$searchQuery'" else "Empty category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        modifier = Modifier.fillMaxSize().padding(horizontal = padding),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(tracksToShow) { track ->
                            _root_ide_package_.org.vaulture.project.presentation.ui.components.RhythmItem(
                                track = track,
                                isSelected = uiState.currentTrack?.id == track.id,
                                onClick = {
                                    viewModel.playTrack(track)
                                    navController.navigate("rhythmPlayer/${track.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
