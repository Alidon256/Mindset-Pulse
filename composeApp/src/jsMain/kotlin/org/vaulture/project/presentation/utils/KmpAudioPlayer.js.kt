package org.vaulture.project.presentation.utils

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLAudioElement
import kotlin.js.then

class WebAudioPlayer : KmpAudioPlayer {
    // Keep a reference to the single audio element
    private var audio: HTMLAudioElement? = window.document.createElement("audio") as HTMLAudioElement

    override val isPlaying = MutableStateFlow(false)
    override val currentPosition = MutableStateFlow(0L)
    override val duration = MutableStateFlow(0L)

    init {
        setupListeners()
    }

    private fun setupListeners() {
        val a = audio ?: return
        a.onplay = { isPlaying.value = true }
        a.onpause = { isPlaying.value = false }
        a.ontimeupdate = { currentPosition.value = (a.currentTime * 1000).toLong() }
        a.onloadedmetadata = { duration.value = (a.duration * 1000).toLong() }
        a.onended = { isPlaying.value = false } // Important for UI to update when song finishes
    }

    override fun play(url: String, title: String, artist: String) {
        val a = audio ?: return
        a.pause()
        a.src = url
        a.load()
        val playPromise = a.play()

        playPromise.asDynamic().then({
            println("Playback started on Web")
        }, { error ->
            println("Playback blocked or failed: $error")
        })
    }

    override fun pause() { audio?.pause() }
    override fun resume() { audio?.play() }

    override fun stop() {
        val a = audio ?: return
        a.pause()
        a.currentTime = 0.0
    }

    override fun seekTo(positionMs: Long) {
        audio?.currentTime = positionMs / 1000.0
    }

    // New cleanup method to remove the element
    fun cleanup() {
        stop()
        audio?.src = ""
        audio = null
    }
}

@Composable
actual fun rememberKmpAudioPlayer(): KmpAudioPlayer {
    // Use DisposableEffect to clean up when the screen is left
    val player = remember { WebAudioPlayer() }

    DisposableEffect(player) {
        onDispose {
            player.cleanup()
        }
    }
    return player
}
