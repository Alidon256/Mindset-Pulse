package org.vaulture.project.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Represents a user in the application.
 *
 * **FIXED**: The field is now consistently `displayName` to match the data being
 * pulled from Firebase Auth and to be used across the entire application.
 */
@Immutable
@Serializable
data class User(
    val uid: String,
    val displayName: String?, // Using displayName consistently as requested.
    val email: String?,
    val isAnonymous: Boolean = false,
    val photoUrl: String?
)