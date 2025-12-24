package com.aevrontech.finevo.presentation.habit

import androidx.lifecycle.ViewModel
import com.aevrontech.finevo.domain.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HabitUiState(
    val isLoading: Boolean = false
)

class HabitViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()
}
