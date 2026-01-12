package com.aevrontech.finevo.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import com.aevrontech.finevo.domain.model.BudgetStatus
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.domain.repository.AccountRepository
import com.aevrontech.finevo.domain.repository.BudgetRepository
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import com.aevrontech.finevo.presentation.expense.BarChartItem
import com.aevrontech.finevo.presentation.expense.CategoryBreakdown
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** ViewModel for Budget feature */
class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val defaultUserId = "local_user"
    private var selectedBudgetJob: Job? = null

    init {
        loadBudgets()
        loadCategories()
        loadAccounts()
    }

    private val _periodFilter = MutableStateFlow<BudgetPeriod?>(BudgetPeriod.MONTHLY)
    private var budgetRefreshJob: Job? = null

    private fun loadBudgets() {
        budgetRefreshJob?.cancel()
        budgetRefreshJob =
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                // First recalculate spent amounts
                budgetRepository.recalculateAllBudgets(defaultUserId)

                // Then load budgets with filter
                kotlinx.coroutines.flow
                    .combine(
                        budgetRepository.getActiveBudgets(defaultUserId),
                        _periodFilter
                    ) { budgets, filter ->
                        val filtered =
                            if (filter == null) {
                                budgets
                            } else {
                                budgets.filter {
                                    it.period == filter
                                }
                            }
                        Pair(filtered, filter)
                    }
                    .collect { (budgets, filter) ->
                        val today = getCurrentLocalDate()

                        // Calculate transaction counts for each budget for
                        // the CURRENT
                        // period
                        val enrichedBudgets =
                            budgets.map { budget ->
                                val (start, end) =
                                    budgetRepository
                                        .getBudgetPeriodDates(
                                            budget.period,
                                            budget.startDate,
                                            budget.endDate,
                                            today,
                                            0 // Always
                                            // show
                                            // current
                                            // period
                                            // status
                                            // in list
                                        )

                                // We need to get transactions to
                                // count them
                                // Note: This could be optimized by
                                // batching or a
                                // specific count
                                // query
                                val transactions =
                                    expenseRepository
                                        .getTransactions(
                                            start,
                                            end
                                        )
                                        .first()

                                val count =
                                    transactions.count { txn ->
                                        val categoryMatch =
                                            if (budget.categoryIds
                                                    .isNotEmpty()
                                            ) {
                                                txn.categoryId in
                                                    budget.categoryIds
                                            } else {
                                                txn.categoryId ==
                                                    budget.categoryId
                                            }

                                        val accountMatch =
                                            budget.accountIds
                                                .isEmpty() ||
                                                txn.accountId in
                                                budget.accountIds

                                        categoryMatch &&
                                            accountMatch &&
                                            txn.type ==
                                            TransactionType
                                                .EXPENSE
                                    }

                                budget.copy(
                                    transactionCount = count
                                )
                            }

                        val totalBudget =
                            enrichedBudgets.sumOf { it.amount }
                        val totalSpent = enrichedBudgets.sumOf { it.spent }
                        val onTrackCount =
                            enrichedBudgets.count {
                                it.status == BudgetStatus.ON_TRACK
                            }
                        val warningCount =
                            enrichedBudgets.count {
                                it.status == BudgetStatus.WARNING
                            }
                        val overCount =
                            enrichedBudgets.count {
                                it.status == BudgetStatus.OVER
                            }

                        _uiState.update { state ->
                            state.copy(
                                budgets = enrichedBudgets,
                                totalBudget = totalBudget,
                                totalSpent = totalSpent,
                                onTrackCount = onTrackCount,
                                warningCount = warningCount,
                                overCount = overCount,
                                isLoading = false,
                                periodFilter = filter
                            )
                        }
                    }
            }
    }

    fun setPeriodFilter(period: BudgetPeriod?) {
        _periodFilter.value = period
    }

    fun refresh() {
        loadBudgets()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            expenseRepository.getCategories().collect { categories ->
                // Filter to only expense categories
                val expenseCategories =
                    categories.filter {
                        it.type ==
                            com.aevrontech.finevo.domain.model
                                .TransactionType.EXPENSE
                    }
                _uiState.update {
                    it.copy(
                        categories = expenseCategories,
                        availableCategories = expenseCategories
                    )
                }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAccounts(defaultUserId).collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingBudget = null) }
    }

    fun showEditDialog(budget: Budget) {
        _uiState.update { it.copy(showAddDialog = true, editingBudget = budget) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingBudget = null) }
    }

    /** Add new budget with enhanced parameters */
    fun addBudget(
        name: String,
        categoryIds: List<String>,
        accountIds: List<String>,
        amount: Double,
        currency: String,
        period: BudgetPeriod,
        startDate: LocalDate,
        endDate: LocalDate?,
        notifyOverspent: Boolean,
        alertThreshold: Int,
        notifyRisk: Boolean
    ) {
        viewModelScope.launch {
            val now = Instant.fromEpochMilliseconds(getCurrentTimeMillis())

            // Use first category as primary (for backwards compatibility)
            val primaryCategoryId = categoryIds.firstOrNull() ?: return@launch

            val budget =
                Budget(
                    id =
                        now.toEpochMilliseconds().toString() +
                            (1000..9999).random(),
                    userId = defaultUserId,
                    name = name.ifBlank { null },
                    categoryId = primaryCategoryId,
                    categoryIds = categoryIds,
                    accountIds = accountIds,
                    amount = amount,
                    spent = 0.0,
                    period = period,
                    startDate = startDate,
                    endDate = endDate,
                    alertThreshold = alertThreshold,
                    rollover = false,
                    notifyOverspent = notifyOverspent,
                    notifyRisk = notifyRisk,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )

            budgetRepository.addBudget(budget)
            _uiState.update {
                it.copy(showAddDialog = false, successMessage = "Budget created!")
            }
            loadBudgets()
            loadCategories()
        }
    }

    /** Legacy addBudget for backwards compatibility */
    fun addBudget(
        categoryId: String,
        amount: Double,
        period: BudgetPeriod,
        alertThreshold: Int = 80,
        rollover: Boolean = false
    ) {
        val today = getCurrentLocalDate()
        addBudget(
            name = "",
            categoryIds = listOf(categoryId),
            accountIds = emptyList(),
            amount = amount,
            currency = "MYR",
            period = period,
            startDate = today,
            endDate = null,
            notifyOverspent = true,
            alertThreshold = alertThreshold,
            notifyRisk = true
        )
    }

    fun updateBudget(
        budget: Budget,
        name: String,
        categoryIds: List<String>,
        accountIds: List<String>,
        amount: Double,
        period: BudgetPeriod,
        startDate: LocalDate,
        endDate: LocalDate?,
        notifyOverspent: Boolean,
        alertThreshold: Int,
        notifyRisk: Boolean
    ) {
        viewModelScope.launch {
            val updated =
                budget.copy(
                    name = name.ifBlank { null },
                    categoryId = categoryIds.firstOrNull() ?: budget.categoryId,
                    categoryIds = categoryIds,
                    accountIds = accountIds,
                    amount = amount,
                    period = period,
                    startDate = startDate,
                    endDate = endDate,
                    alertThreshold = alertThreshold,
                    notifyOverspent = notifyOverspent,
                    notifyRisk = notifyRisk,
                    updatedAt =
                        Instant.fromEpochMilliseconds(
                            getCurrentTimeMillis()
                        )
                )
            budgetRepository.updateBudget(updated)
            _uiState.update {
                it.copy(showAddDialog = false, successMessage = "Budget updated!")
            }
            loadBudgets()
        }
    }

    /** Legacy updateBudget for backwards compatibility */
    fun updateBudget(
        budget: Budget,
        amount: Double,
        period: BudgetPeriod,
        alertThreshold: Int
    ) {
        updateBudget(
            budget = budget,
            name = budget.name ?: "",
            categoryIds = budget.categoryIds.ifEmpty { listOf(budget.categoryId) },
            accountIds = budget.accountIds,
            amount = amount,
            period = period,
            startDate = budget.startDate,
            endDate = budget.endDate,
            notifyOverspent = budget.notifyOverspent,
            alertThreshold = alertThreshold,
            notifyRisk = budget.notifyRisk
        )
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget.id)
            _uiState.update { it.copy(successMessage = "Budget deleted") }
            loadBudgets()
            loadCategories()
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun selectBudget(budget: Budget) {
        // Reset period offset when selecting a new budget
        _uiState.update { it.copy(periodOffset = 0) }
        loadBudgetData(budget.id, 0)
    }

    /** Navigate to a different period (offset: 0 = current, -1 = previous, etc.) */
    fun navigatePeriod(offset: Int) {
        val selectedBudget = _uiState.value.selectedBudget ?: return
        val newOffset = _uiState.value.periodOffset + offset

        // Don't allow navigating before budget creation
        val today = getCurrentLocalDate()
        val testDates =
            budgetRepository.getBudgetPeriodDates(
                selectedBudget.period,
                selectedBudget.startDate,
                selectedBudget.endDate,
                today,
                newOffset
            )

        _uiState.update { it.copy(periodOffset = newOffset) }
        loadBudgetData(selectedBudget.id, newOffset)
    }

    private fun canNavigateEarlier(budget: Budget, currentOffset: Int): Boolean {
        return budget.period != BudgetPeriod.ONCE
    }

    private fun loadBudgetData(budgetId: String, periodOffset: Int) {
        selectedBudgetJob?.cancel()
        selectedBudgetJob =
            viewModelScope.launch {
                budgetRepository
                    .getBudgetFlow(budgetId)
                    .flatMapLatest { updatedBudget ->
                        if (updatedBudget == null) {
                            flowOf(null)
                        } else {
                            val today = getCurrentLocalDate()
                            val dates =
                                budgetRepository
                                    .getBudgetPeriodDates(
                                        updatedBudget
                                            .period,
                                        updatedBudget
                                            .startDate,
                                        updatedBudget
                                            .endDate,
                                        today,
                                        periodOffset
                                    )

                            expenseRepository.getTransactions(
                                dates.first,
                                dates.second
                            )
                                .map { transactions ->
                                    Triple(
                                        updatedBudget,
                                        dates,
                                        transactions
                                    )
                                }
                        }
                    }
                    .collect { result ->
                        if (result == null) {
                            clearSelectedBudget()
                            return@collect
                        }

                        val (updatedBudget, dates, transactions) = result

                        // Generate period label
                        val periodLabel =
                            generatePeriodLabel(
                                updatedBudget.period,
                                dates.first,
                                dates.second
                            )

                        // Determine navigation capabilities
                        val canNavigateNext = true
                        val canNavigatePrev =
                            canNavigateEarlier(
                                updatedBudget,
                                periodOffset
                            )

                        // Filter transactions relevant to this budget
                        val uniqueCategoryIds =
                            if (updatedBudget.categoryIds.isNotEmpty())
                                updatedBudget.categoryIds.toSet()
                            else
                                listOf(updatedBudget.categoryId)
                                    .toSet()
                        val uniqueAccountIds =
                            updatedBudget.accountIds.toSet()

                        val filtered =
                            transactions
                                .filter { txn ->
                                    (uniqueCategoryIds
                                        .isEmpty() ||
                                        txn.categoryId in
                                        uniqueCategoryIds) &&
                                        (uniqueAccountIds
                                            .isEmpty() ||
                                            txn.accountId in
                                            uniqueAccountIds) &&
                                        txn.type ==
                                        TransactionType
                                            .EXPENSE
                                }
                                .sortedByDescending { it.createdAt }

                        // Compute charts
                        val dailySpending =
                            computeDailySpending(
                                filtered,
                                updatedBudget.period
                            )
                        val breakdown = computeCategoryBreakdown(filtered)
                        val trendData =
                            computeTrend(
                                filtered,
                                updatedBudget.amount,
                                dates.first,
                                dates.second,
                                updatedBudget.period
                            )

                        val currentPeriodSpent =
                            filtered.sumOf { it.amount }
                        val budgetForDisplay =
                            updatedBudget.copy(
                                spent = currentPeriodSpent,
                                transactionCount = filtered.size
                            )

                        _uiState.update {
                            it.copy(
                                selectedBudget = budgetForDisplay,
                                budgetTransactions = filtered,
                                budgetDailySpending = dailySpending,
                                budgetCategoryBreakdown = breakdown,
                                budgetTrend = trendData,
                                currentPeriodLabel = periodLabel,
                                canNavigateNext = canNavigateNext,
                                canNavigatePrevious =
                                    canNavigatePrev,
                                isDetailsLoading = false
                            )
                        }
                    }
            }
    }

    private fun generatePeriodLabel(
        period: BudgetPeriod,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        return when (period) {
            BudgetPeriod.WEEKLY -> {
                val today = getCurrentLocalDate()
                if (endDate == today) {
                    val monthNames =
                        listOf(
                            "Jan",
                            "Feb",
                            "Mar",
                            "Apr",
                            "May",
                            "Jun",
                            "Jul",
                            "Aug",
                            "Sep",
                            "Oct",
                            "Nov",
                            "Dec"
                        )
                    "${monthNames[startDate.monthNumber - 1]} ${startDate.dayOfMonth}, Today"
                } else {
                    val monthNames =
                        listOf(
                            "Jan",
                            "Feb",
                            "Mar",
                            "Apr",
                            "May",
                            "Jun",
                            "Jul",
                            "Aug",
                            "Sep",
                            "Oct",
                            "Nov",
                            "Dec"
                        )
                    val startYear =
                        if (startDate.year != today.year ||
                            startDate.year != endDate.year
                        )
                            ", ${startDate.year}"
                        else ""
                    val endYear =
                        if (endDate.year != today.year ||
                            startDate.year != endDate.year
                        )
                            ", ${endDate.year}"
                        else ""

                    val startStr =
                        "${monthNames[startDate.monthNumber - 1]} ${startDate.dayOfMonth}$startYear"
                    val endStr =
                        "${monthNames[endDate.monthNumber - 1]} ${endDate.dayOfMonth}$endYear"
                    "$startStr - $endStr"
                }
            }
            BudgetPeriod.MONTHLY -> {
                val monthNames =
                    listOf(
                        "Jan",
                        "Feb",
                        "Mar",
                        "Apr",
                        "May",
                        "Jun",
                        "Jul",
                        "Aug",
                        "Sep",
                        "Oct",
                        "Nov",
                        "Dec"
                    )
                "${monthNames[startDate.monthNumber - 1]} ${startDate.year}"
            }
            BudgetPeriod.YEARLY -> {
                "${startDate.year}"
            }
            BudgetPeriod.ONCE -> {
                val startStr =
                    "${startDate.dayOfMonth}/${startDate.monthNumber}/${startDate.year}"
                val endStr =
                    "${endDate.dayOfMonth}/${endDate.monthNumber}/${endDate.year}"
                "$startStr - $endStr"
            }
        }
    }

    fun clearSelectedBudget() {
        selectedBudgetJob?.cancel()
        _uiState.update {
            it.copy(
                selectedBudget = null,
                budgetTransactions = emptyList(),
                budgetDailySpending = emptyList(),
                budgetCategoryBreakdown = emptyList(),
                periodOffset = 0,
                currentPeriodLabel = ""
            )
        }
    }

    private fun computeDailySpending(
        transactions: List<Transaction>,
        period: BudgetPeriod
    ): List<BarChartItem> {
        // Group by day of month for Monthly, Day of Year for others for simplicity in daily
        // view
        // Or if Weekly, day of week.
        // Let's stick to simple grouping by day number for now like ExpenseReportScreen
        val grouped =
            transactions.groupBy {
                it.date.dayOfMonth // Simple day of month grouping
            }

        // Ideally we want to fill gaps.
        // For simplicity, just return present days sorted.
        return grouped
            .map { (day, txns) ->
                BarChartItem(
                    label = day.toString(),
                    value = txns.sumOf { it.amount }
                )
            }
            .sortedBy { it.label.toIntOrNull() ?: 0 }
    }

    private fun computeCategoryBreakdown(
        transactions: List<Transaction>
    ): List<CategoryBreakdown> {
        val categories = _uiState.value.availableCategories
        return transactions
            .groupBy { it.categoryId }
            .map { (categoryId, txns) ->
                val category = categories.find { it.id == categoryId }
                CategoryBreakdown(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "Other",
                    categoryIcon = category?.icon ?: "💰",
                    categoryColor = category?.color ?: "#808080",
                    total = txns.sumOf { it.amount },
                    count = txns.size
                )
            }
            .sortedByDescending { it.total }
    }

    private fun computeTrend(
        transactions: List<Transaction>,
        budgetAmount: Double,
        startDate: LocalDate,
        endDate: LocalDate,
        period: BudgetPeriod
    ): BudgetTrendData {
        val today = getCurrentLocalDate()

        // Calculate visual range
        val (visibleStart, visibleEnd) =
            if (period == BudgetPeriod.WEEKLY) {
                val monday =
                    startDate.minus(
                        DatePeriod(days = startDate.dayOfWeek.ordinal)
                    )
                val sunday = monday.plus(DatePeriod(days = 6))
                monday to sunday
            } else {
                startDate to endDate
            }

        // 1. Calculate Daily Average (Spent so far / Days passed)
        val daysPassed = startDate.daysUntil(today).coerceAtLeast(0) + 1
        val currentTotalSpent = transactions.sumOf { it.amount }
        val dailyAverage = if (daysPassed > 0) currentTotalSpent / daysPassed else 0.0

        // 2. Calculate Daily Recommended (Total Budget / Total Days in Period)
        // This gives a consistent daily target based on the full period
        val totalDaysInPeriod = (startDate.daysUntil(endDate) + 1).coerceAtLeast(1)
        val dailyRecommended = budgetAmount / totalDaysInPeriod

        // 3. Generate Cumulative Trend Points (Start -> Today)
        val trendPoints = mutableListOf<Pair<LocalDate, Double>>()
        var cumulativeSpent = 0.0

        // Map transactions by date
        val txnsByDate = transactions.groupBy { it.date }

        val daysToProcess = if (today >= startDate) startDate.daysUntil(today) else -1
        if (daysToProcess >= 0) {
            for (i in 0..daysToProcess) {
                val date = startDate.plus(DatePeriod(days = i))
                if (date > endDate) break

                val dailySum = txnsByDate[date]?.sumOf { it.amount } ?: 0.0
                cumulativeSpent += dailySum
                trendPoints.add(date to cumulativeSpent)
            }
        }

        // 4. Generate Forecast Points (Today -> End or Start -> End for future)
        val forecastPoints = mutableListOf<Pair<LocalDate, Double>>()

        // Determine start of forecast
        val forecastStartDate = if (today < startDate) startDate else today
        var forecastCumulative = if (today < startDate) 0.0 else cumulativeSpent

        if (forecastStartDate <= endDate) {
            forecastPoints.add(forecastStartDate to forecastCumulative)

            val daysToForecast = forecastStartDate.daysUntil(endDate)
            for (i in 1..daysToForecast) {
                val date = forecastStartDate.plus(DatePeriod(days = i))
                forecastCumulative += dailyRecommended
                forecastPoints.add(date to forecastCumulative)
            }
        }

        return BudgetTrendData(
            dailyAverage = dailyAverage,
            dailyRecommended = dailyRecommended,
            trendPoints = trendPoints,
            forecastPoints = forecastPoints,
            startDate = startDate,
            endDate = endDate,
            budgetLimit = budgetAmount,
            visibleStartDate = visibleStart,
            visibleEndDate = visibleEnd
        )
    }
}

