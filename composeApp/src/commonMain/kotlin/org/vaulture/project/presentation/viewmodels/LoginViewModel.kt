package org.vaulture.project.presentation.viewmodels

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.vaulture.project.data.remote.AuthService

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val profilePicture: ByteArray? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(val authService: AuthService) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _criticalError = MutableStateFlow<String?>(null)
    val criticalError: StateFlow<String?> = _criticalError.asStateFlow()
    val isAuthenticated: StateFlow<Boolean> = authService.isAuthenticated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }
    fun onUsernameChange(username: String) = _uiState.update { it.copy(username = username, error = null) }
    fun onProfilePictureChange(bytes: ByteArray?) = _uiState.update { it.copy(profilePicture = bytes, error = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearCriticalError() = _criticalError.update { null }

    fun onSignInClick() {
        if (_uiState.value.isLoading) return
        val (email, password) = _uiState.value
        if (!isEmailValid(email) || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a valid email and password.") }
            return
        }

        performAuthAction("Email Sign-In") {
            authService.signInWithEmail(email, password)
            authService.onSignInSuccess()
        }
    }

    fun onCreateAccountClick() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value

        if (state.username.isBlank() || !isEmailValid(state.email) || state.password.length < 6) {
            _uiState.update { it.copy(error = "Please provide a username, valid email, and a password of at least 6 characters.") }
            return
        }

        performAuthAction("Account Creation") {
            val uid = authService.createAuthUser(state.email, state.password)
            authService.createUserProfile(
                uid = uid,
                email = state.email,
                username = state.username,
                profilePicture = state.profilePicture
            )

            authService.onSignInSuccess()
        }
    }

    private fun performAuthAction(actionName: String, action: suspend () -> Unit) {
        println("ViewModel: Starting action '$actionName'.")
        _uiState.update { it.copy(isLoading = true, error = null) }
        _criticalError.update { null }

        viewModelScope.launch {
            try {
                action()
                println("ViewModel: Action '$actionName' completed successfully.")
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred."
                println("ViewModel: Action '$actionName' FAILED. Error: $errorMessage")
                e.printStackTrace()

                if (errorMessage.contains("400") && (errorMessage.contains("Bad Request") || errorMessage.contains("INVALID_REFRESH_TOKEN"))) {
                    _criticalError.update { "Your session has expired. Please sign in again." }
                } else {
                    _uiState.update { it.copy(error = errorMessage) }
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && "@" in email && email.substringAfterLast("@").contains(".")
    }
}
