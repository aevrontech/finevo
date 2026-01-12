package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.domain.model.Label
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.Expense
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Data class for grouped transactions by date. */
data class TransactionGroup(
    val dateLabel: String,
    val date: LocalDate,
    val transactions: List<Transaction>,
    val totalExpense: Double,
    val totalIncome: Double
)

/** Groups transactions by date and creates appropriate labels. */
@OptIn(kotlin.time.ExperimentalTime::class)
fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionGroup> {
    val today = getCurrentLocalDate()
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return transactions
        .groupBy { it.date }
        .map { (date, txns) ->
            val dateLabel =
                when (date) {
                    today -> "TODAY"
                    yesterday -> "YESTERDAY"
                    else -> formatDateLabel(date)
                }
            TransactionGroup(
                dateLabel = dateLabel,
                date = date,
                transactions = txns.sortedByDescending { it.createdAt },
                totalExpense =
                    txns.filter { it.type == TransactionType.EXPENSE }.sumOf {
                        it.amount
                    },
                totalIncome =
                    txns.filter { it.type == TransactionType.INCOME }.sumOf {
                        it.amount
                    }
            )
        }
        .sortedByDescending { it.date }
}

private fun formatDateLabel(date: LocalDate): String {
    val months =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )
    return "${date.dayOfMonth.toString().padStart(2, '0')} ${months[date.monthNumber - 1]} ${date.year}"
}

/** Grouped transaction list content for LazyColumn. */
fun LazyListScope.groupedTransactionItems(
    groups: List<TransactionGroup>,
    availableLabels: List<Label>,
    currencySymbol: String,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionDelete: (Transaction) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    groups.forEach { group ->
        item(key = "group_${group.date}") {
            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Date header inside card
                    TransactionDateHeader(
                        date = group.date,
                        totalExpense = group.totalExpense,
                        totalIncome = group.totalIncome,
                        currencySymbol = currencySymbol,
                        onClick = { onDateClick(group.date) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = OnSurfaceVariant.copy(alpha = 0.1f)
                    )

                    // Transactions
                    group.transactions.forEachIndexed { index, transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            availableLabels = availableLabels,
                            currencySymbol = currencySymbol,
                            onClick = {
                                onTransactionClick(transaction)
                            },
                            onDelete = {
                                onTransactionDelete(transaction)
                            }
                        )
                        if (index < group.transactions.size - 1) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        horizontal = 16.dp
                                    ),
                                color =
                                    OnSurfaceVariant.copy(
                                        alpha = 0.1f
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionDateHeader(
    date: LocalDate,
    totalExpense: Double,
    totalIncome: Double,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val dateString = formatDateLabel(date)

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
            Text(
                text = dayOfWeek,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateString,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalExpense > 0) {
                Text(
                    text = "-$currencySymbol${totalExpense.formatDecimal(0)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Expense
                )
            }
            if (totalIncome > 0) {
                Text(
                    text = "+$currencySymbol${totalIncome.formatDecimal(0)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Income
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    availableLabels: List<Label>,
    currencySymbol: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    showDeleteConfirmation = true
                }
                false // Don't actually dismiss, show confirmation instead
            }
        )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(Error.copy(alpha = 0.9f))
                        .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                    Text(
                        "Delete",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        content = {
            TransactionItemContent(
                transaction = transaction,
                availableLabels = availableLabels,
                currencySymbol = currencySymbol,
                onClick = onClick
            )
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Transaction") },
            text = {
                Text(
                    "Are you sure you want to delete this transaction of $currencySymbol ${
                        transaction.amount.formatDecimal(2)
                    }?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) { Text("Delete", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionItemContent(
    transaction: Transaction,
    availableLabels: List<Label>,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            parseColorSafe(
                                transaction.categoryColor
                                    ?: "#607D8B"
                            )
                                .copy(alpha = 0.15f)
                        ),
                contentAlignment = Alignment.Center
            ) { Text(text = transaction.categoryIcon ?: "💵", fontSize = 20.sp) }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy((-2).dp) // Reduced spacing
            ) {
                // Row 1: Category - Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.categoryName
                            ?: transaction.description
                            ?: "Transaction",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp, // Reduced to 15sp
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text =
                            "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"} $currencySymbol ${
                                transaction.amount.formatDecimal(2)
                            }",
                        color =
                            if (transaction.type ==
                                TransactionType.EXPENSE
                            )
                                Expense
                            else Income,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp // Reduced to 15sp
                    )
                }

                // Row 2: Account - Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.accountName ?: "Cash",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Time formatting
                    val timeString =
                        transaction.time?.let {
                            val parts = it.split(":")
                            if (parts.size >= 2) {
                                val hour = parts[0].toInt()
                                val minute = parts[1].toInt()
                                val amPm =
                                    if (hour < 12) "AM"
                                    else "PM"
                                val hour12 =
                                    if (hour % 12 == 0) 12
                                    else hour % 12
                                "${hour12}:${minute.toString().padStart(2, '0')} $amPm"
                            } else it
                        }
                            ?: ""

                    Text(
                        text = timeString,
                        color = OnSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                // Note (if present and distinct)
                val noteText = transaction.note ?: transaction.description
                if (!noteText.isNullOrBlank() &&
                    noteText != transaction.categoryName
                ) {
                    // Removed Spacer to tighten spacing
                    Text(
                        text = "\"$noteText\"",
                        color = OnSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1
                    )
                }

                // Labels
                if (transaction.labels.isNotEmpty() && availableLabels.isNotEmpty()
                ) {
                    Spacer(
                        modifier = Modifier.height(2.dp)
                    ) // Reduced from 4.dp
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        transaction.labels.forEach { labelId ->
                            val label =
                                availableLabels.find {
                                    it.id == labelId
                                }
                            if (label != null) {
                                val labelColor =
                                    parseColorSafe(label.color)
                                Box(
                                    modifier =
                                        Modifier.clip(
                                            RoundedCornerShape(
                                                4.dp
                                            )
                                        )
                                            .background(
                                                labelColor
                                                    .copy(
                                                        alpha =
                                                            0.2f
                                                    )
                                            )
                                            .padding(
                                                horizontal =
                                                    6.dp,
                                                vertical =
                                                    2.dp
                                            )
                                ) {
                                    Text(
                                        text = label.name,
                                        color = labelColor,
                                        fontSize = 10.sp,
                                        fontWeight =
                                            FontWeight
                                                .Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseColorSafe(hex: String?): Color {
    if (hex == null) return Primary
    return try {
        val colorString = hex.removePrefix("#")
        val colorLong = colorString.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF) / 255f,
            green = ((colorLong shr 8) and 0xFF) / 255f,
            blue = (colorLong and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Primary
    }
}
