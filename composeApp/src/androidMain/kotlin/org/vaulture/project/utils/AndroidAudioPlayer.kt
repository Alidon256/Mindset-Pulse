package org.vaulture.project.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AndroidAudioPlayer(context: android.content.Context) : KmpAudioPlayer {
    private val exoPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(context).build()

    override val isPlaying = MutableStateFlow(false)
    override val currentPosition = MutableStateFlow(0L)
    override val duration = MutableStateFlow(0L)

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying.value = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    override fun play(url: String, title: String, artist: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun pause() = exoPlayer.pause()
    override fun resume() = exoPlayer.play()
    override fun stop() = exoPlayer.stop()
    override fun seekTo(positionMs: Long) = exoPlayer.seekTo(positionMs)

    // Helper to update progress
    suspend fun updateProgress() {
        while (true) {
            if (isPlaying.value) {
                currentPosition.value = exoPlayer.currentPosition
            }
            delay(500)
        }
    }
}

@Composable
actual fun rememberKmpAudioPlayer(): KmpAudioPlayer {
    val context = LocalContext.current
    val player = remember { AndroidAudioPlayer(context) }

    LaunchedEffect(player) {
        player.updateProgress()
    }

    DisposableEffect(Unit) {
        onDispose { player.stop() }
    }

    return player
}
