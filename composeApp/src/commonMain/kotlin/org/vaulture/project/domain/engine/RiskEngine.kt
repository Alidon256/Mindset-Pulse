package org.vaulture.project.domain.engine

import kotlinx.serialization.Serializable

enum class MentalState(val colorHex: Long, val label: String) {
    STABLE(0xFF4CAF50, "Stable"),
    MILD_STRESS(0xFFFFC107, "Mild Stress"),
    HIGH_STRESS(0xFFFF5722, "High Stress"),
    BURNOUT_RISK(0xFFD32F2F, "Burnout Risk")
}

@Serializable
data class CheckInResult(
    val score: Int,
    val state: MentalState,
    val aiInsight: String,
    val timestamp: Long
)

object RiskEngine {
    fun calculateRisk(
        answers: List<Int>, // 1 (Good) to 5 (Bad)
        sentimentScore: Float // -1.0 (Negative) to 1.0 (Positive)
    ): Pair<Int, MentalState> {

        // 1. Quantitative Score (Objective)
        val maxPossible = answers.size * 5
        val rawScore = answers.sum()

        // Normalize to 0-100 (Higher = Worse)
        var riskScore = ((rawScore.toFloat() / maxPossible.toFloat()) * 100).toInt()

        // 2. Qualitative Modifier (AI Subjective)
        // If AI detects very negative language, we bump the risk up slightly as a safety precaution.
        // We do NOT let AI lower the risk significantly to avoid false negatives.
        if (sentimentScore < -0.6f) {
            riskScore += 15 // Significant negative sentiment detected
        } else if (sentimentScore < -0.3f) {
            riskScore += 5
        } else if (sentimentScore > 0.7f) {
            // Only lower risk slightly if sentiment is overwhelmingly positive
            riskScore -= 5
        }

        riskScore = riskScore.coerceIn(0, 100)

        // 3. Classification
        val state = when {
            riskScore < 35 -> MentalState.STABLE
            riskScore < 65 -> MentalState.MILD_STRESS
            riskScore < 85 -> MentalState.HIGH_STRESS
            else -> MentalState.BURNOUT_RISK
        }

        return Pair(riskScore, state)
    }
}
