package org.vaulture.project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RhythmPlaylist(
    val id: Int,
    val name: String="",
    val description: String = "",
    val coverUrl: String = "",
    val tracks: List<RhythmTrack> = emptyList(),
    val owner: String = "",
    val isPublic: Boolean = true,
    val followerCount: Int = 0
)
