package org.vaulture.project.data.repos

import org.vaulture.project.domain.model.Channel
import org.vaulture.project.domain.model.RhythmTrack


sealed class SearchableItem{
    abstract val id: String
    abstract val title: String // Common property for display
    abstract val type: ItemType
    abstract val thumbnailUrl: String // Common property for display

    data class ChannelItem(
        val channel: Channel
    ) : SearchableItem() {
        override val id: String get() = channel.id
        override val title: String get() = channel.displayName
        override val type: ItemType get() = ItemType.CHANNEL
        override val thumbnailUrl: String get() = channel.imageUrl
    }

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

data class SimpleItem(
    override val title: String,
    override val type: ItemType
) : SearchableItem() {
    override val id: String get() = title
    override val thumbnailUrl: String get() = ""
}
