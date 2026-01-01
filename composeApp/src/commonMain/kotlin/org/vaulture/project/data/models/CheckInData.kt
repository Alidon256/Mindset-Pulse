package org.vaulture.project.data.models

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

// --- NEW: Mental State Enum for Risk Classification ---
enum class MentalState(val colorHex: Long, val label: String) {
    STABLE(0xFF4CAF50, "Stable"),       // Green
    MILD_STRESS(0xFFFFC107, "Mild Stress"), // Amber
    HIGH_STRESS(0xFFFF5722, "High Stress"), // Deep Orange
    BURNOUT_RISK(0xFFD32F2F, "Burnout Risk") // Red
}

enum class CbtExerciseType(val displayName: String) {
    THOUGHT_RECORD("Thought Record"),
    BEHAVIORAL_ACTIVATION("Behavioral Activation"),
    GRATITUDE_JOURNALING("Gratitude Journaling"),
    PROBLEM_SOLVING("Problem Solving"),
    MINDFULNESS_REFLECTION("Mindfulness Reflection"),
    NONE("Daily Check-In");
}

@Serializable
data class EmotionRating(
    val emotion: String = "",
    val intensity: Int = 5
)

@Serializable
data class ThoughtRecordEntry(
    val situation: String = "",
    val automaticNegativeThought: String = "",
    val emotionsBefore: List<EmotionRating> = emptyList(),
    val cognitiveDistortions: List<String> = emptyList(),
    val evidenceForThought: String = "",
    val evidenceAgainstThought: String = "",
    val alternativeThought: String = "",
    val emotionsAfter: List<EmotionRating> = emptyList()
)

@Serializable
data class CheckInEntry(
    val id: String = "",
    val userId: String = "",
    val timestamp: Timestamp = Timestamp(0,0), // Default to 0, updated on creation

    // --- MINDSET PULSE CORE FIELDS (NEW) ---
    val score: Int = 0, // 0-100 Risk Score
    val state: MentalState = MentalState.STABLE, // Calculated by RiskEngine
    val aiInsight: String = "", // Gemini's short observation
    val sentimentScore: Float = 0f, // -1.0 to 1.0

    // Existing CBT Fields
    val overallMood: String = "",
    val moodIntensity: Int = 5,
    val primaryEmotions: List<EmotionRating> = emptyList(),
    val generalThoughts: String = "",
    val positiveHighlights: String = "",
    val challengesFaced: String = "",
    val significantActivities: List<String> = emptyList(),
    val selfCareActivities: List<String> = emptyList(),
    val cbtExerciseType: CbtExerciseType = CbtExerciseType.NONE,
    val thoughtRecord: ThoughtRecordEntry? = null,
    val cbtReflectionPrompt: String? = null,
    val cbtReflectionResponse: String? = null,
    val learnedFromCbt: String = ""
)
// Predefined list of emotions for selection
val commonEmotions = listOf(
    "Happy", "Excited", "Joyful", "Content", "Proud",
    "Sad", "Disappointed", "Grieving", "Lonely",
    "Angry", "Frustrated", "Irritated", "Resentful",
    "Anxious", "Worried", "Stressed", "Overwhelmed", "Fearful",
    "Calm", "Relaxed", "Peaceful", "Neutral",
    "Surprised", "Confused", "Guilty", "Ashamed", "Hopeful"
).sorted()

// Predefined list of cognitive distortions
val cognitiveDistortionsList = listOf(
    "All-or-Nothing Thinking",
    "Overgeneralization",
    "Mental Filter",
    "Disqualifying the Positive",
    "Jumping to Conclusions (Mind Reading)",
    "Jumping to Conclusions (Fortune Telling)",
    "Magnification (Catastrophizing)",
    "Minimization",
    "Emotional Reasoning",
    "Should Statements",
    "Labeling/Mislabeling",
    "Personalization"
).sorted()