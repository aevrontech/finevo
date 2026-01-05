package com.aevrontech.finevo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.domain.model.UserPreferences
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(val isLoading: Boolean = false)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

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
}
