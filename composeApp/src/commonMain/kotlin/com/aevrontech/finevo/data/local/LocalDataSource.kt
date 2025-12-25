package com.aevrontech.finevo.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.aevrontech.finevo.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

/**
 * Local data source wrapping SQLDelight database queries. Provides Flow-based reactive data access.
 */
class LocalDataSource(private val database: FinEvoDatabase) {
    private val queries
        get() = database.finEvoDatabaseQueries

    // Default user ID for local storage (single user mode)
    private val defaultUserId = "local_user"

    // ============================================
    // CATEGORIES
    // ============================================

    fun getCategories(): Flow<List<Category>> {
        return queries.selectAllCategories(defaultUserId).asFlow().mapToList(Dispatchers.IO).map {
                list ->
            list.map { it.toDomainCategory() }
        }
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return queries.selectCategoriesByType(defaultUserId, type.name)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainCategory() } }
    }

    suspend fun insertCategory(category: Category) =
            withContext(Dispatchers.IO) {
                queries.insertCategory(
                        id = category.id,
                        user_id = category.userId,
                        name = category.name,
                        icon = category.icon,
                        color = category.color,
                        type = category.type.name,
                        is_default = if (category.isDefault) 1L else 0L,
                        sort_order = category.order.toLong(),
                        created_at = Clock.System.now().toEpochMilliseconds()
                )
            }

    suspend fun insertDefaultCategories() =
            withContext(Dispatchers.IO) {
                val now = Clock.System.now().toEpochMilliseconds()
                val expenseCategories =
                        listOf(
                                Triple("cat_food", "Food & Dining", "🍔" to "#FF5252"),
                                Triple("cat_transport", "Transportation", "🚗" to "#FF9800"),
                                Triple("cat_shopping", "Shopping", "🛍️" to "#E91E63"),
                                Triple("cat_bills", "Bills & Utilities", "📄" to "#9C27B0"),
                                Triple("cat_entertainment", "Entertainment", "🎬" to "#673AB7"),
                                Triple("cat_health", "Health", "💊" to "#4CAF50"),
                                Triple("cat_education", "Education", "📚" to "#2196F3"),
                                Triple("cat_other_exp", "Other", "📦" to "#607D8B")
                        )

                expenseCategories.forEachIndexed { index, (id, name, iconColor) ->
                    queries.insertCategory(
                            id = id,
                            user_id = null,
                            name = name,
                            icon = iconColor.first,
                            color = iconColor.second,
                            type = "EXPENSE",
                            is_default = 1L,
                            sort_order = index.toLong(),
                            created_at = now
                    )
                }

                val incomeCategories =
                        listOf(
                                Triple("cat_salary", "Salary", "💰" to "#4CAF50"),
                                Triple("cat_freelance", "Freelance", "💼" to "#8BC34A"),
                                Triple("cat_investment", "Investment", "📈" to "#00BCD4"),
                                Triple("cat_gift", "Gift", "🎁" to "#FF4081"),
                                Triple("cat_other_inc", "Other", "💵" to "#607D8B")
                        )

                incomeCategories.forEachIndexed { index, (id, name, iconColor) ->
                    queries.insertCategory(
                            id = id,
                            user_id = null,
                            name = name,
                            icon = iconColor.first,
                            color = iconColor.second,
                            type = "INCOME",
                            is_default = 1L,
                            sort_order = index.toLong(),
                            created_at = now
                    )
                }
            }

    // ============================================
    // ACCOUNTS
    // ============================================

    fun getAllAccounts(userId: String): Flow<List<Accounts>> {
        return queries.selectAllAccounts(userId).asFlow().mapToList(Dispatchers.IO)
    }

    fun getActiveAccounts(userId: String): Flow<List<Accounts>> {
        return queries.selectActiveAccounts(userId).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getActiveAccountsSync(userId: String): List<Accounts> =
            withContext(Dispatchers.IO) { queries.selectActiveAccounts(userId).executeAsList() }

    suspend fun getAccountById(accountId: String): Accounts? =
            withContext(Dispatchers.IO) {
                queries.selectAccountById(accountId).executeAsOneOrNull()
            }

    suspend fun getDefaultAccount(userId: String): Accounts? =
            withContext(Dispatchers.IO) {
                queries.selectDefaultAccount(userId).executeAsOneOrNull()
            }

    suspend fun insertAccount(account: Accounts) =
            withContext(Dispatchers.IO) {
                queries.insertAccount(
                        id = account.id,
                        user_id = account.user_id,
                        name = account.name,
                        balance = account.balance,
                        currency = account.currency,
                        type = account.type,
                        color = account.color,
                        icon = account.icon,
                        is_default = account.is_default,
                        is_active = account.is_active,
                        is_excluded_from_total = account.is_excluded_from_total,
                        sort_order = account.sort_order,
                        created_at = account.created_at,
                        updated_at = account.updated_at
                )
            }

    suspend fun updateAccountBalance(accountId: String, newBalance: Double, updatedAt: Long) =
            withContext(Dispatchers.IO) {
                queries.updateAccountBalance(newBalance, updatedAt, accountId)
            }

    suspend fun deleteAccount(accountId: String) =
            withContext(Dispatchers.IO) { queries.deleteAccount(accountId) }

    // ============================================
    // TRANSACTIONS
    // ============================================

    fun getTransactions(): Flow<List<Transaction>> {
        return queries.selectAllTransactions(defaultUserId)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainTransaction() } }
    }

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> {
        return queries.selectTransactionsByAccount(defaultUserId, accountId)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainTransaction() } }
    }

    fun getTransactionsByDateRange(
            startDate: LocalDate,
            endDate: LocalDate
    ): Flow<List<Transaction>> {
        return queries.selectTransactionsByDateRange(
                        user_id = defaultUserId,
                        date = startDate.toString(),
                        date_ = endDate.toString()
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainTransaction() } }
    }

    fun getTransactionsByAccountAndDateRange(
            accountId: String,
            startDate: LocalDate,
            endDate: LocalDate
    ): Flow<List<Transaction>> {
        return queries.selectTransactionsByAccountAndDateRange(
                        user_id = defaultUserId,
                        account_id = accountId,
                        date = startDate.toString(),
                        date_ = endDate.toString()
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainTransaction() } }
    }

    suspend fun insertTransaction(transaction: Transaction) =
            withContext(Dispatchers.IO) {
                val now = Clock.System.now().toEpochMilliseconds()
                queries.insertTransaction(
                        id = transaction.id,
                        user_id = defaultUserId,
                        account_id = transaction.accountId,
                        type = transaction.type.name,
                        amount = transaction.amount,
                        currency = transaction.currency,
                        category_id = transaction.categoryId,
                        description = transaction.description,
                        note = transaction.note,
                        date = transaction.date.toString(),
                        time = transaction.time,
                        location = transaction.location,
                        location_lat = transaction.locationLat,
                        location_lng = transaction.locationLng,
                        labels = transaction.labels.joinToString(","),
                        photo_path = transaction.photoPath,
                        is_recurring = if (transaction.isRecurring) 1L else 0L,
                        recurring_id = transaction.recurringId,
                        receipt_url = transaction.receiptUrl,
                        tags = transaction.tags.joinToString(","),
                        is_synced = 0L,
                        created_at = now,
                        updated_at = now
                )
            }

    suspend fun deleteTransaction(id: String) =
            withContext(Dispatchers.IO) { queries.deleteTransaction(id) }

    // ============================================
    // BUDGETS
    // ============================================

    fun getBudgets(): Flow<List<Budget>> {
        return queries.selectAllBudgets(defaultUserId).asFlow().mapToList(Dispatchers.IO).map { list
            ->
            list.map { it.toDomainBudget() }
        }
    }

    suspend fun insertBudget(budget: Budget) =
            withContext(Dispatchers.IO) {
                val now = Clock.System.now().toEpochMilliseconds()
                queries.insertBudget(
                        id = budget.id,
                        user_id = defaultUserId,
                        category_id = budget.categoryId,
                        amount = budget.amount,
                        spent = budget.spent,
                        period = budget.period.name,
                        start_date = budget.startDate.toString(),
                        alert_threshold = budget.alertThreshold.toLong(),
                        rollover = 0L,
                        is_active = if (budget.isActive) 1L else 0L,
                        created_at = now,
                        updated_at = now
                )
            }

    // ============================================
    // HABITS
    // ============================================

    fun getHabits(): Flow<List<Habit>> {
        return queries.selectAllHabits(defaultUserId).asFlow().mapToList(Dispatchers.IO).map { list
            ->
            list.map { it.toDomainHabit() }
        }
    }

    fun getActiveHabits(): Flow<List<Habit>> {
        return queries.selectActiveHabits(defaultUserId).asFlow().mapToList(Dispatchers.IO).map {
                list ->
            list.map { it.toDomainHabit() }
        }
    }

    suspend fun insertHabit(habit: Habit) =
            withContext(Dispatchers.IO) {
                val now = Clock.System.now().toEpochMilliseconds()
                queries.insertHabit(
                        id = habit.id,
                        user_id = defaultUserId,
                        name = habit.name,
                        description = habit.description,
                        icon = habit.icon,
                        color = habit.color,
                        category_id = habit.categoryId,
                        frequency = habit.frequency.name,
                        target_days = null,
                        target_count = habit.targetCount.toLong(),
                        reminder_time = habit.reminderTime?.toString(),
                        reminder_enabled = 0L,
                        current_streak = habit.currentStreak.toLong(),
                        best_streak = habit.bestStreak.toLong(),
                        total_completions = habit.totalCompletions.toLong(),
                        xp_reward = habit.xpReward.toLong(),
                        is_active = if (habit.isActive) 1L else 0L,
                        is_archived = if (habit.isArchived) 1L else 0L,
                        sort_order = habit.order.toLong(),
                        is_synced = 0L,
                        created_at = now,
                        updated_at = now
                )
            }

    suspend fun updateHabitStreak(
            habitId: String,
            currentStreak: Int,
            bestStreak: Int,
            totalCompletions: Int
    ) =
            withContext(Dispatchers.IO) {
                queries.updateHabitStreak(
                        current_streak = currentStreak.toLong(),
                        best_streak = bestStreak.toLong(),
                        total_completions = totalCompletions.toLong(),
                        updated_at = Clock.System.now().toEpochMilliseconds(),
                        id = habitId
                )
            }

    suspend fun deleteHabit(id: String) = withContext(Dispatchers.IO) { queries.deleteHabit(id) }

    // ============================================
    // HABIT LOGS
    // ============================================

    fun getHabitLogsForDate(date: LocalDate): Flow<List<HabitLog>> {
        return queries.selectHabitLogsByDate(date.toString())
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { list -> list.map { it.toDomainHabitLog() } }
    }

    fun getHabitLogsForHabit(habitId: String): Flow<List<HabitLog>> {
        return queries.selectHabitLogsByHabitId(habitId).asFlow().mapToList(Dispatchers.IO).map {
                list ->
            list.map { it.toDomainHabitLog() }
        }
    }

    suspend fun insertHabitLog(log: HabitLog) =
            withContext(Dispatchers.IO) {
                queries.insertHabitLog(
                        id = log.id,
                        habit_id = log.habitId,
                        date = log.date.toString(),
                        completed_count = log.completedCount.toLong(),
                        note = log.note,
                        skipped = if (log.skipped) 1L else 0L,
                        created_at = Clock.System.now().toEpochMilliseconds()
                )
            }

    suspend fun deleteHabitLog(habitId: String, date: LocalDate) =
            withContext(Dispatchers.IO) { queries.deleteHabitLog(habitId, date.toString()) }

    // ============================================
    // DEBTS
    // ============================================

    fun getDebts(): Flow<List<Debt>> {
        return queries.selectAllDebts(defaultUserId).asFlow().mapToList(Dispatchers.IO).map { list
            ->
            list.map { it.toDomainDebt() }
        }
    }

    fun getActiveDebts(): Flow<List<Debt>> {
        return queries.selectActiveDebts(defaultUserId).asFlow().mapToList(Dispatchers.IO).map {
                list ->
            list.map { it.toDomainDebt() }
        }
    }

    suspend fun insertDebt(debt: Debt) =
            withContext(Dispatchers.IO) {
                val now = Clock.System.now().toEpochMilliseconds()
                queries.insertDebt(
                        id = debt.id,
                        user_id = defaultUserId,
                        name = debt.name,
                        type = debt.type.name,
                        original_amount = debt.originalAmount,
                        current_balance = debt.currentBalance,
                        interest_rate = debt.interestRate,
                        minimum_payment = debt.minimumPayment,
                        due_day = debt.dueDay.toLong(),
                        lender_name = debt.lenderName,
                        account_number = debt.accountNumber,
                        notes = debt.notes,
                        start_date = debt.startDate.toString(),
                        target_payoff_date = debt.targetPayoffDate?.toString(),
                        is_active = if (debt.isActive) 1L else 0L,
                        is_paid_off = if (debt.isPaidOff) 1L else 0L,
                        paid_off_date = debt.paidOffDate?.toString(),
                        color = debt.color,
                        priority = debt.priority.toLong(),
                        is_synced = 0L,
                        created_at = now,
                        updated_at = now
                )
            }

    suspend fun deleteDebt(id: String) = withContext(Dispatchers.IO) { queries.deleteDebt(id) }

    // ============================================
    // DEBT PAYMENTS
    // ============================================

    fun getPaymentsForDebt(debtId: String): Flow<List<DebtPayment>> {
        return queries.selectPaymentsByDebtId(debtId).asFlow().mapToList(Dispatchers.IO).map { list
            ->
            list.map { it.toDomainDebtPayment() }
        }
    }

    suspend fun insertDebtPayment(payment: DebtPayment) =
            withContext(Dispatchers.IO) {
                queries.insertDebtPayment(
                        id = payment.id,
                        debt_id = payment.debtId,
                        amount = payment.amount,
                        principal_amount = payment.principalAmount,
                        interest_amount = payment.interestAmount,
                        date = payment.date.toString(),
                        note = payment.note,
                        is_extra_payment = if (payment.isExtraPayment) 1L else 0L,
                        created_at = Clock.System.now().toEpochMilliseconds()
                )
            }

    // ============================================
    // APP CONFIG
    // ============================================

    suspend fun getConfigValue(key: String): String? =
            withContext(Dispatchers.IO) { queries.selectAppConfig(key).executeAsOneOrNull() }

    suspend fun setConfigValue(key: String, value: String) =
            withContext(Dispatchers.IO) {
                queries.insertAppConfig(key, value, Clock.System.now().toEpochMilliseconds())
            }
}

