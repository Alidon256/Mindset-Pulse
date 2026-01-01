package org.vaulture.project.utils

import androidx.compose.runtime.Composable

/**
 * An expect composable that provides a mechanism to launch a platform-specific
 * image picker and returns the selected image as a ByteArray.
 *
 * @param show A boolean to control the visibility of the picker.
 * @param onImageSelected A lambda that will be invoked with the ByteArray of the selected image.
 *                        The ByteArray will be null if the user cancels the operation.
 */
@Composable
internal expect fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit,
)
