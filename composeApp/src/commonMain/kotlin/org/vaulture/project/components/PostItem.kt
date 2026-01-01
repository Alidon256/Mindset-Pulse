package org.vaulture.project.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.vaulture.project.data.models.Story
import org.vaulture.project.utils.formatTimestamp

@Composable
fun PostItem(
    post: Story,
    currentUserId: String,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onOptionClick: (String) -> Unit = {}
) {
    val isLiked = post.likedBy.contains(currentUserId)
    val isBookmarked = post.bookmarkedBy.contains(currentUserId) // Check bookmark state

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userProfileUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onProfileClick(post.userId) },
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onProfileClick(post.userId) }
                    )
                    Text(
                        text = formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onOptionClick(post.storyId) }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Perfected "Read More" Text Content ---
            if (!post.textContent.isNullOrBlank()) {
                var isExpanded by remember { mutableStateOf(false) }
                val textLimit = 150 // Character limit before "See more"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateContentSize() // Smooth transition when expanding
                ) {
                    val annotatedText = buildAnnotatedString {
                        if (post.textContent.length > textLimit && !isExpanded) {
                            append(post.textContent.take(textLimit))
                            withStyle(style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(" ... See more")
                            }
                        } else {
                            append(post.textContent)
                            if (isExpanded && post.textContent.length > textLimit) {
                                withStyle(style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(" Show less")
                                }
                            }
                        }
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        modifier = Modifier.clickable {
                            if (post.textContent.length > textLimit) isExpanded = !isExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- Optimized Media Content ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                when (post.contentType) {
                    Story.ContentType.PHOTO -> if (!post.contentUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = post.contentUrl,
                            contentDescription = "Post image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 330.dp) // Prevents ultra-long vertical images from breaking UX
                                .wrapContentHeight(),
                            contentScale = ContentScale.FillWidth, // Facebook style: fill width, clip height if too long
                        )
                    }
                    Story.ContentType.VIDEO -> if (!post.contentUrl.isNullOrEmpty()) {
                        VideoPlayerPlaceholder(
                            thumbnailUrl = post.thumbnailUrl,
                            aspectRatio = post.aspectRatio ?: (16f / 9f)
                        )
                    }
                    else -> {}
                }
            }

            // --- Stats Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Icon + Count
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${post.likeCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${post.commentCount} comments",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PostActionButton(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    text = "Support",
                    onClick = { onLikeClick(post.storyId) },
                    isActivated = isLiked
                )
                PostActionButton(
                    icon = Icons.AutoMirrored.Filled.Comment,
                    text = "Reply",
                    onClick = { onCommentClick(post.storyId) }
                )
                PostActionButton(
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                    text = "Save",
                    onClick = { onBookmarkClick(post.storyId) },
                    isActivated = isBookmarked
                )
            }
        }
    }
}

@Composable
fun PostActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isActivated: Boolean = false
) {
    val color = if (isActivated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    TextButton(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun VideoPlayerPlaceholder(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.8f)
            )
        }
        // Play button: Using PlayArrow for better compatibility
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.padding(12.dp).fillMaxSize()
            )
        }
    }
}
