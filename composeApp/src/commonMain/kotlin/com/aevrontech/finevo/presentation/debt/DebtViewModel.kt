package com.aevrontech.finevo.presentation.debt

import androidx.lifecycle.ViewModel
import com.aevrontech.finevo.domain.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DebtUiState(
    val isLoading: Boolean = false
)

class DebtViewModel(
    private val debtRepository: DebtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()
}
