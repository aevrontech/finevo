package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.label.LabelViewModel
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.compose.viewmodel.koinViewModel

/** Full screen report with swipe-able charts and history - Redesigned with Blue Header */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(onDismiss: () -> Unit) {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        // Set transparent status and navigation bars
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = false)
        // Also set specifically if needed
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = false)
        systemUiController.setNavigationBarColor(Color.Transparent, darkIcons = false)

        onDispose {}
    }

    val expenseViewModel: ExpenseViewModel = koinViewModel()
    val labelViewModel: LabelViewModel = koinViewModel()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val labelState by labelViewModel.uiState.collectAsState()
    val filterPeriod by expenseViewModel.filterPeriod.collectAsState()
    val periodOffset by expenseViewModel.periodOffset.collectAsState()

    // Filter Sheet State
    var showFilterSheet by remember { mutableStateOf(false) }
    var isLineChart by remember { mutableStateOf(false) }

    // Filter transactions based on selected period
    val filteredTransactions =
        remember(expenseState.transactions, filterPeriod, periodOffset) {
            expenseViewModel.getFilteredTransactions()
        }

    // Group transactions by date for the unified list
    val transactionGroups =
        remember(filteredTransactions) { groupTransactionsByDate(filteredTransactions) }

    // Chart Data Computation
    val barChartData =
        remember(filteredTransactions, filterPeriod) {
            val expenseTransactions =
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }

            when (filterPeriod) {
                FilterPeriod.DAY -> {
                    val grouped =
                        expenseTransactions.groupBy {
                            it.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
                        }
                    (0 until 24).map { hour ->
                        val total = grouped[hour]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(
                            label = if (hour % 6 == 0) "${hour}h" else "",
                            value = total
                        )
                    }
                }
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
                    val grouped = expenseTransactions.groupBy { it.date.dayOfMonth }
                    (1..31).map { day ->
                        val total = grouped[day]?.sumOf { it.amount } ?: 0.0
                        // Sparse labels: Show every odd day (1, 3, 5...)
                        val label = if (day % 2 != 0) day.toString() else ""
                        BarChartItem(label = label, value = total)
                    }
                }
                FilterPeriod.YEAR -> {
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

    // Gestures for swiping time periods
    val swipeModifier =
        Modifier.pointerInput(Unit) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (totalDrag > 100) { // Dragged Right -> Previous
                        expenseViewModel.setPeriodOffset(periodOffset - 1)
                    } else if (totalDrag < -100) { // Dragged Left -> Next
                        if (periodOffset < 0) {
                            expenseViewModel.setPeriodOffset(periodOffset + 1)
                        }
                    }
                    totalDrag = 0f
                }
            ) { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            }
        }

    if (showFilterSheet) {
        TimeRangeSelectionSheet(
            currentRange = expenseState.timeRange,
            onRangeSelected = {
                expenseViewModel.setTimeRange(it)
                // Note: setTimeRange updates filterPeriod/offset internally in VM
            },
            onDismissRequest = { showFilterSheet = false }
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    DashboardGradientStart,
                                    DashboardGradientMid,
                                    DashboardGradientEnd
                                )
                        )
                )
    ) {
        // --- HEADER SECTION (Gradient) ---
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(text = "Statistics", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isLineChart = !isLineChart }) {
                        Icon(
                            imageVector =
                                if (isLineChart) Icons.Filled.SignalCellularAlt
                                else Icons.Filled.Timeline,
                            contentDescription = "Toggle Chart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Smart Filter (Central)
            TimeFilterSection(
                currentRange = expenseState.timeRange,
                onNavigate = { direction -> expenseViewModel.navigateTimeRange(direction) },
                onFilterClick = { showFilterSheet = true },
                containerBrush =
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Chart (Line or Bar)
            Box(Modifier.padding(horizontal = 20.dp).height(200.dp)) {
                if (isLineChart) {
                    LineChart(
                        data = barChartData,
                        modifier = Modifier.fillMaxSize(),
                        monthName = "",
                        period = filterPeriod
                    )
                } else {
                    GradientBarChart(
                        data = barChartData,
                        modifier = Modifier.fillMaxSize(),
                        barWidth = if (filterPeriod == FilterPeriod.MONTH) 6.dp else 16.dp,
                        barBrush =
                            Brush.verticalGradient(
                                listOf(Color.White, Color.White.copy(alpha = 0.5f))
                            ),
                        axisLabelColor = Color.White,
                        gridLineColor = Color.White.copy(alpha = 0.2f),
                        period = filterPeriod
                    )
                }
            }
        }

        // --- BODY SECTION (White Sheet) ---
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .weight(1f) // Fill remaining space
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background) // White
                    .then(swipeModifier) // Apply swipe to body too
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                // Unified Transaction History (Grouped)
                groupedTransactionItems(
                    groups = transactionGroups,
                    availableLabels = labelState.labels,
                    currencySymbol = expenseState.currencySymbol,
                    onTransactionClick = { /* No-op for report mode per request */ },
                    onTransactionDelete = { transaction ->
                        expenseViewModel.deleteTransaction(transaction.id)
                    },
                    onDateClick = { date ->
                        // Optional: Navigate/Zoom to that date.
                        // For now, mirroring Wallet behavior or leaving as simple interaction.
                        // Wallet calculates offset and sets to DAY view.
                        val today = getCurrentLocalDate()
                        val daysDiff = (date.toEpochDays() - today.toEpochDays()).toInt()
                        expenseViewModel.setFilterPeriod(FilterPeriod.DAY)
                        expenseViewModel.setPeriodOffset(daysDiff)
                    }
                )
            }
        }
    }
}
