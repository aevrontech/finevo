package com.aevrontech.finevo.presentation.expense

import kotlinx.datetime.LocalDate

enum class FilterPeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

sealed interface TimeRange {
    val label: String

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

// Top-level definitions to avoid resolution issues
data class LastDaysRange(val days: Int, override val label: String) : TimeRange

data class CalendarTimeRange(val period: FilterPeriod, val offset: Int = 0) : TimeRange {
    override val label: String
        get() =
            when (offset) {
                0 ->
                    when (period) {
                        FilterPeriod.DAY -> "Today"
                        FilterPeriod.WEEK -> "This Week"
                        FilterPeriod.MONTH -> "This Month"
                        FilterPeriod.YEAR -> "This Year"
                    }
                -1 ->
                    when (period) {
                        FilterPeriod.DAY -> "Yesterday"
                        FilterPeriod.WEEK -> "Last Week"
                        FilterPeriod.MONTH -> "Last Month"
                        FilterPeriod.YEAR -> "Last Year"
                    }
                else ->
                    when (period) {
                        FilterPeriod.DAY -> "Day"
                        FilterPeriod.WEEK -> "Week"
                        FilterPeriod.MONTH -> "Month"
                        FilterPeriod.YEAR -> "Year"
                    }
            }
}

data class CustomTimeRange(val start: LocalDate, val end: LocalDate) : TimeRange {
    override val label: String = "Custom"
}

data object AllTimeRange : TimeRange {
    override val label: String = "All Time"
}
