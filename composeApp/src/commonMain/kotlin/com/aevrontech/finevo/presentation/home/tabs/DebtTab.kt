package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Debt
import com.aevrontech.finevo.presentation.budget.AddBudgetScreen
import com.aevrontech.finevo.presentation.budget.BudgetCard
import com.aevrontech.finevo.presentation.budget.BudgetOverviewCard
import com.aevrontech.finevo.presentation.budget.BudgetViewModel
import com.aevrontech.finevo.presentation.budget.EmptyBudgetState
import com.aevrontech.finevo.presentation.components.AddDebtDialog
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.Warning
import org.koin.compose.viewmodel.koinViewModel

object DebtTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 2u,
                title = "Budget",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Add
                    )
            )

    @Composable
    override fun Content() {
        DebtTabContent()
    }
}

@Composable
private fun DebtTabContent() {
    val debtViewModel: DebtViewModel = koinViewModel()
    val budgetViewModel: BudgetViewModel = koinViewModel()

    val debtState by debtViewModel.uiState.collectAsState()
    val budgetState by budgetViewModel.uiState.collectAsState()

    // Refresh budget data when tab becomes active
    androidx.compose.runtime.LaunchedEffect(Unit) { budgetViewModel.refresh() }

    // 0 = Budgets, 1 = Debts
    var selectedTab by remember { mutableIntStateOf(0) }

    // Show AddDebt dialog
    if (debtState.showAddDialog) {
        AddDebtDialog(
            onDismiss = { debtViewModel.hideAddDialog() },
            onConfirm = { name, type, amount, interest, minPayment, dueDay ->
                debtViewModel.addDebt(name, type, amount, amount, interest, minPayment, dueDay)
            }
        )
    }

    // Show Budget Detail Screen
    if (budgetState.selectedBudget != null && !budgetState.showAddDialog) {
        com.aevrontech.finevo.presentation.budget.BudgetDetailScreen(
            budget = budgetState.selectedBudget!!,
            onBack = { budgetViewModel.clearSelectedBudget() },
            onEdit = { budgetViewModel.showEditDialog(budgetState.selectedBudget!!) }
        )
    } else if (budgetState.showAddDialog) {
        AddBudgetScreen(
            categories = budgetState.availableCategories,
            accounts = budgetState.accounts,
            editingBudget = budgetState.editingBudget,
            onDismiss = { budgetViewModel.hideDialog() },
            onSave = { name,
                       categoryIds,
                       accountIds,
                       amount,
                       currency,
                       period,
                       startDate,
                       endDate,
                       notifyOverspent,
                       alertThreshold,
                       notifyRisk ->
                if (budgetState.editingBudget != null) {
                    budgetViewModel.updateBudget(
                        budgetState.editingBudget!!,
                        name,
                        categoryIds,
                        accountIds,
                        amount,
                        period,
                        startDate,
                        endDate,
                        notifyOverspent,
                        alertThreshold,
                        notifyRisk
                    )
                } else {
                    budgetViewModel.addBudget(
                        name,
                        categoryIds,
                        accountIds,
                        amount,
                        currency,
                        period,
                        startDate,
                        endDate,
                        notifyOverspent,
                        alertThreshold,
                        notifyRisk
                    )
                }
            }
        )
    } else {
        // Regular content - only shown when not adding/editing budget
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Tab Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab Selector
                    TabSelector(
                        tabs = listOf("Budgets", "Debts"),
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    // Add Button (context-aware)
                    GradientFab(
                        onClick = {
                            if (selectedTab == 0) {
                                budgetViewModel.showAddDialog()
                            } else {
                                debtViewModel.showAddDialog()
                            }
                        },
                        icon = Icons.Filled.Add,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Content based on selected tab
            item {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        0 ->
                            BudgetContent(
                                budgetState = budgetState,
                                onBudgetClick = { budgetViewModel.selectBudget(it) },
                                onAddClick = { budgetViewModel.showAddDialog() },
                                onPeriodSelected = { budgetViewModel.setPeriodFilter(it) }
                            )
                        1 -> DebtContent(debtState = debtState)
                    }
                }
            }
        }
    }
}

/** Tab Selector Component */
@Composable
private fun TabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier.clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(
                                        DashboardGradientStart,
                                        DashboardGradientMid,
                                        DashboardGradientEnd
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color =
                        if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Budget Content Section */
@Composable
private fun BudgetContent(
    budgetState: com.aevrontech.finevo.presentation.budget.BudgetUiState,
    onBudgetClick: (com.aevrontech.finevo.domain.model.Budget) -> Unit,
    onAddClick: () -> Unit,
    onPeriodSelected: (com.aevrontech.finevo.domain.model.BudgetPeriod?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Overview Card (Always show to allow filtering)
        BudgetOverviewCard(
            totalBudget = budgetState.totalBudget,
            totalSpent = budgetState.totalSpent,
            budgetCount = budgetState.budgetCount,
            onTrackCount = budgetState.onTrackCount,
            warningCount = budgetState.warningCount,
            overCount = budgetState.overCount,
            selectedPeriod = budgetState.periodFilter,
            onPeriodSelected = { onPeriodSelected(it) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Budget List or Empty State
        if (budgetState.budgets.isNotEmpty()) {
            budgetState.budgets.forEach { budget ->
                BudgetCard(budget = budget, onClick = { onBudgetClick(budget) })
            }
        } else if (!budgetState.isLoading) {
            EmptyBudgetState(onAddClick = onAddClick)
        }
    }
}

/** Debt Content Section */
@Composable
private fun DebtContent(debtState: com.aevrontech.finevo.presentation.debt.DebtUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Total Debt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Total Debt",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RM ${debtState.totalDebt.formatDecimal(2)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (debtState.totalDebt > 0) Warning else Income
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "${debtState.debtCount} debts",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        "Min. payment: RM ${debtState.totalMinimumPayment.formatDecimal(0)}/mo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Debt List
        if (debtState.debts.isEmpty() && !debtState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No debts tracked", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("You're debt-free! 🎉", color = Income, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            debtState.debts.forEach { debt -> DebtItem(debt = debt) }
        }
    }
}

@Composable
private fun DebtItem(debt: Debt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = debt.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = debt.type.name.replace("_", " "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${debt.currentBalance.formatDecimal(2)}",
                        color = Warning,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${debt.interestRate.formatDecimal(1)}% APR",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (debt.percentPaid / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Income,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${debt.percentPaid.formatDecimal(1)}% paid off",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
