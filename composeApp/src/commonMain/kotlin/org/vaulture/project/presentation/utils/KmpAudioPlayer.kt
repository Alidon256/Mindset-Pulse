package org.vaulture.project.presentation.utils

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface KmpAudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>

    fun play(url: String, title: String, artist: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
}

@Composable
expect fun rememberKmpAudioPlayer(): KmpAudioPlayer
