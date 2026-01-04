package org.vaulture.project.presentation.utils


import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.vaulture.project.presentation.utils.KmpAudioPlayer
import org.w3c.dom.HTMLAudioElement

class WebAudioPlayer : KmpAudioPlayer {
    private val audio = window.document.createElement("audio") as HTMLAudioElement

    override val isPlaying = MutableStateFlow(false)
    override val currentPosition = MutableStateFlow(0L)
    override val duration = MutableStateFlow(0L)

    init {
        audio.onplay = { isPlaying.value = true }
        audio.onpause = { isPlaying.value = false }
        audio.ontimeupdate = { currentPosition.value = (audio.currentTime * 1000).toLong() }
        audio.onloadedmetadata = { duration.value = (audio.duration * 1000).toLong() }
    }


    override fun play(url: String, title: String, artist: String) {
        audio.pause()
        audio.src = url
        audio.load() // Force browser to re-check the file
        val playPromise = audio.play()

        // Web specific: handle "Autoplay" block gracefully
        playPromise.asDynamic().then({
            println("Playback started on Web")
        }, { error ->
            println("Playback blocked: $error. User must click play button.")
        })
    }


    override fun pause() { audio.pause() }
    override fun resume() { audio.play() }
    override fun stop() { audio.pause(); audio.currentTime = 0.0 }
    override fun seekTo(positionMs: Long) { audio.currentTime = positionMs / 1000.0 }
}

@Composable
actual fun rememberKmpAudioPlayer(): KmpAudioPlayer = remember { WebAudioPlayer() }
