package org.vaulture.project.presentation.utils

import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Instant

fun formatCount(count: Long): String {
    fun oneDecimal(value: Float): String {
        val rounded = round(value * 10f) / 10f
        return rounded.toString()
    }
    return when {
        count >= 1_000_000 -> "${oneDecimal(count / 1_000_000f)}M"
        count >= 1_000 -> "${oneDecimal(count / 1_000f)}K"
        else -> count.toString()
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun formatTimestamp(timestamp: Timestamp?): String {
    val actualTimestamp = (timestamp as? Timestamp) ?: return "Just now"

    val now = Clock.System.now()
    val postTime = Instant.fromEpochSeconds(actualTimestamp.seconds)
    val duration = now - postTime

    return when {
        duration < 1.minutes -> "Just now"
        duration < 1.hours -> "${duration.inWholeMinutes}m ago"
        duration < 24.hours -> "${duration.inWholeHours}h ago"
        duration < 7.days -> "${duration.inWholeDays}d ago"
        else -> {
            val date = postTime.toLocalDateTime(TimeZone.currentSystemDefault())
            "${date.day} ${date.month.name.take(3)} ${date.year}"
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatTimestamp2(timestamp: Timestamp, format: String = "MMM d, yyyy"): String {
    val instant = Instant.fromEpochSeconds(timestamp.seconds, timestamp.nanoseconds.toLong())
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return when (format) {
        "h:mm a" -> {
            val hour = if (localDateTime.hour % 12 == 0) 12 else localDateTime.hour % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (localDateTime.hour < 12) "AM" else "PM"
            "$hour:$minute $amPm"
        }
        else -> {
            val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
        }
    }
}
fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val messageDateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return if (messageDateTime.date == now.date) {
        "${messageDateTime.hour.toString().padStart(2, '0')}:${messageDateTime.minute.toString().padStart(2, '0')}"
    } else {
        "${messageDateTime.dayOfMonth}/${messageDateTime.monthNumber}/${messageDateTime.year % 100}"
    }
}