/** UI State for Budget screen */
data class BudgetUiState(
    val isLoading: Boolean = true,
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val onTrackCount: Int = 0,
    val warningCount: Int = 0,
    val overCount: Int = 0,
    val showAddDialog: Boolean = false,
    val editingBudget: Budget? = null,
    val selectedBudget: Budget? = null,
    val budgetTransactions: List<Transaction> = emptyList(),
    val budgetDailySpending: List<BarChartItem> = emptyList(),
    val budgetCategoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val budgetTrend: BudgetTrendData? = null,
    val isDetailsLoading: Boolean = false,
    val periodFilter: BudgetPeriod? = null,
    val periodOffset: Int = 0, // 0 = current period, -1 = previous, etc.
    val currentPeriodLabel: String = "",
    val canNavigateNext: Boolean = false,
    val canNavigatePrevious: Boolean = true,
    val successMessage: String? = null,
    val error: String? = null
) {
    val budgetCount: Int
        get() = budgets.size
    val totalRemaining: Double
        get() = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val totalPercentUsed: Double
        get() = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0
}

data class BudgetTrendData(
    val dailyAverage: Double,
    val dailyRecommended: Double,
    val trendPoints: List<Pair<LocalDate, Double>>,
    val forecastPoints: List<Pair<LocalDate, Double>>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val budgetLimit: Double,
    val visibleStartDate: LocalDate = startDate,
    val visibleEndDate: LocalDate = endDate
)
