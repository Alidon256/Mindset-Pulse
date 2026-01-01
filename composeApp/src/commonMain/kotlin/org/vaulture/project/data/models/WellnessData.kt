package org.vaulture.project.data.models

import kotlinx.serialization.Serializable
import dev.gitlive.firebase.firestore.Timestamp

enum class WellnessType(val label: String, val icon: String) {
    BREATHING("Breathing", "Air"),
    YOGA("Yoga", "SelfImprovement"),
    MEDITATION("Meditation", "Spa")
}

@Serializable
data class WellnessRecord(
    val id: String = "",
    val userId: String = "",
    val type: WellnessType,
    val durationSeconds: Int,
    val timestamp: Timestamp = Timestamp(0, 0)
)

@Serializable
data class WellnessStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: String = "",
    val totalMinutes: Int = 0,
    val sessionsToday: Int = 0, // 0-5 visual indicator
    val resiliencePoints: Int = 0 // XP-style progression
)

