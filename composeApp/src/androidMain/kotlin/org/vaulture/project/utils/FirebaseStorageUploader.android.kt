package org.vaulture.project.utils

import dev.gitlive.firebase.storage.StorageReference
import dev.gitlive.firebase.storage.android
import kotlinx.coroutines.tasks.await

internal actual suspend fun StorageReference.upload(bytes: ByteArray) {
    this.android.putBytes(bytes).await()
}