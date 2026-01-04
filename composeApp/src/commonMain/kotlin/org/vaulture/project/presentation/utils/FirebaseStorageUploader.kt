package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.storage.StorageReference

// We EXPECT each platform to provide a way to upload a byte array to a storage reference.
internal expect suspend fun StorageReference.upload(bytes: ByteArray)

