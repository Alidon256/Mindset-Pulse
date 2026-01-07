package org.vaulture.project.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.data.repos.ItemType
import org.vaulture.project.data.repos.SearchableItem

@Composable
fun SearchBar(
    query:String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String ="Search...",
    onSuggestionClick: (SearchableItem) -> Unit = {},
    suggestions: List<SearchableItem> = emptyList(),
    recentSearches: List<String> = emptyList(),
    onRecentSearchClick: (String) -> Unit = {}
){
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val searchBarHorizontalPadding by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 300),
        label = "SearchBarPadding"
    )
    val searchBarElevation by animateDpAsState(
        targetValue = if (isExpanded) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "SearchBarElevation"
    )

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = searchBarHorizontalPadding)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(if (isExpanded) 0.dp else 28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = searchBarElevation),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clickable(
                        enabled = !isExpanded,
                        onClick = {
                            if (!isExpanded) onToggleExpanded()
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleExpanded) {
                    Crossfade(
                        targetState = isExpanded,
                        animationSpec = tween(300),
                        label = "SearchIconCrossfade"
                    ) { expanded ->
                        Icon(
                            imageVector = if (expanded) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Search,
                            contentDescription = if (expanded) "Collapse Search" else "Expand Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (isExpanded) {
                        TextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && isExpanded) {
                                        // if (query.isEmpty()) onToggleExpand()
                                    }
                                },
                            placeholder = {
                                Text(
                                    placeholderText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onSearch(query)
                                    focusManager.clearFocus() // Hide keyboard
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } else {
                        Text(
                            text = placeholderText,
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp)
                                .align(Alignment.CenterStart),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isExpanded && query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") /* Clears query */ }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Query",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (isExpanded && query.isEmpty()) {
                    Spacer(Modifier.width(48.dp))
                }
            }
        }

        // Suggestions List
        AnimatedVisibility(
            visible = isExpanded && (suggestions.isNotEmpty() || (query.isEmpty() && recentSearches.isNotEmpty())),
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 100
                )
            ) + slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { -it / 2 }),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)) + slideOutVertically(
                animationSpec = tween(durationMillis = 200),
                targetOffsetY = { -it / 2 })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = if (isExpanded) 0.dp else 16.dp),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
            ) {
                Column {
                    if (query.isEmpty() && recentSearches.isNotEmpty()) {
                        Text(
                            "Recent Searches",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                        recentSearches.forEach { recent ->
                            SuggestionRow(
                                icon = Icons.Default.History,
                                text = recent,
                                highlight = "", // No highlight for recent searches
                                onClick = {
                                    onQueryChange(recent) // Set query to recent
                                    onRecentSearchClick(recent) // Specific callback
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    } else if (suggestions.isNotEmpty()) {
                        if (query.isNotBlank()) {
                            SuggestionRow(
                                icon = Icons.Default.Search,
                                text = query,
                                highlight = query, // Highlight the typed query within itself
                                onClick = {
                                    onSearch(query)
                                    focusManager.clearFocus()
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                        suggestions.filterNot { it.title.equals(query, ignoreCase = true) }
                            .forEachIndexed { idx, suggestion ->
                                SuggestionRow(
                                    icon = when (suggestion.type) {
                                        ItemType.VIDEO -> Icons.Default.VideoLibrary
                                        ItemType.CHANNEL -> Icons.Default.AccountBox
                                        ItemType.SHORTS -> Icons.Default.SmartDisplay
                                        ItemType.AUDIO -> Icons.Default.MusicNote
                                        ItemType.SONG -> TODO()
                                        ItemType.ARTIST -> TODO()
                                        ItemType.ALBUM -> TODO()
                                        ItemType.PLAYLIST -> TODO()
                                        ItemType.GENRE -> TODO()
                                        ItemType.PODCAST -> TODO()
                                    },
                                    text = suggestion.title,
                                    highlight = query,
                                    onClick = {
                                        onQueryChange(suggestion.title)
                                        onSuggestionClick(suggestion)
                                    }
                                )
                                if (idx != suggestions.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}
@Composable
private fun SuggestionRow(
    icon: ImageVector,
    text: String,
    highlight: String,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val annotatedText = remember(text, highlight, primaryColor) {
        if (highlight.isBlank()) return@remember AnnotatedString(text)
        val startIndex = text.indexOf(highlight, ignoreCase = true)
        if (startIndex == -1) return@remember AnnotatedString(text)
        val endIndex = startIndex + highlight.length
        buildAnnotatedString {
            append(text.substring(0, startIndex))
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            ) {
                append(text.substring(startIndex, endIndex))
            }
            append(text.substring(endIndex))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun SearchBarPreviewCollapsed() {
    MaterialTheme {
        var isExpanded by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded }
        )
    }
}

@Preview
@Composable
fun SearchBarPreviewExpandedEmpty() {
    MaterialTheme {
        var isExpanded by remember { mutableStateOf(true) }
        var query by remember { mutableStateOf("") }
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded },
            recentSearches = listOf("Mindset", "Mental health", "wellness", "Relaxation")
        )
    }
}

@Preview
@Composable
fun SearchBarPreviewExpandedWithQuery() {
    MaterialTheme {
        var isExpanded by remember { mutableStateOf(true) }
        var query by remember { mutableStateOf("Compose") }
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded },
            suggestions = listOf(),
            recentSearches = listOf("Mindset", "Mental health", "wellness", "Relaxation")
        )
    }
}

