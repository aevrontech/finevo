package com.aevrontech.finevo.presentation.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.domain.model.CurrencyProvider
import com.aevrontech.finevo.presentation.expense.CalculatorKeypad
import com.aevrontech.finevo.presentation.expense.evaluateExpression
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/** Full-screen Add/Edit Budget Screen */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBudgetScreen(
    categories: List<Category>,
    accounts: List<Account>,
    editingBudget: Budget? = null,
    onDismiss: () -> Unit,
    onSave:
        (
        name: String,
        categoryIds: List<String>,
        accountIds: List<String>,
        amount: Double,
        currency: String,
        period: BudgetPeriod,
        startDate: kotlinx.datetime.LocalDate,
        endDate: kotlinx.datetime.LocalDate?,
        notifyOverspent: Boolean,
        alertThreshold: Int,
        notifyRisk: Boolean) -> Unit
) {
    val isEditing = editingBudget != null

    // Form state
    var budgetName by remember { mutableStateOf(editingBudget?.name ?: "") }
    var selectedPeriod by remember {
        mutableStateOf(editingBudget?.period ?: BudgetPeriod.MONTHLY)
    }
    var amountText by remember { mutableStateOf(editingBudget?.amount?.toString() ?: "") }
    var selectedCurrency by remember { mutableStateOf("MYR") }
    var selectedCategoryIds by remember {
        mutableStateOf(
            editingBudget?.let {
                if (it.categoryIds.isNotEmpty()) it.categoryIds.toSet()
                else setOf(it.categoryId)
            }
                ?: emptySet()
        )
    }
    var selectedAccountIds by remember {
        mutableStateOf(editingBudget?.accountIds?.toSet() ?: emptySet())
    }
    var isAllAccounts by remember { mutableStateOf(editingBudget?.isAllAccounts ?: true) }

    // Date state for ONCE period
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var startDate by remember { mutableStateOf(editingBudget?.startDate ?: today) }
    var endDate by remember {
        mutableStateOf<LocalDate>(
            editingBudget?.endDate ?: today.plus(DatePeriod(months = 1))
        )
    }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Notification state
    var notifyOverspent by remember { mutableStateOf(editingBudget?.notifyOverspent ?: true) }
    var alertThreshold by remember {
        mutableFloatStateOf(editingBudget?.alertThreshold?.toFloat() ?: 80f)
    }
    var notifyRisk by remember { mutableStateOf(editingBudget?.notifyRisk ?: true) }

    // Dropdown states
    var periodDropdownExpanded by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var categoriesExpanded by remember { mutableStateOf(false) }
    var accountsExpanded by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var calculatorExpression by remember { mutableStateOf(amountText) }

    // Search states
    var categorySearchQuery by remember { mutableStateOf("") }
    var currencySearchQuery by remember { mutableStateOf("") }

    // Get currencies from CurrencyProvider
    val allCurrencies = remember { CurrencyProvider.getAllCurrencies() }
    val filteredCurrencies =
        remember(currencySearchQuery) {
            allCurrencies.filter { it.matchesSearch(currencySearchQuery) }
        }
    val filteredCategories =
        remember(categorySearchQuery, categories) {
            if (categorySearchQuery.isBlank()) categories
            else
                categories.filter {
                    it.name.contains(categorySearchQuery, ignoreCase = true)
                }
        }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Gradient Header
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                DashboardGradientStart,
                                DashboardGradientMid,
                                DashboardGradientEnd
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                Text(
                    text = if (isEditing) "Edit Budget" else "New budget",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onSave(
                                budgetName,
                                selectedCategoryIds.toList(),
                                if (isAllAccounts) emptyList()
                                else selectedAccountIds.toList(),
                                amount,
                                selectedCurrency,
                                selectedPeriod,
                                startDate,
                                if (selectedPeriod ==
                                    BudgetPeriod.ONCE
                                )
                                    endDate
                                else null,
                                notifyOverspent,
                                alertThreshold.toInt(),
                                notifyRisk
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }
            }
        }

        // Form Content
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Name Field
            FormLabel("Name")
            BasicTextField(
                value = budgetName,
                onValueChange = { newValue ->
                    // Auto-capitalize first letter
                    budgetName =
                        if (newValue.isNotEmpty()) {
                            newValue.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase()
                                else it.toString()
                            }
                        } else newValue
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                decorationBox = { innerTextField ->
                    if (budgetName.isEmpty()) {
                        Text(
                            text = "Budget name",
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Period Selector
            FormLabel("Period")
            Box {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { periodDropdownExpanded = true }
                            .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPeriod.label,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select period",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = periodDropdownExpanded,
                    onDismissRequest = { periodDropdownExpanded = false }
                ) {
                    BudgetPeriod.entries.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period.label) },
                            onClick = {
                                selectedPeriod = period
                                periodDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Range for ONCE period
            AnimatedVisibility(visible = selectedPeriod == BudgetPeriod.ONCE) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Start Date
                    FormLabel("Start Date")
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable {
                                    showStartDatePicker = true
                                }
                                .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                "${startDate.dayOfMonth}/${startDate.monthNumber}/${startDate.year}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select start date",
                            tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date
                    FormLabel("End Date")
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable {
                                    showEndDatePicker = true
                                }
                                .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                "${endDate.dayOfMonth}/${endDate.monthNumber}/${endDate.year}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select end date",
                            tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Amount and Currency Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount - clickable to open calculator
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Amount")
                    Box {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        calculatorExpression =
                                            amountText
                                        showCalculator =
                                            true
                                    }
                                    .padding(vertical = 12.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = amountText.ifEmpty { "0" },
                                fontSize = 16.sp,
                                color =
                                    if (amountText.isEmpty())
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                    else
                                        MaterialTheme
                                            .colorScheme
                                            .onSurface
                            )
                            Icon(
                                imageVector =
                                    Icons.Default
                                        .KeyboardArrowDown,
                                contentDescription = "Enter amount",
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }

                // Currency - with search from CurrencyProvider
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Currency")
                    Box {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        currencyDropdownExpanded =
                                            true
                                    }
                                    .padding(vertical = 12.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCurrency,
                                fontSize = 16.sp,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                            Icon(
                                imageVector =
                                    Icons.Default
                                        .KeyboardArrowDown,
                                contentDescription =
                                    "Select currency",
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = {
                                currencyDropdownExpanded = false
                                currencySearchQuery = ""
                            },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            // Search field
                            OutlinedTextField(
                                value = currencySearchQuery,
                                onValueChange = {
                                    currencySearchQuery = it
                                },
                                placeholder = {
                                    Text("Search currency")
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default
                                            .Search,
                                        contentDescription =
                                            null
                                    )
                                },
                                singleLine = true,
                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                colors =
                                    OutlinedTextFieldDefaults
                                        .colors()
                            )
                            HorizontalDivider()

                            filteredCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${currency.code} - ${currency.displayName}"
                                        )
                                    },
                                    onClick = {
                                        selectedCurrency =
                                            currency.code
                                        currencyDropdownExpanded =
                                            false
                                        currencySearchQuery =
                                            ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Categories Multi-Select with Search
            FormLabel("Categories")
            Box {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { categoriesExpanded = true }
                            .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            if (selectedCategoryIds.isEmpty()) "All"
                            else "${selectedCategoryIds.size} selected",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select categories",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = categoriesExpanded,
                    onDismissRequest = {
                        categoriesExpanded = false
                        categorySearchQuery = ""
                    },
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    // Search field for categories
                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        placeholder = { Text("Search categories") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    HorizontalDivider()

                    filteredCategories.forEach { category ->
                        val isSelected =
                            selectedCategoryIds.contains(category.id)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment =
                                        Alignment
                                            .CenterVertically
                                ) {
                                    Text(
                                        "${category.icon} ${category.name}"
                                    )
                                    if (isSelected) {
                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    8.dp
                                                )
                                        )
                                        Icon(
                                            imageVector =
                                                Icons.Default
                                                    .Check,
                                            contentDescription =
                                                "Selected",
                                            tint =
                                                DashboardGradientStart,
                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedCategoryIds =
                                    if (isSelected) {
                                        selectedCategoryIds -
                                            category.id
                                    } else {
                                        selectedCategoryIds +
                                            category.id
                                    }
                            }
                        )
                    }
                }
            }

            // Selected categories chips
            if (selectedCategoryIds.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedCategoryIds.forEach { categoryId ->
                        val category =
                            categories.find { it.id == categoryId }
                        if (category != null) {
                            FilterChip(
                                selected = true,
                                onClick = {
                                    selectedCategoryIds =
                                        selectedCategoryIds -
                                            categoryId
                                },
                                label = {
                                    Text(
                                        "${category.icon} ${category.name}"
                                    )
                                },
                                colors =
                                    FilterChipDefaults
                                        .filterChipColors(
                                            selectedContainerColor =
                                                DashboardGradientStart
                                                    .copy(
                                                        alpha =
                                                            0.2f
                                                    )
                                        )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Account Multi-Select
            FormLabel("Account")
            Box {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { accountsExpanded = true }
                            .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            if (isAllAccounts) "All"
                            else "${selectedAccountIds.size} selected",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select accounts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = accountsExpanded,
                    onDismissRequest = { accountsExpanded = false }
                ) {
                    // All option
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text("All")
                                if (isAllAccounts) {
                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                8.dp
                                            )
                                    )
                                    Icon(
                                        imageVector =
                                            Icons.Default
                                                .Check,
                                        contentDescription =
                                            "Selected",
                                        tint =
                                            DashboardGradientStart,
                                        modifier =
                                            Modifier.size(
                                                16.dp
                                            )
                                    )
                                }
                            }
                        },
                        onClick = {
                            isAllAccounts = true
                            selectedAccountIds = emptySet()
                        }
                    )
                    HorizontalDivider()
                    accounts.forEach { account ->
                        val isSelected =
                            !isAllAccounts &&
                                selectedAccountIds.contains(
                                    account.id
                                )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment =
                                        Alignment
                                            .CenterVertically
                                ) {
                                    Text(
                                        "${account.icon} ${account.name}"
                                    )
                                    if (isSelected) {
                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    8.dp
                                                )
                                        )
                                        Icon(
                                            imageVector =
                                                Icons.Default
                                                    .Check,
                                            contentDescription =
                                                "Selected",
                                            tint =
                                                DashboardGradientStart,
                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                        )
                                    }
                                }
                            },
                            onClick = {
                                isAllAccounts = false
                                selectedAccountIds =
                                    if (isSelected) {
                                        selectedAccountIds -
                                            account.id
                                    } else {
                                        selectedAccountIds +
                                            account.id
                                    }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notifications Section - Full width (no horizontal padding)
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            color =
                                MaterialTheme.colorScheme
                                    .surfaceContainerLow
                        )
                        .padding(vertical = 12.dp, horizontal = 0.dp)
            ) {
                Text(
                    text = "NOTIFICATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            // Budget Overspent Toggle
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Budget overspent",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text =
                                "Notify when amount has exceeded the budget",
                            fontSize = 12.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notifyOverspent,
                        onCheckedChange = { notifyOverspent = it },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor =
                                    DashboardGradientStart
                            )
                    )
                }

                // Alert Threshold Slider (only when overspent notification is
                // enabled)
                AnimatedVisibility(visible = notifyOverspent) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text =
                                "Alert at ${alertThreshold.toInt()}% spent",
                            fontSize = 14.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                        Slider(
                            value = alertThreshold,
                            onValueChange = { alertThreshold = it },
                            valueRange = 50f..100f,
                            steps = 9,
                            colors =
                                SliderDefaults.colors(
                                    thumbColor =
                                        DashboardGradientStart,
                                    activeTrackColor =
                                        DashboardGradientStart
                                )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Risk of Overspending Toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Risk of overspending",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text =
                            "Notify when budget is trending to be overspent",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notifyRisk,
                    onCheckedChange = { notifyRisk = it },
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DashboardGradientStart
                        )
                )
            }

            // Extra bottom padding for navbar
            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    // Calculator Bottom Sheet
    if (showCalculator) {
        ModalBottomSheet(
            onDismissRequest = { showCalculator = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .padding(
                            bottom = 32.dp
                        ) // Extra padding for safe area
            ) {
                // Display current expression/result
                Text(
                    text = calculatorExpression.ifEmpty { "0" },
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )

                // Calculator Keypad
                CalculatorKeypad(
                    expression = calculatorExpression,
                    onExpressionChange = { calculatorExpression = it },
                    onEquals = {
                        val result =
                            evaluateExpression(calculatorExpression)
                        amountText =
                            if (result == result.toLong().toDouble()) {
                                result.toLong().toString()
                            } else {
                                // Manual formatting to 2 decimal
                                // places to avoid standard
                                // lib dependency issues if any
                                // But usually String.format is fine
                                // in JVM/Android.
                                // Since this is KMP, String.format
                                // might not be available
                                // in commonMain.
                                // I should check if String.format
                                // is used elsewhere.
                                // The previous code used
                                // String.format("%.2f", result).
                                // If this is commonMain,
                                // String.format is NOT available by
                                // default unless expected/actual.
                                // However, the file is in
                                // commonMain.
                                // Wait, if the previous code used
                                // String.format, it might
                                // have been an error or it IS
                                // available (maybe through a
                                // library or alias).
                                // A safer way in KMP is simply
                                // result.toString() or custom
                                // formatting.
                                // BUT, the previous code HAD
                                // String.format in the viewed
                                // snippet:
                                // String.format("%.2f", result)
                                // So I will assume it works or
                                // stick to it.
                                // Actually, let's just use a simple
                                // rounding if needed or
                                // keep existing logic.
                                // I'll stick to the existing logic
                                // for now.
                                try {
                                    val rounded =
                                        (result * 100)
                                            .toLong() /
                                            100.0
                                    rounded.toString()
                                } catch (e: Exception) {
                                    result.toString()
                                }
                            }
                        showCalculator = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
