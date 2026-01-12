package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.vaulture.project.domain.model.WellnessType
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.viewmodels.WellnessPhase
import org.vaulture.project.presentation.viewmodels.WellnessUiState
import org.vaulture.project.presentation.viewmodels.WellnessViewModel

@Composable
fun WellnessTimerScreen(viewModel: WellnessViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val currentActivity = state.currentActivity ?: return

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            currentActivity.gradientStartColor.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        val contentModifier = if (maxWidth > 920.dp) {
            Modifier
                .width(500.dp)
                .align(Alignment.Center)
        } else {
            Modifier.fillMaxSize()
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back"
            )
        }

        AnimatedContent(
            targetState = state.phase,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "WellnessPhaseAnimation"
        ) { phase ->
            when (phase) {
                WellnessPhase.SETUP -> SetupContent(
                    contentModifier,
                    viewModel,
                    onBack
                )
                WellnessPhase.ACTIVE -> ActiveTimerContent(
                    contentModifier,
                    state,
                    viewModel
                )
                WellnessPhase.SUMMARY -> SummaryContent(
                    contentModifier,
                    state,
                    onBack
                )
            }
        }
    }
}

@Composable
private fun SetupContent(modifier: Modifier, viewModel: WellnessViewModel, onBack: () -> Unit) {
    val activity = viewModel.uiState.value.currentActivity ?: return

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.5f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activity.gradientStartColor.copy(alpha = 0.5f),
                            activity.gradientEndColor.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = activity.label,
                modifier = Modifier.size(64.dp),
                tint = activity.gradientStartColor
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Prepare for ${activity.label}",
            style = PoppinsTypography().headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        ExpandableInfoCard(
            title = "Why this helps",
            description = activity.description
        )

        Spacer(Modifier.weight(1f))

        Text(
            "Select duration",
            style = PoppinsTypography().titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 5, 10, 15).forEach { mins ->
                OutlinedButton(
                    onClick = { viewModel.startTimer(mins) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$mins min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text(
                "Maybe later",
                style = PoppinsTypography().titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.weight(0.2f))
    }
}

@Composable
private fun ExpandableInfoCard(title: String, description: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "ExpandIconRotation")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = PoppinsTypography().titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = description,
                    style = PoppinsTypography().bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
private fun ActiveTimerContent(
    modifier: Modifier,
    state: WellnessUiState,
    viewModel: WellnessViewModel
) {
    val currentType = state.currentActivity ?: WellnessType.BREATHING

    val pulseConfig = when (currentType) {
        WellnessType.BREATHING -> Pair(4000, 1.15f)
        WellnessType.MEDITATION -> Pair(8000, 1.08f)
        WellnessType.YOGA -> Pair(12000, 1.04f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mindful_rhythm")
    val animScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseConfig.second,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseConfig.first, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
         Text(
            text = state.breathText,
            style = PoppinsTypography().headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(80.dp).wrapContentHeight()
        )

        Text(
            text = currentType.name,
            style = PoppinsTypography().labelLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(48.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(300.dp)
                .scale(animScale)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )

            val animatedProgress by animateFloatAsState(
                targetValue = if (state.totalDurationSeconds > 0)
                    state.timeLeftSeconds.toFloat() / state.totalDurationSeconds.toFloat()
                else 0f,
                animationSpec = tween(1000, easing = LinearEasing),
                label = "sessionProgress"
            )

            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(state.timeLeftSeconds),
                    style = PoppinsTypography().displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = currentType.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        OutlinedButton(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.Close,
                null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "End Session Early",
                style = PoppinsTypography().titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                   Text(
                    "End session early?",
                    style = PoppinsTypography().titleMedium,
                    fontWeight = FontWeight.SemiBold
                   )
                },
                text = {
                    Text(
                        "Every moment of mindfulness builds your Mindset Battery. Are you sure you want to pause your progress?",
                        style = PoppinsTypography().bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            viewModel.resetToSetup()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                      Text(
                        "End Session",
                        style = PoppinsTypography().titleMedium,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                      Text(
                        "Stay Present",
                        style = PoppinsTypography().titleMedium,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
private fun SummaryContent(modifier: Modifier, state: WellnessUiState, onBack: () -> Unit) {
    Column(
        modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // XP Badge
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.AutoAwesome,
                null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(0.1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "RQ POINTS",
                    style = PoppinsTypography().labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "+${state.stats.resiliencePoints}",
                    style = PoppinsTypography().displaySmall,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Your Mindset Battery is refilling!",
            style = PoppinsTypography().titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            "Great job on completing your session.",
            style = PoppinsTypography().bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                val isActive = index < state.stats.sessionsToday
                Box(
                    Modifier.size(width = 40.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    null,
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        "${state.stats.currentStreak} Day Streak!",
                        style = PoppinsTypography().titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Consistency is the key to clarity.",
                        style = PoppinsTypography().bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Return to Dashboard",
                style = PoppinsTypography().titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
