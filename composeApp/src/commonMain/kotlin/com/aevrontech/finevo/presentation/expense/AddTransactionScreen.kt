package com.aevrontech.finevo.presentation.expense

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.HabitGradientEnd
import com.aevrontech.finevo.ui.theme.HabitGradientStart
import com.aevrontech.finevo.ui.theme.Success
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** Comprehensive transaction entry screen with calculator. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionType: TransactionType,
    accounts: List<Account>,
    categories: List<Category>,
    selectedAccount: Account?,
    editingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm:
        (
        type: TransactionType,
        amount: Double,
        accountId: String?,
        categoryId: String,
        note: String?,
        date: LocalDate,
        time: String?) -> Unit
) {
    val isEditing = editingTransaction != null

    var expression by remember {
        mutableStateOf(
            if (isEditing) String.format("%.2f", editingTransaction?.amount ?: 0.0) else ""
        )
    }
    var computedAmount by remember { mutableStateOf(editingTransaction?.amount ?: 0.0) }
    var selectedAccountLocal by remember { mutableStateOf(selectedAccount) }
    var selectedCategory by remember {
        mutableStateOf<Category?>(
            if (isEditing) categories.find { it.id == editingTransaction?.categoryId } else null
        )
    }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            editingTransaction?.date ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
        )
    }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(editingTransaction?.type ?: transactionType) }

    // Filter categories by type
    val filteredCategories = categories.filter { it.type == type }

    // Select first category if none selected
    LaunchedEffect(filteredCategories) {
        if (selectedCategory == null || selectedCategory?.type != type) {
            selectedCategory = filteredCategories.firstOrNull()
        }
    }

    val isValid = computedAmount > 0 && selectedCategory != null

    // Theme colors
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) {
                            if (type == TransactionType.EXPENSE) "Edit Expense"
                            else "Edit Income"
                        } else {
                            if (type == TransactionType.EXPENSE) "Add Expense"
                            else "Add Income"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = onSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        containerColor = surfaceColor
    ) { padding ->
        var showCalculator by remember { mutableStateOf(true) }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ===== EXPENSE/INCOME TOGGLE =====
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(shape = RoundedCornerShape(50), color = surfaceContainerColor) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        TypeToggleButton(
                            text = "Expense",
                            isSelected = type == TransactionType.EXPENSE,
                            color = Error,
                            unselectedColor = onSurfaceVariantColor,
                            onClick = { type = TransactionType.EXPENSE }
                        )
                        TypeToggleButton(
                            text = "Income",
                            isSelected = type == TransactionType.INCOME,
                            color = Success,
                            unselectedColor = onSurfaceVariantColor,
                            onClick = { type = TransactionType.INCOME }
                        )
                    }
                }
            }

            // ===== AMOUNT DISPLAY (Clickable to open calculator) =====
            Surface(
                onClick = { showCalculator = true },
                modifier =
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color =
                    if (type == TransactionType.EXPENSE) Error.copy(alpha = 0.1f)
                    else Success.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        selectedAccountLocal?.currency ?: "MYR",
                        fontSize = 14.sp,
                        color = onSurfaceVariantColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (expression.isEmpty()) "0.00" else expression,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (type == TransactionType.EXPENSE) Error else Success,
                        maxLines = 1
                    )
                    if (expression.isNotEmpty() && expression.any { it in "÷×−+" }) {
                        Text(
                            "= ${formatAmount(computedAmount)}",
                            fontSize = 16.sp,
                            color = onSurfaceVariantColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to open calculator",
                        fontSize = 11.sp,
                        color = onSurfaceVariantColor.copy(alpha = 0.6f)
                    )
                }
            }

            // ===== SCROLLABLE FIELDS SECTION =====
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Account Selector
                if (accounts.isNotEmpty()) {
                    FieldRow(
                        icon = Icons.Default.Home,
                        label = "Account",
                        value = selectedAccountLocal?.name ?: "Select Account",
                        surfaceColor = surfaceContainerColor,
                        labelColor = onSurfaceVariantColor,
                        valueColor = onSurfaceColor,
                        onClick = {
                            showCalculator = false
                            showAccountPicker = true
                        }
                    )
                }

                // Category Selector
                FieldRow(
                    icon =
                        if (selectedCategory != null) null
                        else Icons.AutoMirrored.Filled.List,
                    emoji = selectedCategory?.icon,
                    label = "Category",
                    value = selectedCategory?.name ?: "Select Category",
                    surfaceColor = surfaceContainerColor,
                    labelColor = onSurfaceVariantColor,
                    valueColor = onSurfaceColor,
                    onClick = {
                        showCalculator = false
                        showCategoryPicker = true
                    }
                )

                // Date Selector
                FieldRow(
                    icon = Icons.Default.DateRange,
                    label = "Date",
                    value = formatDate(selectedDate),
                    surfaceColor = surfaceContainerColor,
                    labelColor = onSurfaceVariantColor,
                    valueColor = onSurfaceColor,
                    onClick = {
                        showCalculator = false
                        showDatePicker = true
                    }
                )

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add note...") },
                    modifier = Modifier.fillMaxWidth().clickable { showCalculator = false },
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = surfaceContainerColor,
                            focusedContainerColor = surfaceContainerColor,
                            unfocusedContainerColor = surfaceContainerColor
                        ),
                    maxLines = 2,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = onSurfaceVariantColor
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===== GRADIENT SAVE BUTTON =====
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp)
                        .shadow(
                            elevation = if (isValid) 8.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = HabitGradientStart.copy(alpha = 0.3f),
                            spotColor = HabitGradientStart.copy(alpha = 0.2f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush =
                                if (isValid)
                                    Brush.horizontalGradient(
                                        listOf(
                                            HabitGradientStart,
                                            HabitGradientEnd
                                        )
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(
                                            onSurfaceVariantColor
                                                .copy(
                                                    alpha =
                                                        0.3f
                                                ),
                                            onSurfaceVariantColor
                                                .copy(
                                                    alpha =
                                                        0.3f
                                                )
                                        )
                                    )
                        )
                        .clickable(enabled = isValid) {
                            selectedCategory?.let { cat ->
                                onConfirm(
                                    type,
                                    computedAmount,
                                    selectedAccountLocal?.id,
                                    cat.id,
                                    note.takeIf { it.isNotBlank() },
                                    selectedDate,
                                    null
                                )
                            }
                        },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEditing) "Update Transaction" else "Save Transaction",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        // ===== CALCULATOR BOTTOM SHEET =====
        if (showCalculator) {
            ModalBottomSheet(
                onDismissRequest = { showCalculator = false },
                containerColor = surfaceContainerHighestColor,
                dragHandle = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier =
                                Modifier.width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        onSurfaceVariantColor.copy(alpha = 0.4f)
                                    )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            ) {
                CalculatorKeypad(
                    expression = expression,
                    onExpressionChange = { newExpr ->
                        expression = newExpr
                        computedAmount = evaluateExpression(newExpr)
                    },
                    onEquals = {
                        if (expression.isNotEmpty()) {
                            val result = evaluateExpression(expression)
                            expression = formatAmount(result)
                            computedAmount = result
                        }
                        showCalculator = false
                    },
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
                )
            }
        }
    }

    // Category picker bottom sheet
    if (showCategoryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false },
            containerColor = surfaceColor
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Select Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                filteredCategories.forEach { category ->
                    Surface(
                        onClick = {
                            selectedCategory = category
                            showCategoryPicker = false
                        },
                        color =
                            if (category == selectedCategory)
                                primaryColor.copy(alpha = 0.1f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.icon, fontSize = 24.sp)
                            Text(
                                category.name,
                                color = onSurfaceColor,
                                modifier = Modifier.weight(1f)
                            )
                            if (category == selectedCategory) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Account picker bottom sheet
    if (showAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAccountPicker = false },
            containerColor = surfaceColor
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Select Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                accounts.forEach { account ->
                    Surface(
                        onClick = {
                            selectedAccountLocal = account
                            showAccountPicker = false
                        },
                        color =
                            if (account == selectedAccountLocal)
                                primaryColor.copy(alpha = 0.1f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(account.icon, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    account.name,
                                    color = onSurfaceColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${account.currency} ${formatAmount(account.balance)}",
                                    fontSize = 12.sp,
                                    color = onSurfaceVariantColor
                                )
                            }
                            if (account == selectedAccountLocal) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date picker
    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate =
                                Instant.fromEpochMilliseconds(millis)
                                    .toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    )
                                    .date
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun TypeToggleButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) color else Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else unselectedColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emoji: String? = null,
    label: String,
    value: String,
    surfaceColor: Color,
    labelColor: Color,
    valueColor: Color,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = surfaceColor) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 24.sp)
            } else if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = labelColor)
                Text(
                    value,
                    fontSize = 15.sp,
                    color = valueColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format("%.2f", amount)
}

private fun formatDate(date: LocalDate): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when {
        date == today -> "Today"
        date == today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else ->
            "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
    }
}
