package com.aevrontech.finevo.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.domain.repository.AccountRepository
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/** ViewModel for Expense Tracker feature. */
class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Get current month date range
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val monthStart = LocalDate(today.year, today.month, 1)
    private val monthEnd =
        LocalDate(today.year, today.month, today.month.length(today.year % 4 == 0))

    init {
        loadAccounts()
        loadCategories()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getActiveAccounts("local_user").collect { accounts ->
                val selectedAccount = _uiState.value.selectedAccount ?: accounts.firstOrNull()
                _uiState.update { it.copy(accounts = accounts, selectedAccount = selectedAccount) }

                // Load transactions for selected account
                selectedAccount?.let { loadTransactionsForAccount(it.id) }
            }
        }
    }

    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
        loadTransactionsForAccount(account.id)
    }

    private fun loadTransactionsForAccount(accountId: String) {
        viewModelScope.launch {
            expenseRepository.getTransactionsByAccount(accountId, monthStart, monthEnd).collect { transactions ->
                // Enrich transactions with category data
                val enrichedTransactions = enrichWithCategoryData(transactions)

                val income =
                    enrichedTransactions.filter { it.type == TransactionType.INCOME }.sumOf {
                        it.amount
                    }
                val expense =
                    enrichedTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf {
                        it.amount
                    }

                _uiState.update {
                    it.copy(
                        transactions = enrichedTransactions,
                        isLoading = false,
                        accountIncome = income,
                        accountExpense = expense
                    )
                }
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            expenseRepository.getTransactions(monthStart, monthEnd).collect { transactions ->
                // Enrich transactions with category data
                val enrichedTransactions = enrichWithCategoryData(transactions)
                _uiState.update { it.copy(transactions = enrichedTransactions, isLoading = false) }
            }
        }
    }

    /**
     * Enriches transactions with category name/icon/color from loaded categories.
     *
     * Performance: O(n + m) where n = transactions, m = categories
     * - Uses HashMap for O(1) category lookups instead of O(m) list scan
     * - Scales well to 10,000+ transactions with 100+ categories
     */
    private fun enrichWithCategoryData(transactions: List<Transaction>): List<Transaction> {
        // Build category lookup map once - O(m)
        val categoryMap = _uiState.value.categories.associateBy { it.id }

        // Enrich each transaction with O(1) lookup - O(n)
        return transactions.map { transaction ->
            val category = categoryMap[transaction.categoryId]
            if (category != null &&
                (transaction.categoryName == null || transaction.categoryIcon == null)
            ) {
                transaction.copy(
                    categoryName = category.name,
                    categoryIcon = category.icon,
                    categoryColor = category.color
                )
            } else {
                transaction
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
        accountId: String?,
        categoryId: String,
        note: String?,
        date: LocalDate
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val selectedAccount = _uiState.value.accounts.find { it.id == accountId }
            val transaction =
                Transaction(
                    id = generateId(),
                    userId = "local", // Will be replaced with actual user ID
                    accountId = accountId,
                    type = type,
                    amount = amount,
                    currency = selectedAccount?.currency ?: "MYR",
                    categoryId = categoryId,
                    categoryName =
                        _uiState.value.categories.find { it.id == categoryId }?.name,
                    categoryIcon =
                        _uiState.value.categories.find { it.id == categoryId }?.icon,
                    categoryColor =
                        _uiState.value.categories.find { it.id == categoryId }?.color,
                    description = note,
                    note = note,
                    date = date,
                    createdAt = now,
                    updatedAt = now
                )

            when (val result = expenseRepository.addTransaction(transaction)) {
                is Result.Success -> {
                    // Update account balance if account selected
                    if (accountId != null && selectedAccount != null) {
                        val balanceChange = if (type == TransactionType.EXPENSE) -amount else amount
                        accountRepository.updateAccountBalance(
                            accountId,
                            selectedAccount.balance + balanceChange
                        )
                    }
                    _uiState.update {
                        it.copy(successMessage = "Transaction added!", showAddDialog = false)
                    }
                    // Reload transactions
                    loadTransactions()
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
            // Find the transaction to get its amount and account
            val transaction = _uiState.value.transactions.find { it.id == id }

            when (val result = expenseRepository.deleteTransaction(id)) {
                is Result.Success -> {
                    // Reverse the balance effect
                    if (transaction != null && transaction.accountId != null) {
                        val account =
                            _uiState.value.accounts.find { it.id == transaction.accountId }
                        if (account != null) {
                            val balanceChange =
                                if (transaction.type == TransactionType.EXPENSE) {
                                    transaction.amount // Add back what was subtracted
                                } else {
                                    -transaction.amount // Subtract what was added
                                }
                            accountRepository.updateAccountBalance(
                                transaction.accountId!!,
                                account.balance + balanceChange
                            )
                        }
                    }
                    _uiState.update { it.copy(successMessage = "Transaction deleted") }
                    loadTransactions()
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }

                is Result.Loading -> {}
            }
        }
    }

    fun updateTransaction(
        id: String,
        type: TransactionType,
        amount: Double,
        accountId: String?,
        categoryId: String,
        note: String?,
        date: LocalDate
    ) {
        viewModelScope.launch {
            // Find the old transaction to calculate balance delta
            val oldTransaction = _uiState.value.transactions.find { it.id == id }

            val now = Clock.System.now()
            val selectedAccount = _uiState.value.accounts.find { it.id == accountId }
            val transaction =
                Transaction(
                    id = id,
                    userId = "local",
                    accountId = accountId,
                    type = type,
                    amount = amount,
                    currency = selectedAccount?.currency ?: "MYR",
                    categoryId = categoryId,
                    categoryName =
                        _uiState.value.categories.find { it.id == categoryId }?.name,
                    categoryIcon =
                        _uiState.value.categories.find { it.id == categoryId }?.icon,
                    categoryColor =
                        _uiState.value.categories.find { it.id == categoryId }?.color,
                    description = note,
                    note = note,
                    date = date,
                    createdAt = oldTransaction?.createdAt ?: now,
                    updatedAt = now
                )

            when (val result = expenseRepository.updateTransaction(transaction)) {
                is Result.Success -> {
                    // Update account balance: reverse old effect, apply new effect
                    if (oldTransaction != null && accountId != null && selectedAccount != null) {
                        // Calculate old balance effect (what we need to reverse)
                        // Expense was subtracted from balance, so ADD it back (positive)
                        // Income was added to balance, so SUBTRACT it (negative)
                        val oldEffect =
                            if (oldTransaction.type == TransactionType.EXPENSE) {
                                oldTransaction.amount // Add expense back (reverse subtraction)
                            } else {
                                -oldTransaction.amount // Subtract income (reverse addition)
                            }

                        // Calculate new balance effect
                        // Expense subtracts from balance (negative)
                        // Income adds to balance (positive)
                        val newEffect =
                            if (type == TransactionType.EXPENSE) {
                                -amount // Expense subtracts from balance
                            } else {
                                amount // Income adds to balance
                            }

                        // Net change = reverse old + apply new
                        val netChange = oldEffect + newEffect

                        // Only update if there's actually a change
                        if (netChange != 0.0) {
                            accountRepository.updateAccountBalance(
                                accountId,
                                selectedAccount.balance + netChange
                            )
                        }
                    }

                    _uiState.update { it.copy(successMessage = "Transaction updated!") }
                    loadTransactions()
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
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val summary: TransactionSummary? = null,
    val accountIncome: Double = 0.0,
    val accountExpense: Double = 0.0,
    val showAddDialog: Boolean = false,
    val showAddAccountDialog: Boolean = false,
    val selectedTransactionType: TransactionType = TransactionType.EXPENSE,
    val error: String? = null,
    val successMessage: String? = null
) {
    val expenseTransactions: List<Transaction>
        get() = transactions.filter { it.type == TransactionType.EXPENSE }

    val incomeTransactions: List<Transaction>
        get() = transactions.filter { it.type == TransactionType.INCOME }

    val monthlyTotal: Double
        get() = summary?.netAmount ?: (accountIncome - accountExpense)

    val monthlyExpense: Double
        get() = summary?.totalExpense ?: accountExpense

    val monthlyIncome: Double
        get() = summary?.totalIncome ?: accountIncome

    val accountBalance: Double
        get() = selectedAccount?.balance ?: 0.0

    val accountBalancePercentage: Double
        get() {
            val balance = selectedAccount?.balance ?: 0.0
            return if (balance > 0) {
                ((balance - accountExpense) / balance * 100).coerceIn(0.0, 100.0)
            } else 0.0
        }
}
