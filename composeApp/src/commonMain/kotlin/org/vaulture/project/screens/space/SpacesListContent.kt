/*package org.vaulture.project.screens.space

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.vaulture.project.data.models.Space

@Composable
fun SpacesListContent(spaces: List<Space>, onSpaceClick: (String) -> Unit) {
    // produceState converts a Flow into a Compose State object.
    val uiState by produceState<SpacesUiState>(initialValue = SpacesUiState.Loading, key1 = Unit) {
        val firestore = Firebase.firestore
        firestore.collection("spaces")
            .snapshots // This is a Flow<QuerySnapshot>
            .map<QuerySnapshot, SpacesUiState> { snapshot ->
                // If successful, map the QuerySnapshot to our Success state
                val spaceList = snapshot.documents.map { it.data<Space>() }
                SpacesUiState.Success(spaceList)
            }
            .catch { exception ->
                // If an exception occurs in the flow, catch it and emit our Error state
                println("Error fetching spaces: ${exception.message}")
                emit(SpacesUiState.Error(exception.message ?: "An unknown error occurred."))
            }
            .collect { state ->
                // Collect the emitted SpacesUiState (either Success or Error)
                value = state
            }
    }

    // Render the UI based on the current state from produceState
    when (val state = uiState) {
        is SpacesUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SpacesUiState.Success -> {
            if (state.spaces.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No spaces yet. Be the first to create a community!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.spaces, key = { it.id }) { space ->
                        SpaceListItem(space = space, onClick = { onSpaceClick(space.id) })
                    }
                }
            }
        }
        is SpacesUiState.Error -> {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Error loading spaces: ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}*/
package org.vaulture.project.screens.space

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.vaulture.project.components.SpaceListItem
import org.vaulture.project.data.models.Space

@Composable
fun SpacesListContent(
    spaces: List<Space>, // These are the filtered results from the ViewModel
    onSpaceClick: (String) -> Unit
) {
    if (spaces.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No spaces match your search. Try a different keyword or create a new community!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(spaces, key = { it.id }) { space ->
                // Ensure this component is defined in your project
                SpaceListItem(
                    space = space,
                    unreadCount = space.unreadCount,
                    memberPhotos = space.memberPhotoUrls,
                    onClick = { onSpaceClick(space.id) }
                )
            }
        }
    }
}

