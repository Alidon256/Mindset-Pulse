package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.storage.StorageReference

internal expect suspend fun StorageReference.upload(bytes: ByteArray)

