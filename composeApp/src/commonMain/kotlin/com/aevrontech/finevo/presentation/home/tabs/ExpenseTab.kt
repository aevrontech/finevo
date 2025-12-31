package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.presentation.expense.AccountCardsRow
import com.aevrontech.finevo.presentation.expense.AccountSummaryCard
import com.aevrontech.finevo.presentation.expense.AccountViewModel
import com.aevrontech.finevo.presentation.expense.AddAccountScreen
import com.aevrontech.finevo.presentation.expense.AddTransactionScreen
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.expense.groupTransactionsByDate
import com.aevrontech.finevo.presentation.expense.groupedTransactionItems
import com.aevrontech.finevo.presentation.home.LocalSetNavBarVisible
import com.aevrontech.finevo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

object ExpenseTab : Tab {
    override val options: TabOptions
        @Composable
        get() =
            TabOptions(
                index = 1u,
                title = "Wallet",
                icon =
                    androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Star
                    )
            )

    @Composable
    override fun Content() {
        ExpenseTabContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseTabContent() {
    val expenseViewModel: ExpenseViewModel = koinViewModel()
    val accountViewModel: AccountViewModel = koinViewModel()
    val expenseState by expenseViewModel.uiState.collectAsState()

    // Nav bar visibility control
    val setNavBarVisible = LocalSetNavBarVisible.current

    // Overlay states - managed locally in this tab
    var showAddTransaction by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var showAddAccount by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var defaultCurrency by remember { mutableStateOf("MYR") }

    // Account management dialog state
    var accountToManage by remember { mutableStateOf<Account?>(null) }

    // Update nav bar visibility when any overlay is shown
    val isOverlayVisible =
        showAddTransaction ||
            transactionToEdit != null ||
            showAddAccount ||
            accountToEdit != null

    LaunchedEffect(isOverlayVisible) { setNavBarVisible?.invoke(!isOverlayVisible) }

    // Account options dialog
    if (accountToManage != null) {
        AlertDialog(
            onDismissRequest = { accountToManage = null },
            title = { Text("Account Options") },
            text = { Text("What would you like to do with \"${accountToManage!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountToManage?.let { accountToEdit = it }
                        accountToManage = null
                    }
                ) { Text("Edit", color = Primary) }
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
        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 160.dp),
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Account Cards - Horizontal Scroll
            item {
                AccountCardsRow(
                    accounts = expenseState.accounts,
                    selectedAccount = expenseState.selectedAccount,
                    onAccountClick = { account -> expenseViewModel.selectAccount(account) },
                    onAccountLongClick = { account -> accountToManage = account },
                    onAddAccountClick = {
                        defaultCurrency = "MYR"
                        showAddAccount = true
                    }
                )
            }

            // Account Summary Card
            item {
                AccountSummaryCard(
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "This Month",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                val groupedTransactions = groupTransactionsByDate(expenseState.transactions)
                groupedTransactionItems(
                    groups = groupedTransactions,
                    onTransactionClick = { tx -> transactionToEdit = tx },
                    onTransactionDelete = { tx -> expenseViewModel.deleteTransaction(tx.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // FAB
        GradientFab(
            onClick = {
                transactionType = TransactionType.EXPENSE
                showAddTransaction = true
            },
            icon = Icons.Default.Add,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 130.dp, end = 20.dp)
        )

        // ========== OVERLAYS ==========

        // Add Transaction Overlay
        AnimatedVisibility(
            visible = showAddTransaction,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            AddTransactionScreen(
                transactionType = transactionType,
                accounts = expenseState.accounts,
                categories = expenseState.categories,
                selectedAccount = expenseState.selectedAccount,
                onDismiss = { showAddTransaction = false },
                onConfirm = { type, amount, accountId, categoryId, note, date, _ ->
                    expenseViewModel.addTransaction(
                        type,
                        amount,
                        accountId,
                        categoryId,
                        note,
                        date
                    )
                    showAddTransaction = false
                }
            )
        }

        // Edit Transaction Overlay
        AnimatedVisibility(
            visible = transactionToEdit != null,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            transactionToEdit?.let { tx ->
                AddTransactionScreen(
                    transactionType = tx.type,
                    accounts = expenseState.accounts,
                    categories = expenseState.categories,
                    selectedAccount = expenseState.accounts.find { it.id == tx.accountId },
                    editingTransaction = tx,
                    onDismiss = { transactionToEdit = null },
                    onConfirm = { type, amount, accountId, categoryId, note, date, _ ->
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
            }
        }

        // Add Account Overlay
        AnimatedVisibility(
            visible = showAddAccount,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            AddAccountScreen(
                onDismiss = { showAddAccount = false },
                onConfirm = { name, balance, currency, type, color ->
                    accountViewModel.createAccount(name, balance, currency, type, color)
                    showAddAccount = false
                },
                defaultCurrency = defaultCurrency
            )
        }

        // Edit Account Overlay
        AnimatedVisibility(
            visible = accountToEdit != null,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) {
            accountToEdit?.let { acc ->
                AddAccountScreen(
                    onDismiss = { accountToEdit = null },
                    onConfirm = { name, balance, currency, type, color ->
                        accountViewModel.updateAccount(
                            acc.id,
                            name,
                            balance,
                            currency,
                            type,
                            color
                        )
                        accountToEdit = null
                    },
                    defaultCurrency = acc.currency,
                    editingAccount = acc
                )
            }
        }
    }
}

@Composable
internal fun GradientFab(onClick: () -> Unit, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.size(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = HabitGradientStart.copy(alpha = 0.5f),
                    spotColor = HabitGradientStart.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    HabitGradientStart,
                                    HabitGradientEnd
                                )
                        )
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
internal fun TransactionItem(transaction: Transaction, modifier: Modifier = Modifier) {
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
                    "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"} RM ${
                        String.format("%.2f",
                            transaction.amount)
                    }",
                color = if (transaction.type == TransactionType.EXPENSE) Expense else Income,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
