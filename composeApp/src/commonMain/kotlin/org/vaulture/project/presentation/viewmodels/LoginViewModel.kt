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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LoginUiState

        if (isLoading != other.isLoading) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (username != other.username) return false
        if (!profilePicture.contentEquals(other.profilePicture)) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + (profilePicture?.contentHashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

class LoginViewModel(val authService: AuthService) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _criticalError = MutableStateFlow<String?>(null)
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
