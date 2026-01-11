package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/** BudgetRepository implementation using SQLDelight for local storage. */
class BudgetRepositoryImpl(private val localDataSource: LocalDataSource) : BudgetRepository {

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
        val updated = budget.copy(spent = spent, updatedAt = Clock.System.now())
        localDataSource.insertBudget(updated)
    }

    override suspend fun deleteBudget(id: String) {
        // Use the existing deleteBudget query
        // Note: Need to add this to LocalDataSource if not present
        val budgets = localDataSource.getBudgets().first()
        val budget = budgets.find { it.id == id } ?: return
        // Mark as inactive instead of deleting for data safety
        localDataSource.insertBudget(budget.copy(isActive = false, updatedAt = Clock.System.now()))
    }

    override suspend fun recalculateAllBudgets(userId: String) {
        val budgets = localDataSource.getBudgets().first().filter { it.isActive }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        for (budget in budgets) {
            val (startDate, endDate) =
                getBudgetPeriodDates(budget.period, budget.startDate, budget.endDate, today)

            // Get transactions for this category in the budget period
            val transactions =
                localDataSource.getTransactionsByDateRange(startDate, endDate).first()
            val categorySpent =
                transactions
                    .filter {
                        it.categoryId == budget.categoryId &&
                            it.type == TransactionType.EXPENSE
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
}
