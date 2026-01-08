package org.vaulture.project.presentation.viewmodels

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.vaulture.project.domain.model.RhythmTrack
import org.vaulture.project.data.repos.SearchContext
import org.vaulture.project.presentation.utils.KmpAudioPlayer // Ensure this interface exists in commonMain

data class RhythmUiState(
    val tracks: List<RhythmTrack> = emptyList(),
    val searchQuery: String = "",
    val isTracksLoading: Boolean = false,
    val currentTrack: RhythmTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)

class RhythmViewModel(
    private val audioPlayer: KmpAudioPlayer
) : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(RhythmUiState())
    val uiState: StateFlow<RhythmUiState> = _uiState.asStateFlow()
    private val _searchSuggestions = MutableStateFlow<List<RhythmTrack>>(emptyList())
    val searchSuggestions: StateFlow<List<RhythmTrack>> = _searchSuggestions.asStateFlow()

    init {
        observeAudioPlayer()
        loadTracks()
    }

    private fun observeAudioPlayer() {
        viewModelScope.launch {
            combine(audioPlayer.isPlaying, audioPlayer.currentPosition, audioPlayer.duration) { playing, pos, dur ->
                _uiState.update { it.copy(
                    isPlaying = playing,
                    currentPositionMs = pos,
                    durationMs = dur,
                    progress = pos,
                    duration = dur
                )}
            }.collect()
        }
    }

    fun updateSearchQuery(query: String, context: SearchContext) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
        } else {
            val filtered = _uiState.value.tracks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
            _searchSuggestions.value = filtered
        }
    }

    fun playNext() {
        val current = _uiState.value.currentTrack ?: return
        val list = _uiState.value.tracks
        val index = list.indexOf(current)
        if (index != -1 && index < list.lastIndex) {
            playTrack(list[index + 1])
        }
    }

    fun playPrevious() {
        val current = _uiState.value.currentTrack ?: return
        val list = _uiState.value.tracks
        val index = list.indexOf(current)
        if (index > 0) {
            playTrack(list[index - 1])
        }
    }

    fun closePlayer() {
        audioPlayer.stop()
        _uiState.update { it.copy(currentTrack = null, isPlaying = false, progress = 0) }
    }

    fun loadTrackById(trackId: String) {
        val existingTrack = _uiState.value.tracks.find { it.id == trackId }
        if (existingTrack != null) {
            _uiState.update { it.copy(currentTrack = existingTrack) }
            if (audioPlayer.isPlaying.value == false || _uiState.value.currentTrack?.id != trackId) {
                playTrack(existingTrack)
            }
        } else {
            viewModelScope.launch {
                try {
                    val doc = firestore.collection("tracks").document(trackId).get()
                    val track = doc.data<RhythmTrack>().copy(id = doc.id)
                    _uiState.update { it.copy(currentTrack = track) }
                    playTrack(track)
                } catch (e: Exception) {
                    println("ERROR: Could not load track $trackId - ${e.message}")
                }
            }
        }
    }

    fun playTrack(track: RhythmTrack) {
        _uiState.update { it.copy(currentTrack = track) }

        val url = if (track.previewUrl.isNotBlank()) track.previewUrl else track.previewUrl
        if (url.isNotBlank()) {
            println(" RHYTHM: Attempting to play URL: $url")
            audioPlayer.play(url, track.title, track.artist)

            incrementListenerCount(track.id)
        } else {
            println("RHYTHM: No valid URL found for track ${track.title}")
        }
    }
    private fun incrementListenerCount(trackId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("tracks").document(trackId).update(
                    "listenerCount" to FieldValue.increment(1)
                )
                println("Listener count incremented for $trackId")
            } catch (e: Exception) {
                println("Failed to increment listener count: ${e.message}")
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTracksLoading = true) }
            try {
                firestore.collection("tracks")
                    .orderBy("listenerCount", Direction.DESCENDING)
                    .snapshots()
                    .collect { snapshot ->
                        val tracks = snapshot.documents.map { doc ->
                            doc.data<RhythmTrack>().copy(id = doc.id)
                        }

                        println("RHYTHM LOG: Received ${tracks.size} tracks from Firestore")

                        _uiState.update { it.copy(
                            tracks = tracks,
                            isTracksLoading = false,
                            error = null
                        ) }
                    }
            } catch (e: Exception) {
                println("RHYTHM ERROR: ${e.message}")
                _uiState.update { it.copy(isTracksLoading = false, error = e.message) }
            }
        }
    }

    fun togglePlayback() {
        if (audioPlayer.isPlaying.value) audioPlayer.pause() else audioPlayer.resume()
    }


    fun pauseTrack() = audioPlayer.pause()
    fun resumeTrack() = audioPlayer.resume()
    fun seekTo(pos: Long) = audioPlayer.seekTo(pos)

    override fun onCleared() {
        audioPlayer.stop()
        super.onCleared()
    }
}
