package com.aevrontech.finevo.presentation.habit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.UserTier
import com.aevrontech.finevo.ui.theme.*
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

/** Report period type */
enum class ReportPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY
}

/** Habit Report Screen - Premium feature showing habit completion statistics */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitReportScreen(
        userTier: UserTier = UserTier.PREMIUM, // TODO: Get from actual user state
        onDismiss: () -> Unit
) {
    val viewModel: HabitViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val isPremium = userTier in listOf(UserTier.PREMIUM, UserTier.FAMILY, UserTier.FAMILY_MEMBER)

    // Report state
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.WEEKLY) }
    var currentDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    BackHandler { onDismiss() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                    title = { Text("Habit Report", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    colors =
                            TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                            )
            )

            if (!isPremium) {
                // Premium upgrade prompt
                PremiumUpgradePrompt(onDismiss = onDismiss)
            } else {
                // Period Tabs
                PeriodTabs(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Header decoration
                ReportHeader()

                // Period Navigator
                PeriodNavigator(
                        period = selectedPeriod,
                        currentDate = currentDate,
                        onPreviousPeriod = {
                            currentDate = getPreviousPeriodDate(currentDate, selectedPeriod)
                        },
                        onNextPeriod = {
                            currentDate = getNextPeriodDate(currentDate, selectedPeriod)
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on period
                when (selectedPeriod) {
                    ReportPeriod.WEEKLY ->
                            WeeklyReportView(
                                    habits = uiState.habits,
                                    currentDate = currentDate,
                                    completedHabitIds = uiState.completedHabitIds,
                                    modifier = Modifier.weight(1f)
                            )
                    ReportPeriod.MONTHLY ->
                            MonthlyReportView(
                                    habits = uiState.habits,
                                    currentDate = currentDate,
                                    modifier = Modifier.weight(1f)
                            )
                    ReportPeriod.YEARLY ->
                            YearlyReportView(
                                    habits = uiState.habits,
                                    currentDate = currentDate,
                                    modifier = Modifier.weight(1f)
                            )
                }

                // Summary Statistics
                SummaryStatistics(
                        habits = uiState.habits,
                        completedCount = uiState.completedHabitIds.size,
                        totalCount = uiState.habits.size
                )
            }
        }
    }
}

@Composable
private fun PremiumUpgradePrompt(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock icon with gradient background
                Box(
                        modifier =
                                Modifier.size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                                Brush.linearGradient(
                                                        colors =
                                                                listOf(
                                                                        PremiumGradientStart,
                                                                        PremiumGradientMid,
                                                                        PremiumGradientEnd
                                                                )
                                                )
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Premium",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                        text = "Premium Feature",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text =
                                "Habit Reports help you track your progress over time. Upgrade to Premium to unlock detailed weekly, monthly, and yearly insights.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                        onClick = { /* TODO: Navigate to premium purchase */},
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Upgrade to Premium", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PeriodTabs(selectedPeriod: ReportPeriod, onPeriodSelected: (ReportPeriod) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val backgroundColor by
                    animateColorAsState(
                            targetValue = if (isSelected) Primary else Color.Transparent,
                            label = "tabColor"
                    )
            val textColor by
                    animateColorAsState(
                            targetValue =
                                    if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "textColor"
                    )

            Box(
                    modifier =
                            Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(backgroundColor)
                                    .clickable { onPeriodSelected(period) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                        text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Filter button (placeholder)
        Button(
                onClick = { /* TODO: Filter dialog */},
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) { Text("Filter", fontSize = 14.sp) }
    }
}

@Composable
private fun ReportHeader() {
    Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
        ) {
            Text("☀️", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text = "Habit Tracker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B9D) // Pink color like reference
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🌙", fontSize = 24.sp)
        }
    }
}

@Composable
private fun PeriodNavigator(
        period: ReportPeriod,
        currentDate: LocalDate,
        onPreviousPeriod: () -> Unit,
        onNextPeriod: () -> Unit
) {
    val periodText =
            when (period) {
                ReportPeriod.WEEKLY -> {
                    val startOfWeek = getStartOfWeek(currentDate)
                    val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                    "${startOfWeek.dayOfMonth}/${startOfWeek.monthNumber}~${endOfWeek.dayOfMonth}/${endOfWeek.monthNumber}"
                }
                ReportPeriod.MONTHLY -> {
                    "${currentDate.year} ${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)}"
                }
                ReportPeriod.YEARLY -> {
                    "${currentDate.year}"
                }
            }

    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousPeriod) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = Primary)
        }

        Box(
                modifier =
                        Modifier.clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFF9C4)) // Light yellow background
                                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) { Text(text = periodText, fontWeight = FontWeight.Medium, fontSize = 16.sp) }

        IconButton(onClick = onNextPeriod) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Primary)
        }
    }
}

