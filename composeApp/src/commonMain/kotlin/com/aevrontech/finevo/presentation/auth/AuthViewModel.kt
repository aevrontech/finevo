package com.aevrontech.finevo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.User
import com.aevrontech.finevo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel for authentication screens. */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Observe auth state
    val authState =
            authRepository.currentUser.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
            )

    init {
        // Check if already authenticated
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn.first()
            if (isLoggedIn) {
                val user = authRepository.currentUser.first()
                _uiState.value = _uiState.value.copy(isLoggedIn = true, user = user)
            }
        }
    }

    /** Sign in with email and password. */
    fun signIn(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = result.data,
                                    error = null
                            )
                }
                is Result.Error -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    /** Sign up with email and password. */
    fun signUp(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signUp(email, password)) {
                is Result.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = result.data,
                                    error = null,
                                    successMessage = "Account created successfully!"
                            )
                }
                is Result.Error -> {
                    val message = result.exception.message
                    // Check if it's a "confirm email" case
                    if (message?.contains("confirm", ignoreCase = true) == true ||
                                    message?.contains("verify", ignoreCase = true) == true
                    ) {
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        error = null,
                                        successMessage =
                                                "Please check your email to confirm your account"
                                )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    /** Sign in with Google ID token (and optional nonce for Credential Manager). */
    fun signInWithGoogle(idToken: String, nonce: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signInWithGoogle(idToken, nonce)) {
                is Result.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = result.data,
                                    error = null,
                                    successMessage =
                                            "Welcome, ${result.data.displayName ?: "User"}!"
                            )
                }
                is Result.Error -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Sign in with Apple ID token. */
    fun signInWithApple(idToken: String, nonce: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signInWithApple(idToken, nonce)) {
                is Result.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = result.data,
                                    error = null,
                                    successMessage =
                                            "Welcome, ${result.data.displayName ?: "User"}!"
                            )
                }
                is Result.Error -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Called when social login is initiated (to show loading). */
    fun onSocialLoginStarted() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    }

    /** Called when social login fails before reaching the server. */
    fun onSocialLoginError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }

    /** Send password reset email. */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    successMessage = "Password reset email sent"
                            )
                }
                is Result.Error -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Sign out the current user. */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState()
        }
    }

    /** Clear error message. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Clear success message. */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email")
            return false
        }
        if (!email.contains("@")) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email")
            return false
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your password")
            return false
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return false
        }
        return true
    }
}

/** UI state for authentication screens. */
data class AuthUiState(
        val isLoading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val user: User? = null,
        val error: String? = null,
        val successMessage: String? = null
)
