package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.expense.CategoryBreakdown
import com.aevrontech.finevo.presentation.expense.CategoryReportSection
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.home.HomeViewModel
import com.aevrontech.finevo.ui.theme.ActionBill
import com.aevrontech.finevo.ui.theme.ActionBillBg
import com.aevrontech.finevo.ui.theme.ActionMode
import com.aevrontech.finevo.ui.theme.ActionModeBg
import com.aevrontech.finevo.ui.theme.ActionTopUp
import com.aevrontech.finevo.ui.theme.ActionTopUpBg
import com.aevrontech.finevo.ui.theme.ActionTransfer
import com.aevrontech.finevo.ui.theme.ActionTransferBg
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.ThemeColors
import org.koin.compose.viewmodel.koinViewModel

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 0u,
                title = "Home",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Home
                    )
            )

    @Composable
    override fun Content() {
        DashboardContent()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent() {
    val viewModel: HomeViewModel = koinViewModel()
    val expenseViewModel: ExpenseViewModel = koinViewModel()
    val expenseState by expenseViewModel.uiState.collectAsState()

    // Get transactions for category breakdown
    val transactions = expenseState.transactions

    // Calculate income and expense breakdowns
    val incomeBreakdown =
        remember(transactions, expenseState.categories) {
            transactions
                .filter { it.type == TransactionType.INCOME }
                .groupBy { it.categoryId }
                .map { (categoryId, txList) ->
                    val category = expenseState.categories.find { it.id == categoryId }
                    CategoryBreakdown(
                        categoryId = categoryId,
                        categoryName = category?.name ?: "Other",
                        categoryIcon = category?.icon ?: "💰",
                        categoryColor = category?.color ?: "#4CAF50",
                        total = txList.sumOf { it.amount },
                        count = txList.size
                    )
                }
                .sortedByDescending { it.total }
        }

    val expenseBreakdown =
        remember(transactions, expenseState.categories) {
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .map { (categoryId, txList) ->
                    val category = expenseState.categories.find { it.id == categoryId }
                    CategoryBreakdown(
                        categoryId = categoryId,
                        categoryName = category?.name ?: "Other",
                        categoryIcon = category?.icon ?: "💵",
                        categoryColor = category?.color ?: "#808080",
                        total = txList.sumOf { it.amount },
                        count = txList.size
                    )
                }
                .sortedByDescending { it.total }
        }

    val navigator = LocalTabNavigator.current
    var showFilterSheet by remember { mutableStateOf(false) }

    // Use dashboard specific totals
    val incomeTotal = expenseState.dashboardIncome
    val expenseTotal = expenseState.dashboardExpense
    val totalBalance = expenseState.totalBalance
    val currencyCode = expenseState.currencyCode

    // Formatting helper (simple for now, could be extracted)
    fun formatMoney(amount: Double): String {
        // Basic formatting, ideally use NumberFormat
        // Assuming amount is potentially large, we just show it directly
        // currencyCode + " " + amount
        // But for better visuals let's just stick to code + space + amount
        // Or if we want commas:
        // val formatted = amount.toString() // naive
        // Better:
        val parts = amount.toString().split('.')
        val integerPart = parts[0].reversed().chunked(3).joinToString(",").reversed()
        val fractionalPart = if (parts.size > 1) "." + parts[1].take(2) else ""
        return "$currencyCode $integerPart$fractionalPart"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp) // Manual spacing control
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Account Cards Pager
        item {
            if (expenseState.accounts.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { expenseState.accounts.size })

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    ) { page ->
                        val account = expenseState.accounts[page]
                        DashboardAccountCard(
                            account = account,
                            onClick = {
                                navigator.current =
                                    com.aevrontech.finevo.presentation.home.tabs.ExpenseTab
                            }
                        )
                    }

                    // Pager Indicator
                    Row(
                        Modifier.height(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration)
                                    androidx.compose.material3.MaterialTheme.colorScheme
                                        .primary
                                else ThemeColors.onSurface.copy(alpha = 0.2f)
                            Box(
                                modifier =
                                    Modifier.padding(2.dp)
                                        .clip(
                                            androidx.compose.foundation.shape
                                                .CircleShape
                                        )
                                        .background(color)
                                        .size(8.dp)
                            )
                        }
                    }
                }
            } else {
                Text("No accounts found", modifier = Modifier.padding(16.dp))
            }
            // Space between Carousel and Quick Actions (Reduced from ~40dp to 16dp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Refresh,
                    label = "Transfer",
                    color = ActionTransfer,
                    backgroundColor = ActionTransferBg
                )
                QuickActionButton(
                    icon = Icons.Filled.Add,
                    label = "Top-up",
                    color = ActionTopUp,
                    backgroundColor = ActionTopUpBg
                )
                QuickActionButton(
                    icon = Icons.Filled.DateRange,
                    label = "Bill",
                    color = ActionBill,
                    backgroundColor = ActionBillBg
                )
                QuickActionButton(
                    icon = Icons.Filled.Menu,
                    label = "More",
                    color = ActionMode,
                    backgroundColor = ActionModeBg
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Category Report Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryReportSection(
                    incomeTotal = incomeTotal,
                    expenseTotal = expenseTotal,
                    incomeBreakdown = incomeBreakdown,
                    expenseBreakdown = expenseBreakdown,
                    currencySymbol = currencyCode,
                    onIncomeClick = {},
                    onExpenseClick = {},
                    onSeeReportClick = {}
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable {}) {
        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = OnSurface, fontWeight = FontWeight.Medium)
    }
}
