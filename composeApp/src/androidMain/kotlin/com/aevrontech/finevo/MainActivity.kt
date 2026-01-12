package com.aevrontech.finevo

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.aevrontech.finevo.domain.repository.SettingsRepository
import com.aevrontech.finevo.presentation.auth.LoginScreen
import com.aevrontech.finevo.presentation.auth.SocialLoginHandler
import com.aevrontech.finevo.presentation.common.ImagePicker
import com.aevrontech.finevo.presentation.home.HomeScreen
import com.aevrontech.finevo.presentation.onboarding.OnboardingScreen
import com.aevrontech.finevo.ui.theme.ThemeManager
import com.aevrontech.finevo.ui.theme.initThemePreferences
import com.aevrontech.finevo.util.ActivityProvider
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityProvider.currentActivity = this
        val activityStart = System.currentTimeMillis()
        android.util.Log.i("StartupTiming", "▶ MainActivity.onCreate START")

        // Initialize theme preferences with context
        val themeStart = System.currentTimeMillis()
        initThemePreferences(this)
        android.util.Log.i(
            "StartupTiming",
            "  Theme init: ${System.currentTimeMillis() - themeStart}ms"
        )

        // Set up theme change callback to recreate activity
        ThemeManager.onThemeChanged = { recreate() }

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Register ImagePicker launchers for camera/gallery access
        ImagePicker.registerLaunchers(this)

        // Get SettingsRepository for fast local checks
        val injectStart = System.currentTimeMillis()
        val settingsRepository: SettingsRepository by inject()
        android.util.Log.i(
            "StartupTiming",
            "  SettingsRepository inject: ${System.currentTimeMillis() - injectStart}ms"
        )

        // Synchronous startup checks (all from SharedPrefs - instant, no coroutines needed)
        val checksStart = System.currentTimeMillis()
        val isLoggedIn = settingsRepository.isLoggedIn()
        val hasCompletedOnboarding = settingsRepository.hasCompletedOnboarding()
        val isPinEnabled = settingsRepository.isPinEnabled()
        android.util.Log.i(
            "StartupTiming",
            "  Settings checks: ${System.currentTimeMillis() - checksStart}ms (loggedIn=$isLoggedIn, onboarding=$hasCompletedOnboarding, pin=$isPinEnabled)"
        )

        // Determine initial screen without any network calls
        val initialScreen =
            when {
                isLoggedIn && isPinEnabled ->
                    com.aevrontech.finevo.presentation.security.SecurityScreen()
                isLoggedIn -> HomeScreen()
                hasCompletedOnboarding -> LoginScreen()
                else -> OnboardingScreen()
            }
        android.util.Log.i("StartupTiming", "  Screen: ${initialScreen::class.simpleName}")

        val composeStart = System.currentTimeMillis()
        setContent { App(initialScreen) }
        android.util.Log.i(
            "StartupTiming",
            "  setContent call returned: ${System.currentTimeMillis() - composeStart}ms"
        )
        android.util.Log.i(
            "StartupTiming",
            "◀ MainActivity.onCreate END: ${System.currentTimeMillis() - activityStart}ms"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            ThemeManager.onThemeChanged = null
        }
        ActivityProvider.currentActivity = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Google Sign-In result
        SocialLoginHandler.handleActivityResult(requestCode, data)
    }
}
