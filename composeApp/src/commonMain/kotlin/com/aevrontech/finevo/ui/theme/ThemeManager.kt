package com.aevrontech.finevo.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Singleton object to manage theme state globally */
object ThemeManager {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private var isInitialized = false

    // Callback to trigger Activity recreation
    var onThemeChanged: (() -> Unit)? = null

    fun setDarkMode(enabled: Boolean) {
        if (_isDarkMode.value != enabled) {
            println("ThemeManager: setDarkMode changing from ${_isDarkMode.value} to $enabled")
            _isDarkMode.value = enabled
            if (isInitialized) {
                saveDarkModePreference(enabled)
                // Trigger Activity recreation
                onThemeChanged?.invoke()
            }
        }
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    // Initialize from saved preference
    fun initialize() {
        if (!isInitialized) {
            val savedValue = loadDarkModePreference()
            println("ThemeManager: initialize() loaded saved value: $savedValue")
            _isDarkMode.value = savedValue
            isInitialized = true
        }
    }
}

// Expect functions for platform-specific storage
expect fun saveDarkModePreference(enabled: Boolean)

expect fun loadDarkModePreference(): Boolean
