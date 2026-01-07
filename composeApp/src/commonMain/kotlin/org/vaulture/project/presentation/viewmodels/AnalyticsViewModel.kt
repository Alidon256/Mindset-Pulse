package org.vaulture.project.presentation.viewmodels

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
import org.vaulture.project.domain.model.AnalyticsSummary
import org.vaulture.project.domain.model.CheckInEntry
import org.vaulture.project.domain.model.CbtExerciseType
import org.vaulture.project.domain.model.CbtUsageData
import org.vaulture.project.domain.model.EmotionFrequency
import org.vaulture.project.domain.model.MoodTrendData
import org.vaulture.project.domain.engine.CheckInResult
import org.vaulture.project.domain.engine.MentalState
import org.vaulture.project.data.remote.GeminiService

data class AnalyticsScreenUiState(
    val isLoading: Boolean = true,
    val isLoadingGemini: Boolean = false,
    val error: String? = null,
    val summary: AnalyticsSummary? = null,
    val geminiInsights: String? = null
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
                if (allCheckIns.isEmpty() || forceRefreshGemini) {
                    val querySnapshot = firestore.collection("users").document(user.uid)
                        .collection("cbts")
                        .orderBy("timestamp", Direction.DESCENDING)
                        .limit(30)
                        .get()

                    allCheckIns = querySnapshot.documents.map { it.data() }
                }

                if (allCheckIns.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, summary = AnalyticsSummary(0)) }
                    return@launch
                }

                val localSummary = processCheckInsForAnalytics(allCheckIns)
                _uiState.update { it.copy(isLoading = false, summary = localSummary) }

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
        val report = geminiService.generateAnalyticsReport(allCheckIns)
        _uiState.update { it.copy(isLoadingGemini = false, geminiInsights = report) }
    }


    private fun processCheckInsForAnalytics(checkIns: List<CheckInEntry>): AnalyticsSummary {
        val total = checkIns.size

        // Mood Distribution
        val moodCounts = checkIns.groupBy { it.overallMood }
            .map { (mood, list) ->
                MoodTrendData(mood, list.size, list.map { it.moodIntensity }.average().toFloat())
            }
            .sortedByDescending { it.count }

        // Top Emotions
        val emotionFreq = checkIns.flatMap { it.primaryEmotions }
            .groupBy { it.emotion }
            .map { (emo, list) ->
                EmotionFrequency(emo, list.size, list.map { it.intensity }.average().toFloat())
            }
            .sortedByDescending { it.count }
            .take(5)

        // CBT Usage
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
        if (allCheckIns.isNotEmpty()) {
            fetchGeminiReport()
        } else {
            loadAnalyticsData(forceRefreshGemini = true)
        }
    }
}
