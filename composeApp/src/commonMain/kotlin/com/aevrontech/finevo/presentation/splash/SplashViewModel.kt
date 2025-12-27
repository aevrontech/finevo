package com.aevrontech.finevo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the splash screen.
 * Handles initialization and navigation decisions.
 */
class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    /**
     * Start the splash animation and determine navigation.
     */
    fun startAnimation() {
        viewModelScope.launch {
            // Start animation
            _uiState.value = _uiState.value.copy(isAnimating = true)

            // Brief delay for logo animation
            delay(500)

            // Show tagline
            _uiState.value = _uiState.value.copy(showTagline = true)

            // Check authentication state
            delay(1500)

            val isLoggedIn = authRepository.isLoggedIn.first()

            // TODO: Check if onboarding has been completed
            val hasCompletedOnboarding = false // For now, always show onboarding

            // Determine navigation destination
            val destination = when {
                isLoggedIn -> SplashDestination.HOME
                hasCompletedOnboarding -> SplashDestination.LOGIN
                else -> SplashDestination.ONBOARDING
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                navigateTo = destination
            )
        }
    }
}

/**
 * UI state for splash screen
 */
data class SplashUiState(
    val isAnimating: Boolean = false,
    val showTagline: Boolean = false,
    val isLoading: Boolean = true,
    val navigateTo: SplashDestination? = null
)

/**
 * Possible navigation destinations from splash
 */
enum class SplashDestination {
    ONBOARDING,
    LOGIN,
    HOME
}
