package com.aevrontech.finevo.presentation.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aevrontech.finevo.core.util.Result
import com.aevrontech.finevo.domain.model.DailyHabitSummary
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitFrequency
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

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    init {
        loadHabits()
        loadTodayLogs()
        loadDailySummary()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getActiveHabits().collect { habits ->
                _uiState.update { it.copy(habits = habits, isLoading = false) }
            }
        }
    }

    private fun loadTodayLogs() {
        viewModelScope.launch {
            habitRepository.getLogsForDate(today).collect { logs ->
                val completedIds = logs.filter { !it.skipped }.map { it.habitId }.toSet()
                _uiState.update { it.copy(completedHabitIds = completedIds) }
            }
        }
    }

    private fun loadDailySummary() {
        viewModelScope.launch {
            when (val result = habitRepository.getDailySummary(today)) {
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
                when (val result = habitRepository.uncompleteHabit(habitId, today)) {
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
                when (val result = habitRepository.completeHabit(habitId, today, null)) {
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
    val habits: List<Habit> = emptyList(),
    val completedHabitIds: Set<String> = emptySet(),
    val dailySummary: DailyHabitSummary? = null,
    val showAddDialog: Boolean = false,
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
