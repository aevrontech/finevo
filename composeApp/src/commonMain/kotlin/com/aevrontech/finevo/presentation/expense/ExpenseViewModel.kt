package com.aevrontech.finevo.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/** ViewModel for Expense Tracker feature. */
class ExpenseViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Get current month date range
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val monthStart = LocalDate(today.year, today.month, 1)
    private val monthEnd =
            LocalDate(today.year, today.month, today.month.length(today.year % 4 == 0))

    init {
        loadTransactions()
        loadCategories()
        observeTransactionSummary()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            expenseRepository.getTransactions(monthStart, monthEnd).collect { transactions ->
                _uiState.update { it.copy(transactions = transactions, isLoading = false) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            expenseRepository.getCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun observeTransactionSummary() {
        viewModelScope.launch {
            expenseRepository.getTransactionSummary(monthStart, monthEnd).collect { summary ->
                _uiState.update { it.copy(summary = summary) }
            }
        }
    }

    fun addTransaction(
            type: TransactionType,
            amount: Double,
            categoryId: String,
            description: String?,
            note: String? = null
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val transaction =
                    Transaction(
                            id = generateId(),
                            userId = "local", // Will be replaced with actual user ID
                            type = type,
                            amount = amount,
                            currency = "MYR",
                            categoryId = categoryId,
                            categoryName =
                                    _uiState.value.categories.find { it.id == categoryId }?.name,
                            categoryIcon =
                                    _uiState.value.categories.find { it.id == categoryId }?.icon,
                            categoryColor =
                                    _uiState.value.categories.find { it.id == categoryId }?.color,
                            description = description,
                            note = note,
                            date = today,
                            createdAt = now,
                            updatedAt = now
                    )

            when (val result = expenseRepository.addTransaction(transaction)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Transaction added!", showAddDialog = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            when (val result = expenseRepository.deleteTransaction(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Transaction deleted") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun showAddDialog(type: TransactionType = TransactionType.EXPENSE) {
        _uiState.update { it.copy(showAddDialog = true, selectedTransactionType = type) }
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

/** UI state for Expense screen. */
data class ExpenseUiState(
        val isLoading: Boolean = true,
        val transactions: List<Transaction> = emptyList(),
        val categories: List<Category> = emptyList(),
        val summary: TransactionSummary? = null,
        val showAddDialog: Boolean = false,
        val selectedTransactionType: TransactionType = TransactionType.EXPENSE,
        val error: String? = null,
        val successMessage: String? = null
) {
    val expenseTransactions: List<Transaction>
        get() = transactions.filter { it.type == TransactionType.EXPENSE }

    val incomeTransactions: List<Transaction>
        get() = transactions.filter { it.type == TransactionType.INCOME }

    val monthlyTotal: Double
        get() = summary?.netAmount ?: 0.0

    val monthlyExpense: Double
        get() = summary?.totalExpense ?: 0.0

    val monthlyIncome: Double
        get() = summary?.totalIncome ?: 0.0
}
