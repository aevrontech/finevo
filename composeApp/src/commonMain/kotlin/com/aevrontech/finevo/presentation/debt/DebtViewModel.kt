package com.aevrontech.finevo.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.domain.repository.DebtRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/** ViewModel for Debt Payoff Planner feature. */
class DebtViewModel(private val debtRepository: DebtRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    init {
        loadDebts()
        observeTotalDebt()
    }

    private fun loadDebts() {
        viewModelScope.launch {
            debtRepository.getActiveDebts().collect { debts ->
                _uiState.update { it.copy(debts = debts, isLoading = false) }
            }
        }
    }

    private fun observeTotalDebt() {
        viewModelScope.launch {
            debtRepository.getTotalDebt().collect { total ->
                _uiState.update { it.copy(totalDebt = total) }
            }
        }
    }

    fun addDebt(
            name: String,
            type: DebtType,
            originalAmount: Double,
            currentBalance: Double,
            interestRate: Double,
            minimumPayment: Double,
            dueDay: Int
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val debt =
                    Debt(
                            id = generateId(),
                            userId = "local",
                            name = name,
                            type = type,
                            originalAmount = originalAmount,
                            currentBalance = currentBalance,
                            interestRate = interestRate,
                            minimumPayment = minimumPayment,
                            dueDay = dueDay,
                            startDate = today,
                            createdAt = now,
                            updatedAt = now
                    )

            when (val result = debtRepository.addDebt(debt)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Debt added!", showAddDialog = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun recordPayment(debtId: String, amount: Double, isExtraPayment: Boolean = false) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Simple split: 80% principal, 20% interest (simplified)
            val interestAmount = amount * 0.2
            val principalAmount = amount - interestAmount

            val payment =
                    DebtPayment(
                            id = generateId(),
                            debtId = debtId,
                            amount = amount,
                            principalAmount = principalAmount,
                            interestAmount = interestAmount,
                            date = today,
                            isExtraPayment = isExtraPayment,
                            createdAt = now
                    )

            when (val result = debtRepository.addPayment(payment)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Payment recorded!") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch {
            when (val result = debtRepository.deleteDebt(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Debt deleted") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun setPayoffStrategy(strategy: PayoffStrategy) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
        calculatePayoffPlan()
    }

    fun calculatePayoffPlan() {
        viewModelScope.launch {
            val strategy = _uiState.value.selectedStrategy
            val extraPayment = _uiState.value.extraMonthlyPayment

            when (val result = debtRepository.calculatePayoffPlan(strategy, extraPayment)) {
                is Result.Success -> {
                    _uiState.update { it.copy(payoffPlan = result.data) }
                }
                is Result.Error -> {
                    // Ignore for now
                }
                is Result.Loading -> {}
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString() +
                (1000..9999).random().toString()
    }
}

/** UI state for Debt screen. */
data class DebtUiState(
        val isLoading: Boolean = true,
        val debts: List<Debt> = emptyList(),
        val totalDebt: Double = 0.0,
        val payoffPlan: PayoffPlan? = null,
        val selectedStrategy: PayoffStrategy = PayoffStrategy.AVALANCHE,
        val extraMonthlyPayment: Double = 0.0,
        val showAddDialog: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
) {
    val debtCount: Int
        get() = debts.size

    val averageInterestRate: Double
        get() = if (debts.isNotEmpty()) debts.map { it.interestRate }.average() else 0.0

    val totalMinimumPayment: Double
        get() = debts.sumOf { it.minimumPayment }

    val totalPaid: Double
        get() = debts.sumOf { it.totalPaid }
}
