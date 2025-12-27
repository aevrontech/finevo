package com.aevrontech.finevo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.components.AddDebtDialog
import com.aevrontech.finevo.presentation.components.AddHabitDialog
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.expense.groupTransactionsByDate
import com.aevrontech.finevo.presentation.expense.groupedTransactionItems
import com.aevrontech.finevo.presentation.habit.HabitViewModel
import com.aevrontech.finevo.presentation.settings.SettingsViewModel
import com.aevrontech.finevo.ui.components.GlassmorphicNavBar
import com.aevrontech.finevo.ui.components.NavBarItem
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
        ) { _ ->
            val tabNavigator = LocalTabNavigator.current
            val tabs = listOf(DashboardTab, ExpenseTab, DebtTab, HabitTab, SettingsTab)
            val selectedIndex = tabs.indexOf(tabNavigator.current)

            Box(
                    modifier =
                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                // Content area with bottom padding for nav bar
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(
                                                bottom = 120.dp
                                        ) // Space for floating nav + system nav
                ) { CurrentTab() }

                // Glassmorphic Floating Navigation Bar
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding() // Respect system
                        // navigation bars
                        ) {
                    GlassmorphicNavBar(
                            items =
                                    listOf(
                                            NavBarItem(Icons.Filled.Home, "Home"),
                                            NavBarItem(Icons.Filled.List, "Expenses"),
                                            NavBarItem(Icons.Filled.ShoppingCart, "Debts"),
                                            NavBarItem(Icons.Filled.CheckCircle, "Habits"),
                                            NavBarItem(Icons.Filled.Settings, "Settings")
                                    ),
                            selectedIndex = selectedIndex,
                            onItemSelected = { index -> tabNavigator.current = tabs[index] }
                    )
                }
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
                    color = ThemeColors.onSurface
            )
        }

        // Balance Card
        item {
            Card(
                    modifier = Modifier.fillMaxWidth().height(240.dp), // Increased height
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                            )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // SVG Pattern Background
                    DashboardCardBackground()

                    // Content Overlay
                    Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                    text = "Your available balance",
                                    color =
                                            androidx.compose.ui.graphics.Color.White.copy(
                                                    alpha = 0.9f
                                            ),
                                    fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = "$ 24,500",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White
                            )
                        }

                        Spacer(
                                modifier = Modifier.height(16.dp)
                        ) // Space between balance and income/expense

                        // Income/Expense Row with translucent background
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(
                                                        color =
                                                                androidx.compose.ui.graphics.Color
                                                                        .White.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BalanceItem(
                                    label = "Income",
                                    amount = "$5,086",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    icon = Icons.Filled.KeyboardArrowDown
                            )
                            BalanceItem(
                                    label = "Expense",
                                    amount = "$5,086",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    icon = Icons.Filled.KeyboardArrowUp
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions - Added margin/padding as requested
        item {
            Spacer(modifier = Modifier.height(8.dp)) // Extra spacing between card and actions
        }

        item {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                        icon = Icons.Filled.Refresh, // Was SwapVert (Transfer)
                        label = "Transfer",
                        color = ActionTransfer,
                        backgroundColor = ActionTransferBg
                )
                QuickActionButton(
                        icon = Icons.Filled.Add, // Was CreditCard (Top-up)
                        label = "Top-up",
                        color = ActionTopUp,
                        backgroundColor = ActionTopUpBg
                )
                QuickActionButton(
                        icon = Icons.Filled.List, // Was Receipt (Bill)
                        label = "Bill",
                        color = ActionBill,
                        backgroundColor = ActionBillBg
                )
                QuickActionButton(
                        icon = Icons.Filled.Menu, // Was GridView (More)
                        label = "More",
                        color = ActionMode,
                        backgroundColor = ActionModeBg
                )
            }
        }

        // Coming soon placeholder
        item {
            Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeColors.surfaceContainer)
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
                            color = ThemeColors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCardBackground() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Gradient Background
        drawRect(
                brush =
                        Brush.linearGradient(
                                colors =
                                        listOf(
                                                DashboardGradientStart,
                                                DashboardGradientMid,
                                                DashboardGradientEnd
                                        ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(width, height)
                        )
        )

        // Bubble Groups - Group 1 (Top Left)
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.1f, y = height * 0.13f),
                alpha = 0.25f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.15f, y = height * 0.2f),
                alpha = 0.25f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.21f, y = height * 0.15f),
                alpha = 0.25f
        )

        // Bubble Groups - Group 2 (Top Right)
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.83f, y = height * 0.11f),
                alpha = 0.25f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.89f, y = height * 0.20f),
                alpha = 0.25f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.94f, y = height * 0.14f),
                alpha = 0.25f
        )

        // Bubble Groups - Group 3 (Left Mid)
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.14f, y = height * 0.39f),
                alpha = 0.15f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.18f, y = height * 0.47f),
                alpha = 0.15f
        )

        // Bubble Groups - Group 4 (Right Mid)
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 7.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.76f, y = height * 0.55f),
                alpha = 0.15f
        )
        drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = width * 0.90f, y = height * 0.58f),
                alpha = 0.15f
        )
        // Large bubbles removed as per user request
    }
}

