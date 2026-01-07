package org.vaulture.project.data.repos
import org.vaulture.project.domain.model.RhythmTrack


sealed class SearchableItem{
    abstract val id: String
    abstract val title: String
    abstract val type: ItemType
    abstract val thumbnailUrl: String

    data class RhythmItem(
        val track: RhythmTrack
    ) : SearchableItem() {
        override val id: String get() = track.id
        override val title: String get() = track.title
        override val type: ItemType get() =ItemType.AUDIO
        override val thumbnailUrl: String get() = track.thumbnailUrl
    }
}
enum class ItemType{
    VIDEO,
    AUDIO,
    SHORTS,
    CHANNEL,
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST,
    GENRE,
    PODCAST
}

enum class SearchContext{
    RHYTHM,
    SPACES,
    POSTS
}

