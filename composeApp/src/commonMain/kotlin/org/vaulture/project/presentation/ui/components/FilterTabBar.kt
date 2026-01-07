package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vaulture.project.presentation.ui.screens.space.SpaceFilter

@Composable
fun FilterTabBar(
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
                Icon(
                    Icons.Outlined.People,
                    contentDescription = null,
                    tint = spacesContent,
                    modifier = Modifier.size(20.dp)
                )
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
                Icon(
                    Icons.Outlined.Stream,
                    contentDescription = null,
                    tint = memoriesContent,
                    modifier = Modifier.size(20.dp)
                )
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
