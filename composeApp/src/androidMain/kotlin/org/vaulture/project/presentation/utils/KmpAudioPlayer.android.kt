package org.vaulture.project.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.vaulture.project.presentation.utils.AndroidAudioPlayer

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