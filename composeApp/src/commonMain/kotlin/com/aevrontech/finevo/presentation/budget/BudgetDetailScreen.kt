package com.aevrontech.finevo.presentation.budget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.presentation.expense.CategoryDonutChartWithIcons
import com.aevrontech.finevo.presentation.expense.groupTransactionsByDate
import com.aevrontech.finevo.presentation.expense.groupedTransactionItems
import com.aevrontech.finevo.presentation.label.LabelViewModel
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BudgetDetailScreen(budget: Budget, onBack: () -> Unit, onEdit: () -> Unit) {
    val budgetViewModel: BudgetViewModel = koinViewModel()
    val labelViewModel: LabelViewModel = koinViewModel()
    val state by budgetViewModel.uiState.collectAsState()
    val labelState by labelViewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(budget.id) { budgetViewModel.selectBudget(budget) }

    val displayBudget = state.selectedBudget ?: budget

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget") },
            text = {
                Text(
                    "Are you sure you want to delete '${displayBudget.name ?: displayBudget.categoryName}'? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        budgetViewModel.deleteBudget(displayBudget)
                        showDeleteDialog = false
                        onBack()
                    }
                ) { Text("Delete", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            brush =
                                androidx.compose.ui.graphics.Brush
                                    .horizontalGradient(
                                        colors =
                                            listOf(
                                                com.aevrontech
                                                    .finevo
                                                    .ui
                                                    .theme
                                                    .DashboardGradientStart,
                                                com.aevrontech
                                                    .finevo
                                                    .ui
                                                    .theme
                                                    .DashboardGradientEnd
                                            )
                                    )
                        )
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                "Budget Detail",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                            }
                        },
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                    )

                    // Tabs inside the gradient header
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier =
                                    androidx.compose.ui.Modifier.tabIndicatorOffset(
                                        tabPositions[pagerState.currentPage]
                                    ),
                                color = Color.White
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                            text = {
                                Text(
                                    "Overview",
                                    fontWeight =
                                        if (pagerState.currentPage == 0)
                                            FontWeight.Bold
                                        else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            text = {
                                Text(
                                    "Records",
                                    fontWeight =
                                        if (pagerState.currentPage == 1)
                                            FontWeight.Bold
                                        else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
        ) {
            // Period Navigation Header
            if (state.currentPeriodLabel.isNotEmpty() &&
                displayBudget.period !=
                com.aevrontech.finevo.domain.model.BudgetPeriod.ONCE
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { budgetViewModel.navigatePeriod(-1) },
                        enabled = state.canNavigatePrevious
                    ) {
                        Icon(
                            imageVector =
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled
                                    .ArrowBack,
                            contentDescription = "Previous period",
                            tint =
                                if (state.canNavigatePrevious)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    Text(
                        text = state.currentPeriodLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { budgetViewModel.navigatePeriod(1) },
                        enabled = state.canNavigateNext
                    ) {
                        Icon(
                            imageVector =
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled
                                    .ArrowForward,
                            contentDescription = "Next period",
                            tint =
                                if (state.canNavigateNext) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> {
                        // Overview Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { BudgetCard(budget = displayBudget, onClick = {}) }

                            // Trend Graph
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.surface
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "Spending Trend",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(16.dp))

                                        Box(Modifier.height(260.dp)) {
                                            val trend = state.budgetTrend
                                            if (trend != null) {
                                                BudgetTrendChart(
                                                    data = trend,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) { Text("No data", color = Color.Gray) }
                                            }
                                        }

                                        val trendStats = state.budgetTrend
                                        if (trendStats != null) {
                                            Spacer(Modifier.height(16.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text =
                                                            "MYR ${trendStats.dailyAverage.formatDecimal()}",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurface
                                                    )
                                                    Text(
                                                        text = "Daily average",
                                                        fontSize = 14.sp,
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurfaceVariant
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text =
                                                            "MYR ${trendStats.dailyRecommended.formatDecimal()}",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurface
                                                    )
                                                    Text(
                                                        text = "Daily recommended",
                                                        fontSize = 14.sp,
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Category Breakdown
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.surface
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "Spending by Category",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(24.dp))

                                        if (state.budgetCategoryBreakdown.isNotEmpty()) {
                                            Box(
                                                modifier =
                                                    Modifier.fillMaxWidth().height(260.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CategoryDonutChartWithIcons(
                                                    breakdown = state.budgetCategoryBreakdown,
                                                    totalAmount =
                                                        state.budgetTransactions.sumOf {
                                                            it.amount
                                                        },
                                                    chartSize = 130.dp,
                                                    strokeWidth = 28.dp,
                                                    currencySymbol = "RM"
                                                )
                                                Column(
                                                    horizontalAlignment =
                                                        Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        "Total",
                                                        fontSize = 12.sp,
                                                        color = OnSurfaceVariant
                                                    )
                                                    Text(
                                                        text =
                                                            "RM${
                                                                state.budgetTransactions.sumOf { it.amount }
                                                                    .formatDecimal(2)
                                                            }",
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurface
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(24.dp))

                                            androidx.compose.material3.HorizontalDivider(
                                                modifier = Modifier.padding(bottom = 16.dp),
                                                color =
                                                    MaterialTheme.colorScheme.outlineVariant
                                                        .copy(alpha = 0.5f)
                                            )

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(0.dp)
                                            ) {
                                                state.budgetCategoryBreakdown.forEachIndexed { index,
                                                                                               category ->
                                                    Column(
                                                        modifier =
                                                            Modifier.fillMaxWidth()
                                                                .clickable {
                                                                    selectedCategoryFilter =
                                                                        category.categoryId
                                                                    coroutineScope
                                                                        .launch {
                                                                            pagerState
                                                                                .animateScrollToPage(
                                                                                    1
                                                                                )
                                                                        }
                                                                }
                                                                .padding(
                                                                    vertical = 12.dp
                                                                )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment =
                                                                Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                category.categoryIcon,
                                                                fontSize = 20.sp
                                                            )
                                                            Spacer(Modifier.width(12.dp))
                                                            Column(Modifier.weight(1f)) {
                                                                Text(
                                                                    category.categoryName,
                                                                    fontWeight =
                                                                        FontWeight.Medium
                                                                )
                                                                Text(
                                                                    "${category.count} transactions",
                                                                    fontSize = 12.sp,
                                                                    color = OnSurfaceVariant
                                                                )
                                                            }
                                                            Text(
                                                                "RM ${category.total.formatDecimal(2)}",
                                                                fontWeight = FontWeight.Bold,
                                                                color =
                                                                    MaterialTheme
                                                                        .colorScheme
                                                                        .onSurface
                                                            )
                                                        }
                                                    }
                                                    if (index <
                                                        state.budgetCategoryBreakdown
                                                            .lastIndex
                                                    ) {
                                                        androidx.compose.material3
                                                            .HorizontalDivider(
                                                                color =
                                                                    MaterialTheme
                                                                        .colorScheme
                                                                        .outlineVariant
                                                                        .copy(
                                                                            alpha =
                                                                                0.3f
                                                                        )
                                                            )
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                Modifier.fillMaxWidth().height(100.dp),
                                                contentAlignment = Alignment.Center
                                            ) { Text("No spending yet", color = OnSurfaceVariant) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Records Tab
                        val filteredTransactions =
                            remember(state.budgetTransactions, selectedCategoryFilter) {
                                if (selectedCategoryFilter != null) {
                                    state.budgetTransactions.filter {
                                        it.categoryId == selectedCategoryFilter
                                    }
                                } else {
                                    state.budgetTransactions
                                }
                            }

                        val groups =
                            remember(filteredTransactions) {
                                groupTransactionsByDate(filteredTransactions)
                            }

                        Column(Modifier.fillMaxSize()) {
                            // Filter Chip
                            if (selectedCategoryFilter != null) {
                                val categoryName =
                                    state.budgetCategoryBreakdown
                                        .find { it.categoryId == selectedCategoryFilter }
                                        ?.categoryName
                                        ?: "Category"
                                Box(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    androidx.compose.material3.FilterChip(
                                        selected = true,
                                        onClick = { selectedCategoryFilter = null },
                                        label = { Text("Category: $categoryName") },
                                        trailingIcon = {
                                            Icon(
                                                androidx.compose.material.icons.Icons
                                                    .Default.Close,
                                                "Clear"
                                            )
                                        }
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                    androidx.compose.foundation.layout.PaddingValues(
                                        bottom = 100.dp
                                    )
                            ) {
                                if (groups.isEmpty()) {
                                    item {
                                        Box(
                                            Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text("No records found", color = Color.Gray) }
                                    }
                                } else {
                                    groupedTransactionItems(
                                        groups = groups,
                                        availableLabels = labelState.labels,
                                        currencySymbol = "RM", // Should use
                                        // correct
                                        // currency
                                        onTransactionClick = {},
                                        onTransactionDelete = {},
                                        onDateClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
