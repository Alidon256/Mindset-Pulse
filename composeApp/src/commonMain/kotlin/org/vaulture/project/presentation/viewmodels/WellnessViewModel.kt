package org.vaulture.project.presentation.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.vaulture.project.domain.model.WellnessRecord
import org.vaulture.project.domain.model.WellnessStats
import org.vaulture.project.domain.model.WellnessType
import kotlin.time.Clock


data class WellnessUiState(
    val phase: WellnessPhase = WellnessPhase.SETUP,
    val isTimerRunning: Boolean = false,
    val timeLeftSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val currentActivity: WellnessType? = null,
    val stats: WellnessStats = WellnessStats(),
    val isCompleting: Boolean = false,
    val breathText: String = "Prepare",
    val sessionSaved: Boolean = false
)

enum class WellnessPhase { SETUP, ACTIVE, SUMMARY }

class WellnessViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(WellnessUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var statsListenerJob: Job? = null

    init {
        println("🚀 [BREADCRUMB] WELLNESS_VM: Global Instance Active")
        observeAuthAndLoadStats()
    }

    private fun observeAuthAndLoadStats() {
        viewModelScope.launch {
            auth.authStateChanged.collect { user ->
                if (user != null) {
                    println("👤 [BREADCRUMB] AUTH: User detected (${user.uid}). Loading stats...")
                    loadStats(user.uid)
                } else {
                    println("🚪 [BREADCRUMB] AUTH: Sign-out. Killing listeners.")
                    statsListenerJob?.cancel()
                    _uiState.update { WellnessUiState() }
                }
            }
        }
    }

    private fun calculateUpdatedStats(current: WellnessStats, durationMins: Int): WellnessStats {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStr = now.toString()
        val yesterdayStr = now.minus(1, DateTimeUnit.DAY).toString()

        println("📊 [LOG] CALC_STATS: Today: $todayStr | Last DB Date: ${current.lastActivityDate}")

        val isNewDay = current.lastActivityDate != todayStr

        val newStreak = when (current.lastActivityDate) {
            todayStr -> current.currentStreak
            yesterdayStr -> current.currentStreak + 1
            "" -> 1
            else -> 1 // Streak broken
        }

        val sessionsToday = if (isNewDay) 1 else (current.sessionsToday + 1).coerceAtMost(5)
        val pointsEarned = 50 + (durationMins * 10) + (sessionsToday * 20)

        val updated = current.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(newStreak, current.longestStreak),
            lastActivityDate = todayStr,
            totalMinutes = current.totalMinutes + durationMins,
            sessionsToday = sessionsToday,
            resiliencePoints = current.resiliencePoints + pointsEarned
        )

        println("📊 [LOG] CALC_STATS: Final -> Streak: ${updated.currentStreak}, XP: ${updated.resiliencePoints}")
        return updated
    }

    fun startTimer(durationMinutes: Int) {
        timerJob?.cancel()
        val totalSeconds = durationMinutes * 60
        val type = _uiState.value.currentActivity ?: WellnessType.BREATHING

        println("⏱️ [BREADCRUMB] TIMER: Starting $type for $totalSeconds seconds")

        _uiState.update { it.copy(
            phase = WellnessPhase.ACTIVE,
            timeLeftSeconds = totalSeconds,
            totalDurationSeconds = totalSeconds,
            isTimerRunning = true,
            isCompleting = false,
            sessionSaved = false
        )}

        timerJob = viewModelScope.launch(Dispatchers.Main) {
            try {
                var elapsed = 0
                while (isActive && _uiState.value.timeLeftSeconds > 0) {
                    delay(1000)
                    elapsed++
                    val cycle = elapsed % 8
                    val guide = if (cycle < 4) "BREATHE IN" else "BREATHE OUT"

                    _uiState.update { it.copy(
                        timeLeftSeconds = it.timeLeftSeconds - 1,
                        breathText = guide
                    )}
                }

                if (isActive && _uiState.value.timeLeftSeconds == 0) {
                    println("🏁 [BREADCRUMB] TIMER: Reached zero. Triggering database sync...")
                    completeActivity()
                }
            } catch (e: Exception) {
                println("🛑 [LOG] TIMER: Coroutine interrupted: ${e.message}")
            }
        }
    }

    private fun completeActivity() {
        val uid = auth.currentUser?.uid ?: return
        val type = _uiState.value.currentActivity ?: return
        val duration = _uiState.value.totalDurationSeconds

        println("💾 [BREADCRUMB] SYNC: Starting Global Upload Sequence...")
        _uiState.update { it.copy(isTimerRunning = false, isCompleting = true) }

        // We use viewModelScope but wrap in NonCancellable to ensure
        // the data hits the DB even if the user switches screens.
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    // STEP 1: UPLOAD SESSION RECORD
                    val record = WellnessRecord(
                        userId = uid,
                        type = type,
                        durationSeconds = duration,
                        timestamp = Timestamp.now()
                    )
                    println("📡 [DB_STEP 1] Uploading WellnessRecord to 'wellnessRecords'...")
                    db.collection("users").document(uid).collection("wellnessRecords").add(record)
                    println("✅ [DB_STEP 1] Record Uploaded Successfully.")

                    // STEP 2: ATOMIC STATS UPDATE
                    println("📡 [DB_STEP 2] Fetching current stats from 'stats/wellness'...")
                    val statsRef = db.collection("users").document(uid).collection("stats").document("wellness")
                    val snapshot = statsRef.get()
                    val currentStats = if (snapshot.exists) snapshot.data<WellnessStats>() else WellnessStats()

                    val updated = calculateUpdatedStats(currentStats, duration / 60)

                    println("📡 [DB_STEP 3] Writing updated stats to Firestore...")
                    statsRef.set(updated)
                    println("✅ [DB_STEP 3] Stats Uploaded Successfully.")

                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(
                            stats = updated,
                            phase = WellnessPhase.SUMMARY,
                            isCompleting = false,
                            sessionSaved = true
                        )}
                        println("🎉 [BREADCRUMB] SYNC: COMPLETE. Mindset Battery updated.")
                    }
                } catch (e: Exception) {
                    println("❌ [BREADCRUMB] DB FATAL ERROR: ${e.message}")
                    e.printStackTrace()
                    _uiState.update { it.copy(isCompleting = false) }
                }
            }
        }
    }

    private fun loadStats(uid: String) {
        statsListenerJob?.cancel()
        statsListenerJob = viewModelScope.launch {
            println("👂 [BREADCRUMB] LISTENER: Attaching to /stats/wellness")
            try {
                db.collection("users").document(uid).collection("stats").document("wellness")
                    .snapshots().collect { snap ->
                        if (snap.exists) {
                            val s = snap.data<WellnessStats>()
                            println("📥 [DB_LISTENER] Incoming Data: Streak ${s.currentStreak}, XP: ${s.resiliencePoints}")
                            _uiState.update { it.copy(stats = s) }
                        }
                    }
            } catch (e: Exception) {
                println("❌ [DB_LISTENER] Error: ${e.message}")
            }
        }
    }

    fun selectActivity(type: WellnessType) {
        println("👆 [LOG] UI_ACTION: Selected $type")
        _uiState.update { it.copy(currentActivity = type, phase = WellnessPhase.SETUP) }
    }

    fun resetToSetup() {
        println("🔄 [LOG] UI_ACTION: Resetting session state")
        timerJob?.cancel()
        _uiState.update { it.copy(phase = WellnessPhase.SETUP, isTimerRunning = false, timeLeftSeconds = 0) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        statsListenerJob?.cancel()
        super.onCleared()
    }
}

