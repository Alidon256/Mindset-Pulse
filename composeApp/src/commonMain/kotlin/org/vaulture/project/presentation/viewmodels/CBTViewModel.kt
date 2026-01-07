package org.vaulture.project.presentation.viewmodels

import androidx.compose.runtime.mutableStateListOf
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vaulture.project.domain.model.CbtExerciseType
import org.vaulture.project.domain.model.CheckInEntry
import org.vaulture.project.domain.model.EmotionRating
import org.vaulture.project.domain.model.ThoughtRecordEntry

data class CBTScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val overallMood: String = "Okay",
    val moodIntensity: Int = 5, // 1-10
    val generalThoughts: String = "",
    val positiveHighlights: String = "",
    val challengesFaced: String = "",
    val cbtExerciseType: CbtExerciseType = CbtExerciseType.NONE,
    val trSituation: String = "",
    val trAutomaticNegativeThought: String = "",
    val trEvidenceForThought: String = "",
    val trEvidenceAgainstThought: String = "",
    val trAlternativeThought: String = "",
    val cbtReflectionResponse: String = "",
    val learnedFromCbt: String = ""
)

class CBTViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val _uiState = MutableStateFlow(CBTScreenUiState())
    val uiState: StateFlow<CBTScreenUiState> = _uiState.asStateFlow()
    val selectedPrimaryEmotions = mutableStateListOf<EmotionRating>()
    val selectedCognitiveDistortions = mutableStateListOf<String>()
    val selectedTrEmotionsBefore = mutableStateListOf<EmotionRating>()
    val selectedTrEmotionsAfter = mutableStateListOf<EmotionRating>()
    val selectedSignificantActivities = mutableStateListOf<String>()
    val selectedSelfCareActivities = mutableStateListOf<String>()


    private val userId: String?
        get() = auth.currentUser?.uid

    fun onOverallMoodChange(mood: String) {
        _uiState.value = _uiState.value.copy(overallMood = mood)
    }

    fun onMoodIntensityChange(intensity: Int) {
        _uiState.value = _uiState.value.copy(moodIntensity = intensity.coerceIn(1, 10))
    }

    fun onGeneralThoughtsChange(thoughts: String) {
        _uiState.value = _uiState.value.copy(generalThoughts = thoughts)
    }

    fun onPositiveHighlightsChange(highlights: String) {
        _uiState.value = _uiState.value.copy(positiveHighlights = highlights)
    }

    fun onChallengesFacedChange(challenges: String) {
        _uiState.value = _uiState.value.copy(challengesFaced = challenges)
    }

    fun onCbtExerciseTypeChange(type: CbtExerciseType) {
        _uiState.value = _uiState.value.copy(cbtExerciseType = type)
        if (type != CbtExerciseType.THOUGHT_RECORD) {
            clearThoughtRecordFields()
        }
        if (type == CbtExerciseType.NONE) {
            _uiState.value = _uiState.value.copy(cbtReflectionResponse = "", learnedFromCbt = "")
        }
    }
    fun onTrSituationChange(text: String) {
        _uiState.value = _uiState.value.copy(trSituation = text)
    }

    fun onTrAutomaticNegativeThoughtChange(text: String) {
        _uiState.value = _uiState.value.copy(trAutomaticNegativeThought = text)
    }

    fun onTrEvidenceForThoughtChange(text: String) {
        _uiState.value = _uiState.value.copy(trEvidenceForThought = text)
    }

    fun onTrEvidenceAgainstThoughtChange(text: String) {
        _uiState.value = _uiState.value.copy(trEvidenceAgainstThought = text)
    }

    fun onTrAlternativeThoughtChange(text: String) {
        _uiState.value = _uiState.value.copy(trAlternativeThought = text)
    }

    fun onCbtReflectionResponseChange(text: String) {
        _uiState.value = _uiState.value.copy(cbtReflectionResponse = text)
    }

    fun onLearnedFromCbtChange(text: String) {
        _uiState.value = _uiState.value.copy(learnedFromCbt = text)
    }
    fun togglePrimaryEmotion(emotion: String, intensity: Int = 5) {
        val existing = selectedPrimaryEmotions.find { it.emotion == emotion }
        if (existing != null) {
            selectedPrimaryEmotions.remove(existing)
        } else {
            selectedPrimaryEmotions.add(EmotionRating(emotion, intensity))
        }
    }
    fun updatePrimaryEmotionIntensity(emotion: String, intensity: Int) {
        val index = selectedPrimaryEmotions.indexOfFirst { it.emotion == emotion }
        if (index != -1) {
            selectedPrimaryEmotions[index] = selectedPrimaryEmotions[index].copy(intensity = intensity.coerceIn(1,10))
        }
    }


    fun toggleCognitiveDistortion(distortion: String) {
        if (selectedCognitiveDistortions.contains(distortion)) {
            selectedCognitiveDistortions.remove(distortion)
        } else {
            selectedCognitiveDistortions.add(distortion)
        }
    }

    fun addSignificantActivity(activity: String) {
        if (activity.isNotBlank() && !selectedSignificantActivities.contains(activity)) {
            selectedSignificantActivities.add(activity)
        }
    }
    fun removeSignificantActivity(activity: String) {
        selectedSignificantActivities.remove(activity)
    }

    fun addSelfCareActivity(activity: String) {
        if (activity.isNotBlank() && !selectedSelfCareActivities.contains(activity)) {
            selectedSelfCareActivities.add(activity)
        }
    }
    fun removeSelfCareActivity(activity: String) {
        selectedSelfCareActivities.remove(activity)
    }


    fun submitCheckIn() {
        val currentUserId = userId
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated.", submissionSuccess = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, submissionSuccess = false)
            val currentState = _uiState.value

            val thoughtRecordEntry = if (currentState.cbtExerciseType == CbtExerciseType.THOUGHT_RECORD) {
                ThoughtRecordEntry(
                    situation = currentState.trSituation,
                    automaticNegativeThought = currentState.trAutomaticNegativeThought,
                    emotionsBefore = selectedTrEmotionsBefore.toList(),
                    cognitiveDistortions = selectedCognitiveDistortions.toList(),
                    evidenceForThought = currentState.trEvidenceForThought,
                    evidenceAgainstThought = currentState.trEvidenceAgainstThought,
                    alternativeThought = currentState.trAlternativeThought,
                    emotionsAfter = selectedTrEmotionsAfter.toList()
                )
            } else {
                null
            }

            val newEntry = CheckInEntry(
                id = "",
                userId = currentUserId,
                timestamp = Timestamp.now(),
                overallMood = currentState.overallMood,
                moodIntensity = currentState.moodIntensity,
                primaryEmotions = selectedPrimaryEmotions.toList(),
                generalThoughts = currentState.generalThoughts,
                positiveHighlights = currentState.positiveHighlights,
                challengesFaced = currentState.challengesFaced,
                significantActivities = selectedSignificantActivities.toList(),
                selfCareActivities = selectedSelfCareActivities.toList(),
                cbtExerciseType = currentState.cbtExerciseType,
                thoughtRecord = thoughtRecordEntry,
                cbtReflectionPrompt = getCbtPromptForType(currentState.cbtExerciseType),
                cbtReflectionResponse = if (currentState.cbtExerciseType != CbtExerciseType.THOUGHT_RECORD) currentState.cbtReflectionResponse else null,
                learnedFromCbt = currentState.learnedFromCbt
            )

            try {
                firestore.collection("users").document(currentUserId)
                    .collection("cbts")
                    .add(newEntry)
                _uiState.value = _uiState.value.copy(isLoading = false, submissionSuccess = true)
                resetAllFields()
            } catch (e: Exception) {
                println("Error submitting check-in: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to submit: ${e.message}", submissionSuccess = false)
            }
        }
    }

    private fun getCbtPromptForType(type: CbtExerciseType): String? {
        return when (type) {
            CbtExerciseType.GRATITUDE_JOURNALING -> "What are three things you are grateful for today and why?"
            CbtExerciseType.BEHAVIORAL_ACTIVATION -> "What small, positive activity did you engage in or plan to engage in?"
            CbtExerciseType.PROBLEM_SOLVING -> "Describe a problem you worked on and the steps you took or considered."
            CbtExerciseType.MINDFULNESS_REFLECTION -> "Describe your mindfulness practice and any observations."
            else -> null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSubmissionStatus() {
        _uiState.value = _uiState.value.copy(submissionSuccess = false)
    }

    private fun clearThoughtRecordFields() {
        _uiState.value = _uiState.value.copy(
            trSituation = "",
            trAutomaticNegativeThought = "",
            trEvidenceForThought = "",
            trEvidenceAgainstThought = "",
            trAlternativeThought = ""
        )
        selectedCognitiveDistortions.clear()
        selectedTrEmotionsBefore.clear()
        selectedTrEmotionsAfter.clear()
    }

    private fun resetAllFields() {
        _uiState.value = CBTScreenUiState()
        selectedPrimaryEmotions.clear()
        selectedCognitiveDistortions.clear()
        selectedTrEmotionsBefore.clear()
        selectedTrEmotionsAfter.clear()
        selectedSignificantActivities.clear()
        selectedSelfCareActivities.clear()
    }
}
