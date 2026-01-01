package org.vaulture.project.utils

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android-specific implementation of [PlatformAction].
 */
class AndroidPlatformAction(private val context: android.content.Context) : PlatformAction {

    override fun shareText(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        // Required to start an activity from a non-activity context
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * The actual Composable for Android that remembers and provides the [AndroidPlatformAction].
 */
@Composable
actual fun rememberPlatformAction(): PlatformAction {
    val context = LocalContext.current
    return remember {
        AndroidPlatformAction(context)
    }
}
