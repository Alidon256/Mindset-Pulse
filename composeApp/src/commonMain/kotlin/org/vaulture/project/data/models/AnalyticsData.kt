package org.vaulture.project.data.models

data class MoodTrendData(
    val mood: String,
    val count: Int,
    val averageIntensity: Float
)

data class EmotionFrequency(
    val emotion: String,
    val count: Int,
    val averageIntensity: Float
)

data class CbtUsageData(
    val exerciseType: CbtExerciseType,
    val count: Int
)

data class AnalyticsSummary(
    val totalCheckIns: Int = 0,
    val overallMoodDistribution: List<MoodTrendData> = emptyList(),
    val mostFrequentEmotions: List<EmotionFrequency> = emptyList(),
    val cbtExerciseUsage: List<CbtUsageData> = emptyList(),
    val averageMoodIntensity: Float = 0f,
    val selfCareActivityFrequency: Map<String, Int> = emptyMap(),
    val significantActivityFrequency: Map<String, Int> = emptyMap(),
    val personalizedSuggestions: List<String> = emptyList()
)
