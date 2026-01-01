package org.vaulture.project.data.models


import kotlin.time.Duration



data class Video(
     var videoId: String = "",
    var title: String = "",
    var description: String = "",
    var thumbnailUrl: String = "",
    var videoUrl: String = "",
    var channelThumbnailUrl: String = "",
    var channelTitle: String = "",
    var publishDate: String = "",
    var duration: String = "PT0M0S", // ISO 8601 Duration. Consider Long (seconds/ms) if parsing is complex/frequent
    var viewCount: Long = 0L,
    var likeCount: Long = 0L,
    var commentCount: Long = 0L,
    var isLive: Boolean = false,

    var tags: List<String> = emptyList(),
    var timeToWatch: String = "",
    var aspectRatio: String = "16:9",
    var shareCount: Long = 0L,
    var completionRate: Double = 0.0,
    var creatorId: String = "",
    var last24HrViews: Long = 0L,
    var height: Int = 0 // This property was in the data class but not Parcelable logic
) {
    val durationInSeconds: Long
        get() = try {
            if (duration.isBlank() || duration == "PT0M0S") { // Check default explicitly if needed
                if (timeToWatch.isNotBlank()) {
                    parseTimeToWatchToSeconds(timeToWatch)
                } else 0L
            } else Duration.parse(duration).inWholeSeconds
        } catch (e: Exception) {
            // Fallback to timeToWatch if duration parsing fails
            if (timeToWatch.isNotBlank()) {
                parseTimeToWatchToSeconds(timeToWatch)
            } else 0L
        }
    val isShort: Boolean
        get() {
            if (isLive) return false
            val isVertical = aspectRatio == "9:16" || aspectRatio.toAspectRatioFloat() < 1.0f
            return durationInSeconds <= 180 && isVertical // 180 seconds = 3 minutes
        }
    private fun parseTimeToWatchToSeconds(timeToWatchStr: String): Long {
        return try {
            val parts = timeToWatchStr.split(":")
            when (parts.size) {
                3 -> parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()
                2 -> parts[0].toLong() * 60 + parts[1].toLong()
                1 -> parts[0].toLong() // If it's just seconds
                else -> 0L
            }
        } catch (e: NumberFormatException) {
            0L
        }
    }

    private fun String.toAspectRatioFloat(): Float {
        return try {
            val parts = split(":")
            if (parts.size == 2) {
                val width = parts[0].toFloatOrNull() ?: 16f
                val heightValue = parts[1].toFloatOrNull() ?: 9f // Renamed to avoid conflict
                if (heightValue != 0f) width / heightValue else 1.7778f // 16/9 default
            } else 1.7778f
        } catch (e: Exception) {
            1.7778f
        }
    }

}


