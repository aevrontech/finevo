package com.aevrontech.finevo.presentation.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.DailyHabitSummary
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitFrequency
import com.aevrontech.finevo.domain.model.TimeOfDay
import com.aevrontech.finevo.domain.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** ViewModel for Habit Tracker feature. */
class HabitViewModel(private val habitRepository: HabitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    // Mutable selected date - can be changed by date selector
    private var selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // Keep track of all habits for filtering
    private var allHabitsCache: List<Habit> = emptyList()

    init {
        loadHabits()
        loadLogsForSelectedDate()
        loadDailySummary()
    }

    /** Set the selected date and refresh habits/logs */
    fun setSelectedDate(date: kotlinx.datetime.LocalDate) {
        selectedDate = date
        // Re-filter habits for the new date
        val habitsForDate = allHabitsCache.filter { habit -> isScheduledForDate(habit, date) }
        _uiState.update { it.copy(habits = habitsForDate, selectedDate = date) }
        // Load logs for the new date
        loadLogsForSelectedDate()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getActiveHabits().collect { allHabits ->
                // Cache all habits for filtering
                allHabitsCache = allHabits

                // Debug: Log all habits and their configurations
                println(
                        "=== DEBUG: Loading habits for date: $selectedDate (${selectedDate.dayOfWeek}) ==="
                )
                println("=== DEBUG: dayOfWeek ordinal: ${selectedDate.dayOfWeek.ordinal} ===")
                val dateIndex = ((selectedDate.dayOfWeek.ordinal + 1) % 7) + 1
                println("=== DEBUG: WeekDaySelector index: $dateIndex ===")

                allHabits.forEach { habit ->
                    println(
                            "  Habit: ${habit.name}, frequency: ${habit.frequency}, targetDays: ${habit.targetDays}"
                    )
                }

                // Filter habits that should appear for selected date
                val habitsForDate =
                        allHabits.filter { habit -> isScheduledForDate(habit, selectedDate) }

                println(
                        "=== DEBUG: Habits scheduled for $selectedDate: ${habitsForDate.map { it.name }} ==="
                )

                _uiState.update {
                    it.copy(
                            habits = habitsForDate,
                            allHabits = allHabits, // Keep all for reporting
                            selectedDate = selectedDate,
                            isLoading = false
                    )
                }
            }
        }
    }

    /** Check if a habit is scheduled for a specific date */
    private fun isScheduledForDate(habit: Habit, date: kotlinx.datetime.LocalDate): Boolean {
        // Check date range (startDate and endDate)
        habit.startDate?.let { startDate -> if (date < startDate) return false }
        habit.endDate?.let { endDate -> if (date > endDate) return false }

        // Check frequency
        val result =
                when (habit.frequency) {
                    HabitFrequency.DAILY -> true
                    HabitFrequency.WEEKLY, HabitFrequency.SPECIFIC_DAYS -> {
                        // targetDays uses: Sunday=1, Monday=2, ... Saturday=7
                        // kotlinx.datetime.DayOfWeek: MONDAY=0, TUESDAY=1, ... SUNDAY=6
                        // Convert: Sunday(ordinal=6) -> 1, Monday(ordinal=0) -> 2, etc.
                        val dayOfWeek = ((date.dayOfWeek.ordinal + 1) % 7) + 1
                        val matches = habit.targetDays.contains(dayOfWeek)
                        println(
                                "    Checking ${habit.name}: dayOfWeek=$dayOfWeek, targetDays=${habit.targetDays}, matches=$matches"
                        )
                        matches
                    }
                    HabitFrequency.MONTHLY -> {
                        // targetDays contains day of month (1-31)
                        habit.targetDays.contains(date.dayOfMonth)
                    }
                }
        return result
    }

    private fun loadLogsForSelectedDate() {
        viewModelScope.launch {
            habitRepository.getLogsForDate(selectedDate).collect { logs ->
                val completedIds = logs.filter { !it.skipped }.map { it.habitId }.toSet()
                _uiState.update { it.copy(completedHabitIds = completedIds) }
            }
        }
    }

    private fun loadDailySummary() {
        viewModelScope.launch {
            when (val result = habitRepository.getDailySummary(selectedDate)) {
                is Result.Success -> {
                    _uiState.update { it.copy(dailySummary = result.data) }
                }
                else -> {}
            }
        }
    }

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            val isCompleted = _uiState.value.completedHabitIds.contains(habitId)

            if (isCompleted) {
                // Uncomplete
                when (val result = habitRepository.uncompleteHabit(habitId, selectedDate)) {
                    is Result.Success -> {
                        // UI will update via flow
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.exception.message) }
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Complete
                when (val result = habitRepository.completeHabit(habitId, selectedDate, null)) {
                    is Result.Success -> {
                        val habit = _uiState.value.habits.find { it.id == habitId }
                        _uiState.update {
                            it.copy(successMessage = "+${habit?.xpReward ?: 10} XP! 🎉")
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.exception.message) }
                    }
                    is Result.Loading -> {}
                }
            }

            // Refresh summary
            loadDailySummary()
        }
    }

    /** Toggle habit completion with a specific value (for INPUT_VALUE gesture mode) */
    fun toggleHabitWithValue(habitId: String, value: Int) {
        viewModelScope.launch {
            when (val result =
                            habitRepository.completeHabit(habitId, selectedDate, value.toString())
            ) {
                is Result.Success -> {
                    val habit = _uiState.value.habits.find { it.id == habitId }
                    val percentage =
                            if (habit != null && habit.goalValue > 0)
                                    (value * 100 / habit.goalValue)
                            else 100
                    _uiState.update {
                        it.copy(
                                successMessage =
                                        "+${habit?.xpReward ?: 10} XP! Completed $value ${habit?.goalUnit ?: ""} ($percentage%) 🎉"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
            loadDailySummary()
        }
    }

    fun addHabit(
            name: String,
            icon: String,
            color: String,
            frequency: HabitFrequency = HabitFrequency.DAILY,
            xpReward: Int = 10
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()

            val habit =
                    Habit(
                            id = generateId(),
                            userId = "local",
                            name = name,
                            icon = icon,
                            color = color,
                            frequency = frequency,
                            xpReward = xpReward,
                            createdAt = now,
                            updatedAt = now
                    )

            when (val result = habitRepository.addHabit(habit)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Habit created!", showAddDialog = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Create a habit with all configuration options from the new Add Habit flow */
    fun createHabit(
            name: String,
            icon: String,
            color: String,
            frequency: HabitFrequency,
            targetDays: List<Int>,
            goalValue: Int,
            goalUnit: String,
            timeOfDay: com.aevrontech.finevo.domain.model.TimeOfDay,
            gestureMode: String,
            reminderEnabled: Boolean,
            reminderTime: kotlinx.datetime.LocalTime?,
            startDate: kotlinx.datetime.LocalDate?,
            endDate: kotlinx.datetime.LocalDate?,
            subCategory: String? = null
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()

            val habit =
                    Habit(
                            id = generateId(),
                            userId = "local",
                            name = name,
                            icon = icon,
                            color = color,
                            subCategory = subCategory,
                            frequency = frequency,
                            targetDays = targetDays,
                            targetCount = 1,
                            goalValue = goalValue,
                            goalUnit = goalUnit,
                            timeOfDay = timeOfDay,
                            gestureMode = gestureMode,
                            startDate = startDate,
                            endDate = endDate,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            xpReward = 10,
                            createdAt = now,
                            updatedAt = now
                    )

            when (val result = habitRepository.addHabit(habit)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Habit created! 🎉", showAddDialog = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            when (val result = habitRepository.deleteHabit(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Habit deleted") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Update an existing habit with new values */
    fun updateHabit(
            id: String,
            name: String,
            icon: String,
            color: String,
            frequency: HabitFrequency,
            targetDays: List<Int>,
            goalValue: Int,
            goalUnit: String,
            timeOfDay: TimeOfDay,
            gestureMode: String,
            reminderEnabled: Boolean,
            reminderTime: kotlinx.datetime.LocalTime?,
            startDate: kotlinx.datetime.LocalDate?,
            endDate: kotlinx.datetime.LocalDate?
    ) {
        viewModelScope.launch {
            val existingHabit = _uiState.value.habits.find { it.id == id }
            if (existingHabit == null) {
                _uiState.update { it.copy(error = "Habit not found") }
                return@launch
            }

            val now = Clock.System.now()
            val updatedHabit =
                    existingHabit.copy(
                            name = name,
                            icon = icon,
                            color = color,
                            frequency = frequency,
                            targetDays = targetDays,
                            goalValue = goalValue,
                            goalUnit = goalUnit,
                            timeOfDay = timeOfDay,
                            gestureMode = gestureMode,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            startDate = startDate,
                            endDate = endDate,
                            updatedAt = now
                    )

            when (val result = habitRepository.updateHabit(updatedHabit)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Habit updated! ✅") }
                    loadHabits()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    /** Skip a habit for today (marks it as completed but with "skipped" status) */
    fun skipHabitToday(habitId: String) {
        viewModelScope.launch {
            // Skip = mark as completed but we'll track it separately if needed
            when (val result = habitRepository.completeHabit(habitId, selectedDate, null)) {
                is Result.Success -> {
                    _uiState.update { it.copy(successMessage = "Habit skipped for today") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
            loadDailySummary()
        }
    }

    /** Set a habit for editing */
    fun setEditingHabit(habit: Habit?) {
        _uiState.update { it.copy(editingHabit = habit) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString() +
                (1000..9999).random().toString()
    }
}

/** UI state for Habit screen. */
data class HabitUiState(
        val isLoading: Boolean = true,
        val habits: List<Habit> = emptyList(), // Habits for selected date (filtered)
        val allHabits: List<Habit> = emptyList(), // All active habits (for reporting)
        val selectedDate: kotlinx.datetime.LocalDate =
                kotlinx.datetime.Clock.System.todayIn(
                        kotlinx.datetime.TimeZone.currentSystemDefault()
                ),
        val completedHabitIds: Set<String> = emptySet(),
        val dailySummary: DailyHabitSummary? = null,
        val showAddDialog: Boolean = false,
        val editingHabit: Habit? = null,
        val error: String? = null,
        val successMessage: String? = null
) {
    val completedCount: Int
        get() = completedHabitIds.size
    val totalCount: Int
        get() = habits.size

    val completionPercentage: Float
        get() = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val currentStreak: Int
        get() = habits.maxOfOrNull { it.currentStreak } ?: 0

    val totalXpToday: Int
        get() = habits.filter { completedHabitIds.contains(it.id) }.sumOf { it.xpReward }
}
