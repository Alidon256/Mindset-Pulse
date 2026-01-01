package org.vaulture.project.screens.space

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.vaulture.project.components.PostItem
import org.vaulture.project.data.models.Story
import org.vaulture.project.viewmodels.SpaceViewModel

@Composable
fun MemoriesScreen(
    stories: List<Story>, // Now correctly receives filtered results from parent
    viewModel: SpaceViewModel,
    modifier: Modifier = Modifier,
    onCommentClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val currentUser by viewModel.auth.authStateChanged.collectAsState(initial = null)
    val currentUserId = currentUser?.uid ?: ""

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            // 1. GLOBAL LOADING: Initial data fetch
            isLoading && stories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            }

            // 2. ERROR STATE: Database or Network failure
            error != null -> {
                ErrorPlaceholder(errorMessage = error!!)
            }

            // 3. SEARCH EMPTY STATE: User searched for something that doesn't exist
            stories.isEmpty() && !isLoading -> {
                EmptySearchPlaceholder()
            }

            // 4. CONTENT STATE: Display the Feed (Shared UI between Android & Web)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = stories,
                        key = { it.storyId }
                    ) { post ->
                        PostItem(
                            post = post,
                            currentUserId = currentUserId,
                            onLikeClick = { viewModel.toggleLike(post) },
                            onCommentClick = { onCommentClick(post.storyId) },
                            onProfileClick = { /* Navigate to Profile */ },
                            onBookmarkClick = { viewModel.toggleBookmark(post) },
                            onOptionClick = {}
                        )
                        ListItemDivider()
                    }
                }
            }
        }

        if (isSearching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).height(2.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptySearchPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Reflections Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Try a different keyword or check your spelling.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun ErrorPlaceholder(errorMessage: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Connection Interrupted",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ListItemDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
