package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/** Habit domain model */
@Serializable
data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val icon: String, // Emoji or icon name
    val color: String, // Hex color
    val categoryId: String? = null,
    val subCategory: String? = null, // SubCategory key for pre-defined habits
    val frequency: HabitFrequency,
    val targetDays: List<Int> = emptyList(), // 1-7 (Mon-Sun) for weekly, 1-31 for monthly
    val targetCount: Int = 1, // Times per period
    val goalValue: Int = 1, // Target value (e.g., 8 for 8 glasses)
    val goalUnit: String = "count", // Unit: count, steps, ml, min, etc. (supports custom)
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val gestureMode: String = "MARK_AS_DONE", // MARK_AS_DONE or INPUT_VALUE
    val startDate: LocalDate? = null, // Habit start date
    val endDate: LocalDate? = null, // Habit end date (null = ongoing)
    val reminderTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val xpReward: Int = 10, // XP earned per completion
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val order: Int = 0,
    val isSynced: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

/** Habit frequency options */
@Serializable
enum class HabitFrequency {
    DAILY, // Every day
    SPECIFIC_DAYS, // Specific days of week
    WEEKLY, // X times per week
    MONTHLY // X times per month
}

/** Habit completion log */
@Serializable
data class HabitLog(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val completedCount: Int = 1,
    val note: String? = null,
    val skipped: Boolean = false,
    val createdAt: Instant
)

/** Habit category */
@Serializable
data class HabitCategory(
    val id: String,
    val userId: String? = null, // null = system default
    val name: String,
    val icon: String,
    val color: String,
    val order: Int = 0
)

/** Time of day grouping for habits */
@Serializable
enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    ANYTIME
}

/** Gamification - User stats */
@Serializable
data class UserStats(
    val userId: String,
    val totalXp: Int = 0,
    val level: Int = 1,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalHabitsCompleted: Int = 0,
    val perfectDays: Int = 0, // Days with all habits completed
    val achievements: List<String> = emptyList(),
    val lastActiveDate: LocalDate? = null
) {
    val xpForNextLevel: Int
        get() = level * 100
    val xpProgress: Int
        get() = totalXp % 100
    val levelProgress: Double
        get() = xpProgress.toDouble() / xpForNextLevel
}

/** Achievement definition */
@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val requirement: AchievementRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Instant? = null
)

/** Achievement requirement types */
@Serializable
sealed class AchievementRequirement {
    @Serializable
    data class StreakDays(val days: Int) : AchievementRequirement()

    @Serializable
    data class TotalCompletions(val count: Int) : AchievementRequirement()

    @Serializable
    data class PerfectDays(val count: Int) : AchievementRequirement()

    @Serializable
    data class LevelReached(val level: Int) : AchievementRequirement()

    @Serializable
    data class HabitsCreated(val count: Int) : AchievementRequirement()
}

/** Daily habit summary */
data class DailyHabitSummary(
    val date: LocalDate,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val xpEarned: Int,
    val isPerfectDay: Boolean
) {
    val completionRate: Double
        get() = if (habitsTotal > 0) habitsCompleted.toDouble() / habitsTotal else 0.0
}

/** Weekly habit analytics */
data class WeeklyHabitAnalytics(
    val weekStartDate: LocalDate,
    val dailySummaries: List<DailyHabitSummary>,
    val totalXpEarned: Int,
    val perfectDays: Int,
    val averageCompletionRate: Double,
    val bestHabit: Habit?,
    val needsImprovementHabit: Habit?
)
