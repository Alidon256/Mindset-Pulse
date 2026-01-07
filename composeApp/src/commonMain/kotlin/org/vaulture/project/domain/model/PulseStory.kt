package org.vaulture.project.domain.model

data class PulseStory(
    val id: String,
    val title: String,
    val imageUrl: String,
    val category: String,
    val isLarge: Boolean = false,
    val span: Int = 1
)