// ============================================
// EXTENSION FUNCTIONS FOR MAPPING
// ============================================

private fun Categories.toDomainCategory(): Category =
        Category(
                id = id,
                userId = user_id,
                name = name,
                icon = icon,
                color = color,
                type = TransactionType.valueOf(type),
                isDefault = is_default == 1L,
                order = sort_order.toInt()
        )

private fun Transactions.toDomainTransaction(): Transaction =
        Transaction(
                id = id,
                userId = user_id,
                accountId = account_id,
                categoryId = category_id,
                type = TransactionType.valueOf(type),
                amount = amount,
                currency = currency,
                date = LocalDate.parse(date),
                time = time,
                location = location,
                locationLat = location_lat,
                locationLng = location_lng,
                labels = labels?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                photoPath = photo_path,
                description = description,
                note = note,
                tags = tags?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                receiptUrl = receipt_url,
                isRecurring = is_recurring == 1L,
                recurringId = recurring_id,
                createdAt = Instant.fromEpochMilliseconds(created_at),
                updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )

private fun Budgets.toDomainBudget(): Budget =
        Budget(
                id = id,
                userId = user_id,
                categoryId = category_id,
                amount = amount,
                spent = spent,
                period = BudgetPeriod.valueOf(period),
                startDate = LocalDate.parse(start_date),
                isActive = is_active == 1L,
                alertThreshold = alert_threshold.toInt(),
                createdAt = Instant.fromEpochMilliseconds(created_at),
                updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )

