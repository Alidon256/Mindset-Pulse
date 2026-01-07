package org.vaulture.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.vaulture.project.presentation.navigation.AppNavigation

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    Firebase.initialize(
        options = FirebaseOptions(
            applicationId = "1:410223288840:web:1806d9da91aa069af9a7bd",
            apiKey = "AIzaSyC3U-h5ywzlrOQ5w7tui7f4gM_nGTBn7_4",
            projectId = "tija-a7b75",
            storageBucket = "tija-a7b75.firebasestorage.app",

        )
    )

    onWasmReady {
        val body = document.body ?: return@onWasmReady
        document.title = "Mindset Pulse"
        ComposeViewport(viewportContainer = body) {
            AppNavigation()
        }
    }
}
