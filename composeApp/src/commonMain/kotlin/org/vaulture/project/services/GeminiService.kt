/*package org.vaulture.project.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- Gemini Data Models ---
@Serializable
data class GeminiRequest(val contents: List<Content>)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>?)

@Serializable
data class Candidate(val content: Content?)

// --- App Models ---
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

    // ⚠️ REPLACE WITH YOUR REAL API KEY
    //private val apiKey = "YOUR_REAL_API_KEY_HERE"
    private val apiKey = "AIzaSyABoQ4u6fddnHlZ9tyQ_DWXFZ2i7mEYf7o"
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

    /**
     * GENERATE QUESTIONS:
     * Asks Gemini to create 5 relevant check-in questions based on the time of day or user history.
     */
    suspend fun generateDailyQuestions(): List<String> {
        val promptText = """
            Generate 5 short, empathetic mental health check-in questions for a professional young adult.
            focus on stress, sleep, and workload.
            
            Strictly return them as a list separated by pipes "|".
            Example: How did you sleep?|What is your main focus today?|Do you feel supported?
        """.trimIndent()

        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        try {
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            // Clean up response and split by pipe
            val questions = rawText.replace("\n", "").split("|").map { it.trim() }

            // Fallback safety if AI format fails
            return if (questions.size >= 3) questions.take(5) else getDefaultQuestions()

        } catch (e: Exception) {
            println("Gemini Error (Questions): ${e.message}")
            return getDefaultQuestions()
        }
    }

    /**
     * ANALYZE ANSWERS:
     * Existing logic to analyze the text response.
     */
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
            return AnalysisResult(0.0f, "Your check-in has been saved locally.")
        }
    }

    private fun parseAnalysisResponse(rawText: String): AnalysisResult {
        var score = 0.0f
        var insight = "Take a moment to breathe."
        rawText.lines().forEach { line ->
            if (line.contains("SCORE:", ignoreCase = true)) {
                score = line.substringAfter(":").trim().toFloatOrNull() ?: 0.0f
            }
            if (line.contains("INSIGHT:", ignoreCase = true)) {
                insight = line.substringAfter(":").trim()
            }
        }
        return AnalysisResult(score, insight)
    }

    private fun getDefaultQuestions() = listOf(
        "How would you rate your sleep quality?",
        "How is your energy level right now?",
        "Do you feel overwhelmed by tasks today?",
        "Have you taken a break recently?",
        "How optimistic do you feel about tomorrow?"
    )*/

package org.vaulture.project.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vaulture.project.domain.CheckInResult

// --- Gemini Data Models ---
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null,
    val promptFeedback: PromptFeedback? = null // Often returned on safety blocks
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

// --- App Models ---
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

    // ⚠️ REPLACE WITH YOUR REAL API KEY
    // private val apiKey = "YOUR_REAL_API_KEY_HERE"
    private val apiKey = "AIzaSyDgJF1P9RqPmFejUbGD1Rc9YYMAEzWfOfE"
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

    /**
     * GENERATE QUESTIONS:
     * Asks Gemini to create 5 relevant check-in questions.
     */
    suspend fun generateDailyQuestions(): List<String> {
        val promptText = """
            Generate 5 short, empathetic mental health check-in questions for a professional young adult.
            focus on stress, sleep, and workload.
            
            Strictly return them as a list separated by pipes "|".
            Example: How did you sleep?|What is your main focus today?|Do you feel supported?
        """.trimIndent()

        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        try {
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val questions = rawText.replace("\n", "").split("|").map { it.trim() }

            return if (questions.size >= 3) questions.take(5) else getDefaultQuestions()
        } catch (e: Exception) {
            println("GeminiService: Error generating questions: ${e.message}")
            return getDefaultQuestions()
        }
    }

    /**
     * ANALYZE ANSWERS:
     * Analyzes a single text response for immediate feedback.
     */
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

    /**
     * GENERATE ANALYTICS REPORT:
     * Takes a list of past check-ins and generates a weekly summary/insight report in Markdown.
     * This corresponds to the `getAnalyticsInsights` feature.
     */
   /* suspend fun generateAnalyticsReport(checkIns: List<CheckInResult>): String {
        if (checkIns.isEmpty()) return "No data available for analysis yet."

        println("GeminiService: Generating analytics report for ${checkIns.size} entries.")

        val promptText = constructAnalyticsPrompt(checkIns)
        val requestBody = GeminiRequest(listOf(Content(listOf(Part(promptText)))))

        return try {
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!rawText.isNullOrBlank()) {
                println("GeminiService: Analytics report generated successfully.")
                rawText
            } else {
                "Unable to generate report at this time."
            }
        } catch (e: Exception) {
            println("GeminiService: Error generating analytics report: ${e.message}")
            "Error connecting to AI service. Please check your connection."
        }
    }*/
    /* ... inside GeminiService.kt ... */

    /**
     * GENERATE ANALYTICS REPORT
     */
    suspend fun generateAnalyticsReport(checkIns: List<CheckInResult>): String {
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

        // Safety check for empty strings
        if (rawText.isBlank()) return AnalysisResult(score, insight)

        rawText.lines().forEach { line ->
            if (line.contains("SCORE:", ignoreCase = true)) {
                // Better parsing for floats that might have brackets or text
                val scorePart = line.substringAfter(":").replace("[", "").replace("]", "").trim()
                score = scorePart.toFloatOrNull() ?: 0.0f
            }
            if (line.contains("INSIGHT:", ignoreCase = true)) {
                insight = line.substringAfter(":").replace("[", "").replace("]", "").trim()
            }
        }
        return AnalysisResult(score, insight)
    }


    /**
     * Constructs the rich prompt for the analytics report.
     * Adapted to use CheckInResult data available in KMP.
     */
    private fun constructAnalyticsPrompt(checkIns: List<CheckInResult>): String {
        // Summarize the recent history for the AI
        val summary = checkIns.takeLast(10).joinToString("\n") { entry ->
            "- Date: ${formatTimestamp(entry.timestamp)} | Score: ${entry.score}/100 (${entry.state.label}) | Previous Insight: ${entry.aiInsight}"
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

   /* private fun parseAnalysisResponse(rawText: String): AnalysisResult {
        var score = 0.0f
        var insight = "Take a moment to breathe."
        rawText.lines().forEach { line ->
            if (line.contains("SCORE:", ignoreCase = true)) {
                score = line.substringAfter(":").trim().toFloatOrNull() ?: 0.0f
            }
            if (line.contains("INSIGHT:", ignoreCase = true)) {
                insight = line.substringAfter(":").trim()
            }
        }
        return AnalysisResult(score, insight)
    }*/

    private fun getDefaultQuestions() = listOf(
        "How would you rate your sleep quality?",
        "How is your energy level right now?",
        "Do you feel overwhelmed by tasks today?",
        "Have you taken a break recently?",
        "How optimistic do you feel about tomorrow?"
    )

    // Helper to format timestamp in KMP
    private fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${date.month.name} ${date.dayOfMonth}"
    }
}

