package org.vaulture.project.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

/**
 * Web-specific implementation of [PlatformAction].
 */
class JsPlatformAction : PlatformAction {

    override fun shareText(text: String) {
        // The Web Share API is the modern way to do this.
        if (js("typeof navigator.share !== 'undefined'").unsafeCast<Boolean>()) {
            val shareData = js("{}").unsafeCast<dynamic>().apply {
                val shareData = js("{}").unsafeCast<dynamic>()
                shareData.title = "Check out this trip!"
                shareData.text = text
                shareData.url = window.location.href
            }

            // Call the raw JS API so we get a Promise and can use `.then`/`.catch`
            val promise = window.asDynamic().navigator.share(shareData)
            promise.then(
                { _: dynamic -> println("Shared successfully") },
                { err: dynamic -> println("Share failed: ${err?.message}") }
            )
        } else {
            // Fallback for browsers that don't support the Web Share API
            println("Web Share API not supported. Text to share: $text")
            window.alert("Sharing is not supported on this browser. You can copy the link manually.")
        }
    }

    override fun openUrl(url: String) {
        TODO("Not yet implemented")
    }
}
    // Data class for the Web Share API's parameter
private external interface ShareData {
    var url: String?
    var text: String?
    var title: String?
}

/**
 * The actual Composable for Web that remembers and provides the [JsPlatformAction].
 */
@Composable
actual fun rememberPlatformAction(): PlatformAction {
    return remember {
        JsPlatformAction()
    }
}
