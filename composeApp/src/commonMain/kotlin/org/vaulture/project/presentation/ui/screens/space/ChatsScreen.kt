package org.vaulture.project.presentation.ui.screens.space

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.vaulture.project.presentation.viewmodels.ChatViewModel
import kotlin.time.Clock

@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    onChatClick: (chatId: String) -> Unit,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Loading State
        if (uiState.isLoading && uiState.chats.isEmpty() && uiState.suggestedUsers.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }

        // Suggestions Section
        if (uiState.suggestedUsers.isNotEmpty()) {
            item {
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.ListHeader(
                    "Suggestions"
                )
            }
            items(uiState.suggestedUsers, key = { "suggestion_${it.uid}" }) { user ->
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.UnifiedChatItem(
                    imageUrl = user.photoUrl,
                    title = user.displayName ?: "New User",
                    subtitle = "Tap to start a conversation",
                    isNewUser = true,
                    onClick = {
                        viewModel.startChatWithUser(user) { chatId ->
                            onChatClick(chatId)
                        }
                    }
                )
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.ListDivider()
            }
        }

        // Active Chats Section
        if (uiState.chats.isNotEmpty()) {
            item {
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.ListHeader(
                    "Chats"
                )
            }
            items(uiState.chats, key = { it.id }) { chat ->
                val participant = chat.otherParticipant()
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.UnifiedChatItem(
                    imageUrl = participant?.avatarUrl,
                    title = participant?.name ?: "Chat User",
                    subtitle = chat.lastMessage.ifBlank { "Start chatting..." },
                    timestamp = chat.lastMessageTimestamp,
                    unreadCount = chat.unreadCount,
                    onClick = {
                        viewModel.selectChat(chat.id)
                        onChatClick(chat.id)
                    }
                )
                _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.ListDivider()
            }
        }

        // Empty State
        if (uiState.chats.isEmpty() && uiState.suggestedUsers.isEmpty() && !uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillParentMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text("No chats yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ListDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 88.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedChatItem(
    imageUrl: String?,
    title: String,
    subtitle: String,
    timestamp: Long = 0,
    unreadCount: Int = 0,
    isNewUser: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl ?: "https://placehold.co/100x100.png", // Fallback image
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isNewUser) {
                    Spacer(Modifier.width(8.dp))
                    Badge(containerColor = Color(0xFF34A853)) {
                        Text("NEW", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (timestamp > 0) {
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = _root_ide_package_.org.vaulture.project.presentation.ui.screens.space.formatTimestamp(
                        timestamp
                    ),
                    fontSize = 12.sp,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (unreadCount > 0) {
                    Badge {
                        Text(
                            text = unreadCount.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val messageDateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return if (messageDateTime.date == now.date) {
        "${messageDateTime.hour.toString().padStart(2, '0')}:${messageDateTime.minute.toString().padStart(2, '0')}"
    } else {
        "${messageDateTime.dayOfMonth}/${messageDateTime.monthNumber}/${messageDateTime.year % 100}"
    }
}
