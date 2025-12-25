package com.aevrontech.finevo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.components.AddDebtDialog
import com.aevrontech.finevo.presentation.components.AddHabitDialog
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.habit.HabitViewModel
import com.aevrontech.finevo.presentation.settings.SettingsViewModel
import com.aevrontech.finevo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        TabNavigator(
                tab = DashboardTab,
                tabDisposable = {
                    TabDisposable(
                            it,
                            listOf(DashboardTab, ExpenseTab, DebtTab, HabitTab, SettingsTab)
                    )
                }
        ) { tabNavigator ->
            Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceContainer, contentColor = OnSurface) {
                            TabNavigationItem(DashboardTab)
                            TabNavigationItem(ExpenseTab)
                            TabNavigationItem(DebtTab)
                            TabNavigationItem(HabitTab)
                            TabNavigationItem(SettingsTab)
                        }
                    }
            ) { paddingValues ->
                Box(
                        modifier =
                                Modifier.fillMaxSize().padding(paddingValues).background(Background)
                ) { CurrentTab() }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    NavigationBarItem(
            selected = selected,
            onClick = { tabNavigator.current = tab },
            icon = {
                tab.options.icon?.let { painter ->
                    Icon(painter = painter, contentDescription = tab.options.title)
                }
            },
            label = { Text(text = tab.options.title, fontSize = 11.sp) },
            colors =
                    NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant,
                            indicatorColor = PrimaryContainer
                    )
    )
}

// ============================================
// TAB DEFINITIONS
// ============================================

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
                TabOptions(
                        index = 0u,
                        title = "Home",
                        icon = rememberVectorPainter(Icons.Filled.Home)
                )

    @Composable
    override fun Content() {
        DashboardContent()
    }
}

object ExpenseTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
                TabOptions(
                        index = 1u,
                        title = "Expenses",
                        icon = rememberVectorPainter(Icons.Filled.List) // Changed from Receipt
                )

    @Composable
    override fun Content() {
        ExpenseTabContent()
    }
}

object DebtTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
                TabOptions(
                        index = 2u,
                        title = "Debts",
                        icon =
                                rememberVectorPainter(
                                        Icons.Filled.AccountCircle
                                ) // Changed from CreditCard for better availability
                )

    @Composable
    override fun Content() {
        DebtTabContent()
    }
}

object HabitTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
                TabOptions(
                        index = 3u,
                        title = "Habits",
                        icon = rememberVectorPainter(Icons.Filled.Check) // Changed from CheckCircle
                )

    @Composable
    override fun Content() {
        HabitTabContent()
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
                TabOptions(
                        index = 4u,
                        title = "Settings",
                        icon = rememberVectorPainter(Icons.Filled.Settings)
                )

    @Composable
    override fun Content() {
        SettingsTabContent()
    }
}

@Composable
private fun rememberVectorPainter(imageVector: ImageVector) =
        androidx.compose.ui.graphics.vector.rememberVectorPainter(imageVector)

// ============================================
// TAB CONTENT
// ============================================

@Composable
private fun DashboardContent() {
    val viewModel: HomeViewModel = koinViewModel()

    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
            )
        }

        // Balance Card
        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                    shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                Brush.linearGradient(
                                                        colors =
                                                                listOf(
                                                                        GradientStart.copy(
                                                                                alpha = 0.3f
                                                                        ),
                                                                        GradientMid.copy(
                                                                                alpha = 0.2f
                                                                        )
                                                                )
                                                )
                                        )
                                        .padding(20.dp)
                ) {
                    Column {
                        Text(text = "Total Balance", color = OnSurfaceVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = "RM 12,345.67",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BalanceItem(label = "Income", amount = "+RM 5,000", color = Income)
                            BalanceItem(label = "Expenses", amount = "-RM 3,200", color = Expense)
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                        icon = Icons.Filled.Add,
                        label = "Add\nExpense",
                        color = Expense,
                        modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                        icon = Icons.Filled.Star,
                        label = "Add\nIncome",
                        color = Income,
                        modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                        icon = Icons.Filled.AccountCircle,
                        label = "Pay\nDebt",
                        color = Warning,
                        modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                        icon = Icons.Filled.Check,
                        label = "Log\nHabit",
                        color = Primary,
                        modifier = Modifier.weight(1f)
                )
            }
        }

        // Coming soon placeholder
        item {
            Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🚀", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            text = "More features coming soon!",
                            fontSize = 16.sp,
                            color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(label: String, amount: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, color = OnSurfaceVariant, fontSize = 12.sp)
        Text(text = amount, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun QuickActionButton(
        icon: ImageVector,
        label: String,
        color: androidx.compose.ui.graphics.Color,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                    modifier =
                            Modifier.size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant, lineHeight = 14.sp)
        }
    }
}

