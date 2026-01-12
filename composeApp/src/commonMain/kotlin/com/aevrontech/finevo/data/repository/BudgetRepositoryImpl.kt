package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.manager.NotificationManager
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** BudgetRepository implementation using SQLDelight for local storage. */
class BudgetRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val notificationManager: NotificationManager
) : BudgetRepository {

    private val defaultUserId = "local_user"

    override fun getAllBudgets(userId: String): Flow<List<Budget>> {
        return localDataSource.getBudgets().map { budgets ->
            enrichBudgetsWithCategoryData(budgets)
        }
    }

    override fun getActiveBudgets(userId: String): Flow<List<Budget>> {
        return localDataSource.getBudgets().map { budgets ->
            enrichBudgetsWithCategoryData(budgets.filter { it.isActive })
        }
    }

    override fun getBudgetFlow(id: String): Flow<Budget?> {
        return localDataSource.getBudgets().map { budgets ->
            budgets.find { it.id == id }?.let { enrichBudgetWithCategoryData(it) }
        }
    }

    override suspend fun getBudgetById(id: String): Budget? {
        return localDataSource.getBudgets().first().find { it.id == id }?.let {
            enrichBudgetWithCategoryData(it)
        }
    }

    override suspend fun getBudgetByCategory(userId: String, categoryId: String): Budget? {
        return localDataSource
            .getBudgets()
            .first()
            .find { it.categoryId == categoryId && it.isActive }
            ?.let { enrichBudgetWithCategoryData(it) }
    }

    override suspend fun addBudget(budget: Budget) {
        localDataSource.insertBudget(budget)
    }

    override suspend fun updateBudget(budget: Budget) {
        // Uses INSERT OR REPLACE
        localDataSource.insertBudget(budget)
    }

    override suspend fun updateBudgetSpent(budgetId: String, spent: Double) {
        val budget = getBudgetById(budgetId) ?: return
        val updated =
            budget.copy(
                spent = spent,
                updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
            )
        localDataSource.insertBudget(updated)
    }

    override suspend fun deleteBudget(id: String) {
        // Use the existing deleteBudget query
        // Note: Need to add this to LocalDataSource if not present
        val budgets = localDataSource.getBudgets().first()
        val budget = budgets.find { it.id == id } ?: return
        // Mark as inactive instead of deleting for data safety
        localDataSource.insertBudget(
            budget.copy(
                isActive = false,
                updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
            )
        )
    }

    override suspend fun recalculateAllBudgets(userId: String) {
        val budgets = localDataSource.getBudgets().first().filter { it.isActive }
        val today = getCurrentLocalDate()

        for (budget in budgets) {
            val (startDate, endDate) =
                getBudgetPeriodDates(budget.period, budget.startDate, budget.endDate, today)

            // Get transactions for this category in the budget period
            val transactions =
                localDataSource.getTransactionsByDateRange(startDate, endDate).first()
            val categorySpent =
                transactions
                    .filter { txn ->
                        // Check Category: Matches primary ID OR is in list of IDs
                        val categoryMatch =
                            if (budget.categoryIds.isNotEmpty()) {
                                txn.categoryId in budget.categoryIds
                            } else {
                                txn.categoryId == budget.categoryId
                            }

                        // Check Account: Matches list of IDs OR list is empty (all
                        // accounts)
                        val accountMatch =
                            budget.accountIds.isEmpty() ||
                                txn.accountId in budget.accountIds

                        categoryMatch && accountMatch && txn.type == TransactionType.EXPENSE
                    }
                    .sumOf { it.amount }

            // Update spent amount if changed
            if (categorySpent != budget.spent) {
                updateBudgetSpent(budget.id, categorySpent)
            }
        }
    }

    /** Calculate the date range for a budget period */
    override fun getBudgetPeriodDates(
        period: BudgetPeriod,
        startDate: LocalDate,
        endDate: LocalDate?,
        today: LocalDate,
        periodOffset: Int
    ): Pair<LocalDate, LocalDate> {
        return when (period) {
            BudgetPeriod.WEEKLY -> {
                // Get current week boundaries (Monday to Sunday)
                val currentDayOfWeek = today.dayOfWeek.ordinal // 0 = Monday
                val currentWeekStart = today.minus(DatePeriod(days = currentDayOfWeek))
                val currentWeekEnd = currentWeekStart.plus(DatePeriod(days = 6))

                // Apply offset (negative = past weeks)
                val targetWeekStart = currentWeekStart.plus(DatePeriod(days = 7 * periodOffset))
                val targetWeekEnd = currentWeekEnd.plus(DatePeriod(days = 7 * periodOffset))

                // For the first period (when budget was created), start from startDate
                targetWeekStart to targetWeekEnd
            }
            BudgetPeriod.MONTHLY -> {
                // Get current month boundaries (1st to last day)
                val currentMonthStart = LocalDate(today.year, today.month, 1)
                val daysInCurrentMonth = getDaysInMonth(today.monthNumber, today.year)
                val currentMonthEnd = LocalDate(today.year, today.month, daysInCurrentMonth)

                // Apply offset (negative = past months)
                val targetMonthStart = currentMonthStart.plus(DatePeriod(months = periodOffset))
                val daysInTargetMonth =
                    getDaysInMonth(targetMonthStart.monthNumber, targetMonthStart.year)
                val targetMonthEnd =
                    LocalDate(targetMonthStart.year, targetMonthStart.month, daysInTargetMonth)

                // For the first period (when budget was created), start from startDate
                targetMonthStart to targetMonthEnd
            }
            BudgetPeriod.YEARLY -> {
                // Get current year boundaries (Jan 1 to Dec 31)
                val currentYearStart = LocalDate(today.year, 1, 1)
                val currentYearEnd = LocalDate(today.year, 12, 31)

                // Apply offset (negative = past years)
                val targetYearStart = LocalDate(today.year + periodOffset, 1, 1)
                val targetYearEnd = LocalDate(today.year + periodOffset, 12, 31)

                // For the first period (when budget was created), start from startDate
                targetYearStart to targetYearEnd
            }
            BudgetPeriod.ONCE -> {
                // One-time budget: from start date to end date (or far future if not set)
                startDate to (endDate ?: LocalDate(2099, 12, 31))
            }
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 30
        }
    }

    /** Enrich budgets with category information */
    private suspend fun enrichBudgetsWithCategoryData(budgets: List<Budget>): List<Budget> {
        val categories = localDataSource.getCategories().first()
        val categoryMap = categories.associateBy { it.id }

        return budgets.map { budget ->
            val category = categoryMap[budget.categoryId]
            budget.copy(
                categoryName = category?.name,
                categoryIcon = category?.icon,
                categoryColor = category?.color
            )
        }
    }

    private suspend fun enrichBudgetWithCategoryData(budget: Budget): Budget {
        val category = localDataSource.getCategoryById(budget.categoryId)
        return budget.copy(
            categoryName = category?.name,
            categoryIcon = category?.icon,
            categoryColor = category?.color
        )
    }

    override suspend fun checkAndTriggerAlerts(transaction: Transaction) {
        if (transaction.type != TransactionType.EXPENSE) return

        val budgets =
            localDataSource.getBudgets().first().filter { budget ->
                val categoryMatch =
                    if (budget.categoryIds.isNotEmpty()) {
                        transaction.categoryId in budget.categoryIds
                    } else {
                        budget.categoryId == transaction.categoryId
                    }
                budget.isActive && categoryMatch
            }
        val today = transaction.date

        for (budget in budgets) {
            val (startDate, endDate) =
                getBudgetPeriodDates(budget.period, budget.startDate, budget.endDate, today)
            // Check if transaction falls within this budget period
            if (transaction.date >= startDate && (endDate == null || transaction.date <= endDate)) {
                // Calculate total spent including this transaction
                // We need to fetch fresh because recalculate might not have happened yet or we want
                // to be sure
                // However, recalculateAllBudgets updates the 'spent' field. If we assume that ran
                // or we run it now...
                // Let's rely on calculating it fresh for accuracy
                val transactions =
                    localDataSource.getTransactionsByDateRange(startDate, endDate).first()
                val totalSpent =
                    transactions
                        .filter { txn ->
                            // Check Category: Matches primary ID OR is in list of IDs
                            val categoryMatch =
                                if (budget.categoryIds.isNotEmpty()) {
                                    txn.categoryId in budget.categoryIds
                                } else {
                                    txn.categoryId == budget.categoryId
                                }

                            // Check Account: Matches list of IDs OR list is empty (all
                            // accounts)
                            val accountMatch =
                                budget.accountIds.isEmpty() ||
                                    txn.accountId in budget.accountIds

                            categoryMatch &&
                                accountMatch &&
                                txn.type == TransactionType.EXPENSE
                        }
                        .sumOf { it.amount }

                val percentage = (totalSpent / budget.amount) * 100

                if (percentage >= 100) {
                    notificationManager.showNotification(
                        id = budget.id.hashCode(),
                        title = "Budget Exceeded: ${budget.categoryName ?: "Uncategorized"}",
                        message =
                            "You have exceeded your ${budget.period.name.lowercase()} budget limit of ${budget.amount}."
                    )
                } else if (percentage >= 80) { // Warning threshold
                    notificationManager.showNotification(
                        id = budget.id.hashCode(),
                        title = "Budget Warning: ${budget.categoryName ?: "Uncategorized"}",
                        message = "You have used ${percentage.toInt()}% of your budget."
                    )
                }
            }
        }
    }
}
