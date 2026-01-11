package com.aevrontech.finevo.presentation.budget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Budget
import com.aevrontech.finevo.domain.model.BudgetPeriod
import com.aevrontech.finevo.domain.model.BudgetStatus
import com.aevrontech.finevo.domain.model.Category
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.Warning

/** Budget Overview Card - Shows summary of all budgets with arc progress */
@Composable
fun BudgetOverviewCard(
    totalBudget: Double,
    totalSpent: Double,
    budgetCount: Int,
    onTrackCount: Int,
    warningCount: Int,
    overCount: Int,
    currencySymbol: String = "RM",
    selectedPeriod: BudgetPeriod? = null,
    onPeriodSelected: (BudgetPeriod?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val percentUsed = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val animatedProgress by
    animateFloatAsState(
        targetValue = percentUsed.coerceIn(0f, 1f),
        animationSpec = tween(800)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Your Budgets",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$budgetCount budget tracked",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Period Filter
                Box {
                    var expanded by remember { mutableStateOf(false) }

                    androidx.compose.material3.Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(20.dp),
                        color =
                            MaterialTheme.colorScheme
                                .surfaceContainerHigh,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 12.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedPeriod?.label
                                    ?: "Monthly",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                            Icon(
                                imageVector =
                                    Icons.Default.ArrowDropDown,
                                contentDescription = "Filter",
                                modifier = Modifier.size(16.dp),
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                        }
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier =
                            Modifier.background(
                                MaterialTheme.colorScheme
                                    .surfaceContainer
                            )
                    ) {
                        BudgetPeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        period.label,
                                        fontWeight =
                                            if (period ==
                                                selectedPeriod
                                            )
                                                FontWeight
                                                    .Bold
                                            else
                                                FontWeight
                                                    .Normal
                                    )
                                },
                                onClick = {
                                    onPeriodSelected(period)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arc Progress Indicator with labels
            val arcSize = 240.dp
            Box(
                modifier = Modifier.size(arcSize),
                contentAlignment = Alignment.Center
            ) {
                // Draw the arc
                Canvas(modifier = Modifier.size(arcSize)) {
                    val strokeWidth = 12.dp.toPx()
                    val arcDiameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc (track)
                    drawArc(
                        color = Color(0xFFE8EAF0),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcDiameter, arcDiameter),
                        style =
                            Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                    )

                    // Progress arc
                    val progressColor =
                        when {
                            animatedProgress > 1f ->
                                Color(0xFFFF5252) // Error
                            animatedProgress > 0.8f ->
                                Color(0xFFFFB74D) // Warning
                            else -> Color(0xFF1E88E5) // Primary blue
                        }

                    drawArc(
                        color = progressColor,
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcDiameter, arcDiameter),
                        style =
                            Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                    )
                }

                // Center content - show remaining budget
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    // Wallet icon
                    Text(text = "💰", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Left to spend",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val remaining =
                        (totalBudget - totalSpent).coerceAtLeast(0.0)
                    val remainingText =
                        "$currencySymbol ${remaining.formatDecimal(2)}"
                    val dynamicFontSize =
                        when {
                            remainingText.length > 13 ->
                                20.sp // e.g. RM 100,000.00
                            remainingText.length > 10 -> 24.sp
                            else -> 32.sp
                        }
                    Text(
                        text = remainingText,
                        fontSize = dynamicFontSize,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (remaining > 0)
                                MaterialTheme.colorScheme.onSurface
                            else Error
                    )
                    Text(
                        text =
                            "of $currencySymbol ${totalBudget.formatDecimal(2)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 0% label - positioned at arc start (150° = lower left)
                Text(
                    text = "0%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.align(Alignment.BottomStart)
                            .padding(start = 8.dp, bottom = 28.dp)
                )

                // 100% label - positioned at arc end (30° = lower right)
                Text(
                    text = "100%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.align(Alignment.BottomEnd)
                            .padding(end = 0.dp, bottom = 28.dp)
                )
            }

            // Status badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusChip(count = onTrackCount, label = "On Track", color = Income)
                StatusChip(count = warningCount, label = "Warning", color = Warning)
                StatusChip(count = overCount, label = "Over", color = Error)
            }
        }
    }
}

