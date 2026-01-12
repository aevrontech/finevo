package com.aevrontech.finevo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.manager.NotificationManager
import com.aevrontech.finevo.domain.model.UserPreferences
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

data class SettingsUiState(val isLoading: Boolean = false)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val preferences =
        settingsRepository
            .getPreferences()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UserPreferences(userId = "")
            )

    fun updateCurrency(currencyCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = settingsRepository.setCurrency(currencyCode)
            // Handle result if needed (e.g. show error)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            val currentPrefs = preferences.value
            if (enabled) {
                // Determine time: existing time or default 20:00 (8 PM)
                val timeStr = currentPrefs.dailyReminderTime ?: "20:00"
                if (notificationManager.requestPermission()) {
                    val timeParts = timeStr.split(":").map { it.toInt() }
                    notificationManager.scheduleDailyReminder(LocalTime(timeParts[0], timeParts[1]))
                    // Save enabled state and time
                    settingsRepository.updatePreferences(
                        currentPrefs.copy(
                            dailyReminderEnabled = true,
                            dailyReminderTime = timeStr
                        )
                    )
                } else {
                    // Update UI to reflect permission denied? For now just don't enable
                }
            } else {
                notificationManager.cancelDailyReminder()
                settingsRepository.updatePreferences(
                    currentPrefs.copy(dailyReminderEnabled = false)
                )
            }
        }
    }

    fun updateDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val time = LocalTime(hour, minute)
            val timeStr =
                "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

            notificationManager.scheduleDailyReminder(time)
            settingsRepository.updatePreferences(
                preferences.value.copy(dailyReminderTime = timeStr)
            )
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            settingsRepository.setPinEnabled(false, null)
            settingsRepository.setBiometricEnabled(false) // Disable bio if pin disabled
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            settingsRepository.setBiometricEnabled(enabled)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
