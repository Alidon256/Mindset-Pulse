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

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit,
) {
    LaunchedEffect(show) {
        if (show) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"

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
                            onImageSelected(null)
                        }
                    }
                    reader.readAsArrayBuffer(file)
                } else {
                    onImageSelected(null)
                }
            }

            // Trigger the file picker dialog
            input.click()
        }
    }
}
