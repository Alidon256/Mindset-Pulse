package org.vaulture.project.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class User(
    val uid: String,
    val displayName: String?,
    val username: String?,
    val email: String?,
    val isAnonymous: Boolean = false,
    val photoUrl: String?
)