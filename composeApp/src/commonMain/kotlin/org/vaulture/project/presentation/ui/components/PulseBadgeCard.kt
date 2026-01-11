package org.vaulture.project.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vaulture.project.domain.model.Level
import org.vaulture.project.presentation.theme.PoppinsTypography

private val mindsetLevels = listOf(
    Level("Initiate", Icons.Default.Brightness1, Color(0xFFB0BEC5), Color(0xFF78909C), 0),
    Level("Seeker", Icons.Default.WavingHand, Color(0xFF90CAF9), Color(0xFF42A5F5), 100),
    Level("Apprentice", Icons.Default.Psychology, Color(0xFF80DEEA), Color(0xFF26C6DA), 250),
    Level("Acolyte", Icons.Default.Spa, Color(0xFF81C784), Color(0xFF66BB6A), 500),
    Level("Adept", Icons.Default.SelfImprovement, Color(0xFFAED581), Color(0xFF9CCC65), 800),
    Level("Defender", Icons.Default.Shield, Color(0xFFFFF176), Color(0xFFFFEE58), 1200),
    Level("Guardian", Icons.Default.Security, Color(0xFFFFB74D), Color(0xFFFFA726), 1800),
    Level("Vindicator", Icons.Default.GppGood, Color(0xFFFF8A65), Color(0xFFFF7043), 2500),
    Level("Champion", Icons.Default.EmojiEvents, Color(0xFFF06292), Color(0xFFEC407A), 3500),
    Level("Hero", Icons.Default.MilitaryTech, Color(0xFFBA68C8), Color(0xFFAB47BC), 5000),
    Level("Legend", Icons.Default.WorkspacePremium, Color(0xFF9575CD), Color(0xFF7E57C2), 7000),
    Level("Mythus", Icons.Default.AutoStories, Color(0xFF7986CB), Color(0xFF5C6BC0), 9000),
    Level("Sage", Icons.Default.FilterVintage, Color(0xFF64B5F6), Color(0xFF42A5F5), 12000),
    Level("Oracle", Icons.Default.Visibility, Color(0xFF4DD0E1), Color(0xFF26C6DA), 15000),
    Level("Luminary", Icons.Default.Flare, Color(0xFF4DB6AC), Color(0xFF26A69A), 20000),
    Level("Ascendant", Icons.Default.TrendingUp, Color(0xFFDCE775), Color(0xFFD4E157), 25000),
    Level("Transcendent", Icons.Default.Key, Color(0xFFFFF176), Color(0xFFFFEE58), 35000),
    Level("Celestial", Icons.Default.Star, Color(0xFFFFD54F), Color(0xFFFFCA28), 50000),
    Level("Zenith", Icons.Default.WbSunny, Color(0xFFFFB74D), Color(0xFFFFA726), 75000),
    Level("Pulse Master", Icons.Default.AutoAwesome, Color(0xFFE57373), Color(0xFFEF5350), 100000)
)

// Helper function to find the current level based on points
private fun getLevelForPoints(points: Int): Level {
    return mindsetLevels.lastOrNull { points >= it.threshold } ?: mindsetLevels.first()
}

@Composable
fun PulseBadgeCard(
    totalPoints: Int
) {

    val currentLevel = remember(totalPoints) { getLevelForPoints(totalPoints) }
    val nextLevel = mindsetLevels.getOrNull(mindsetLevels.indexOf(currentLevel) + 1)
    val progressToNextLevel = if (nextLevel != null) {
        val pointsInCurrentLevel = totalPoints - currentLevel.threshold
        val pointsForNextLevel = nextLevel.threshold - currentLevel.threshold
        (pointsInCurrentLevel.toFloat() / pointsForNextLevel).coerceIn(0f, 1f)
    } else {
        1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressToNextLevel,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 50f),
        label = "LevelProgressAnimation"
    )

    var oldPoints by remember { mutableStateOf(totalPoints) }
    val scale by animateFloatAsState(
        targetValue = if (totalPoints != oldPoints) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "PulseAnimation"
    )

    LaunchedEffect(totalPoints) {
        oldPoints = totalPoints
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(currentLevel.color1, currentLevel.color2)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentLevel.icon,
                        contentDescription = "Badge",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentLevel.name,
                        style = PoppinsTypography().titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Mindset Level ${mindsetLevels.indexOf(currentLevel) + 1}",
                        style = PoppinsTypography().bodySmall,
                        color = currentLevel.color2,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = currentLevel.color1,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$totalPoints XP",
                        style = PoppinsTypography().labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (nextLevel != null) {
                        Text(
                            text = "${nextLevel.threshold} XP to next level",
                            style = PoppinsTypography().labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "Max Level Reached!",
                            style = PoppinsTypography().labelSmall,
                            color = currentLevel.color2,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
