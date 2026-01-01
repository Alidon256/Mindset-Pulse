package org.vaulture.project.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.vaulture.project.theme.PulseColors
import org.vaulture.project.Routes

data class PulseActionItem(
    val title: String,
    val description: String,
    val buttonText: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val route: Any // Navigation route
)

@Composable
fun CardCarousel(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val actions = remember {
        listOf(
            PulseActionItem(
                title = "Daily Check-In",
                description = "Track your stress levels in 60 seconds.",
                buttonText = "Start Now",
                backgroundColor = PulseColors.SoftBlue,
                contentColor = PulseColors.TextBlack,
                icon = Icons.Default.EditCalendar,
                route = Routes.CHECK_IN
            ),
            PulseActionItem(
                title = "Community Chats",
                description = "Connect with like-minded people in moderated groups.",
                buttonText = "Join Chat",
                backgroundColor = PulseColors.WarmBeige,
                contentColor = PulseColors.TextBlack,
                icon = Icons.AutoMirrored.Filled.Chat,
                route = Routes.SPACES_HOME
            ),
            PulseActionItem(
                title = "Focus Sounds",
                description = "Curated audio for burnout prevention.",
                buttonText = "Listen",
                backgroundColor = PulseColors.MintGreen,
                contentColor = PulseColors.TextBlack,
                icon = Icons.Default.MusicNote,
                route = Routes.RHYTHM_HOME
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }

    // Automatic Rotation Logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L) // 5 seconds per card
            currentIndex = (currentIndex + 1) % actions.size
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Card with Animation
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = action.backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
                                containerColor = PulseColors.TextBlack,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(action.buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Large Decorative Icon on the right
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

        // Professional Pager Indicator
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