private fun Habits.toDomainHabit(): Habit =
        Habit(
                id = id,
                userId = user_id,
                categoryId = category_id,
                name = name,
                description = description,
                icon = icon,
                color = color,
                targetCount = target_count.toInt(),
                frequency = HabitFrequency.valueOf(frequency),
                reminderTime = reminder_time?.let { kotlinx.datetime.LocalTime.parse(it) },
                currentStreak = current_streak.toInt(),
                bestStreak = best_streak.toInt(),
                totalCompletions = total_completions.toInt(),
                xpReward = xp_reward.toInt(),
                isActive = is_active == 1L,
                isArchived = is_archived == 1L,
                order = sort_order.toInt(),
                createdAt = Instant.fromEpochMilliseconds(created_at),
                updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )

private fun Habit_logs.toDomainHabitLog(): HabitLog =
        HabitLog(
                id = id,
                habitId = habit_id,
                date = LocalDate.parse(date),
                completedCount = completed_count.toInt(),
                note = note,
                skipped = skipped == 1L,
                createdAt = Instant.fromEpochMilliseconds(created_at)
        )

private fun Debts.toDomainDebt(): Debt =
        Debt(
                id = id,
                userId = user_id,
                name = name,
                type = DebtType.valueOf(type),
                originalAmount = original_amount,
                currentBalance = current_balance,
                interestRate = interest_rate,
                minimumPayment = minimum_payment,
                dueDay = due_day.toInt(),
                lenderName = lender_name,
                accountNumber = account_number,
                notes = notes,
                startDate = LocalDate.parse(start_date),
                targetPayoffDate = target_payoff_date?.let { LocalDate.parse(it) },
                isActive = is_active == 1L,
                isPaidOff = is_paid_off == 1L,
                paidOffDate = paid_off_date?.let { LocalDate.parse(it) },
                color = color,
                priority = priority.toInt(),
                isSynced = is_synced == 1L,
                createdAt = Instant.fromEpochMilliseconds(created_at),
                updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )

private fun Debt_payments.toDomainDebtPayment(): DebtPayment =
        DebtPayment(
                id = id,
                debtId = debt_id,
                amount = amount,
                principalAmount = principal_amount,
                interestAmount = interest_amount,
                date = LocalDate.parse(date),
                note = note,
                isExtraPayment = is_extra_payment == 1L,
                createdAt = Instant.fromEpochMilliseconds(created_at)
        )
