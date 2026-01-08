package org.vaulture.project.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.vaulture.project.domain.model.RhythmTrack
import org.vaulture.project.presentation.viewmodels.RhythmUiState
import org.vaulture.project.presentation.viewmodels.RhythmViewModel

@Composable
fun RhythmPlayerScreen(trackId: String?, viewModel: RhythmViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val track = state.currentTrack

   /* LaunchedEffect(track) {
        if (track != null) {
            viewModel.incrementListenerCount(track.id)
        }
    }*/
    LaunchedEffect(trackId) {
        if (trackId != null && state.currentTrack?.id != trackId) {
            viewModel.loadTrackById(trackId)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val maxHeight = maxHeight
        val isCompactHeight = maxHeight < 800.dp

        AsyncImage(
            model = track?.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text(
                    text = "Relaxation Rhythm",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isCompactHeight) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PlayerContent(track, state, viewModel, isCompact = true)
                }
            } else {

                Spacer(Modifier.weight(0.5f))

                Box(
                    modifier = Modifier
                        .weight(4f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerContent(track, state, viewModel, isCompact = false)
                }

                Spacer(Modifier.weight(0.5f))
            }
        }
    }
}

@Composable
private fun PlayerContent(
    track: RhythmTrack?,
    state: RhythmUiState,
    viewModel: RhythmViewModel,
    isCompact: Boolean
) {
    val cardSize = if (isCompact) 200.dp else 300.dp
    val spacerSize = if (isCompact) 16.dp else 32.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ElevatedCard(
            modifier = Modifier
                .size(cardSize)
                .aspectRatio(1f),
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

        Spacer(Modifier.height(spacerSize))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = track?.title ?: "Select a Track",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track?.artist ?: "Mindset Pulse",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(spacerSize))

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = state.progress.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..(state.duration.toFloat().coerceAtLeast(1f)),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(0.3f)
                ),
                modifier = Modifier.height(20.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatMillis(state.progress),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    formatMillis(state.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(spacerSize))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = { viewModel.playPrevious() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = { viewModel.togglePlayback() },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = { viewModel.playNext() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
