package com.aevrontech.finevo.presentation.expense

import com.aevrontech.finevo.core.util.getCurrentLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class FilterPeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

sealed interface TimeRange {
    val label: String
    val displayLabel: String // Explicit date label (e.g. "Jan 2026")

    companion object {
        val Today
            get() = CalendarTimeRange(FilterPeriod.DAY, 0)
        val ThisWeek
            get() = CalendarTimeRange(FilterPeriod.WEEK, 0)
        val ThisMonth
            get() = CalendarTimeRange(FilterPeriod.MONTH, 0)
        val ThisYear
            get() = CalendarTimeRange(FilterPeriod.YEAR, 0)
    }
}

data class LastDaysRange(val days: Int, override val label: String) : TimeRange {
    override val displayLabel: String
        get() = label
}

data class CalendarTimeRange(val period: FilterPeriod, val offset: Int = 0) : TimeRange {
    override val displayLabel: String
        get() {
            val today = getCurrentLocalDate()
            val (start, end) = getRangeDates(period, offset, today)
            return getFormattedDate(period, start, end, today)
        }

    override val label: String
        get() {
            val today = getCurrentLocalDate()

            // Special cases for Selection List ("Today", "This Month", etc)
            if (offset == 0) {
                return when (period) {
                    FilterPeriod.DAY -> "Today"
                    FilterPeriod.WEEK -> "This Week"
                    FilterPeriod.MONTH -> "This Month"
                    FilterPeriod.YEAR -> "This Year"
                }
            }
            if (offset == -1) {
                return when (period) {
                    FilterPeriod.DAY -> "Yesterday"
                    FilterPeriod.WEEK -> "Last Week"
                    FilterPeriod.MONTH -> "Last Month"
                    FilterPeriod.YEAR -> "Last Year"
                }
            }

            // Default to displayLabel for other offsets
            return displayLabel
        }

    private fun getFormattedDate(
        period: FilterPeriod,
        start: LocalDate,
        end: LocalDate,
        today: LocalDate
    ): String {
        return when (period) {
            FilterPeriod.DAY -> {
                // "Jan 5" or "Dec 31, 2025"
                if (start.year == today.year) {
                    "${
                        start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    } ${start.dayOfMonth}"
                } else {
                    "${
                        start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    } ${start.dayOfMonth}, ${start.year}"
                }
            }
            FilterPeriod.WEEK -> {
                // "Dec 29, 2025 - Jan 4, 2026"
                // User requested full years in example "Dec 22, 2025 - Dec 28, 2025"
                val startStr =
                    "${
                        start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    } ${start.dayOfMonth}, ${start.year}"
                val endStr =
                    "${
                        end.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    } ${end.dayOfMonth}, ${end.year}"
                "$startStr - $endStr"
            }
            FilterPeriod.MONTH -> {
                // "Dec 2025"
                "${start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${start.year}"
            }
            FilterPeriod.YEAR -> {
                // "2025"
                "${start.year}"
            }
        }
    }

    private fun getRangeDates(
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

data class CustomTimeRange(val start: LocalDate, val end: LocalDate) : TimeRange {
    override val displayLabel: String
        get() = label

    override val label: String
        get() {
            val startStr =
                "${
                    start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                } ${start.dayOfMonth}, ${start.year}"
            // If same year, we could shorten startStr, but user example implies full dates: Dec
            // 20025 - Jan 4
            // Let's use full date for clarity
            val endStr =
                "${
                    end.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                } ${end.dayOfMonth}, ${end.year}"
            return "$startStr - $endStr"
        }
}

data object AllTimeRange : TimeRange {
    override val label: String = "All Time"
    override val displayLabel: String = "All Time"
}
