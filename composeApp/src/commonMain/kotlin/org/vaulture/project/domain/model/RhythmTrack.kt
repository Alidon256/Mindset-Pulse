package org.vaulture.project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RhythmTrack(
    var id: String = "",
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var thumbnailUrl: String = "",
    var duration: String = "",
    var tags: List<String> = emptyList(),
    var listenerCount: Int = 0,
    var isExplicit: Boolean = false,
    var isLiked: Boolean = false,
    var releaseDate: String = "",
    var previewUrl: String = "",
    var playlists: List<RhythmPlaylist> = emptyList()
) {

}
