package com.aevrontech.finevo.data.repository

import com.aevrontech.finevo.core.util.AppException
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.core.util.getCurrentTimeMillis
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.domain.model.Achievement
import com.aevrontech.finevo.domain.model.DailyHabitSummary
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitCategory
import com.aevrontech.finevo.domain.model.HabitLog
import com.aevrontech.finevo.domain.model.UserStats
import com.aevrontech.finevo.domain.model.WeeklyHabitAnalytics
import com.aevrontech.finevo.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/** HabitRepository implementation using SQLDelight for local storage. */
class HabitRepositoryImpl(private val localDataSource: LocalDataSource) : HabitRepository {

    override fun getHabits(): Flow<List<Habit>> {
        return localDataSource.getHabits()
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return localDataSource.getActiveHabits()
    }

    override fun getHabitsByCategory(categoryId: String): Flow<List<Habit>> {
        return localDataSource.getHabits().map { list ->
            list.filter { it.categoryId == categoryId }
        }
    }

    override suspend fun getHabit(id: String): Result<Habit> {
        return try {
            val habit = localDataSource.getHabits().first().find { it.id == id }

            if (habit != null) Result.success(habit)
            else Result.error(AppException.NotFound("Habit"))
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun addHabit(habit: Habit): Result<Habit> {
        return try {
            localDataSource.insertHabit(habit)
            Result.success(habit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to add habit"))
        }
    }

    override suspend fun updateHabit(habit: Habit): Result<Habit> {
        return try {
            // Use insert with same ID (INSERT OR REPLACE)
            localDataSource.insertHabit(habit)
            Result.success(habit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to update habit"))
        }
    }

    override suspend fun deleteHabit(id: String): Result<Unit> {
        return try {
            localDataSource.deleteHabit(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to delete habit"))
        }
    }

    override suspend fun archiveHabit(id: String): Result<Habit> {
        return try {
            val habit =
                localDataSource.getHabits().first().find { it.id == id }
                    ?: return Result.error(AppException.NotFound("Habit"))

            val archived =
                habit.copy(
                    isArchived = true,
                    updatedAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                )
            localDataSource.insertHabit(archived)
            Result.success(archived)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to archive habit"))
        }
    }

    override suspend fun reorderHabits(habitIds: List<String>): Result<Unit> {
        return Result.success(Unit) // TODO: Implement
    }

    override fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>> {
        return localDataSource.getHabitLogsForDate(date)
    }

    override fun getLogs(startDate: LocalDate, endDate: LocalDate): Flow<List<HabitLog>> {
        return localDataSource.getHabitLogsForDate(startDate)
    }

    override fun getLogsForHabit(habitId: String): Flow<List<HabitLog>> {
        return localDataSource.getHabitLogsForHabit(habitId)
    }

    override suspend fun completeHabit(
        habitId: String,
        date: LocalDate,
        note: String?
    ): Result<HabitLog> {
        return try {
            val log =
                HabitLog(
                    id = "log_${getCurrentTimeMillis()}",
                    habitId = habitId,
                    date = date,
                    completedCount = 1,
                    note = note,
                    createdAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                )
            localDataSource.insertHabitLog(log)

            // Update streak
            updateStreakForHabit(habitId)

            Result.success(log)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to complete habit"))
        }
    }

    override suspend fun uncompleteHabit(habitId: String, date: LocalDate): Result<Unit> {
        return try {
            localDataSource.deleteHabitLog(habitId, date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to uncomplete habit"))
        }
    }

    override suspend fun skipHabit(habitId: String, date: LocalDate): Result<HabitLog> {
        return try {
            val log =
                HabitLog(
                    id = "log_${getCurrentTimeMillis()}",
                    habitId = habitId,
                    date = date,
                    completedCount = 0,
                    skipped = true,
                    createdAt = Instant.fromEpochMilliseconds(getCurrentTimeMillis())
                )
            localDataSource.insertHabitLog(log)
            Result.success(log)
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Failed to skip habit"))
        }
    }

    override suspend fun isHabitCompleted(habitId: String, date: LocalDate): Boolean {
        return try {
            localDataSource.getHabitLogsForDate(date).first().any {
                it.habitId == habitId && !it.skipped
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getCategories(): Flow<List<HabitCategory>> {
        return flowOf(defaultCategories())
    }

    override suspend fun addCategory(category: HabitCategory): Result<HabitCategory> {
        return Result.success(category)
    }

    override suspend fun updateCategory(category: HabitCategory): Result<HabitCategory> {
        return Result.success(category)
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getUserStats(): Flow<UserStats> {
        return flowOf(UserStats(userId = ""))
    }

    override fun getAchievements(): Flow<List<Achievement>> = flowOf(emptyList())

    override suspend fun checkAchievements(): Result<List<Achievement>> =
        Result.success(emptyList())

    override suspend fun addXp(amount: Int): Result<UserStats> {
        return Result.success(UserStats(userId = "", totalXp = amount))
    }

    override suspend fun getDailySummary(date: LocalDate): Result<DailyHabitSummary> {
        return try {
            val logs = localDataSource.getHabitLogsForDate(date).first()
            val habits = localDataSource.getActiveHabits().first()
            val completed = logs.count { !it.skipped }

            Result.success(
                DailyHabitSummary(
                    date = date,
                    habitsCompleted = completed,
                    habitsTotal = habits.size,
                    xpEarned = completed * 10,
                    isPerfectDay = completed == habits.size && habits.isNotEmpty()
                )
            )
        } catch (e: Exception) {
            Result.error(AppException.DatabaseError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getWeeklyAnalytics(
        weekStartDate: LocalDate
    ): Result<WeeklyHabitAnalytics> {
        return Result.success(
            WeeklyHabitAnalytics(
                weekStartDate = weekStartDate,
                dailySummaries = emptyList(),
                totalXpEarned = 0,
                perfectDays = 0,
                averageCompletionRate = 0.0,
                bestHabit = null,
                needsImprovementHabit = null
            )
        )
    }

    override fun getStreakCalendar(
        habitId: String,
        month: LocalDate
    ): Flow<Map<LocalDate, Boolean>> {
        return localDataSource.getHabitLogsForHabit(habitId).map { logs ->
            logs.associate { it.date to !it.skipped }
        }
    }

    override suspend fun updateStreaks(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun useStreakFreeze(habitId: String, date: LocalDate): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun sync(): Result<Unit> = Result.success(Unit)

    private suspend fun updateStreakForHabit(habitId: String) {
        try {
            val habit = localDataSource.getHabits().first().find { it.id == habitId } ?: return
            val newStreak = habit.currentStreak + 1
            val bestStreak = maxOf(habit.bestStreak, newStreak)

            localDataSource.updateHabitStreak(
                habitId = habitId,
                currentStreak = newStreak,
                bestStreak = bestStreak,
                totalCompletions = habit.totalCompletions + 1
            )
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun defaultCategories(): List<HabitCategory> =
        listOf(
            HabitCategory("hcat_health", null, "Health", "💪", "#4CAF50", 1),
            HabitCategory("hcat_productivity", null, "Productivity", "⚡", "#FF9800", 2),
            HabitCategory("hcat_mindfulness", null, "Mindfulness", "🧘", "#9C27B0", 3),
            HabitCategory("hcat_learning", null, "Learning", "📚", "#2196F3", 4),
            HabitCategory("hcat_social", null, "Social", "👥", "#E91E63", 5),
            HabitCategory("hcat_finance", null, "Finance", "💰", "#00BCD4", 6)
        )
}
