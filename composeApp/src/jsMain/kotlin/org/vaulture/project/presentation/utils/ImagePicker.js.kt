package org.vaulture.project.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * The actual implementation of the ImagePicker for the JavaScript (web) platform.
 * It works by creating a hidden file input element, triggering a click on it,
 * and reading the selected file's content into a ByteArray.
 */
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit,
) {
    // This LaunchedEffect will run when `show` becomes true
    LaunchedEffect(show) {
        if (show) {
            // Create a file input element dynamically
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*" // Accept only image files

            // Set up a listener for when a file is selected
            input.onchange = { event ->
                val file = (event.target as? HTMLInputElement)?.files?.get(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = { loadEvent ->
                        val arrayBuffer = loadEvent.target.asDynamic().result as? ArrayBuffer
                        if (arrayBuffer != null) {
                            val bytes = Int8Array(arrayBuffer).unsafeCast<ByteArray>()
                            onImageSelected(bytes)
                        } else {
                            onImageSelected(null) // Failed to read file
                        }
                    }
                    reader.readAsArrayBuffer(file)
                } else {
                    onImageSelected(null) // No file selected
                }
            }

            // Trigger the file picker dialog
            input.click()
        }
    }
}
