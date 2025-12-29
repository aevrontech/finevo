package com.aevrontech.finevo.presentation.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.ui.theme.HabitGradientEnd
import com.aevrontech.finevo.ui.theme.HabitGradientStart
import com.aevrontech.finevo.ui.theme.Primary
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/** Main Habit Tab screen with redesigned UI. */
@Composable
fun HabitTabScreen(
    modifier: Modifier = Modifier,
    onAddHabitClick: () -> Unit = {},
    onEditHabitClick: (Habit) -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val viewModel: HabitViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var selectedDate by remember {
        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var previousCompletionCount by remember { mutableStateOf(0) }

    // Input value dialog state for INPUT_VALUE gesture mode
    var habitForValueInput by remember { mutableStateOf<Habit?>(null) }
    var inputValue by remember { mutableStateOf("") }

    // Check for all habits completed
    LaunchedEffect(uiState.completedCount, uiState.totalCount) {
        if (uiState.totalCount > 0 &&
            uiState.completedCount == uiState.totalCount &&
            previousCompletionCount < uiState.totalCount
        ) {
            showCompletionDialog = true
        }
        previousCompletionCount = uiState.completedCount
    }

    // Add habit via callback to parent (HomeScreen handles the overlay)

    // Show completion celebration dialog
    if (showCompletionDialog) {
        HabitCompletionDialog(
            totalXpEarned = uiState.totalXpToday,
            currentStreak = uiState.currentStreak,
            onDismiss = { showCompletionDialog = false }
        )
    }

    // Input Value Dialog for INPUT_VALUE gesture mode
    habitForValueInput?.let { habit ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { habitForValueInput = null }
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(habit.icon, fontSize = 48.sp)
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "How much did you complete?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = inputValue,
                            onValueChange = {
                                inputValue =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions =
                                androidx.compose.foundation.text
                                    .KeyboardOptions(
                                        keyboardType =
                                            androidx.compose
                                                .ui
                                                .text
                                                .input
                                                .KeyboardType
                                                .Number
                                    ),
                            singleLine = true,
                            textStyle =
                                androidx.compose.ui.text.TextStyle(
                                    textAlign =
                                        androidx.compose.ui
                                            .text.style
                                            .TextAlign
                                            .Center,
                                    fontSize = 24.sp
                                )
                        )
                        Text(
                            habit.goalUnit,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        "Goal: ${habit.goalValue} ${habit.goalUnit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { habitForValueInput = null },
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancel") }
                        Button(
                            onClick = {
                                val value =
                                    inputValue.toIntOrNull()
                                        ?: 0
                                if (value > 0) {
                                    viewModel
                                        .toggleHabitWithValue(
                                            habit.id,
                                            value
                                        )
                                }
                                habitForValueInput = null
                                inputValue = ""
                            },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Primary
                                )
                        ) { Text("Save") }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with greeting
            item {
                Column(
                    modifier =
                        Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hello,",
                                style =
                                    MaterialTheme.typography
                                        .labelMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                            Text(
                                text = getGreeting(),
                                style =
                                    MaterialTheme.typography
                                        .headlineSmall, // Was 24.sp
                                fontWeight = FontWeight.Bold,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                        }
                        // Streak badge
                        if (uiState.currentStreak > 0) {
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            Primary.copy(
                                                alpha =
                                                    0.1f
                                            )
                                    ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                12.dp,
                                            vertical =
                                                6.dp
                                        ),
                                    verticalAlignment =
                                        Alignment
                                            .CenterVertically,
                                    horizontalArrangement =
                                        Arrangement
                                            .spacedBy(
                                                4.dp
                                            )
                                ) {
                                    Text(
                                        "🔥",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium
                                    )
                                    Text(
                                        text =
                                            "${uiState.currentStreak}",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium,
                                        fontWeight =
                                            FontWeight
                                                .Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }

                        // Report icon button
                        IconButton(
                            onClick = onReportClick,
                            modifier =
                                Modifier.size(40.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Primary.copy(
                                            alpha = 0.1f
                                        )
                                    )
                        ) { Text("📊", fontSize = 20.sp) }
                    }
                }
            }

            // Date Selector
            item {
                DateSelector(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { date -> viewModel.setSelectedDate(date) }
                )
            }

            // Progress Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HabitProgressCard(
                    completedCount = uiState.completedCount,
                    totalCount = uiState.totalCount
                )
            }

            // Today Habits Section Header
            item {
                Text(
                    text = "Today Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                )
            }

            // Habit List
            if (uiState.habits.isEmpty() && !uiState.isLoading) {
                item { EmptyHabitsCard(onAddClick = { viewModel.showAddDialog() }) }
            } else {
                items(uiState.habits) { habit ->
                    val isCompleted =
                        uiState.completedHabitIds.contains(habit.id)
                    SwipeableHabitItem(
                        habit = habit,
                        isCompleted = isCompleted,
                        onToggle = {
                            if (habit.gestureMode == "INPUT_VALUE" &&
                                !isCompleted
                            ) {
                                // Show input dialog for INPUT_VALUE
                                // mode
                                habitForValueInput = habit
                                inputValue = ""
                            } else {
                                // Simple toggle for MARK_AS_DONE
                                // mode
                                viewModel.toggleHabit(habit.id)
                            }
                        },
                        onSkip = { viewModel.skipHabitToday(habit.id) },
                        onEdit = { onEditHabitClick(habit) },
                        onDelete = { viewModel.deleteHabit(habit.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Gradient FAB matching Expense tab
        Box(
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = 130.dp, end = 20.dp)
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor =
                            HabitGradientStart.copy(alpha = 0.5f),
                        spotColor = HabitGradientStart.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        HabitGradientStart,
                                        HabitGradientEnd
                                    )
                            )
                    )
                    .clickable { onAddHabitClick() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Habit",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyHabitsCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎯", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start building good habits today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Create Your First Habit") }
        }
    }
}

private fun getGreeting(): String {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour
    return when {
        hour < 12 -> "Good Morning! ☀️"
        hour < 17 -> "Good Afternoon! 🌤️"
        else -> "Good Evening! 🌙"
    }
}

/** Swipeable habit item with Skip Today, Edit, and Delete actions */
@Composable
private fun SwipeableHabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onSkip: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val actionWidth = 225.dp // 3 buttons x 75dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }

    Box(modifier = modifier) {
        // Background action buttons (revealed on swipe)
        Row(
            modifier = Modifier.matchParentSize().clip(RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.End
        ) {
            // Skip Today button
            Box(
                modifier =
                    Modifier.width(75.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFF9800))
                        .clickable {
                            onSkip()
                            offsetX = 0f
                        },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⏭️", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Skip",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            // Edit button
            Box(
                modifier =
                    Modifier.width(75.dp)
                        .fillMaxHeight()
                        .background(Primary)
                        .clickable {
                            onEdit()
                            offsetX = 0f
                        },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✏️", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Edit",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            // Delete button
            Box(
                modifier =
                    Modifier.width(75.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE53935))
                        .clickable {
                            onDelete()
                            offsetX = 0f
                        },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🗑️", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Delete",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Main card content with swipe gesture
        Box(
            modifier =
                Modifier.offset { IntOffset(offsetX.roundToInt(), 0) }.pointerInput(
                    Unit
                ) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Snap to reveal actions or close
                            offsetX =
                                if (offsetX < -actionWidthPx / 2) {
                                    -actionWidthPx
                                } else {
                                    0f
                                }
                        }
                    ) { _, dragAmount ->
                        val newOffset =
                            (offsetX + dragAmount).coerceIn(
                                -actionWidthPx,
                                0f
                            )
                        offsetX = newOffset
                    }
                }
        ) {
            HabitItemCard(
                habit = habit,
                isCompleted = isCompleted,
                progress = if (isCompleted) 1f else 0f,
                onClick = onToggle
            )
        }
    }
}
