/*package org.vaulture.project.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vaulture.project.viewmodels.WellnessViewModel

@Composable
fun WellnessTimerScreen(viewModel: WellnessViewModel, onBack: () -> Unit) {
    // 1. Observe the State from the shared ViewModel
    val state by viewModel.uiState.collectAsState()

    // 2. Calculate Progress (0.0 to 1.0)
    val progress = remember(state.timeLeftSeconds, state.totalDurationSeconds) {
        if (state.totalDurationSeconds > 0) {
            state.timeLeftSeconds.toFloat() / state.totalDurationSeconds.toFloat()
        } else 0f
    }

    // 3. Professional Smoothing: Linear easing for the timer bar
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )

    // 4. Competition Polish: Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Activity Header
        Text(
            text = state.currentActivity?.label ?: "Wellness",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (state.isTimerRunning) "Focus on your breath" else "Session Complete",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(64.dp))

        // --- The Trinity Timer Engine ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp).scale(if(state.isTimerRunning) breathScale else 1f)
        ) {
            // Static Track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round
            )

            // Dynamic Progress
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round
            )

            // Center Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(state.timeLeftSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state.isCompleting) "SAVING..." else "REMAINING",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = if (state.isCompleting) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        Spacer(Modifier.height(80.dp))

        // Actions
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.timeLeftSeconds == 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (state.timeLeftSeconds == 0) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                if (state.timeLeftSeconds == 0 && !state.isCompleting) "Done" else "Cancel Session",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}*/
package org.vaulture.project.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vaulture.project.viewmodels.WellnessPhase
import org.vaulture.project.viewmodels.WellnessUiState
import org.vaulture.project.viewmodels.WellnessViewModel

@Composable
fun WellnessTimerScreen(viewModel: WellnessViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val contentModifier = if (maxWidth > 600.dp) Modifier.width(500.dp).align(Alignment.Center) else Modifier.fillMaxSize()

        AnimatedContent(targetState = state.phase) { phase ->
            when (phase) {
                WellnessPhase.SETUP -> SetupContent(contentModifier, viewModel, onBack)
                WellnessPhase.ACTIVE -> ActiveTimerContent(contentModifier, state, onBack)
                WellnessPhase.SUMMARY -> SummaryContent(contentModifier, state, onBack)
            }
        }
    }
}


@Composable
private fun SetupContent(modifier: Modifier, viewModel: WellnessViewModel, onBack: () -> Unit) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Prepare for ${viewModel.uiState.value.currentActivity?.label}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        listOf(1, 2, 5, 10).forEach { mins ->
            Button(
                onClick = { viewModel.startTimer(mins) },
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("$mins Minutes Session")
            }
        }
        TextButton(onClick = onBack) { Text("Not now") }
    }
}

@Composable
private fun ActiveTimerContent(modifier: Modifier, state: WellnessUiState, onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse)
    )

    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(state.breathText, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp).scale(breathScale)) {
            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 12.dp, strokeCap = StrokeCap.Round)
            val animatedProgress by animateFloatAsState(state.timeLeftSeconds.toFloat() / state.totalDurationSeconds.toFloat())
            CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary, strokeWidth = 12.dp, strokeCap = StrokeCap.Round)

            Text(text = formatTime(state.timeLeftSeconds), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(64.dp))
        OutlinedButton(onClick = onBack) { Text("End Session Early") }
    }
}


@Composable
private fun SummaryContent(modifier: Modifier, state: org.vaulture.project.viewmodels.WellnessUiState, onBack: () -> Unit) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        // XP Badge
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary.copy(0.1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RQ POINTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text("${state.stats.resiliencePoints}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Your Mindset Battery is refilling!", style = MaterialTheme.typography.titleMedium)

        // Resilience Battery Indicator (5 dots)
        Row(Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) { index ->
                val isActive = index < state.stats.sessionsToday
                Box(
                    Modifier.size(width = 40.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Streak Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF5722), modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("${state.stats.currentStreak} Day Streak!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Consistency is the key to clarity.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Return to Dashboard")
        }
    }
}
private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}

// Reuse the formatTime, SetupContent, and ActiveTimerContent from previous turn

