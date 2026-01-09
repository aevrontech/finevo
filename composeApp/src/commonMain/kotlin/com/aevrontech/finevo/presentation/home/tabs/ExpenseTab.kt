package com.aevrontech.finevo.presentation.home.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.aevrontech.finevo.presentation.expense.AccountViewModel
import com.aevrontech.finevo.presentation.expense.AddAccountScreen
import com.aevrontech.finevo.presentation.expense.AddTransactionScreen
import com.aevrontech.finevo.presentation.expense.BarChartItem
import com.aevrontech.finevo.presentation.expense.ExpenseReportScreen
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.expense.FilterPeriod
import com.aevrontech.finevo.presentation.expense.IncomeExpenseCards
import com.aevrontech.finevo.presentation.expense.TimeFilterSection
import com.aevrontech.finevo.presentation.expense.TimeRangeSelectionSheet
import com.aevrontech.finevo.presentation.expense.groupTransactionsByDate
import com.aevrontech.finevo.presentation.expense.groupedTransactionItems
import com.aevrontech.finevo.presentation.home.LocalSetNavBarVisible
import com.aevrontech.finevo.presentation.label.LabelViewModel
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.HabitGradientEnd
import com.aevrontech.finevo.ui.theme.HabitGradientStart
import com.aevrontech.finevo.ui.theme.Primary
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
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
    val labelViewModel: LabelViewModel = koinViewModel()
    val labelUiState by labelViewModel.uiState.collectAsState()
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

    // Filter state
    val filterPeriod by expenseViewModel.filterPeriod.collectAsState()
    val periodOffset by expenseViewModel.periodOffset.collectAsState()
    var showReportScreen by remember { mutableStateOf(false) }

    // List of transactions to show in Wallet Tab
    // We use the raw loaded transactions (Current Month) instead of 'getFilteredTransactions()'
    // because 'getFilteredTransactions' depends on the filterPeriod/offset which might be
    // changed in the Report Screen (e.g. looking at last month), causing this list to be empty
    // if we return to this screen while the filter is set to the past.
    val filteredTransactions =
        remember(expenseState.transactions) {
            expenseState.transactions.sortedWith(
                compareByDescending<Transaction> { it.date }.thenByDescending {
                    it.createdAt
                }
            )
        }

    val transactionGroups =
        remember(filteredTransactions) { groupTransactionsByDate(filteredTransactions) }

    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        TimeRangeSelectionSheet(
            currentRange = expenseState.timeRange,
            onRangeSelected = { expenseViewModel.setTimeRange(it) },
            onDismissRequest = { showFilterSheet = false }
        )
    }

    // Bar Chart Data computation
    // Bar Chart Data computation
    val barChartData =
        remember(filteredTransactions, filterPeriod) {
            val expenseTransactions =
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }

            when (filterPeriod) {
                FilterPeriod.DAY -> { // Group by Hour (0-23)
                    val grouped =
                        expenseTransactions.groupBy {
                            it.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
                        }
                    (0..23).map { hour ->
                        val total = grouped[hour]?.sumOf { it.amount } ?: 0.0
                        val label = if (hour % 6 == 0) "${hour}h" else ""
                        BarChartItem(label = label, value = total)
                    }
                }
                FilterPeriod.WEEK -> { // Group by Day of Week (Mon-Sun)
                    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val grouped = expenseTransactions.groupBy { it.date.dayOfWeek.ordinal }
                    dayNames.mapIndexed { index, name ->
                        val total = grouped[index]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = name, value = total)
                    }
                }
                FilterPeriod.MONTH -> { // Group by Day of Month (1-31)
                    val grouped = expenseTransactions.groupBy { it.date.dayOfMonth }
                    (1..31).map { day ->
                        val total = grouped[day]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = day.toString(), value = total)
                    }
                }
                FilterPeriod.YEAR -> { // Group by Month (J, F, M...)
                    val monthNames =
                        listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                    val grouped = expenseTransactions.groupBy { it.date.monthNumber }
                    monthNames.mapIndexed { index, name ->
                        val total = grouped[index + 1]?.sumOf { it.amount } ?: 0.0
                        BarChartItem(label = name, value = total)
                    }
                }
            }
        }

    // Update nav bar visibility when any overlay is shown
    val isOverlayVisible =
        showAddTransaction ||
            transactionToEdit != null ||
            showAddAccount ||
            accountToEdit != null ||
            showReportScreen
    LaunchedEffect(isOverlayVisible) { setNavBarVisible?.invoke(!isOverlayVisible) }

    // Delete confirmation state
    var accountToDelete by remember { mutableStateOf<Account?>(null) }

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
                            accountToDelete = accountToManage
                            accountToManage = null
                        }
                    ) { Text("Delete", color = Error) }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Delete Account") },
            text = {
                Text(
                    "Are you sure you want to delete \"${accountToDelete!!.name}\"? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountToDelete?.let { accountViewModel.deleteAccount(it.id) }
                        accountToDelete = null
                    }
                ) { Text("Delete", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) { // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { // Header
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

                    if (expenseState.selectedAccounts.size < expenseState.accounts.size) {
                        TextButton(onClick = { expenseViewModel.selectAllAccounts() }) {
                            Text("Select All")
                        }
                    } else {
                        // Show Filter Icon when Select All is not needed (or always?)
                        // User said "same line with account title".
                        // I'll show it always, maybe next to Select All if present.
                    }

                    // Select All is not needed, user wants FilterBar below
                }
            }

            // Account Cards - Horizontal Scroll
            item {
                AccountCardsRow(
                    accounts = expenseState.accounts,
                    selectedAccounts = expenseState.selectedAccounts,
                    onAccountClick = { account ->
                        expenseViewModel.toggleAccountSelection(account)
                    },
                    onAccountLongClick = { account -> accountToManage = account },
                    onAddAccountClick = {
                        defaultCurrency = "MYR"
                        showAddAccount = true
                    }
                )
            }

            // Account Summary Card
            // Income and Expense Summary Cards (Percentage Round)
            item {
                TimeFilterSection(
                    currentRange = expenseState.timeRange,
                    onNavigate = { direction -> expenseViewModel.navigateTimeRange(direction) },
                    onFilterClick = { showFilterSheet = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                IncomeExpenseCards(
                    income = expenseState.accountIncome,
                    expense = expenseState.accountExpense,
                    currencySymbol = expenseState.currencySymbol,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8.dp
            }

            // Transaction History - Lazy Loaded

            if (transactionGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions here",
                            color =
                                androidx.compose.material3.MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                groupedTransactionItems(
                    groups = transactionGroups,
                    availableLabels = labelUiState.labels,
                    currencySymbol = expenseState.currencySymbol,
                    onTransactionClick = { transaction -> transactionToEdit = transaction },
                    onTransactionDelete = { transaction ->
                        expenseViewModel.deleteTransaction(transaction.id)
                    }
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
                selectedAccount = expenseState.selectedAccounts.firstOrNull()
                    ?: expenseState.accounts.firstOrNull(),
                availableLabels = labelUiState.labels,
                onDismiss = { showAddTransaction = false },
                onConfirm = { type,
                              amount,
                              accountId,
                              categoryId,
                              note,
                              date,
                              time,
                              locationName,
                              locationLat,
                              locationLng,
                              labels,
                              photoPath ->
                    expenseViewModel.addTransaction(
                        type,
                        amount,
                        accountId,
                        categoryId,
                        note,
                        date,
                        time,
                        locationName,
                        locationLat,
                        locationLng,
                        labels,
                        photoPath
                    )
                    showAddTransaction = false
                },
                onAddLabel = { name, color, auto -> labelViewModel.addLabel(name, color, auto) }
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
                    availableLabels = labelUiState.labels,
                    editingTransaction = tx,
                    onDismiss = { transactionToEdit = null },
                    onConfirm = { type,
                                  amount,
                                  accountId,
                                  categoryId,
                                  note,
                                  date,
                                  time,
                                  locationName,
                                  locationLat,
                                  locationLng,
                                  labels,
                                  photoPath ->
                        expenseViewModel.updateTransaction(
                            tx.id,
                            type,
                            amount,
                            accountId,
                            categoryId,
                            note,
                            date,
                            time,
                            locationName,
                            locationLat,
                            locationLng,
                            labels,
                            photoPath
                        )
                        transactionToEdit = null
                    },
                    onAddLabel = { name, color, auto ->
                        labelViewModel.addLabel(name, color, auto)
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

        // Report Screen Overlay
        AnimatedVisibility(
            visible = showReportScreen,
            enter =
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)) +
                    fadeIn(tween(200)),
            exit =
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) +
                    fadeOut(tween(200))
        ) { ExpenseReportScreen(onDismiss = { showReportScreen = false }) }
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

private fun getPeriodLabelForReport(period: FilterPeriod, offset: Int): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return when (period) {
        FilterPeriod.DAY -> {
            val targetDate = today.plus(offset, DateTimeUnit.DAY)
            "${targetDate.dayOfMonth} ${targetDate.month.name.take(3)} ${targetDate.year}"
        }
        FilterPeriod.WEEK -> {
            val weekStart =
                today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    .plus(offset * 7, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            "${weekStart.dayOfMonth} ${weekStart.month.name.take(3)} - ${weekEnd.dayOfMonth} ${weekEnd.month.name.take(3)}"
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