@Composable
private fun StatusChip(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun StatusBadge(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

/** Individual Budget Card */
@Composable
fun BudgetCard(
    budget: Budget,
    currencySymbol: String = "RM",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor =
        when (budget.status) {
            BudgetStatus.ON_TRACK -> Income
            BudgetStatus.WARNING -> Warning
            BudgetStatus.OVER -> Error
        }

    val animatedProgress by
    animateFloatAsState(
        targetValue = (budget.percentUsed / 100).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500)
    )

    val animatedColor by
    animateColorAsState(targetValue = statusColor, animationSpec = tween(300))

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Icon - show count badge for multi-category
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (budget.isMultiCategory
                                    ) {
                                        // Use primary color
                                        // for
                                        // multi-category
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                            .copy(
                                                alpha =
                                                    0.15f
                                            )
                                    } else {
                                        try {
                                            Color(
                                                android.graphics
                                                    .Color
                                                    .parseColor(
                                                        budget.categoryColor
                                                            ?: "#808080"
                                                    )
                                            )
                                                .copy(
                                                    alpha =
                                                        0.15f
                                                )
                                        } catch (
                                            e:
                                            Exception) {
                                            Color.Gray
                                                .copy(
                                                    alpha =
                                                        0.15f
                                                )
                                        }
                                    }
                                ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (budget.isMultiCategory) {
                            // Show category count for multi-category
                            // budgets
                            Text(
                                text = "${budget.categoryIds.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    MaterialTheme.colorScheme
                                        .primary
                            )
                        } else {
                            Text(
                                text = budget.categoryIcon ?: "💰",
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = budget.displayName,
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text =
                                "${budget.period.label} - ${budget.transactionCount} Transactions",
                            fontSize = 12.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text =
                            "$currencySymbol ${budget.spent.formatDecimal(2)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Text(
                        text =
                            "of $currencySymbol ${budget.amount.formatDecimal(2)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier =
                    Modifier.fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                color = animatedColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (budget.isOverBudget) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Over budget",
                            tint = Error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text =
                                "$currencySymbol ${budget.overAmount.formatDecimal(2)} over",
                            fontSize = 12.sp,
                            color = Error
                        )
                    } else {
                        Text(
                            text =
                                "$currencySymbol ${budget.remaining.formatDecimal(2)} remaining",
                            fontSize = 12.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${budget.percentUsed.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = animatedColor
                )
            }
        }
    }
}

/** Add/Edit Budget Dialog */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    availableCategories: List<Category>,
    editingBudget: Budget? = null,
    onDismiss: () -> Unit,
    onConfirm:
        (
        categoryId: String,
        amount: Double,
        period: BudgetPeriod,
        alertThreshold: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var selectedCategory by remember {
        mutableStateOf(
            editingBudget?.let { budget ->
                availableCategories.find { it.id == budget.categoryId }
                    ?: Category(
                        id = budget.categoryId,
                        name = budget.categoryName ?: "Unknown",
                        icon = budget.categoryIcon ?: "💰",
                        color = budget.categoryColor ?: "#808080",
                        type =
                            com.aevrontech.finevo.domain.model
                                .TransactionType.EXPENSE,
                        isDefault = false,
                        order = 0
                    )
            }
        )
    }
    var amountText by remember { mutableStateOf(editingBudget?.amount?.formatDecimal(0) ?: "") }
    var selectedPeriod by remember {
        mutableStateOf(editingBudget?.period ?: BudgetPeriod.MONTHLY)
    }
    var alertThreshold by remember {
        mutableFloatStateOf(editingBudget?.alertThreshold?.toFloat() ?: 80f)
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }

    val isEditing = editingBudget != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Budget" else "Add Budget",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isEditing && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Error
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Category Selector
                if (!isEditing) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value =
                                selectedCategory?.let {
                                    "${it.icon} ${it.name}"
                                }
                                    ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(
                                        expanded =
                                            categoryExpanded
                                    )
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .menuAnchor(
                                        MenuAnchorType
                                            .PrimaryNotEditable,
                                        true
                                    )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = {
                                categoryExpanded = false
                            }
                        ) {
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${category.icon} ${category.name}"
                                        )
                                    },
                                    onClick = {
                                        selectedCategory =
                                            category
                                        categoryExpanded =
                                            false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Show category as read-only for editing
                    OutlinedTextField(
                        value =
                            "${editingBudget?.categoryIcon ?: "💰"} ${editingBudget?.categoryName ?: "Unknown"}",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText =
                            it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Budget Amount") },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Period Selector
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Period") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = periodExpanded
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .menuAnchor(
                                    MenuAnchorType
                                        .PrimaryNotEditable,
                                    true
                                )
                    )
                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        BudgetPeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.label) },
                                onClick = {
                                    selectedPeriod = period
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Alert Threshold Slider
                Column {
                    Text(
                        text = "Alert at ${alertThreshold.toInt()}% spent",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = alertThreshold,
                        onValueChange = { alertThreshold = it },
                        valueRange = 50f..100f,
                        steps = 9,
                        colors =
                            SliderDefaults.colors(
                                thumbColor =
                                    MaterialTheme.colorScheme
                                        .primary,
                                activeTrackColor =
                                    MaterialTheme.colorScheme
                                        .primary
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val categoryId =
                        selectedCategory?.id
                            ?: editingBudget?.categoryId
                            ?: return@TextButton
                    if (amount > 0) {
                        onConfirm(
                            categoryId,
                            amount,
                            selectedPeriod,
                            alertThreshold.toInt()
                        )
                    }
                },
                enabled =
                    amountText.isNotEmpty() &&
                        (selectedCategory != null || isEditing)
            ) {
                Text(
                    if (isEditing) "Update" else "Create",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Empty Budget State */
@Composable
fun EmptyBudgetState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No budgets yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set spending limits for your categories",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAddClick) {
                Text("+ Add Budget", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
