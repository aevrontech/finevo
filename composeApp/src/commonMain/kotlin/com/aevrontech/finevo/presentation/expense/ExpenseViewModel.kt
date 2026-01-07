package com.aevrontech.finevo.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.domain.repository.AccountRepository
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import com.aevrontech.finevo.domain.repository.LabelRepository
import com.aevrontech.finevo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/** ViewModel for Expense Tracker feature. */
class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val accountRepository: AccountRepository,
    private val labelRepository: LabelRepository,
    private val settingsRepository: SettingsRepository
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
        observeCurrency()
    }

    private fun observeCurrency() {
        viewModelScope.launch {
            settingsRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(currencyCode = prefs.currency) }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getActiveAccounts("local_user").collect { accounts ->
                // Default to ALL accounts selected if none previously (or on first load)
                // If previously selected accounts exist, try to keep them.
                val previousSelectedIds = _uiState.value.selectedAccounts.map { it.id }.toSet()

                // New selection: Accounts that were previously selected AND still exist.
                // If the result is empty (e.g. first load, or all selected were deleted), select
                // ALL.
                val newSelected = accounts.filter { it.id in previousSelectedIds }.toSet()

                val finalSelected = if (newSelected.isEmpty()) accounts.toSet() else newSelected

                val totalBalance = accounts.sumOf { it.balance }
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccounts = finalSelected,
                        totalBalance = totalBalance
                    )
                }

                // Load transactions for selected accounts AND Dashboard (All accounts)
                // Filter by date range? "This Month" default?
                // loadTransactionsForAccounts will handle selected accounts.
                // We need separate load for Dashboard? Or shared?
                // User wants Dashboard filtered by TimeRange too.
                loadDataForTimeRange()
            }
        }
    }

    fun toggleAccountSelection(account: Account) {
        val currentSelected = _uiState.value.selectedAccounts.toMutableSet()
        if (currentSelected.contains(account)) {
            // Only remove if it's not the last one selected
            if (currentSelected.size > 1) {
                currentSelected.remove(account)
            }
        } else {
            currentSelected.add(account)
        }
        _uiState.update { it.copy(selectedAccounts = currentSelected) }
        loadTransactionsForAccounts(currentSelected.map { it.id }.toSet())
    }

    fun selectAllAccounts() {
        val allAccounts = _uiState.value.accounts.toSet()
        _uiState.update { it.copy(selectedAccounts = allAccounts) }
        loadTransactionsForAccounts(allAccounts.map { it.id }.toSet())
    }

    private fun loadTransactionsForAccounts(accountIds: Set<String>) {
        viewModelScope.launch {
            // If no accounts selected (shouldn't happen with our logic, but safe guard), load all
            // or none?
            // "Multi-select" usually implies OR. Logic: Get transactions where accountId IN
            // accountIds.
            // Since we don't have a direct "getAllTransactionsForAccountList" in Repo (assuming),
            // and `getTransactionsByAccount` is for ONE account.
            // We might need to load ALL transactions for the period first, then filter in memory?
            // OR iterate and fetch.
            // Current `loadTransactionsForAccount` loads for one.
            // Given local database constraints, "getTransactions(startDate, endDate)" likely gets
            // ALL.
            // Then we filter.

            expenseRepository.getTransactions(monthStart, monthEnd).collect { transactions ->
                val enrichedTransactions = enrichWithCategoryData(transactions)
                val filtered =
                    enrichedTransactions.filter {
                        it.accountId in accountIds && it.accountId != null
                    }

                val income =
                    filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense =
                    filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        transactions =
                            filtered, // Store ONLY filtered transactions for the view?
                        // Or should we store ALL and filter in UI?
                        // The original logic replaced `transactions` with the account-specific
                        // list.
                        // So let's stick to that pattern: `transactions` holds what is shown.
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
        date: LocalDate,
        time: String? = null,
        location: String? = null,
        locationLat: Double? = null,
        locationLng: Double? = null,
        labels: List<String> = emptyList(),
        photoPath: String? = null
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
                    time = time,
                    location = location,
                    locationLat = locationLat,
                    locationLng = locationLng,
                    labels = labels,
                    photoPath = photoPath,
                    createdAt = now,
                    updatedAt = now
                )

            when (val result = expenseRepository.addTransaction(transaction)) {
                is Result.Success<*> -> {
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
                            isLoading = true
                        )
                    }
                    // selectedAccount balance update was manual in original?
                    // Original: selectedAccount = selectedAccount.copy(...)
                    // But `loadAccounts` observes Realm/DB, so it should auto-update.
                    // Let's remove manual state mutation for simplicity and rely on data source.

                    // Set labels for transaction
                    if (labels.isNotEmpty()) {
                        labelRepository.setLabelsForTransaction(transaction.id, labels)
                    }

                    // Reload transactions for selected accounts
                    loadTransactionsForAccounts(
                        _uiState.value.selectedAccounts.map { it.id }.toSet()
                    )
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
                is Result.Success<*> -> {
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
                                transaction.accountId,
                                account.balance + balanceChange
                            )
                        }
                    }
                    _uiState.update { it.copy(successMessage = "Transaction deleted") }
                    // Reload transactions for selected accounts
                    loadTransactionsForAccounts(
                        _uiState.value.selectedAccounts.map { it.id }.toSet()
                    )
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
        date: LocalDate,
        time: String? = null,
        location: String? = null,
        locationLat: Double? = null,
        locationLng: Double? = null,
        labels: List<String> = emptyList(),
        photoPath: String? = null
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
                    time = time,
                    location = location,
                    locationLat = locationLat,
                    locationLng = locationLng,
                    labels = labels,
                    photoPath = photoPath,
                    createdAt = oldTransaction?.createdAt ?: now,
                    updatedAt = now
                )

            when (val result = expenseRepository.updateTransaction(transaction)) {
                is Result.Success<*> -> {
                    // Update account balance: reverse old effect, apply new effect
                    var netChange = 0.0
                    val currentAccount = _uiState.value.accounts.find { it.id == accountId }

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
                        netChange = oldEffect + newEffect

                        // Only update if there's actually a change
                        if (netChange != 0.0) {
                            accountRepository.updateAccountBalance(
                                accountId,
                                selectedAccount.balance + netChange
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(successMessage = "Transaction updated!", isLoading = true)
                    }

                    // Set labels for transaction
                    labelRepository.setLabelsForTransaction(transaction.id, labels)

                    // Reload transactions
                    loadTransactionsForAccounts(
                        _uiState.value.selectedAccounts.map { it.id }.toSet()
                    )
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

    /** Get income category breakdown for pie chart */
    fun getIncomeCategoryBreakdown(): List<CategoryBreakdown> {
        val filtered = getFilteredTransactions().filter { it.type == TransactionType.INCOME }
        val grouped = filtered.groupBy { it.categoryId }

        return grouped
            .map { (categoryId, transactions) ->
                val category = _uiState.value.categories.find { it.id == categoryId }
                CategoryBreakdown(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "Other",
                    categoryIcon = category?.icon ?: "💰",
                    categoryColor = category?.color ?: "#4CAF50",
                    total = transactions.sumOf { it.amount },
                    count = transactions.size
                )
            }
            .sortedByDescending { it.total }
    }

    fun getBarChartData(): List<BarChartDataItem> {
        val period = _filterPeriod.value
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val items = mutableListOf<BarChartDataItem>()

        // For Month and Year, show 12 months. For Week, show 7 days. For Day, show 24 hours (or
        // segments)
        val count =
            when (period) {
                FilterPeriod.DAY -> 24 // Hours
                FilterPeriod.WEEK -> 7 // Days of week
                FilterPeriod.MONTH -> 12 // Months of year (for selected year based on offset)
                FilterPeriod.YEAR -> 12 // Months of year
            }

        for (i in 0 until count) {
            val (rangeStart, rangeEnd) =
                when (period) {
                    FilterPeriod.DAY -> {
                        val startOfDay = today.plus(_periodOffset.value, DateTimeUnit.DAY)
                        // Approximate hourly breakdown unavailable without DateTime
                        // implementation that supports hours?
                        // Transaction 'time' is String "HH:mm". Transaction 'date' is
                        // LocalDate.
                        // We can't filter by Hour easily using LocalDate.
                        // We need to parse 'time'.
                        // Simplified strategy: Show total for the day in one bar? Or skipping
                        // Bar Chart for DAY?
                        // Or implementing partial logic.
                        // Current 'BarChartDataItem' is label/income/expense.
                        // Let's rely on transaction time string "HH:mm".
                        startOfDay to startOfDay
                    }
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
                _uiState.value.transactions
                    .filter { tx -> tx.date >= rangeStart && tx.date <= rangeEnd }
                    .filter { tx ->
                        if (period == FilterPeriod.DAY) {
                            // Filter by Hour 'i'
                            val hour =
                                tx.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
                            hour == i
                        } else true
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
                    FilterPeriod.DAY -> if (i % 6 == 0) "${i}h" else "" // Sparse labels
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
            FilterPeriod.DAY -> {
                val targetDate = today.plus(offset, DateTimeUnit.DAY)
                targetDate to targetDate
            }
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

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(timeRange = range) }

        // Auto-switch chart period for better UX
        val newPeriod =
            when (range) {
                is CalendarTimeRange -> range.period
                is LastDaysRange ->
                    if (range.days <= 7) FilterPeriod.WEEK else FilterPeriod.MONTH
                else -> null
            }
        if (newPeriod != null) {
            _filterPeriod.value = newPeriod
            // Sync period offset
            if (range is CalendarTimeRange) {
                _periodOffset.value = range.offset
            } else {
                _periodOffset.value = 0
            }
        }

        loadDataForTimeRange()
    }

    private fun loadDataForTimeRange() {
        val range = _uiState.value.timeRange
        val (startDate, endDate) = getTimeRangeDates(range)
        val selectedIds = _uiState.value.selectedAccounts.map { it.id }.toSet()

        viewModelScope.launch {
            expenseRepository.getTransactions(startDate, endDate).collect { transactions ->
                val enriched = enrichWithCategoryData(transactions)

                // Dashboard: All Accounts, Filtered by Time
                val dashboardIncome =
                    enriched.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val dashboardExpense =
                    enriched.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                // Wallet: Selected Accounts, Filtered by Time
                val walletTransactions =
                    enriched.filter { it.accountId in selectedIds || it.accountId == null }
                val walletIncome =
                    walletTransactions.filter { it.type == TransactionType.INCOME }.sumOf {
                        it.amount
                    }
                val walletExpense =
                    walletTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf {
                        it.amount
                    }

                _uiState.update {
                    it.copy(
                        transactions = walletTransactions,
                        dashboardIncome = dashboardIncome,
                        dashboardExpense = dashboardExpense,
                        accountIncome = walletIncome,
                        accountExpense = walletExpense,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getTimeRangeDates(range: TimeRange): Pair<LocalDate, LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return when (range) {
            is LastDaysRange -> {
                today.minus(range.days - 1, DateTimeUnit.DAY) to today
            }
            is CalendarTimeRange -> {
                calculateDateRangeForPeriod(range.period, range.offset, today)
            }
            is CustomTimeRange -> range.start to range.end
            is AllTimeRange -> LocalDate(2000, 1, 1) to LocalDate(2100, 12, 31)
        }
    }

    private fun calculateDateRangeForPeriod(
        period: FilterPeriod,
        offset: Int,
        today: LocalDate
    ): Pair<LocalDate, LocalDate> {
        return when (period) {
            FilterPeriod.DAY -> {
                val targetDate = today.plus(offset, DateTimeUnit.DAY)
                targetDate to targetDate
            }
            FilterPeriod.WEEK -> {
                // Assuming Week starts on Monday
                val currentWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                val targetWeekStart = currentWeekStart.plus(offset * 7, DateTimeUnit.DAY)
                targetWeekStart to targetWeekStart.plus(6, DateTimeUnit.DAY)
            }
            FilterPeriod.MONTH -> {
                val totalMonths = today.year * 12 + (today.monthNumber - 1) + offset
                val year = totalMonths / 12
                val month = (totalMonths % 12) + 1
                val start = LocalDate(year, month, 1)
                val lengthOfMonth =
                    start.month.length(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
                start to LocalDate(year, month, lengthOfMonth)
            }
            FilterPeriod.YEAR -> {
                val targetYear = today.year + offset
                LocalDate(targetYear, 1, 1) to LocalDate(targetYear, 12, 31)
            }
        }
    }

    fun navigateTimeRange(direction: Int) {
        val currentRange = _uiState.value.timeRange
        if (currentRange is CalendarTimeRange) {
            val newRange = currentRange.copy(offset = currentRange.offset + direction)
            setTimeRange(newRange)
        }
    }
}

/** UI state for Expense screen. */
data class ExpenseUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val selectedAccounts: Set<Account> = emptySet(),
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val summary: TransactionSummary? = null,
    val accountIncome: Double = 0.0,
    val accountExpense: Double = 0.0,
    val dashboardIncome: Double = 0.0,
    val dashboardExpense: Double = 0.0,
    val totalBalance: Double = 0.0,
    val timeRange: TimeRange = TimeRange.ThisMonth,
    val currencyCode: String = "MYR",
    val showAddDialog: Boolean = false,
    val showAddAccountDialog: Boolean = false,
    val selectedTransactionType: TransactionType = TransactionType.EXPENSE,
    val error: String? = null,
    val successMessage: String? = null
) {
    val currencySymbol: String
        get() = CurrencyProvider.getCurrency(currencyCode)?.symbol ?: currencyCode

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
        get() = selectedAccounts.sumOf { it.balance }

    val accountBalancePercentage: Double
        get() {
            val balance = accountBalance
            return if (balance > 0) {
                ((balance - accountExpense) / balance * 100).coerceIn(0.0, 100.0)
            } else 0.0
        }
}

// Definitions moved to TimeRange.kt

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
