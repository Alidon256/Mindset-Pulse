package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.StorageReference
import org.khronos.webgl.Uint8Array // <-- CRITICAL: Add this import

internal actual suspend fun StorageReference.upload(bytes: ByteArray) {
    this.putData(Data(bytes.toUint8Array()))
}

private fun ByteArray.toUint8Array(): Uint8Array {
    return Uint8Array(this.toTypedArray())
}