@Composable
private fun WeeklyReportView(
        habits: List<Habit>,
        currentDate: LocalDate,
        completedHabitIds: Set<String>,
        modifier: Modifier = Modifier
) {
    val startOfWeek = getStartOfWeek(currentDate)
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    LazyColumn(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Header row with day letters
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Spacer(modifier = Modifier.width(100.dp)) // Space for habit name
                daysOfWeek.forEach { day ->
                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                        Text(
                                text = day,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Habit rows
        items(habits) { habit ->
            WeeklyHabitRow(
                    habit = habit,
                    startOfWeek = startOfWeek,
                    isCompletedToday = completedHabitIds.contains(habit.id)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Best day indicator
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Box(
                        modifier =
                                Modifier.clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text(text = "BestDay", fontSize = 12.sp, color = Primary) }
                Spacer(modifier = Modifier.weight(1f))
                Text("🏆", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun WeeklyHabitRow(habit: Habit, startOfWeek: LocalDate, isCompletedToday: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Habit name with icon
        Row(modifier = Modifier.width(100.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(habit.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = habit.name, fontSize = 12.sp, maxLines = 1)
        }

        // 7 day cells
        repeat(7) { dayIndex ->
            val dayDate = startOfWeek.plus(DatePeriod(days = dayIndex))
            val isToday =
                    dayDate ==
                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val isCompleted = isToday && isCompletedToday

            // Get habit color or default
            val habitColor =
                    try {
                        Color(android.graphics.Color.parseColor(habit.color))
                    } catch (e: Exception) {
                        Primary
                    }

            Box(
                    modifier =
                            Modifier.size(36.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                            if (isCompleted) habitColor.copy(alpha = 0.7f)
                                            else Color.Gray.copy(alpha = 0.1f)
                                    )
                                    .then(
                                            if (isCompleted)
                                                    Modifier.border(
                                                            2.dp,
                                                            habitColor,
                                                            RoundedCornerShape(6.dp)
                                                    )
                                            else Modifier
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                if (!isCompleted) {
                    Text(text = "○", color = Color.Gray.copy(alpha = 0.3f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun MonthlyReportView(
        habits: List<Habit>,
        currentDate: LocalDate,
        modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { items(habits) { habit -> MonthlyHabitCard(habit = habit, currentDate = currentDate) } }
}

@Composable
private fun MonthlyHabitCard(habit: Habit, currentDate: LocalDate) {
    val habitColor =
            try {
                Color(android.graphics.Color.parseColor(habit.color))
            } catch (e: Exception) {
                Primary
            }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Habit name with icon
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(habitColor.copy(alpha = 0.1f))
                                    .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
            ) {
                Text(habit.icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = habit.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini calendar grid (simplified)
            MiniCalendarGrid(currentDate = currentDate, habitColor = habitColor)

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                            "${(1..5).random()}.${(0..99).random()}%",
                            fontSize = 11.sp,
                            color = Primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${(0..7).random()}d", fontSize = 11.sp, color = Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarGrid(currentDate: LocalDate, habitColor: Color) {
    val daysInMonth = currentDate.month.length(isLeapYear(currentDate.year))
    val firstDayOfMonth = LocalDate(currentDate.year, currentDate.month, 1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal

    Column {
        // Day headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(text = day, fontSize = 8.sp, color = Color.Gray)
            }
        }

        // Calendar cells (simplified)
        var dayCounter = 1
        repeat(6) { week ->
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayIndex ->
                    if ((week == 0 && dayIndex < startDayOfWeek) || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.size(14.dp))
                    } else {
                        val isCompleted = (0..1).random() == 1 // Placeholder
                        Box(
                                modifier =
                                        Modifier.size(14.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                        if (isCompleted)
                                                                habitColor.copy(alpha = 0.6f)
                                                        else Color.Gray.copy(alpha = 0.1f)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = dayCounter.toString(),
                                    fontSize = 6.sp,
                                    color = if (isCompleted) Color.White else Color.Gray
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyReportView(
        habits: List<Habit>,
        currentDate: LocalDate,
        modifier: Modifier = Modifier
) {
    LazyColumn(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(habits) { habit -> YearlyHabitRow(habit = habit, year = currentDate.year) } }
}

@Composable
private fun YearlyHabitRow(habit: Habit, year: Int) {
    val habitColor =
            try {
                Color(android.graphics.Color.parseColor(habit.color))
            } catch (e: Exception) {
                Primary
            }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Habit name and stats
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(habit.name, fontWeight = FontWeight.Medium)
            }

            Row {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 14.sp)
                    Text(
                            "${(0..1).random()}.${(0..99).random().toString().padStart(2, '0')}%",
                            fontSize = 12.sp,
                            color = Primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 14.sp)
                    Text("${(0..5).random()}d", fontSize = 12.sp, color = Color(0xFFFF9800))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Year grid (12 months x days)
        YearHeatmapGrid(habitColor = habitColor)
    }
}

@Composable
private fun YearHeatmapGrid(habitColor: Color) {
    Column {
        repeat(12) { month ->
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(31) { day ->
                    val isCompleted = (0..3).random() == 0 // ~25% completion placeholder
                    Box(
                            modifier =
                                    Modifier.size(8.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(
                                                    if (isCompleted) habitColor.copy(alpha = 0.7f)
                                                    else Color.Gray.copy(alpha = 0.1f)
                                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatistics(habits: List<Habit>, completedCount: Int, totalCount: Int) {
    val completionPercentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "${completionPercentage}%", label = "Met", valueColor = Primary)
            StatItem(
                    value = "${(1..7).random()}d",
                    label = "BestDay",
                    valueColor = MaterialTheme.colorScheme.onSurface
            )
            StatItem(value = "$completedCount", label = "TotalDone", valueColor = Primary)
            StatItem(
                    value = "${(0..10).random()}d",
                    label = "BestStreak",
                    valueColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Helper functions
private fun getStartOfWeek(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.ordinal // Monday = 0
    return date.minus(DatePeriod(days = dayOfWeek))
}

private fun getPreviousPeriodDate(date: LocalDate, period: ReportPeriod): LocalDate {
    return when (period) {
        ReportPeriod.WEEKLY -> date.minus(DatePeriod(days = 7))
        ReportPeriod.MONTHLY -> date.minus(DatePeriod(months = 1))
        ReportPeriod.YEARLY -> date.minus(DatePeriod(years = 1))
    }
}

private fun getNextPeriodDate(date: LocalDate, period: ReportPeriod): LocalDate {
    return when (period) {
        ReportPeriod.WEEKLY -> date.plus(DatePeriod(days = 7))
        ReportPeriod.MONTHLY -> date.plus(DatePeriod(months = 1))
        ReportPeriod.YEARLY -> date.plus(DatePeriod(years = 1))
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
