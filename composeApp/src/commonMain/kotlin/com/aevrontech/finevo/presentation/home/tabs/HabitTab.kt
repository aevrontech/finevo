package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitSubCategory
import com.aevrontech.finevo.presentation.habit.AddHabitScreen
import com.aevrontech.finevo.presentation.habit.HabitCategorySelectionScreen
import com.aevrontech.finevo.presentation.habit.HabitReportScreen
import com.aevrontech.finevo.presentation.habit.HabitTabScreen
import com.aevrontech.finevo.presentation.habit.HabitViewModel
import com.aevrontech.finevo.presentation.home.LocalSetNavBarVisible
import org.koin.compose.viewmodel.koinViewModel

object HabitTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 3u,
                title = "Habits",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Check
                    )
            )

    @Composable
    override fun Content() {
        HabitTabContent()
    }
}

@Composable
private fun HabitTabContent() {
    val habitViewModel: HabitViewModel = koinViewModel()

    // Nav bar visibility control
    val setNavBarVisible = LocalSetNavBarVisible.current

    // Overlay states - managed locally in this tab
    var showCategorySelection by remember { mutableStateOf(false) }
    var showAddHabit by remember { mutableStateOf(false) }
    var selectedSubCategory by remember { mutableStateOf<HabitSubCategory?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var showReport by remember { mutableStateOf(false) }

    // Update nav bar visibility when any overlay is shown
    val isOverlayVisible =
        showCategorySelection || showAddHabit || habitToEdit != null || showReport

    LaunchedEffect(isOverlayVisible) { setNavBarVisible?.invoke(!isOverlayVisible) }

    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        // Main content - HabitTabScreen with callbacks
        HabitTabScreen(
            onAddHabitClick = { showCategorySelection = true },
            onEditHabitClick = { habit -> habitToEdit = habit },
            onReportClick = { showReport = true }
        )

        // ========== OVERLAYS ==========

        // Category Selection Overlay
        AnimatedVisibility(
            visible = showCategorySelection,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            HabitCategorySelectionScreen(
                onDismiss = { showCategorySelection = false },
                onSubCategorySelected = { subCategory ->
                    selectedSubCategory = subCategory
                    showCategorySelection = false
                    showAddHabit = true
                }
            )
        }

        // Add Habit Overlay
        AnimatedVisibility(
            visible = showAddHabit,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            AddHabitScreen(
                selectedSubCategory = selectedSubCategory,
                onDismiss = {
                    showAddHabit = false
                    selectedSubCategory = null
                },
                onSave = { name,
                           icon,
                           color,
                           frequency,
                           targetDays,
                           goalValue,
                           goalUnit,
                           timeOfDay,
                           gestureMode,
                           reminderEnabled,
                           reminderTime,
                           startDate,
                           endDate ->
                    habitViewModel.createHabit(
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
                        subCategory = selectedSubCategory?.name
                    )
                    showAddHabit = false
                    selectedSubCategory = null
                }
            )
        }

        // Edit Habit Overlay
        AnimatedVisibility(
            visible = habitToEdit != null,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            habitToEdit?.let { habit ->
                AddHabitScreen(
                    selectedSubCategory = null,
                    habitToEdit = habit,
                    onDismiss = { habitToEdit = null },
                    onSave = { name,
                               icon,
                               color,
                               frequency,
                               targetDays,
                               goalValue,
                               goalUnit,
                               timeOfDay,
                               gestureMode,
                               reminderEnabled,
                               reminderTime,
                               startDate,
                               endDate ->
                        habitViewModel.updateHabit(
                            id = habit.id,
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
                            endDate = endDate
                        )
                        habitToEdit = null
                    }
                )
            }
        }

        // Report Overlay
        AnimatedVisibility(
            visible = showReport,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) { HabitReportScreen(onDismiss = { showReport = false }) }
    }
}
