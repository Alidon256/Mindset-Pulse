package org.vaulture.project.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.vaulture.project.viewmodels.RhythmViewModel

@Composable
fun RhythmPlayerScreen(trackId: String?, viewModel: RhythmViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(trackId, state.tracks) {
        if (trackId != null && state.currentTrack?.id != trackId) {
            viewModel.loadTrackById(trackId)
        }
    }

    val track = state.currentTrack

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Immersive Background (Blurred Art)
        AsyncImage(
            model = track?.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // 2. Gradient Overlay for readability
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
        ))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text("Relaxation Rhythm", color = Color.White.copy(0.7f), fontSize = 12.sp, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            // Main Artwork
            ElevatedCard(
                modifier = Modifier.size(300.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.elevatedCardElevation(12.dp)
            ) {
                AsyncImage(
                    model = track?.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(32.dp))

            // Info
            Text(track?.title ?: "Select a Track", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(track?.artist ?: "Mindset Pulse", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.7f))

            Spacer(Modifier.height(32.dp))

            // Progress Slider
            Slider(
                value = state.progress.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..(state.duration.toFloat().coerceAtLeast(1f)),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMillis(state.progress), color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(formatMillis(state.duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(32.dp))

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp)) }

                FloatingActionButton(
                    onClick = { viewModel.togglePlayback() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(40.dp))
                }

                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
