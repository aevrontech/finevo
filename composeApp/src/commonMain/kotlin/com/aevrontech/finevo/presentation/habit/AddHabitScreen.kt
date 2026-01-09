package com.aevrontech.finevo.presentation.habit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aevrontech.finevo.core.presentation.BackHandler
import com.aevrontech.finevo.core.util.padZero
import com.aevrontech.finevo.domain.model.GoalPeriod
import com.aevrontech.finevo.domain.model.GoalUnit
import com.aevrontech.finevo.domain.model.GoalUnitType
import com.aevrontech.finevo.domain.model.HabitFrequency
import com.aevrontech.finevo.domain.model.HabitGestureMode
import com.aevrontech.finevo.domain.model.HabitSubCategory
import com.aevrontech.finevo.domain.model.HabitTimeRange
import com.aevrontech.finevo.domain.model.TimeOfDay
import com.aevrontech.finevo.presentation.label.LabelColors
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Second screen of Add Habit flow - Habit configuration */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(
    selectedSubCategory: HabitSubCategory?,
    onDismiss: () -> Unit,
    onSave:
        (
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
        reminderTime: LocalTime?,
        startDate: LocalDate?,
        endDate: LocalDate?) -> Unit,
    habitToEdit: com.aevrontech.finevo.domain.model.Habit? = null
) {
    val isEditMode = habitToEdit != null

    // Handle Android back button
    BackHandler(enabled = true) { onDismiss() }

    // Form state - pre-fill with existing habit data if editing
    var habitName by remember {
        mutableStateOf(habitToEdit?.name ?: selectedSubCategory?.displayName ?: "")
    }
    var selectedIcon by remember {
        mutableStateOf(habitToEdit?.icon ?: selectedSubCategory?.icon ?: "💪")
    }
    var selectedColor by remember { mutableStateOf(habitToEdit?.color ?: "#5DADE2") }

    // Goal Period - map from habit frequency
    var goalPeriod by remember {
        mutableStateOf(
            when (habitToEdit?.frequency) {
                HabitFrequency.DAILY -> GoalPeriod.DAILY
                HabitFrequency.WEEKLY -> GoalPeriod.WEEKLY
                HabitFrequency.MONTHLY -> GoalPeriod.MONTHLY
                else -> GoalPeriod.DAILY
            }
        )
    }
    var selectedDays by remember { mutableStateOf(habitToEdit?.targetDays ?: listOf<Int>()) }
    var allDaysSelected by remember {
        mutableStateOf(habitToEdit?.frequency == HabitFrequency.DAILY || habitToEdit == null)
    }

    // Goal Value
    var goalValue by remember {
        mutableStateOf(
            (habitToEdit?.goalValue ?: selectedSubCategory?.defaultGoalValue ?: 1).toString()
        )
    }
    var goalUnit by remember {
        mutableStateOf(habitToEdit?.goalUnit ?: selectedSubCategory?.defaultUnit ?: "count")
    }
    var showUnitPicker by remember { mutableStateOf(false) }

    // Time Range
    var timeRange by remember {
        mutableStateOf(
            when (habitToEdit?.timeOfDay) {
                TimeOfDay.MORNING -> HabitTimeRange.MORNING
                TimeOfDay.AFTERNOON -> HabitTimeRange.AFTERNOON
                TimeOfDay.EVENING -> HabitTimeRange.EVENING
                else -> HabitTimeRange.ANYTIME
            }
        )
    }

    // Reminder
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: false) }
    var reminderHour by remember { mutableStateOf(habitToEdit?.reminderTime?.hour ?: 9) }
    var reminderMinute by remember { mutableStateOf(habitToEdit?.reminderTime?.minute ?: 0) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Gesture Mode
    var gestureMode by remember {
        mutableStateOf(
            when (habitToEdit?.gestureMode) {
                "INPUT_VALUE" -> HabitGestureMode.INPUT_VALUE
                else -> HabitGestureMode.MARK_AS_DONE
            }
        )
    }

    // Habit Term
    var startDate by remember {
        mutableStateOf<LocalDate?>(
            habitToEdit?.startDate
                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    }
    var endDate by remember { mutableStateOf<LocalDate?>(habitToEdit?.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showMonthlyDatePicker by remember { mutableStateOf(false) }

    // Derive frequency from goal period
    val frequency =
        when (goalPeriod) {
            GoalPeriod.DAILY ->
                if (allDaysSelected) HabitFrequency.DAILY else HabitFrequency.SPECIFIC_DAYS
            GoalPeriod.WEEKLY -> HabitFrequency.WEEKLY
            GoalPeriod.MONTHLY -> HabitFrequency.MONTHLY
        }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) { // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Habit" else "Add Habit",
                        fontWeight = FontWeight.Bold
                    )
                },
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

            // Scrollable Content
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) { // Card 1: Habit Name, Icon & Color
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Habit Name")
                        OutlinedTextField(
                            value = habitName,
                            onValueChange = { habitName = it },
                            placeholder = { Text("Enter habit name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Text(selectedIcon, fontSize = 24.sp) }
                        )

                        // Icon Picker
                        Text(
                            "Icon",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val habitIcons =
                            listOf(
                                "💪",
                                "🏃",
                                "🧘",
                                "📚",
                                "💧",
                                "🍎",
                                "😴",
                                "🎯",
                                "✍️",
                                "🎨",
                                "🎵",
                                "🏋️",
                                "🚴",
                                "🧠",
                                "💼",
                                "🌱"
                            )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(habitIcons) { icon ->
                                Box(
                                    modifier =
                                        Modifier.size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (icon == selectedIcon)
                                                    MaterialTheme.colorScheme
                                                        .primary.copy(
                                                            alpha = 0.2f
                                                        )
                                                else Color.Gray.copy(alpha = 0.1f)
                                            )
                                            .border(
                                                width =
                                                    if (icon == selectedIcon)
                                                        2.dp
                                                    else 0.dp,
                                                color =
                                                    if (icon == selectedIcon)
                                                        MaterialTheme
                                                            .colorScheme
                                                            .primary
                                                    else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) { Text(icon, fontSize = 20.sp) }
                            }
                        }

                        // Color Picker
                        Text(
                            "Color",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val habitColors = LabelColors.colors
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            habitColors.forEach { colorHex -> // Use forEach for FlowRow
                                val color = LabelColors.parse(colorHex)
                                Box(
                                    modifier =
                                        Modifier.size(36.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width =
                                                    if (colorHex ==
                                                        selectedColor
                                                    )
                                                        3.dp
                                                    else 0.dp,
                                                color =
                                                    if (colorHex ==
                                                        selectedColor
                                                    )
                                                        Color.White
                                                    else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorHex == selectedColor) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 2: Repeat, Day Selection, Goal Value
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { // Repeat Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionHeader(
                                title = "Repeat",
                                tooltip = "How often do you want to do it"
                            )
                            GoalPeriodSelector(
                                selectedPeriod = goalPeriod,
                                onPeriodSelected = {
                                    goalPeriod = it
                                    selectedDays = emptyList()
                                    allDaysSelected = it == GoalPeriod.DAILY
                                }
                            )
                        }

                        // Day Selection based on period
                        when (goalPeriod) {
                            GoalPeriod.DAILY -> { // No additional
                                // selection needed
                                // for daily
                                Text(
                                    "Repeats every day",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            GoalPeriod.WEEKLY -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Select All Days", fontSize = 14.sp)
                                        Switch(
                                            checked = selectedDays.size == 7,
                                            onCheckedChange = { selectAll ->
                                                selectedDays =
                                                    if (selectAll) {
                                                        listOf(1, 2, 3, 4, 5, 6, 7)
                                                    } else {
                                                        emptyList()
                                                    }
                                            },
                                            colors =
                                                SwitchDefaults.colors(
                                                    checkedTrackColor =
                                                        DashboardGradientMid
                                                )
                                        )
                                    }
                                    WeekDaySelector(
                                        selectedDays = selectedDays,
                                        onDaysChanged = { selectedDays = it }
                                    )
                                }
                            }
                            GoalPeriod.MONTHLY -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Select Dates",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            if (selectedDays.isEmpty()) "No dates selected"
                                            else "${selectedDays.size} dates selected",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { showMonthlyDatePicker = true }) {
                                        Text("Choose", color = DashboardGradientMid)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Goal Value Section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionHeader("Goal Value")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = goalValue,
                                    onValueChange = {
                                        goalValue = it.filter { c -> c.isDigit() }
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedButton(
                                    onClick = { showUnitPicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text(goalUnit) }

                                Text(
                                    "/ ${goalPeriod.displayName.lowercase()}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Card 3: Time Range
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Time Range")
                        TimeRangeSelector(
                            selectedRange = timeRange,
                            onRangeSelected = { timeRange = it }
                        )
                    }
                }

                // Card 4: Reminder
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Reminder")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remind at specific time", fontSize = 14.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (reminderEnabled) {
                                    TextButton(onClick = { showTimePicker = true }) {
                                        val hour12 =
                                            if (reminderHour == 0) 12
                                            else if (reminderHour > 12) reminderHour - 12
                                            else reminderHour
                                        val amPm = if (reminderHour < 12) "AM" else "PM"
                                        Text(
                                            "${hour12}:${reminderMinute.padZero(2)} $amPm",
                                            color = DashboardGradientMid
                                        )
                                    }
                                }
                                Switch(
                                    checked = reminderEnabled,
                                    onCheckedChange = { reminderEnabled = it },
                                    colors =
                                        SwitchDefaults.colors(
                                            checkedTrackColor = DashboardGradientMid
                                        )
                                )
                            }
                        }
                    }
                }

                // Card 5: HabitBar Gesture
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "HabitBar Gesture",
                            tooltip = "How do you want to evaluate your progress"
                        )
                        GestureModeSelector(
                            selectedMode = gestureMode,
                            onModeSelected = { gestureMode = it }
                        )
                    }
                }

                // Card 6: Habit Term
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Habit Term")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Start Date",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showStartDatePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text(startDate?.toString() ?: "Select") }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "End Date",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showEndDatePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text(endDate?.toString() ?: "Ongoing") }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Save Button
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                Button(
                    onClick = {
                        val value = goalValue.toIntOrNull() ?: 1
                        val reminderTimeValue =
                            if (reminderEnabled) {
                                LocalTime(reminderHour, reminderMinute)
                            } else null

                        onSave(
                            habitName,
                            selectedIcon,
                            selectedColor,
                            frequency,
                            selectedDays,
                            value,
                            goalUnit,
                            timeRange.toTimeOfDay(),
                            gestureMode.name,
                            reminderEnabled,
                            reminderTimeValue,
                            startDate,
                            endDate
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor =
                                    DashboardGradientStart.copy(alpha = 0.4f),
                                spotColor =
                                    DashboardGradientStart.copy(alpha = 0.4f)
                            ),
                    enabled = habitName.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(
                                    brush =
                                        Brush.horizontalGradient(
                                            listOf(
                                                DashboardGradientStart,
                                                DashboardGradientMid,
                                                DashboardGradientEnd
                                            )
                                        )
                                ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isEditMode) "Save Changes" else "Add Habit",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Unit Picker Dialog
    if (showUnitPicker) {
        UnitPickerDialog(
            currentUnit = goalUnit,
            onUnitSelected = { goalUnit = it },
            onDismiss = { showUnitPicker = false }
        )
    }

    // Monthly Date Picker Dialog
    if (showMonthlyDatePicker) {
        MonthlyDatePickerDialog(
            selectedDates = selectedDays,
            onDatesSelected = { selectedDays = it },
            onDismiss = { showMonthlyDatePicker = false }
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            hour = reminderHour,
            minute = reminderMinute,
            onTimeSelected = { h, m ->
                reminderHour = h
                reminderMinute = m
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Date Picker Dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            selectedDate = startDate,
            onDateSelected = { startDate = it },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            selectedDate = endDate,
            onDateSelected = { selectedEndDate
                -> // Validate: end date cannot be before start date
                if (startDate != null &&
                    selectedEndDate != null &&
                    selectedEndDate < startDate!!
                ) { // Invalid - end date is before start date, don't set it
                    // You could show a toast/message here
                } else {
                    endDate = selectedEndDate
                }
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String, tooltip: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        tooltip?.let {
            var showTooltip by remember { mutableStateOf(false) }
            Icon(
                Icons.Filled.Info,
                contentDescription = "Info",
                modifier = Modifier.size(16.dp).clickable { showTooltip = !showTooltip },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showTooltip) { // Simple tooltip display
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) { Box(modifier = Modifier.padding(16.dp)) { content() } }
}

@Composable
private fun GoalPeriodSelector(selectedPeriod: GoalPeriod, onPeriodSelected: (GoalPeriod) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GoalPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val backgroundColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid else Color.Transparent,
                label = "periodBg"
            )
            val borderColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid
                else MaterialTheme.colorScheme.outline,
                label = "periodBorder"
            )

            Box(
                modifier =
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WeekDaySelector(selectedDays: List<Int>, onDaysChanged: (List<Int>) -> Unit) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        dayLabels.forEachIndexed { index, label ->
            val dayNumber = index + 1 // 1 = Sunday, 7 = Saturday
            val isSelected = dayNumber in selectedDays
            val backgroundColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid else Color.Transparent,
                label = "dayBg"
            )
            val borderColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid
                else MaterialTheme.colorScheme.outline,
                label = "dayBorder"
            )

            Box(
                modifier =
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, borderColor, CircleShape)
                        .background(backgroundColor)
                        .clickable {
                            onDaysChanged(
                                if (isSelected) selectedDays - dayNumber
                                else selectedDays + dayNumber
                            )
                        },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: HabitTimeRange,
    onRangeSelected: (HabitTimeRange) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(HabitTimeRange.entries.toList()) { range ->
            val isSelected = range == selectedRange
            val backgroundColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid else Color.Transparent,
                label = "rangeBg"
            )

            Box(
                modifier =
                    Modifier.clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (isSelected) DashboardGradientMid
                            else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(16.dp)
                        )
                        .background(backgroundColor)
                        .clickable { onRangeSelected(range) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = range.displayName,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GestureModeSelector(
    selectedMode: HabitGestureMode,
    onModeSelected: (HabitGestureMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitGestureMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val borderColor by
            animateColorAsState(
                if (isSelected) DashboardGradientMid
                else MaterialTheme.colorScheme.outline,
                label = "modeBorder"
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onModeSelected(mode) }
                        .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = mode.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = mode.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onModeSelected(mode) },
                    colors = RadioButtonDefaults.colors(selectedColor = DashboardGradientMid)
                )
            }
        }
    }
}

@Composable
private fun UnitPickerDialog(
    currentUnit: String,
    onUnitSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(GoalUnitType.QUANTITY) }
    var customUnit by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Select Unit", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Tab selector
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GoalUnitType.entries.forEach { type ->
                        val isSelected = type == selectedTab
                        Box(
                            modifier =
                                Modifier.weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color.White
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTab = type }
                                    .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                type.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight =
                                    if (isSelected) FontWeight.SemiBold
                                    else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unit grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GoalUnit.getByType(selectedTab)) { unit ->
                        val isSelected = unit.symbol == currentUnit
                        Box(
                            modifier =
                                Modifier.clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) DashboardGradientMid
                                        else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isSelected)
                                            DashboardGradientMid.copy(
                                                alpha = 0.1f
                                            )
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onUnitSelected(unit.symbol)
                                        onDismiss()
                                    }
                                    .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                unit.symbol,
                                fontWeight =
                                    if (isSelected) FontWeight.SemiBold
                                    else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom unit input
                Text(
                    "Custom Unit",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customUnit,
                        onValueChange = { customUnit = it },
                        placeholder = { Text("e.g., cups") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = {
                            if (customUnit.isNotBlank()) {
                                onUnitSelected(customUnit)
                                onDismiss()
                            }
                        },
                        enabled = customUnit.isNotBlank(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DashboardGradientMid
                            )
                    ) { Text("Add") }
                }
            }
        }
    }
}