@Composable
private fun BalanceItem(
        label: String,
        amount: String,
        color: androidx.compose.ui.graphics.Color,
        icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
                modifier =
                        Modifier.size(32.dp)
                                .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = color.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(text = amount, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun QuickActionButton(
        icon: ImageVector,
        label: String,
        color: androidx.compose.ui.graphics.Color,
        backgroundColor: androidx.compose.ui.graphics.Color,
        modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable {}) {
        Box(
                modifier =
                        Modifier.size(56.dp) // Larger square icon bg
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

    var showAddTransaction by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }
    var transactionToEdit by remember {
        mutableStateOf<com.aevrontech.finevo.domain.model.Transaction?>(null)
    }

    // Show Edit Transaction screen
    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        com.aevrontech.finevo.presentation.expense.AddTransactionScreen(
                transactionType = tx.type,
                accounts = expenseState.accounts,
                categories = expenseState.categories,
                selectedAccount = expenseState.accounts.find { it.id == tx.accountId },
                editingTransaction = tx,
                onDismiss = { transactionToEdit = null },
                onConfirm = { type, amount, accountId, categoryId, note, date, time ->
                    expenseViewModel.updateTransaction(
                            tx.id,
                            type,
                            amount,
                            accountId,
                            categoryId,
                            note,
                            date
                    )
                    transactionToEdit = null
                }
        )
        return
    }

    // Show AddTransaction screen
    if (showAddTransaction) {
        com.aevrontech.finevo.presentation.expense.AddTransactionScreen(
                transactionType = expenseState.selectedTransactionType,
                accounts = expenseState.accounts,
                categories = expenseState.categories,
                selectedAccount = expenseState.selectedAccount,
                onDismiss = { showAddTransaction = false },
                onConfirm = { type, amount, accountId, categoryId, note, date, time ->
                    expenseViewModel.addTransaction(type, amount, accountId, categoryId, note, date)
                    showAddTransaction = false
                }
        )
        return
    }

    // Account options dialog (for long-press edit/delete)
    var accountToManage by remember {
        mutableStateOf<com.aevrontech.finevo.domain.model.Account?>(null)
    }
    var showEditAccount by remember { mutableStateOf(false) }

    // Show Edit Account screen
    if (showEditAccount && accountToManage != null) {
        val acc = accountToManage!!
        com.aevrontech.finevo.presentation.expense.AddAccountScreen(
                onDismiss = {
                    showEditAccount = false
                    accountToManage = null
                },
                onConfirm = { name, balance, currency, type, color ->
                    accountViewModel.updateAccount(acc.id, name, balance, currency, type, color)
                    showEditAccount = false
                    accountToManage = null
                },
                defaultCurrency = acc.currency,
                editingAccount = acc
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

    // Account options dialog (after long-press)
    if (accountToManage != null) {
        AlertDialog(
                onDismissRequest = { accountToManage = null },
                title = { Text("Account Options") },
                text = { Text("What would you like to do with \"${accountToManage!!.name}\"?") },
                confirmButton = {
                    TextButton(onClick = { showEditAccount = true }) {
                        Text("Edit", color = Primary)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { accountToManage = null }) { Text("Cancel") }
                        TextButton(
                                onClick = {
                                    accountToManage?.let { accountViewModel.deleteAccount(it.id) }
                                    accountToManage = null
                                }
                        ) { Text("Delete", color = Error) }
                    }
                }
        )
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
                        onAccountLongClick = { account -> accountToManage = account },
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

            // Transaction List - Grouped by Date
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
                // Group transactions by date
                val groupedTransactions = groupTransactionsByDate(expenseState.transactions)

                groupedTransactionItems(
                        groups = groupedTransactions,
                        onTransactionClick = { tx -> transactionToEdit = tx },
                        onTransactionDelete = { tx -> expenseViewModel.deleteTransaction(tx.id) }
                )
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
                            "${if (transaction.type == com.aevrontech.finevo.domain.model.TransactionType.EXPENSE) "-" else "+"} RM ${
                        String.format(
                            "%.2f",
                            transaction.amount
                        )
                    }",
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
    var showCategoryManagement by remember { mutableStateOf(false) }

    // Observe dark mode state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    // Show Category Management Screen
    if (showCategoryManagement) {
        com.aevrontech.finevo.presentation.category.CategoryManagementScreen(
                onBack = { showCategoryManagement = false }
        )
        return
    }

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
                    color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Appearance Section - Dark Mode Toggle
        item {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isDarkMode) "🌙" else "☀️", fontSize = 24.sp)
                        Column {
                            Text(
                                    "Dark Mode",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                    if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled -> ThemeManager.setDarkMode(enabled) },
                            colors =
                                    SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor =
                                                    MaterialTheme.colorScheme.primaryContainer
                                    )
                    )
                }
            }
        }

        // Categories Section
        item {
            Card(
                    modifier = Modifier.fillMaxWidth().clickable { showCategoryManagement = true },
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📁", fontSize = 24.sp)
                        Column {
                            Text(
                                    "Manage Categories",
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                            )
                            Text(
                                    "Add, edit, or delete categories",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                            )
                        }
                    }
                    Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                    )
                }
            }
        }

        // Account Section
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

        // App Info
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