// ============================================
// EXPENSE TAB CONTENT
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseTabContent() {
    val expenseViewModel: ExpenseViewModel = koinViewModel()
    val accountViewModel: com.aevrontech.finevo.presentation.expense.AccountViewModel =
            koinViewModel()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val accountState by accountViewModel.uiState.collectAsState()

    var showAddTransaction by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }

    // Show AddTransaction screen
    if (showAddTransaction) {
        com.aevrontech.finevo.presentation.expense.AddTransactionScreen(
                transactionType = expenseState.selectedTransactionType,
                accounts = expenseState.accounts,
                categories = expenseState.categories,
                selectedAccount = expenseState.selectedAccount,
                onDismiss = { showAddTransaction = false },
                onConfirm = { type, amount, accountId, categoryId, note, date, time ->
                    expenseViewModel.addTransaction(type, amount, categoryId, note, note)
                    showAddTransaction = false
                }
        )
        return
    }

    // Show AddAccount screen
    if (showAddAccount) {
        com.aevrontech.finevo.presentation.expense.AddAccountScreen(
                onDismiss = { showAddAccount = false },
                onConfirm = { name, balance, currency, type, color ->
                    accountViewModel.createAccount(name, balance, currency, type, color)
                    showAddAccount = false
                },
                defaultCurrency = expenseState.selectedAccount?.currency ?: "MYR"
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Accounts",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                    )
                }
            }

            // Account Cards - Horizontal Scroll
            item {
                com.aevrontech.finevo.presentation.expense.AccountCardsRow(
                        accounts = expenseState.accounts,
                        selectedAccount = expenseState.selectedAccount,
                        onAccountClick = { account -> expenseViewModel.selectAccount(account) },
                        onAddAccountClick = { showAddAccount = true }
                )
            }

            // Account Summary Card
            item {
                com.aevrontech.finevo.presentation.expense.AccountSummaryCard(
                        account = expenseState.selectedAccount,
                        income = expenseState.accountIncome,
                        expense = expenseState.accountExpense
                )
            }

            // Section Header: Transactions
            item {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                    )
                    Text(text = "This Month", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }

            // Transaction List
            if (expenseState.transactions.isEmpty() && !expenseState.isLoading) {
                item {
                    Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💸", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    "No transactions yet",
                                    color = OnSurface,
                                    fontWeight = FontWeight.Medium
                            )
                            Text(
                                    "Tap + to add your first transaction",
                                    color = OnSurfaceVariant,
                                    fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(expenseState.transactions.size) { index ->
                    val transaction = expenseState.transactions[index]
                    TransactionItem(
                            transaction = transaction,
                            modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // FAB at bottom right
        FloatingActionButton(
                onClick = { showAddTransaction = true },
                containerColor = Primary,
                contentColor = OnPrimary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).size(56.dp),
                shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Add Transaction") }
    }
}

@Composable
private fun TransactionItem(
        transaction: com.aevrontech.finevo.domain.model.Transaction,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = transaction.categoryIcon ?: "💵", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = transaction.description
                                            ?: transaction.categoryName ?: "Transaction",
                            color = OnSurface,
                            fontWeight = FontWeight.Medium
                    )
                    Text(
                            text = transaction.date.toString(),
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                    )
                }
            }
            Text(
                    text =
                            "${if (transaction.type == com.aevrontech.finevo.domain.model.TransactionType.EXPENSE) "-" else "+"} RM ${String.format("%.2f", transaction.amount)}",
                    color =
                            if (transaction.type ==
                                            com.aevrontech.finevo.domain.model.TransactionType
                                                    .EXPENSE
                            )
                                    Expense
                            else Income,
                    fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================
// DEBT TAB CONTENT
// ============================================
@Composable
private fun DebtTabContent() {
    val viewModel: DebtViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Show AddDebt dialog
    if (uiState.showAddDialog) {
        AddDebtDialog(
                onDismiss = { viewModel.hideAddDialog() },
                onConfirm = { name, type, amount, interest, minPayment, dueDay ->
                    viewModel.addDebt(name, type, amount, amount, interest, minPayment, dueDay)
                }
        )
    }

    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Debts",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                )
                FloatingActionButton(
                        onClick = { viewModel.showAddDialog() },
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            }
        }

        // Total Debt Card
        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Debt", color = OnSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = "RM ${String.format("%.2f", uiState.totalDebt)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.totalDebt > 0) Warning else Income
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                                "${uiState.debtCount} debts",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp
                        )
                        Text(
                                "Min. payment: RM ${String.format("%.0f", uiState.totalMinimumPayment)}/mo",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Debt List
        if (uiState.debts.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No debts tracked", color = OnSurfaceVariant)
                        Text("You're debt-free! 🎉", color = Income, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(uiState.debts.size) { index ->
                val debt = uiState.debts[index]
                DebtItem(debt = debt)
            }
        }
    }
}

