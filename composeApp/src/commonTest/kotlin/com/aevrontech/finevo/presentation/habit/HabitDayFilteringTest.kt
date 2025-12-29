package com.aevrontech.finevo.presentation.habit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Unit tests for habit day filtering logic.
 *
 * WeekDaySelector uses: Sunday=1, Monday=2, Tuesday=3, Wednesday=4, Thursday=5, Friday=6,
 * Saturday=7 (dayLabels = ["S", "M", "T", "W", "T", "F", "S"], dayNumber = index + 1)
 *
 * kotlinx.datetime.DayOfWeek ordinal: MONDAY=0, TUESDAY=1, WEDNESDAY=2, THURSDAY=3, FRIDAY=4,
 * SATURDAY=5, SUNDAY=6
 *
 * isScheduledForDate formula: ((date.dayOfWeek.ordinal + 1) % 7) + 1
 * - Monday (ordinal=0): ((0+1) % 7) + 1 = 1 + 1 = 2 ✓
 * - Tuesday (ordinal=1): ((1+1) % 7) + 1 = 2 + 1 = 3 ✓
 * - Wednesday (ordinal=2): ((2+1) % 7) + 1 = 3 + 1 = 4 ✓
 * - Thursday (ordinal=3): ((3+1) % 7) + 1 = 4 + 1 = 5 ✓
 * - Friday (ordinal=4): ((4+1) % 7) + 1 = 5 + 1 = 6 ✓
 * - Saturday (ordinal=5): ((5+1) % 7) + 1 = 6 + 1 = 7 ✓
 * - Sunday (ordinal=6): ((6+1) % 7) + 1 = 0 + 1 = 1 ✓
 */
class HabitDayFilteringTest {

    /**
     * Convert kotlinx.datetime.DayOfWeek to WeekDaySelector index Same formula used in
     * isScheduledForDate
     */
    private fun dayOfWeekToSelectorIndex(dayOfWeek: DayOfWeek): Int {
        return ((dayOfWeek.ordinal + 1) % 7) + 1
    }

    @Test
    fun `Monday should map to index 2`() {
        assertEquals(2, dayOfWeekToSelectorIndex(DayOfWeek.MONDAY))
    }

    @Test
    fun `Tuesday should map to index 3`() {
        assertEquals(3, dayOfWeekToSelectorIndex(DayOfWeek.TUESDAY))
    }

    @Test
    fun `Wednesday should map to index 4`() {
        assertEquals(4, dayOfWeekToSelectorIndex(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `Thursday should map to index 5`() {
        assertEquals(5, dayOfWeekToSelectorIndex(DayOfWeek.THURSDAY))
    }

    @Test
    fun `Friday should map to index 6`() {
        assertEquals(6, dayOfWeekToSelectorIndex(DayOfWeek.FRIDAY))
    }

    @Test
    fun `Saturday should map to index 7`() {
        assertEquals(7, dayOfWeekToSelectorIndex(DayOfWeek.SATURDAY))
    }

    @Test
    fun `Sunday should map to index 1`() {
        assertEquals(1, dayOfWeekToSelectorIndex(DayOfWeek.SUNDAY))
    }

    @Test
    fun `December 29 2024 is a Sunday and should map to index 1`() {
        val date = LocalDate(2024, 12, 29)
        assertEquals(DayOfWeek.SUNDAY, date.dayOfWeek)
        assertEquals(1, dayOfWeekToSelectorIndex(date.dayOfWeek))
    }

    @Test
    fun `December 30 2024 is a Monday and should map to index 2`() {
        val date = LocalDate(2024, 12, 30)
        assertEquals(DayOfWeek.MONDAY, date.dayOfWeek)
        assertEquals(2, dayOfWeekToSelectorIndex(date.dayOfWeek))
    }

    @Test
    fun `Habit with Monday targetDays should match Monday date`() {
        val mondayDate = LocalDate(2024, 12, 30) // Monday
        val targetDays = listOf(2) // Monday = 2 in WeekDaySelector

        val dayIndex = dayOfWeekToSelectorIndex(mondayDate.dayOfWeek)
        assertTrue(targetDays.contains(dayIndex), "Monday should be in targetDays for Monday habit")
    }

    @Test
    fun `Habit with Monday targetDays should NOT match Sunday date`() {
        val sundayDate = LocalDate(2024, 12, 29) // Sunday
        val targetDays = listOf(2) // Monday = 2 in WeekDaySelector

        val dayIndex = dayOfWeekToSelectorIndex(sundayDate.dayOfWeek)
        assertFalse(
                targetDays.contains(dayIndex),
                "Sunday should NOT be in targetDays for Monday habit"
        )
    }

    @Test
    fun `Print all day mappings for debugging`() {
        println("Day mappings:")
        DayOfWeek.entries.forEach { day ->
            val index = dayOfWeekToSelectorIndex(day)
            println("  ${day.name} (ordinal=${day.ordinal}) -> WeekDaySelector index: $index")
        }

        println("\nWeekDaySelector UI indices:")
        println("  S (Sunday) = 1")
        println("  M (Monday) = 2")
        println("  T (Tuesday) = 3")
        println("  W (Wednesday) = 4")
        println("  T (Thursday) = 5")
        println("  F (Friday) = 6")
        println("  S (Saturday) = 7")
    }
}
