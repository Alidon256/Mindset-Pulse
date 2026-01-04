package org.vaulture.project.domain.model

data class Channel(
    val id: String = "",
    val displayName: String = "",
    val imageUrl: String = "",
    val subscriberCount: Long = 0
)