@Composable
private fun DebtItem(debt: com.aevrontech.finevo.domain.model.Debt) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = debt.name, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text(
                            text = debt.type.name.replace("_", " "),
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                            text = "RM ${String.format("%.2f", debt.currentBalance)}",
                            color = Warning,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "${String.format("%.1f", debt.interestRate)}% APR",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Progress bar
            LinearProgressIndicator(
                    progress = { (debt.percentPaid / 100).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Income,
                    trackColor = SurfaceContainerHighest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = "${String.format("%.1f", debt.percentPaid)}% paid off",
                    color = OnSurfaceVariant,
                    fontSize = 12.sp
            )
        }
    }
}

// ============================================
// HABIT TAB CONTENT
// ============================================
@Composable
private fun HabitTabContent() {
    val viewModel: HabitViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Show AddHabit dialog
    if (uiState.showAddDialog) {
        AddHabitDialog(
                onDismiss = { viewModel.hideAddDialog() },
                onConfirm = { name, icon, color, frequency, xpReward ->
                    viewModel.addHabit(name, icon, color, frequency, xpReward)
                }
        )
    }

    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Habits",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                )
                FloatingActionButton(
                        onClick = { viewModel.showAddDialog() },
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            }
        }

        // Progress Card
        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today's Progress", color = OnSurfaceVariant, fontSize = 12.sp)
                        Text(
                                text = "${uiState.completedCount}/${uiState.totalCount}",
                                color = Primary,
                                fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                            progress = { uiState.completionPercentage },
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                            color = Primary,
                            trackColor = SurfaceContainerHighest
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text("🔥 ", fontSize = 14.sp)
                            Text(
                                    "${uiState.currentStreak} day streak",
                                    color = OnSurfaceVariant,
                                    fontSize = 14.sp
                            )
                        }
                        Row {
                            Text("⭐ ", fontSize = 14.sp)
                            Text(
                                    "+${uiState.totalXpToday} XP today",
                                    color = Primary,
                                    fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Habit List
        if (uiState.habits.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚀", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No habits yet", color = OnSurfaceVariant)
                        Text(
                                "Tap + to create your first habit",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(uiState.habits.size) { index ->
                val habit = uiState.habits[index]
                val isCompleted = uiState.completedHabitIds.contains(habit.id)
                HabitItem(
                        habit = habit,
                        isCompleted = isCompleted,
                        onToggle = { viewModel.toggleHabit(habit.id) }
                )
            }
        }
    }
}

@Composable
private fun HabitItem(
        habit: com.aevrontech.finevo.domain.model.Habit,
        isCompleted: Boolean,
        onToggle: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isCompleted) Primary.copy(alpha = 0.1f)
                                    else SurfaceContainer
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = habit.icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = habit.name, color = OnSurface, fontWeight = FontWeight.Medium)
                    Row {
                        Text(
                                "🔥 ${habit.currentStreak}",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+${habit.xpReward} XP", color = Primary, fontSize = 12.sp)
                    }
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.Check,
                        contentDescription = if (isCompleted) "Completed" else "Mark complete",
                        tint = if (isCompleted) Income else OnSurfaceVariant,
                        modifier = Modifier.size(if (isCompleted) 32.dp else 24.dp)
                )
            }
        }
    }
}

// ============================================
// SETTINGS TAB CONTENT
// ============================================
@Composable
private fun SettingsTabContent() {
    val viewModel: SettingsViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
            )
        }

        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Account", color = OnSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                            onClick = { authViewModel.signOut() },
                            colors = ButtonDefaults.buttonColors(containerColor = Expense),
                            modifier = Modifier.fillMaxWidth()
                    ) { Text("Sign Out") }
                }
            }
        }

        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("FinEvo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Version 1.0.0", color = OnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
