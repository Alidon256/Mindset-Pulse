package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.StorageReference
import org.khronos.webgl.Uint8Array // <-- CRITICAL: Add this import

// The ACTUAL implementation for the JS target.
internal actual suspend fun StorageReference.upload(bytes: ByteArray) {
    // DEFINITIVE FIX: Convert the Kotlin ByteArray to a JS Uint8Array before creating the Data object.
    this.putData(Data(bytes.toUint8Array()))
}

private fun ByteArray.toUint8Array(): Uint8Array {
    return Uint8Array(this.toTypedArray())
}
