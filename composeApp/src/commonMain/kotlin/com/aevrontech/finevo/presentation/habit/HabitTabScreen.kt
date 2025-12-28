package com.aevrontech.finevo.presentation.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.presentation.components.AddHabitDialog
import com.aevrontech.finevo.ui.theme.Primary
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel

/** Main Habit Tab screen with redesigned UI. */
@Composable
fun HabitTabScreen(modifier: Modifier = Modifier) {
        val viewModel: HabitViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        var selectedDate by remember {
                mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
        }
        var showCompletionDialog by remember { mutableStateOf(false) }
        var previousCompletionCount by remember { mutableStateOf(0) }

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

        // Show add habit dialog
        if (uiState.showAddDialog) {
                AddHabitDialog(
                        onDismiss = { viewModel.hideAddDialog() },
                        onConfirm = { name, icon, color, frequency, xpReward ->
                                viewModel.addHabit(name, icon, color, frequency, xpReward)
                        }
                )
        }

        // Show completion celebration dialog
        if (showCompletionDialog) {
                HabitCompletionDialog(
                        totalXpEarned = uiState.totalXpToday,
                        currentStreak = uiState.currentStreak,
                        onDismiss = { showCompletionDialog = false }
                )
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
                                        }
                                }
                        }

                        // Date Selector
                        item {
                                DateSelector(
                                        selectedDate = selectedDate,
                                        onDateSelected = { date ->
                                                selectedDate = date
                                                // TODO: Load habits for selected date
                                        }
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
                                        HabitItemCard(
                                                habit = habit,
                                                isCompleted = isCompleted,
                                                progress = if (isCompleted) 1f else 0f,
                                                onClick = { viewModel.toggleHabit(habit.id) },
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                }
                        }
                }

                // Floating Action Button
                androidx.compose.material3.FloatingActionButton(
                        onClick = { viewModel.showAddDialog() },
                        containerColor = Primary,
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        modifier =
                                Modifier.align(Alignment.BottomEnd)
                                        .padding(bottom = 100.dp, end = 20.dp)
                                        .size(56.dp),
                        shape = RoundedCornerShape(16.dp)
                ) { Icon(imageVector = Icons.Default.Add, contentDescription = "New Habit") }
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
