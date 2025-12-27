package com.aevrontech.finevo.domain.repository

import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.Achievement
import com.aevrontech.finevo.domain.model.DailyHabitSummary
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitCategory
import com.aevrontech.finevo.domain.model.HabitLog
import com.aevrontech.finevo.domain.model.UserStats
import com.aevrontech.finevo.domain.model.WeeklyHabitAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Habit repository interface
 */
interface HabitRepository {
    // ============================================
    // HABITS
    // ============================================

    /**
     * Get all habits for the current user
     */
    fun getHabits(): Flow<List<Habit>>

    /**
     * Get active habits (not archived)
     */
    fun getActiveHabits(): Flow<List<Habit>>

    /**
     * Get habits by category
     */
    fun getHabitsByCategory(categoryId: String): Flow<List<Habit>>

    /**
     * Get a single habit by ID
     */
    suspend fun getHabit(id: String): Result<Habit>

    /**
     * Add a new habit
     */
    suspend fun addHabit(habit: Habit): Result<Habit>

    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit): Result<Habit>

    /**
     * Delete a habit
     */
    suspend fun deleteHabit(id: String): Result<Unit>

    /**
     * Archive a habit
     */
    suspend fun archiveHabit(id: String): Result<Habit>

    /**
     * Reorder habits
     */
    suspend fun reorderHabits(habitIds: List<String>): Result<Unit>

    // ============================================
    // HABIT LOGS / COMPLETIONS
    // ============================================

    /**
     * Get logs for a specific date
     */
    fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>>

    /**
     * Get logs for a date range
     */
    fun getLogs(startDate: LocalDate, endDate: LocalDate): Flow<List<HabitLog>>

    /**
     * Get logs for a specific habit
     */
    fun getLogsForHabit(habitId: String): Flow<List<HabitLog>>

    /**
     * Mark habit as completed for a date
     */
    suspend fun completeHabit(habitId: String, date: LocalDate, note: String? = null): Result<HabitLog>

    /**
     * Unmark habit completion
     */
    suspend fun uncompleteHabit(habitId: String, date: LocalDate): Result<Unit>

    /**
     * Skip a habit for a date
     */
    suspend fun skipHabit(habitId: String, date: LocalDate): Result<HabitLog>

    /**
     * Check if habit is completed for a date
     */
    suspend fun isHabitCompleted(habitId: String, date: LocalDate): Boolean

    // ============================================
    // CATEGORIES
    // ============================================

    /**
     * Get all habit categories
     */
    fun getCategories(): Flow<List<HabitCategory>>

    /**
     * Add a habit category
     */
    suspend fun addCategory(category: HabitCategory): Result<HabitCategory>

    /**
     * Update a habit category
     */
    suspend fun updateCategory(category: HabitCategory): Result<HabitCategory>

    /**
     * Delete a habit category
     */
    suspend fun deleteCategory(id: String): Result<Unit>

    // ============================================
    // GAMIFICATION
    // ============================================

    /**
     * Get user stats
     */
    fun getUserStats(): Flow<UserStats>

    /**
     * Get all achievements
     */
    fun getAchievements(): Flow<List<Achievement>>

    /**
     * Check and unlock achievements
     */
    suspend fun checkAchievements(): Result<List<Achievement>>

    /**
     * Add XP to user
     */
    suspend fun addXp(amount: Int): Result<UserStats>

    // ============================================
    // ANALYTICS
    // ============================================

    /**
     * Get daily summary for a date
     */
    suspend fun getDailySummary(date: LocalDate): Result<DailyHabitSummary>

    /**
     * Get weekly analytics
     */
    suspend fun getWeeklyAnalytics(weekStartDate: LocalDate): Result<WeeklyHabitAnalytics>

    /**
     * Get streak calendar for a habit
     */
    fun getStreakCalendar(habitId: String, month: LocalDate): Flow<Map<LocalDate, Boolean>>

    // ============================================
    // STREAK MANAGEMENT
    // ============================================

    /**
     * Update streaks for all habits
     */
    suspend fun updateStreaks(): Result<Unit>

    /**
     * Use streak freeze (premium)
     */
    suspend fun useStreakFreeze(habitId: String, date: LocalDate): Result<Boolean>

    // ============================================
    // SYNC
    // ============================================

    /**
     * Sync all habit data with server
     */
    suspend fun sync(): Result<Unit>
}
