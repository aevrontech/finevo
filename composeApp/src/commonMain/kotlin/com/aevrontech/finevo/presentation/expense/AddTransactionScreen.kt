package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.*
import com.aevrontech.finevo.ui.theme.*
import kotlinx.datetime.*

/** Comprehensive transaction entry screen with calculator. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
        transactionType: TransactionType,
        accounts: List<Account>,
        categories: List<Category>,
        selectedAccount: Account?,
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
        var expression by remember { mutableStateOf("") }
        var computedAmount by remember { mutableStateOf(0.0) }
        var selectedAccountLocal by remember { mutableStateOf(selectedAccount) }
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var note by remember { mutableStateOf("") }
        var selectedDate by remember {
                mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
        }
        var showCategoryPicker by remember { mutableStateOf(false) }
        var showAccountPicker by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        var type by remember { mutableStateOf(transactionType) }

        // Filter categories by type
        val filteredCategories = categories.filter { it.type == type }

        // Select first category if none selected
        LaunchedEffect(filteredCategories) {
                if (selectedCategory == null || selectedCategory?.type != type) {
                        selectedCategory = filteredCategories.firstOrNull()
                }
        }

        val isValid = computedAmount > 0 && selectedCategory != null

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                if (type == TransactionType.EXPENSE) "Add Expense"
                                                else "Add Income",
                                                fontWeight = FontWeight.SemiBold
                                        )
                                },
                                navigationIcon = {
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Close"
                                                )
                                        }
                                },
                                actions = {
                                        TextButton(
                                                onClick = {
                                                        selectedCategory?.let { cat ->
                                                                onConfirm(
                                                                        type,
                                                                        computedAmount,
                                                                        selectedAccountLocal?.id,
                                                                        cat.id,
                                                                        note.takeIf {
                                                                                it.isNotBlank()
                                                                        },
                                                                        selectedDate,
                                                                        null
                                                                )
                                                        }
                                                },
                                                enabled = isValid
                                        ) {
                                                Text(
                                                        "Save",
                                                        color =
                                                                if (isValid) Primary
                                                                else OnSurfaceVariant,
                                                        fontWeight = FontWeight.SemiBold
                                                )
                                        }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                        )
                },
                containerColor = Surface
        ) { padding ->
                var showCalculator by remember { mutableStateOf(true) }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                        // ===== EXPENSE/INCOME TOGGLE =====
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                        ) {
                                Surface(shape = RoundedCornerShape(50), color = SurfaceContainer) {
                                        Row(modifier = Modifier.padding(4.dp)) {
                                                TypeToggleButton(
                                                        text = "Expense",
                                                        isSelected =
                                                                type == TransactionType.EXPENSE,
                                                        color = Error,
                                                        onClick = { type = TransactionType.EXPENSE }
                                                )
                                                TypeToggleButton(
                                                        text = "Income",
                                                        isSelected = type == TransactionType.INCOME,
                                                        color = Success,
                                                        onClick = { type = TransactionType.INCOME }
                                                )
                                        }
                                }
                        }

                        // ===== AMOUNT DISPLAY (Clickable to open calculator) =====
                        Surface(
                                onClick = { showCalculator = true },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color =
                                        if (type == TransactionType.EXPENSE)
                                                Error.copy(alpha = 0.1f)
                                        else Success.copy(alpha = 0.1f)
                        ) {
                                Column(
                                        modifier = Modifier.padding(vertical = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                selectedAccountLocal?.currency ?: "MYR",
                                                fontSize = 14.sp,
                                                color = OnSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                                if (expression.isEmpty()) "0.00" else expression,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color =
                                                        if (type == TransactionType.EXPENSE) Error
                                                        else Success,
                                                maxLines = 1
                                        )
                                        if (expression.isNotEmpty() &&
                                                        expression.any { it in "÷×−+" }
                                        ) {
                                                Text(
                                                        "= ${formatAmount(computedAmount)}",
                                                        fontSize = 16.sp,
                                                        color = OnSurfaceVariant,
                                                        fontWeight = FontWeight.Medium
                                                )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                "Tap to open calculator",
                                                fontSize = 11.sp,
                                                color = OnSurfaceVariant.copy(alpha = 0.6f)
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
                                                value = selectedAccountLocal?.name
                                                                ?: "Select Account",
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
                                                else Icons.Default.List,
                                        emoji = selectedCategory?.icon,
                                        label = "Category",
                                        value = selectedCategory?.name ?: "Select Category",
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
                                        modifier =
                                                Modifier.fillMaxWidth().clickable {
                                                        showCalculator = false
                                                },
                                        shape = RoundedCornerShape(12.dp),
                                        colors =
                                                OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Primary,
                                                        unfocusedBorderColor = SurfaceContainer,
                                                        focusedContainerColor = SurfaceContainer,
                                                        unfocusedContainerColor = SurfaceContainer
                                                ),
                                        maxLines = 2,
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = null,
                                                        tint = OnSurfaceVariant
                                                )
                                        }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                        }
                }

                // ===== CALCULATOR BOTTOM SHEET =====
                if (showCalculator) {
                        ModalBottomSheet(
                                onDismissRequest = { showCalculator = false },
                                containerColor = SurfaceContainerHighest,
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
                                                                        .clip(
                                                                                RoundedCornerShape(
                                                                                        2.dp
                                                                                )
                                                                        )
                                                                        .background(
                                                                                OnSurfaceVariant
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.4f
                                                                                        )
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
                                        modifier =
                                                Modifier.padding(
                                                        start = 20.dp,
                                                        end = 20.dp,
                                                        bottom = 32.dp
                                                )
                                )
                        }
                }
        }

        // Category picker bottom sheet
        if (showCategoryPicker) {
                ModalBottomSheet(
                        onDismissRequest = { showCategoryPicker = false },
                        containerColor = Surface
                ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                Text(
                                        "Select Category",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
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
                                                                Primary.copy(alpha = 0.1f)
                                                        else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                        ) {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical = 12.dp,
                                                                                horizontal = 12.dp
                                                                        ),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(12.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(category.icon, fontSize = 24.sp)
                                                        Text(
                                                                category.name,
                                                                color = OnSurface,
                                                                modifier = Modifier.weight(1f)
                                                        )
                                                        if (category == selectedCategory) {
                                                                Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = Primary,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
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
                        containerColor = Surface
                ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                Text(
                                        "Select Account",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
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
                                                                Primary.copy(alpha = 0.1f)
                                                        else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                        ) {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical = 12.dp,
                                                                                horizontal = 12.dp
                                                                        ),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(12.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(account.icon, fontSize = 24.sp)
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        account.name,
                                                                        color = OnSurface,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                Text(
                                                                        "${account.currency} ${formatAmount(account.balance)}",
                                                                        fontSize = 12.sp,
                                                                        color = OnSurfaceVariant
                                                                )
                                                        }
                                                        if (account == selectedAccountLocal) {
                                                                Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = Primary,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
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
                                                                Instant.fromEpochMilliseconds(
                                                                                millis
                                                                        )
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
private fun TypeToggleButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
        Surface(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                color = if (isSelected) color else Color.Transparent
        ) {
                Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else OnSurfaceVariant,
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
        onClick: () -> Unit
) {
        Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = SurfaceContainer) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        if (emoji != null) {
                                Text(emoji, fontSize = 24.sp)
                        } else if (icon != null) {
                                Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                                Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(
                                        value,
                                        fontSize = 15.sp,
                                        color = OnSurface,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                        }
                        Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
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
