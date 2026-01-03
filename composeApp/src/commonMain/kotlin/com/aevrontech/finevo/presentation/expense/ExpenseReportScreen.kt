package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.label.LabelViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/** Full screen report with swipe-able charts and history */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(onDismiss: () -> Unit) {
    val expenseViewModel: ExpenseViewModel = koinViewModel()
    val labelViewModel: LabelViewModel = koinViewModel()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val labelState by labelViewModel.uiState.collectAsState()
    val filterPeriod by expenseViewModel.filterPeriod.collectAsState()
    val periodOffset by expenseViewModel.periodOffset.collectAsState()

    // Filter transactions based on selected period
    val filteredTransactions =
        remember(expenseState.transactions, filterPeriod, periodOffset) {
            expenseViewModel.getFilteredTransactions()
        }

    // Chart Data Computation
    val barChartData =
        remember(filteredTransactions, filterPeriod) {
            val expenseTransactions =
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }

            when (filterPeriod) {
                FilterPeriod.WEEK -> {
                    // Group by Day of Week (Mon-Sun)
                    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val grouped = expenseTransactions.groupBy { it.date.dayOfWeek.ordinal }
                    dayNames.mapIndexed { index, name ->
                        val total = grouped[index]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = name, value = total)
                    }
                }
                FilterPeriod.MONTH -> {
                    // Group by Day of Month
                    // Label every 5th day to avoid clutter or logic inside chart
                    val grouped = expenseTransactions.groupBy { it.date.dayOfMonth }
                    val daysInMonth = 30 // Approx, or calculate based on date
                    // To show all days might be too wide. Let's show all and let chart handle
                    // spacing or just specific days.
                    // For simplicity/visuals: 1..31
                    (1..31).map { day ->
                        val total = grouped[day]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = day.toString(), value = total)
                    }
                }
                FilterPeriod.YEAR -> {
                    // Group by Month
                    val monthNames =
                        listOf(
                            "Jan",
                            "Feb",
                            "Mar",
                            "Apr",
                            "May",
                            "Jun",
                            "Jul",
                            "Aug",
                            "Sep",
                            "Oct",
                            "Nov",
                            "Dec"
                        )
                    val grouped = expenseTransactions.groupBy { it.date.monthNumber }
                    monthNames.mapIndexed { index, name ->
                        val total = grouped[index + 1]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = name, value = total)
                    }
                }
            }
        }

    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ) // Use theme background (White in Light)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (totalDrag > 100) { // Dragged Right
                                expenseViewModel.setPeriodOffset(periodOffset - 1)
                            } else if (totalDrag < -100) { // Dragged Left
                                if (periodOffset < 0
                                ) { // Can't go to future (offset > 0 usually logic)
                                    expenseViewModel.setPeriodOffset(
                                        periodOffset + 1
                                    )
                                }
                            }
                            totalDrag = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                TopAppBar(
                    title = {
                        Text(
                            text = "Statistics",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface // Theme aware
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                )
            }

            // 1. Time Filter Tabs (Top, Outside Card)
            item {
                TimeFilterTabs(
                    selectedPeriod = filterPeriod,
                    onPeriodSelected = { expenseViewModel.setFilterPeriod(it) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 2. Period Navigator (Arrows + Date Range) - Below Tabs
            item {
                val periodLabel = getPeriodLabelForReport(filterPeriod, periodOffset)

                PeriodNavigator(
                    periodLabel = periodLabel,
                    onPrevious = { expenseViewModel.setPeriodOffset(periodOffset - 1) },
                    onNext = { expenseViewModel.setPeriodOffset(periodOffset + 1) },
                    canGoNext = periodOffset < 0,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Statistics Card (Chart Only)

                StatisticsCard(
                    selectedPeriod = filterPeriod,
                    periodOffset = periodOffset,
                    barChartData = barChartData,
                    monthName = periodLabel.split(" ").firstOrNull() ?: "",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Transaction History
            item {
                TransactionHistorySection(
                    transactions = filteredTransactions,
                    limit = 100, // Show more in full report
                    onSeeAllClick = { /* Navigate to full history if needed */ },
                    onTransactionClick = { /* Open details */ },
                    availableLabels = labelState.labels,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

private fun getPeriodLabelForReport(period: FilterPeriod, offset: Int): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return when (period) {
        FilterPeriod.WEEK -> {
            val weekStart =
                today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    .plus(offset * 7, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            "${weekStart.dayOfMonth} - ${weekEnd.dayOfMonth} ${weekEnd.month.name.take(3)} ${weekEnd.year}"
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
            "${Month(targetMonth).name.take(3)} $targetYear"
        }
        FilterPeriod.YEAR -> {
            (today.year + offset).toString()
        }
    }
}
