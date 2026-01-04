package org.vaulture.project.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import dev.gitlive.firebase.firestore.Timestamp

enum class WellnessType(
    val label: String,
    val icon: ImageVector,
    val description: String,
    val gradientStartColor: Color,
    val gradientEndColor: Color
) {
    MEDITATION(
        label = "Meditation",
        icon = Icons.Default.SelfImprovement,
        description = "Meditation helps calm your nervous system and improves focus. By observing your thoughts without judgment, you can reduce stress and increase self-awareness.",
        gradientStartColor = Color(0xFF4C8577),
        gradientEndColor = Color(0xFF2E7D32)
    ),
    BREATHING(
        label = "Breathing",
        icon = Icons.Default.Spa,
        description = "Controlled breathing exercises can lower your heart rate and blood pressure, signaling your body to relax. It's a powerful, instant tool to manage anxiety.",
        gradientStartColor = Color(0xFF0288D1),
        gradientEndColor = Color(0xFF01579B)
    ),
    YOGA(
        label = "Yoga",
        icon = Icons.Default.Bedtime,
        description = "Yoga combines movement, breath, and mindfulness to enhance flexibility, reduce stress, and promote overall well-being.",
        gradientStartColor = Color(0xFF6A1B9A),
        gradientEndColor = Color(0xFF311B92)
    )
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

