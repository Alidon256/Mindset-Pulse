package org.vaulture.project.presentation.viewmodels

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.vaulture.project.domain.model.CheckInEntry
import org.vaulture.project.domain.model.MentalState
import org.vaulture.project.domain.engine.CheckInResult
import org.vaulture.project.domain.engine.RiskEngine
import org.vaulture.project.data.remote.GeminiService
import kotlin.time.Clock
import kotlin.time.Instant

data class CheckInUiState(
    val isLoadingQuestions: Boolean = true,
    val step: Int = 0,
    val questions: List<String> = emptyList(),
    val answers: MutableList<Int> = mutableListOf(),
    val textResponse: String = "",
    val isAnalyzing: Boolean = false,
    val result: CheckInResult? = null
)

class CheckInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState = _uiState.asStateFlow()
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val _latestResult = MutableStateFlow<CheckInResult?>(null)
    val latestResult = _latestResult.asStateFlow()

    private val geminiService = GeminiService()

    private var checkInListenerJob: Job? = null

    init {
        viewModelScope.launch {
            auth.authStateChanged.collect { user ->
                if (user != null) {
                    observeTodayResult()
                    loadDailyQuestions()
                } else {
                    checkInListenerJob?.cancel()
                    _latestResult.value = null
                }
            }
        }
    }

    fun syncResult(result: CheckInResult) {
        _uiState.update { it.copy(result = result) }
    }

    private fun observeTodayResult() {
        val uid = auth.currentUser?.uid ?: return

        checkInListenerJob?.cancel()
        checkInListenerJob = viewModelScope.launch {
            try {
                firestore.collection("users").document(uid)
                    .collection("checkIns")
                    .orderBy("timestamp", Direction.DESCENDING)
                    .limit(1)
                    .snapshots()
                    .collect { snapshot ->
                        val document = snapshot.documents.firstOrNull()
                        if (document != null) {
                            try {
                                val entry = document.data<CheckInEntry>()

                                val entryInstant = Instant.fromEpochSeconds(entry.timestamp.seconds)
                                val entryDate = entryInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                                if (entryDate == todayDate) {
                                    val domainState = when(entry.state.name) {
                                        "STABLE" -> org.vaulture.project.domain.engine.MentalState.STABLE
                                        "MILD_STRESS" -> org.vaulture.project.domain.engine.MentalState.MILD_STRESS
                                        "HIGH_STRESS" -> org.vaulture.project.domain.engine.MentalState.HIGH_STRESS
                                        "BURNOUT_RISK" -> org.vaulture.project.domain.engine.MentalState.BURNOUT_RISK
                                        else -> org.vaulture.project.domain.engine.MentalState.STABLE
                                    }

                                    val domainResult = CheckInResult(
                                        score = entry.score,
                                        state = domainState,
                                        aiInsight = entry.aiInsight,
                                        timestamp = entry.timestamp.seconds * 1000
                                    )

                                    _latestResult.value = domainResult
                                    _uiState.update { it.copy(result = domainResult) }
                                    println("LOADED TODAY'S CHECK-IN")
                                } else {
                                    _latestResult.value = null
                                    _uiState.update { it.copy(result = null) }
                                    println("Latest entry is from $entryDate (Today is $todayDate). Resetting UI.")
                                }

                            } catch (e: Exception) {
                                println("MAPPING ERROR: ${e.message}")
                            }
                        } else {
                            _latestResult.value = null
                        }
                    }
            } catch (e: Exception) {
                println(" FIREBASE ERROR: ${e.message}")
            }
        }
    }


    // 1. Fetch AI Questions on init
    private fun loadDailyQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingQuestions = true) }
            val dynamicQuestions = geminiService.generateDailyQuestions()
            _uiState.update {
                it.copy(
                    isLoadingQuestions = false,
                    questions = dynamicQuestions
                )
            }
        }
    }

    fun selectAnswer(score: Int) {
        val currentAnswers = _uiState.value.answers.toMutableList()
        currentAnswers.add(score)
        _uiState.update { it.copy(answers = currentAnswers, step = it.step + 1) }
    }

    fun onTextChange(text: String) {
        _uiState.update { it.copy(textResponse = text) }
    }


    fun submitCheckIn() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isAnalyzing = true) }

        viewModelScope.launch {
            try {
                // 1. Analyze with Gemini
                val aiResult = geminiService.analyzeJournalEntry(_uiState.value.textResponse)

                // 2. Calculate Risk
                val (riskScore, mentalState) = RiskEngine.calculateRisk(
                    answers = _uiState.value.answers,
                    sentimentScore = aiResult.sentimentScore
                )

                // 3. Prepare Domain Result for immediate UI feedback
                val finalResult = CheckInResult(
                    score = riskScore,
                    state = mentalState,
                    aiInsight = aiResult.insight,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )

                // 4. Map to Data Layer Entity for Firestore
                val dataState = MentalState.valueOf(mentalState.name)
                val newEntry = CheckInEntry(
                    userId = uid,
                    score = riskScore,
                    state = dataState,
                    aiInsight = aiResult.insight,
                    sentimentScore = aiResult.sentimentScore,
                    timestamp = Timestamp.now(),
                    generalThoughts = _uiState.value.textResponse
                )

                // 5. Save to Firestore
                firestore.collection("users").document(uid)
                    .collection("checkIns")
                    .add(newEntry)

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        result = finalResult
                    )
                }
                _uiState.update { it.copy(textResponse = "") }

            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false) }
                println("Submission Error: ${e.message}")
            }
        }
    }



    // Allow user to refresh questions if they don't like them
    fun refreshQuestions() {
        loadDailyQuestions()
    }
}
