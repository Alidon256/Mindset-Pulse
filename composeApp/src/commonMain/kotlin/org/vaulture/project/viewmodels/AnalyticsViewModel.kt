package org.vaulture.project.viewmodels

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vaulture.project.data.models.AnalyticsSummary
import org.vaulture.project.data.models.CheckInEntry
import org.vaulture.project.data.models.CbtExerciseType
import org.vaulture.project.data.models.CbtUsageData
import org.vaulture.project.data.models.EmotionFrequency
import org.vaulture.project.data.models.MoodTrendData
import org.vaulture.project.domain.CheckInResult
import org.vaulture.project.services.GeminiService

data class AnalyticsScreenUiState(
    val isLoading: Boolean = true,
    val isLoadingGemini: Boolean = false,
    val error: String? = null,
    val summary: AnalyticsSummary? = null,
    val geminiInsights: String? = null // The Markdown report
)

class AnalyticsViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(AnalyticsScreenUiState())
    val uiState: StateFlow<AnalyticsScreenUiState> = _uiState.asStateFlow()

    private var allCheckIns: List<CheckInEntry> = emptyList()

    init {
        loadAnalyticsData()
    }

    fun loadAnalyticsData(forceRefreshGemini: Boolean = false) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please sign in to view analytics.") }
            return
        }

        viewModelScope.launch {
            if (_uiState.value.summary == null || forceRefreshGemini) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // 1. Fetch from Firestore (Limit 30 for monthly trends)
                if (allCheckIns.isEmpty() || forceRefreshGemini) {
                    val querySnapshot = firestore.collection("users").document(user.uid)
                        .collection("checkIns")
                        .orderBy("timestamp", Direction.DESCENDING)
                        .limit(30)
                        .get()

                    allCheckIns = querySnapshot.documents.map { it.data() }
                }

                if (allCheckIns.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, summary = AnalyticsSummary(0)) }
                    return@launch
                }

                // 2. Process Deterministic Stats (Local Logic)
                val localSummary = processCheckInsForAnalytics(allCheckIns)
                _uiState.update { it.copy(isLoading = false, summary = localSummary) }

                // 3. Generate AI Report (Cloud Logic)
                if (_uiState.value.geminiInsights == null || forceRefreshGemini) {
                    fetchGeminiReport()
                }

            } catch (e: Exception) {
                println("Analytics Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = "Failed to load data.") }
            }
        }
    }

    private suspend fun fetchGeminiReport() {
        _uiState.update { it.copy(isLoadingGemini = true) }

        // Convert Firestore Entities to Domain Models for Gemini
        val domainResults = allCheckIns.map { entry ->
            val domainState = org.vaulture.project.domain.MentalState.values()
                .firstOrNull { it.name == entry.state.name }
                ?: org.vaulture.project.domain.MentalState.values().first()

            CheckInResult(
                score = entry.score,
                state = domainState,
                aiInsight = entry.aiInsight,
                timestamp = entry.timestamp.seconds * 1000L
            )
        }

        // Call the Service
        val report = geminiService.generateAnalyticsReport(domainResults)

        _uiState.update { it.copy(isLoadingGemini = false, geminiInsights = report) }
    }

    private fun processCheckInsForAnalytics(checkIns: List<CheckInEntry>): AnalyticsSummary {
        val total = checkIns.size

        // 1. Mood Distribution
        val moodCounts = checkIns.groupBy { it.overallMood }
            .map { (mood, list) ->
                MoodTrendData(mood, list.size, list.map { it.moodIntensity }.average().toFloat())
            }
            .sortedByDescending { it.count }

        // 2. Top Emotions
        val emotionFreq = checkIns.flatMap { it.primaryEmotions }
            .groupBy { it.emotion }
            .map { (emo, list) ->
                EmotionFrequency(emo, list.size, list.map { it.intensity }.average().toFloat())
            }
            .sortedByDescending { it.count }
            .take(5)

        // 3. CBT Usage
        // Explicitly name the variable to avoid confusion with KeyEvent.type
        val cbtUsage = checkIns.groupBy { it.cbtExerciseType }
            .map { (typeKey, list) -> CbtUsageData(typeKey, list.size) }
            .filter { data -> data.exerciseType != CbtExerciseType.NONE }
            .sortedByDescending { it.count }

        return AnalyticsSummary(
            totalCheckIns = total,
            overallMoodDistribution = moodCounts,
            mostFrequentEmotions = emotionFreq,
            cbtExerciseUsage = cbtUsage,
            averageMoodIntensity = if (checkIns.isNotEmpty()) checkIns.map { it.moodIntensity }.average().toFloat() else 0f,
            selfCareActivityFrequency = checkIns.flatMap { it.selfCareActivities }.groupingBy { it }.eachCount(),
            significantActivityFrequency = checkIns.flatMap { it.significantActivities }.groupingBy { it }.eachCount(),
            personalizedSuggestions = emptyList()
        )
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    suspend fun refreshGeminiInsights() {
        // This function can be called by a refresh button in the UI
        if (allCheckIns.isNotEmpty()) {
            fetchGeminiReport()
        } else {
            // Optionally, try to reload all data if check-ins are missing
            loadAnalyticsData(forceRefreshGemini = true)
        }
    }
}
