package org.vaulture.project.domain.model

data class MediaFile(
    val content: ByteArray,
    val thumbnail: ByteArray? = null,
    val type: Story.ContentType,
    val aspectRatio: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MediaFile

        if (aspectRatio != other.aspectRatio) return false
        if (!content.contentEquals(other.content)) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aspectRatio.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }
}