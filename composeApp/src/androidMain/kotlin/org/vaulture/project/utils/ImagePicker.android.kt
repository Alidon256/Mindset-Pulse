package org.vaulture.project.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest // <-- Add this import
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/**
 * The actual implementation of the ImagePicker for the Android platform.
 * It uses the modern PickVisualMedia contract to provide a system-native
 * image picker that is backward-compatible.
 */
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit,
) {
    val context = LocalContext.current

    // Set up the modern visual media picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                // When an image is selected, read its content into a ByteArray
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                onImageSelected(bytes)
            } else {
                // The user cancelled the picker
                onImageSelected(null)
            }
        }
    )

    // This LaunchedEffect will run when `show` becomes true
    LaunchedEffect(show) {
        if (show) {
            // Launch the picker
            // CORRECTED: Use PickVisualMediaRequest to construct the launch parameter
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}
