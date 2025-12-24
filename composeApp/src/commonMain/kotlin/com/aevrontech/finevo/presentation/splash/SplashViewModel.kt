package com.aevrontech.finevo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(
    val isAnimating: Boolean = false,
    val showTagline: Boolean = false,
    val isLoading: Boolean = false,
    val navigateTo: SplashDestination? = null
)

enum class SplashDestination {
    ONBOARDING,
    HOME
}

class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun startAnimation() {
        viewModelScope.launch {
            // Start logo animation
            _uiState.update { it.copy(isAnimating = true) }
            
            delay(300)
            
            // Show tagline
            _uiState.update { it.copy(showTagline = true) }
            
            delay(500)
            
            // Check auth and navigate
            _uiState.update { it.copy(isLoading = true) }
            
            // TODO: Check if user is logged in
            val isLoggedIn = checkAuthStatus()
            
            delay(1000) // Minimum splash display time
            
            // Navigate to appropriate screen
            _uiState.update {
                it.copy(
                    isLoading = false,
                    navigateTo = if (isLoggedIn) SplashDestination.HOME else SplashDestination.ONBOARDING
                )
            }
        }
    }

    private suspend fun checkAuthStatus(): Boolean {
        // TODO: Implement actual auth check
        // For now, always go to onboarding
        return false
    }
}
