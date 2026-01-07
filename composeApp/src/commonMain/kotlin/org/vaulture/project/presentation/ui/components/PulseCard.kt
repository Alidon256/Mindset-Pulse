package org.vaulture.project.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.vaulture.project.presentation.navigation.Routes

data class PulseActionItem(
    val title: String,
    val description: String,
    val buttonText: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val route: Any
)

@Composable
fun CardCarousel(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val bgCheckIn = MaterialTheme.colorScheme.primary
    val cColorCheckIn = MaterialTheme.colorScheme.onBackground
    val bgChat = MaterialTheme.colorScheme.secondaryContainer
    val cColorChat = MaterialTheme.colorScheme.onSecondaryContainer
    val bgRhythm = MaterialTheme.colorScheme.tertiaryContainer
    val cColorRhythm = MaterialTheme.colorScheme.onTertiaryContainer


    val actions = remember {
        listOf(
            PulseActionItem(
                title = "Daily Check-In",
                description = "Track your stress levels in 60 seconds.",
                buttonText = "Start Now",
                backgroundColor = bgCheckIn,
                contentColor = cColorCheckIn,
                icon = Icons.Default.EditCalendar,
                route = Routes.CHECK_IN
            ),
            PulseActionItem(
                title = "Spaces",
                description = "Connect with like-minded people in moderated groups.",
                buttonText = "Join Chat",
                backgroundColor = bgChat,
                contentColor = cColorChat,
                icon = Icons.AutoMirrored.Filled.Chat,
                route = Routes.SPACES_HOME
            ),
            PulseActionItem(
                title = "Focus Sounds",
                description = "Curated audio for burnout prevention.",
                buttonText = "Listen",
                backgroundColor = bgRhythm,
                contentColor = cColorRhythm,
                icon = Icons.Default.MusicNote,
                route = Routes.RHYTHM_HOME
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L) // 5 seconds per card
            currentIndex = (currentIndex + 1) % actions.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        AnimatedContent(
            targetState = actions[currentIndex],
            transitionSpec = {
                (fadeIn(animationSpec = tween(500)) + slideInHorizontally(initialOffsetX = { it }))
                    .togetherWith(fadeOut(animationSpec = tween(500)) + slideOutHorizontally(targetOffsetX = { -it }))
            }
        ) { action ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, action.backgroundColor.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    action.backgroundColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                ){
                    Row(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    action.icon,
                                    contentDescription = null,
                                    tint = action.contentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = action.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = action.contentColor
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = action.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = action.contentColor.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { navController.navigate(action.route) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(action.buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Icon(
                            action.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(0.1f),
                            tint = action.contentColor
                        )
                    }
                }

            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            actions.forEachIndexed { index, _ ->
                val width by animateDpAsState(if (index == currentIndex) 24.dp else 8.dp)
                val alpha by animateFloatAsState(if (index == currentIndex) 1f else 0.3f)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
            }
        }
    }
}
