package org.vaulture.project.utils

import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * A custom KSerializer for handling Firebase's Timestamp objects.
 *
 * Firebase Timestamps are represented as objects with 'seconds' and 'nanoseconds' properties.
 * This serializer correctly handles this structure, allowing seamless (de)serialization
 * within your @Serializable data classes.
 */
object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Timestamp") {
        element<Long>("seconds")
        element<Int>("nanoseconds")
    }

    override fun serialize(encoder: Encoder, value: Timestamp) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeLongElement(descriptor, 0, value.seconds)
        composite.encodeIntElement(descriptor, 1, value.nanoseconds)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val composite = decoder.beginStructure(descriptor)
        var seconds = 0L
        var nanoseconds = 0
        while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                0 -> seconds = composite.decodeLongElement(descriptor, 0)
                1 -> nanoseconds = composite.decodeIntElement(descriptor, 1)
                -1 -> break // End of object
                else -> error("Unexpected index: $index")
            }
        }
        composite.endStructure(descriptor)
        return Timestamp(seconds, nanoseconds)
    }
}
fun formatCount(count: Long): String {
    fun oneDecimal(value: Float): String {
        val rounded = kotlin.math.round(value * 10f) / 10f
        return rounded.toString()
    }
    return when {
        count >= 1_000_000 -> "${oneDecimal(count / 1_000_000f)}M"
        count >= 1_000 -> "${oneDecimal(count / 1_000f)}K"
        else -> count.toString()
    }
}


/**
 * Formats a Firebase timestamp into a relative "time ago" string.
 * This is a cross-platform function.
 *
 * @param timestamp The FieldValue which can be a ServerTimestamp or an actual Timestamp.
 * @return A formatted string like "5m ago", "2h ago", or "Just now".
 */
@OptIn(ExperimentalTime::class)
@Composable
fun formatTimestamp(timestamp: Timestamp?): String {
    // The timestamp from Firestore can be one of two things:
    // 1. A FieldValue.serverTimestamp marker before it's saved.
    // 2. An actual Timestamp object after it's been read back.
    val actualTimestamp = (timestamp as? Timestamp) ?: return "Just now"

    val now = kotlin.time.Clock.System.now()
    val postTime = kotlin.time.Instant.fromEpochSeconds(actualTimestamp.seconds)
    val duration = now - postTime

    return when {
        duration < 1.minutes -> "Just now"
        duration < 1.hours -> "${duration.inWholeMinutes}m ago"
        duration < 24.hours -> "${duration.inWholeHours}h ago"
        duration < 7.days -> "${duration.inWholeDays}d ago"
        else -> {
            // For older posts, you might want to show the actual date.
            // This part can be expanded with a date formatting library.
            val date = postTime.toLocalDateTime(TimeZone.currentSystemDefault())
            "${date.day} ${date.month.name.take(3)} ${date.year}"
        }
    }
}

/**
 * Formats a Firestore Timestamp into a readable string.
 * This is a basic implementation. You can expand it for "Today", "Yesterday", etc.
 *
 * @param timestamp The Firestore Timestamp to format.
 * @param format A simple format string (e.g., "h:mm a" for "4:30 PM").
 * @return A formatted date/time string.
 */
@OptIn(ExperimentalTime::class)
fun formatTimestamp2(timestamp: Timestamp, format: String = "MMM d, yyyy"): String {
    val instant = Instant.fromEpochSeconds(timestamp.seconds, timestamp.nanoseconds.toLong())
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    // This is a simplified formatter. For more complex needs, consider a library.
    return when (format) {
        "h:mm a" -> {
            val hour = if (localDateTime.hour % 12 == 0) 12 else localDateTime.hour % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (localDateTime.hour < 12) "AM" else "PM"
            "$hour:$minute $amPm"
        }
        else -> { // Default "MMM d, yyyy" format
            val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
        }
    }
}



