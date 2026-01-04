/*package org.vaulture.project.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.domain.model.Space
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.mindset_pulse_logo

@Composable
fun SpaceListItem(
    space: Space,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (!space.coverImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = space.coverImageUrl,
                    contentDescription = "${space.name} avatar",
                    modifier = Modifier.size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.mindset_pulse_logo),
                    contentDescription = "Space avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }


            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(space.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    space.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Placeholder for Member Avatars
        }
    }
}*/
package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.domain.model.Space
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.mindset_pulse_logo

@Composable
fun SpaceListItem(
    space: Space,
    unreadCount: Int = 0, // Should be part of your Space model or passed here
    memberPhotos: List<String> = emptyList(), // Pass actual URLs from Firestore
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Space Cover/Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (!space.coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = space.coverImageUrl,
                        contentDescription = space.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.mindset_pulse_logo),
                        contentDescription = "Default Icon",
                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = space.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = space.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            _root_ide_package_.org.vaulture.project.presentation.ui.components.MemberAvatarStack(
                photos = memberPhotos,
                totalCount = space.memberIds.size
            )

            Spacer(modifier = Modifier.weight(1f))

            if (unreadCount > 0) {
                _root_ide_package_.org.vaulture.project.presentation.ui.components.UnreadBadge(count = unreadCount)
            }
        }
    }
}


@Composable
fun MemberAvatarStack(
    photos: List<String>,
    totalCount: Int,
    maxVisible: Int = 5
) {
    val overlapOffset = 20.dp // Tight overlap for a modern look
    val avatarSize = 32.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            // 1. Render actual photos
            val visiblePhotos = photos.take(maxVisible)
            visiblePhotos.forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = (index * overlapOffset))
                        .size(avatarSize)
                        .zIndex((maxVisible - index).toFloat()) // Ensures left-to-right layering
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. Render the "+N" badge AS PART OF the stack (No longer isolated)
            if (totalCount > visiblePhotos.size) {
                val remainingCount = totalCount - visiblePhotos.size
                Box(
                    modifier = Modifier
                        .padding(start = (visiblePhotos.size * overlapOffset.value).dp)
                        .size(avatarSize)
                        .zIndex(0f) // Bottom layer
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$remainingCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // 3. Dynamic Spacing for the Support Text
        // Calculate the total width used by the stack to prevent the text from overlapping
        val stackCount = if (totalCount > maxVisible) maxVisible + 1 else photos.size.coerceAtMost(maxVisible)
        val textPadding = (stackCount * overlapOffset) - (overlapOffset / 2)

        Spacer(Modifier.width(8.dp)) // Small gap after the cluster

        Text(
            text = if (totalCount > 0) "Active Support" else "Be the first to join",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (totalCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun UnreadBadge(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}
