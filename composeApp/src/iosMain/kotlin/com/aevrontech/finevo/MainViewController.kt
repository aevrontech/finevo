package com.aevrontech.finevo

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.aevrontech.finevo.domain.repository.SettingsRepository
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.presentation.home.HomeScreen
import com.aevrontech.finevo.presentation.onboarding.OnboardingScreen
import com.aevrontech.finevo.presentation.security.SecurityScreen
import org.koin.compose.koinInject

fun MainViewController() = ComposeUIViewController {
    val settingsRepository = koinInject<SettingsRepository>()

    val initialScreen = remember {
        val isLoggedIn = settingsRepository.isLoggedIn()
        val hasCompletedOnboarding = settingsRepository.hasCompletedOnboarding()
        val isPinEnabled = settingsRepository.isPinEnabled()

        when {
            isLoggedIn && isPinEnabled -> SecurityScreen()
            isLoggedIn -> HomeScreen()
            hasCompletedOnboarding -> LoginScreen()
            else -> OnboardingScreen()
        }
    }

    App(initialScreen)
}
