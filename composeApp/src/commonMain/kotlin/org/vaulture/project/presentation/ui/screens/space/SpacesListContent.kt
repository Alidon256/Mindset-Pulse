package org.vaulture.project.presentation.ui.screens.space

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
import org.vaulture.project.domain.model.Space
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.ui.components.SpaceListItem

@Composable
fun SpacesListContent(
    spaces: List<Space>,
    activeSpaceId: String?,
    onSpaceClick: (String) -> Unit
) {
    if (spaces.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No spaces match your search. Try a different keyword or create a new community!",
                style = PoppinsTypography().bodyLarge,
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
                val isSelected = space.id == activeSpaceId
                SpaceListItem(
                    space = space,
                    isSelected = isSelected,
                    memberPhotos = space.memberPhotoUrls,
                    onClick = {
                        onSpaceClick(space.id) }
                )
            }
        }
    }
}

