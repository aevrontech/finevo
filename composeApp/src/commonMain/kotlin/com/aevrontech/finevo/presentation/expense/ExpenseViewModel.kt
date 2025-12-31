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

    // Filter state
    private val _filterPeriod = MutableStateFlow(FilterPeriod.MONTH)
    val filterPeriod: StateFlow<FilterPeriod> = _filterPeriod.asStateFlow()

    private val _periodOffset = MutableStateFlow(0)
    val periodOffset: StateFlow<Int> = _periodOffset.asStateFlow()

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
                val currentSelectedId = _uiState.value.selectedAccount?.id
                // Find the updated account object from the new list, or default to first if none
                // selected/found
                val selectedAccount =
                    accounts.find { it.id == currentSelectedId } ?: accounts.firstOrNull()

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
        val accountMap = _uiState.value.accounts.associateBy { it.id }

        // Enrich each transaction with O(1) lookup - O(n)
        return transactions.map { transaction ->
            val category = categoryMap[transaction.categoryId]
            val account = accountMap[transaction.accountId]

            var enriched = transaction

            if (category != null &&
                (transaction.categoryName == null || transaction.categoryIcon == null)
            ) {
                enriched =
                    enriched.copy(
                        categoryName = category.name,
                        categoryIcon = category.icon,
                        categoryColor = category.color
                    )
            }

            if (account != null && transaction.accountName == null) {
                enriched = enriched.copy(accountName = account.name)
            }

            enriched
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
                        it.copy(
                            successMessage = "Transaction added!",
                            showAddDialog = false,
                            // Switch to the account used for this transaction if it exists
                            selectedAccount =
                                if (selectedAccount != null)
                                    selectedAccount.copy(
                                        balance =
                                            selectedAccount.balance +
                                                (if (type ==
                                                    TransactionType
                                                        .EXPENSE
                                                )
                                                    -amount
                                                else amount)
                                    )
                                else it.selectedAccount
                        )
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

    // Filter methods
    fun setFilterPeriod(period: FilterPeriod) {
        _filterPeriod.value = period
        _periodOffset.value = 0 // Reset to current period
    }

    fun setPeriodOffset(offset: Int) {
        _periodOffset.value = offset.coerceAtMost(0) // Can't go to future
    }

    /** Get transactions filtered by current filter period and offset */
    fun getFilteredTransactions(): List<Transaction> {
        val (startDate, endDate) = getDateRange(_filterPeriod.value, _periodOffset.value)
        return _uiState.value.transactions
            .filter { tx -> tx.date >= startDate && tx.date <= endDate }
            .sortedByDescending { it.date }
    }

    /** Get income/expense totals for the filtered period */
    fun getFilteredTotals(): Pair<Double, Double> {
        val filtered = getFilteredTransactions()
        val income = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return income to expense
    }

    /** Get category breakdown for pie chart */
    fun getCategoryBreakdown(): List<CategoryBreakdown> {
        val filtered = getFilteredTransactions().filter { it.type == TransactionType.EXPENSE }
        val grouped = filtered.groupBy { it.categoryId }

        return grouped
            .map { (categoryId, transactions) ->
                val category = _uiState.value.categories.find { it.id == categoryId }
                CategoryBreakdown(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "Other",
                    categoryIcon = category?.icon ?: "💵",
                    categoryColor = category?.color ?: "#808080",
                    total = transactions.sumOf { it.amount },
                    count = transactions.size
                )
            }
            .sortedByDescending { it.total }
    }

    /** Get bar chart data (income vs expense by time periods) */
    fun getBarChartData(): List<BarChartDataItem> {
        val period = _filterPeriod.value
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val items = mutableListOf<BarChartDataItem>()

        // For Month and Year, show 12 months. For Week, show 7 days
        val count =
            when (period) {
                FilterPeriod.WEEK -> 7 // Days of week
                FilterPeriod.MONTH -> 12 // Months of year (for selected year based on offset)
                FilterPeriod.YEAR -> 12 // Months of year
            }

        for (i in 0 until count) {
            val (rangeStart, rangeEnd) =
                when (period) {
                    FilterPeriod.WEEK -> {
                        val weekStart =
                            today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                                .plus(_periodOffset.value * 7, DateTimeUnit.DAY)
                        val dayStart = weekStart.plus(i, DateTimeUnit.DAY)
                        dayStart to dayStart
                    }
                    FilterPeriod.MONTH, FilterPeriod.YEAR -> {
                        // For both Month and Year filters, show monthly data
                        val targetYear = today.year + _periodOffset.value
                        val month = i + 1
                        val daysInMonth =
                            when (month) {
                                1, 3, 5, 7, 8, 10, 12 -> 31
                                4, 6, 9, 11 -> 30
                                2 ->
                                    if (targetYear % 4 == 0 &&
                                        (targetYear % 100 != 0 ||
                                            targetYear % 400 == 0)
                                    )
                                        29
                                    else 28
                                else -> 30
                            }
                        LocalDate(targetYear, month, 1) to
                            LocalDate(targetYear, month, daysInMonth)
                    }
                }

            val periodTransactions =
                _uiState.value.transactions.filter { tx ->
                    tx.date >= rangeStart && tx.date <= rangeEnd
                }

            val income =
                periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf {
                    it.amount
                }
            val expense =
                periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf {
                    it.amount
                }

            val label =
                when (period) {
                    FilterPeriod.WEEK -> rangeStart.dayOfWeek.name.take(3)
                    FilterPeriod.MONTH, FilterPeriod.YEAR -> Month(i + 1).name.take(3)
                }

            items.add(BarChartDataItem(label, income, expense))
        }

        return items
    }

    private fun getDateRange(period: FilterPeriod, offset: Int): Pair<LocalDate, LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return when (period) {
            FilterPeriod.WEEK -> {
                val weekStart =
                    today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        .plus(offset * 7, DateTimeUnit.DAY)
                val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                weekStart to weekEnd
            }
            FilterPeriod.MONTH -> {
                var targetYear = today.year
                var targetMonth = today.monthNumber + offset
                while (targetMonth < 1) {
                    targetMonth += 12
                    targetYear -= 1
                }
                while (targetMonth > 12) {
                    targetMonth -= 12
                    targetYear += 1
                }

                val monthStart = LocalDate(targetYear, targetMonth, 1)
                val daysInMonth =
                    when (targetMonth) {
                        1, 3, 5, 7, 8, 10, 12 -> 31
                        4, 6, 9, 11 -> 30
                        2 ->
                            if (targetYear % 4 == 0 &&
                                (targetYear % 100 != 0 || targetYear % 400 == 0)
                            )
                                29
                            else 28
                        else -> 30
                    }
                val monthEnd = LocalDate(targetYear, targetMonth, daysInMonth)
                monthStart to monthEnd
            }
            FilterPeriod.YEAR -> {
                val targetYear = today.year + offset
                LocalDate(targetYear, 1, 1) to LocalDate(targetYear, 12, 31)
            }
        }
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

/** Filter period options for statistics */
enum class FilterPeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

/** Category breakdown data for pie chart */
data class CategoryBreakdown(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val total: Double,
    val count: Int
)

/** Bar chart data item for income vs expense */
data class BarChartDataItem(val label: String, val income: Double, val expense: Double)
