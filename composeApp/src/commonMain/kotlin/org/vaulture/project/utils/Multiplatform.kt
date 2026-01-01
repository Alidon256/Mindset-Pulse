package org.vaulture.project.utils

import androidx.compose.runtime.Composable

/**
 * An `expect` interface for multiplatform actions like sharing text or opening a URL.
 * Each platform (`androidMain`, `jsMain`) will provide its own `actual` implementation.
 */
interface PlatformAction {
    fun shareText(text: String)
    fun openUrl(url: String)
}

/**
 * A Composable that provides the actual implementation of [PlatformAction].
 * This is a clean way to access platform-specific functionality from common UI code.
 */
@Composable
expect fun rememberPlatformAction(): PlatformAction
