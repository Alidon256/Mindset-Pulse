package org.vaulture.project.data.remote

import dev.gitlive.firebase.firestore.Timestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vaulture.project.domain.model.CbtExerciseType
import org.vaulture.project.domain.model.CheckInEntry

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)
@Serializable
data class GeminiRequest(val contents: List<Content>)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)


@Serializable
data class Candidate(val content: Content?)

data class AnalysisResult(val sentimentScore: Float, val insight: String)

class GeminiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // private val apiKey = "YOUR_REAL_API_KEY_HERE"
    private val apiKey = "AIzaSyB1zWHZRsnYW166zW6nG1CGE5IJentaCsQ"
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

    private suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelay: Long = 1000, // 1 second start
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                println("GeminiService: Attempt ${attempt + 1} failed: ${e.message}. Retrying in ${currentDelay}ms...")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return block()
    }
    suspend fun generateDailyQuestions(): List<String> {
        val promptText = """
            Generate 5 short, empathetic mental health check-in questions for a professional young adult.
            focus on stress, sleep, and workload.
            
            Strictly return them as a list separated by pipes "|".
            Example: How did you sleep?|What is your main focus today?|Do you feel supported?
        """.trimIndent()

        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        return try {
            retryWithBackoff {
                println("GeminiService: Requesting questions...")

                val response: GeminiResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }.body()

                if (response.error != null) {
                    throw Exception("API Error: ${response.error.message}")
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response candidates")
                val questions = rawText.replace("\n", "").split("|").map { it.trim() }

                if (questions.size < 3) {
                    throw Exception("Parsing failed or too few questions returned.")
                }

                questions.take(5)
            }
        } catch (e: Exception) {
            println("GeminiService: All retries failed. Falling back to defaults. Error: ${e.message}")
            getDefaultQuestions()
        }
    }

    suspend fun analyzeJournalEntry(text: String): AnalysisResult {
        val promptText = """
            Act as a mental health expert. Analyze this user check-in: "$text"
            
            Output strictly in this format:
            SCORE: [float -1.0 to 1.0]
            INSIGHT: [2 supportive sentences]
        """.trimIndent()

        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        try {
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            return parseAnalysisResponse(rawText)
        } catch (e: Exception) {
            println("GeminiService: Error analyzing entry: ${e.message}")
            return AnalysisResult(0.0f, "Your check-in has been saved locally.")
        }
    }

    suspend fun generateAnalyticsReport(checkIns: List<CheckInEntry>): String {
        if (checkIns.isEmpty()) return "No data available for analysis yet."

        val promptText = constructAnalyticsPrompt(checkIns)
        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        return try {
            val httpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val response: GeminiResponse = httpResponse.body()

            // Handle API Errors returned in the JSON body
            if (response.error != null) {
                println("Gemini API Error: ${response.error.message}")
                return "The AI service is currently unavailable: ${response.error.message}"
            }

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!rawText.isNullOrBlank()) {
                rawText
            } else {
                "We couldn't generate insights right now. Please try again later."
            }
        } catch (e: Exception) {
            println("GeminiService: Exception: ${e.message}")
            "Error connecting to AI service. Please check your connection."
        }
    }

    private fun parseAnalysisResponse(rawText: String): AnalysisResult {
        var score = 0.0f
        var insight = "Take a moment to breathe."

        if (rawText.isBlank()) return AnalysisResult(score, insight)

        rawText.lines().forEach { line ->
            if (line.contains("SCORE:", ignoreCase = true)) {
                val scorePart = line.substringAfter(":").replace("[", "").replace("]", "").trim()
                score = scorePart.toFloatOrNull() ?: 0.0f
            }
            if (line.contains("INSIGHT:", ignoreCase = true)) {
                insight = line.substringAfter(":").replace("[", "").replace("]", "").trim()
            }
        }
        return AnalysisResult(score, insight)
    }


    private fun constructAnalyticsPrompt(checkIns: List<CheckInEntry>): String {
        // Summarize the recent history for the AI

        val summary = checkIns.takeLast(15).joinToString("\n\n---\n\n") { entry ->
            val primaryEmotionsStr = if (entry.primaryEmotions.isNotEmpty()) {
                entry.primaryEmotions.joinToString { "${it.emotion} (${it.intensity}/10)" }
            } else "N/A"

            val selfCareStr = if (entry.selfCareActivities.isNotEmpty()) {
                entry.selfCareActivities.joinToString(", ")
            } else "N/A"

            val cbtDisplayName = if (entry.cbtExerciseType != CbtExerciseType.NONE) {
                entry.cbtExerciseType.name
            } else "None"

            val highlights = entry.positiveHighlights.ifBlank { "N/A" }
            val challenges = entry.challengesFaced.ifBlank { "N/A" }
            val learned = entry.learnedFromCbt.ifBlank { "N/A" }

            """
            Date: ${formatTimestamp(entry.timestamp)}
            Score: ${entry.score}/100 (${entry.state.label})
            Overall Mood: ${entry.overallMood} (Intensity: ${entry.moodIntensity}/10)
            Primary Emotions: $primaryEmotionsStr
            Highlights: $highlights
            Challenges: $challenges
            CBT Exercise: $cbtDisplayName
            Learned from CBT: $learned
            Self-Care Activities: $selfCareStr
            Previous Insight: ${entry.aiInsight}
            """.trimIndent()
        }

            return """
            You are a supportive, empathetic, and insightful AI assistant for the Mindset Pulse app. 
            Your goal is to help a user understand their wellbeing based on their recent check-ins.
            Please use relevant emojis to make the insights more engaging and friendly. 😊

            Here is a summary of their recent check-ins:
            $summary

            Based on this data, please provide a response in well-formatted Markdown. Use H3 level Markdown headings (e.g., ### Section Title).

            ### 1. Key Patterns & Observations 📈
            Identify recurring patterns in stress or mood based on the scores and previous insights. Be empathetic. (Provide 2-3 bullet points)

            ### 2. Potential Insights 🤔
            Offer gentle interpretations. If scores are improving, celebrate that. If they are high, suggest why based on the context. (1 short paragraph)

            ### 3. Actionable & Positive Suggestions 🌱
            Offer practical, positive, and encouraging suggestions. Frame these as invitations. (Provide 2-3 bullet points)

            ### 4. A Word of Encouragement 💖
            End on a positive and supportive note. (1 sentence)

            IMPORTANT:
            * Do NOT give medical advice or diagnoses.
            * Maintain a supportive tone.
            * Format strictly as Markdown.
        """.trimIndent()
    }


    private fun getDefaultQuestions() = listOf(
        "How would you rate your sleep quality?",
        "How is your energy level right now?",
        "Do you feel overwhelmed by tasks today?",
        "Have you taken a break recently?",
        "How optimistic do you feel about tomorrow?"
    )

    private fun formatTimestamp(timestamp: Timestamp): String {
        val instant = kotlin.time.Instant.fromEpochMilliseconds(timestamp.seconds * 1000)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${date.month.name.take(3)} ${date.dayOfMonth}"
    }

}