@Composable
private fun MonthlyDatePickerDialog(
    selectedDates: List<Int>,
    onDatesSelected: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedDates) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Select Dates", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Choose which dates of the month",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date grid (1-31)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(260.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items((1..31).toList()) { day ->
                        val isSelected = day in tempSelected
                        Box(
                            modifier =
                                Modifier.aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) DashboardGradientMid
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) DashboardGradientMid
                                        else
                                            MaterialTheme.colorScheme.outline
                                                .copy(alpha = 0.5f),
                                        CircleShape
                                    )
                                    .clickable {
                                        tempSelected =
                                            if (isSelected) tempSelected - day
                                            else tempSelected + day
                                    },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 12.sp,
                                color =
                                    if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onDatesSelected(tempSelected.sorted())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DashboardGradientMid
                            )
                    ) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempHour by remember { mutableStateOf(hour) }
    var tempMinute by remember { mutableStateOf(minute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Set Reminder Time", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // Simple time display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) { // Hour
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { tempHour = (tempHour + 1) % 24 }) {
                            Text("▲", fontSize = 18.sp)
                        }
                        Text(tempHour.padZero(2), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { tempHour = if (tempHour == 0) 23 else tempHour - 1 }
                        ) { Text("▼", fontSize = 18.sp) }
                    }

                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold)

                    // Minute
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { tempMinute = (tempMinute + 5) % 60 }) {
                            Text("▲", fontSize = 18.sp)
                        }
                        Text(tempMinute.padZero(2), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                tempMinute = if (tempMinute < 5) 55 else tempMinute - 5
                            }
                        ) { Text("▼", fontSize = 18.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onTimeSelected(tempHour, tempMinute)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DashboardGradientMid
                            )
                    ) { Text("Set") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                selectedDate?.let { it.toEpochDays().toLong() * 24 * 60 * 60 * 1000 }
        )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val epochDays = (millis / (24 * 60 * 60 * 1000)).toInt()
                        val date = LocalDate.fromEpochDays(epochDays)
                        onDateSelected(date)
                    }
                    onDismiss()
                }
            ) { Text("OK", color = DashboardGradientMid) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = datePickerState) }
}

// Extension function
private fun HabitTimeRange.toTimeOfDay(): TimeOfDay =
    when (this) {
        HabitTimeRange.ANYTIME -> TimeOfDay.ANYTIME
        HabitTimeRange.MORNING -> TimeOfDay.MORNING
        HabitTimeRange.AFTERNOON -> TimeOfDay.AFTERNOON
        HabitTimeRange.EVENING -> TimeOfDay.EVENING
    }
