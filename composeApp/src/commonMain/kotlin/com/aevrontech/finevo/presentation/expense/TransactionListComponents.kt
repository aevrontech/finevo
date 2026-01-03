package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Label
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.Error
import com.aevrontech.finevo.ui.theme.Expense
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import com.aevrontech.finevo.ui.theme.SurfaceContainer
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/** Data class for grouped transactions by date. */
data class TransactionGroup(
    val dateLabel: String,
    val date: LocalDate,
    val transactions: List<Transaction>,
    val totalExpense: Double,
    val totalIncome: Double
)

/** Groups transactions by date and creates appropriate labels. */
fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionGroup> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
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
            "JANUARY",
            "FEBRUARY",
            "MARCH",
            "APRIL",
            "MAY",
            "JUNE",
            "JULY",
            "AUGUST",
            "SEPTEMBER",
            "OCTOBER",
            "NOVEMBER",
            "DECEMBER"
        )
    return "${date.dayOfMonth} ${months[date.monthNumber - 1]} ${date.year}"
}

/** Grouped transaction list content for LazyColumn. */
fun LazyListScope.groupedTransactionItems(
    groups: List<TransactionGroup>,
    availableLabels: List<Label>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionDelete: (Transaction) -> Unit
) {
    groups.forEach { group ->
        // Date header
        item(key = "header_${group.date}") {
            TransactionDateHeader(
                dateLabel = group.dateLabel,
                totalExpense = group.totalExpense,
                totalIncome = group.totalIncome
            )
        }

        // Transactions for this date
        item(key = "transactions_${group.date}") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    group.transactions.forEachIndexed { index, transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            availableLabels = availableLabels,
                            onClick = { onTransactionClick(transaction) },
                            onDelete = { onTransactionDelete(transaction) }
                        )
                        if (index < group.transactions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = OnSurfaceVariant.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }

        // No spacer between groups - compact layout
    }
}

@Composable
private fun TransactionDateHeader(dateLabel: String, totalExpense: Double, totalIncome: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalExpense > 0) {
                Text(
                    text = "-${totalExpense.formatDecimal(0)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Expense
                )
            }
            if (totalIncome > 0) {
                Text(
                    text = "+${totalIncome.formatDecimal(0)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Income
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    availableLabels: List<Label>,
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
                    "Are you sure you want to delete this transaction of RM ${
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
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionItemContent(
    transaction: Transaction,
    availableLabels: List<Label>,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = SurfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category icon - compact size
                Box(
                    modifier =
                        Modifier.size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                parseColorSafe(
                                    transaction.categoryColor
                                        ?: "#607D8B"
                                )
                                    .copy(alpha = 0.15f)
                            ),
                    contentAlignment = Alignment.Center
                ) { Text(text = transaction.categoryIcon ?: "💵", fontSize = 16.sp) }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    // Primary: Category name (e.g., "Food and Dining")
                    // Fallback: Description or generic "Transaction"
                    Text(
                        text = transaction.categoryName
                            ?: transaction.description ?: "Transaction",
                        color = OnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    // Secondary: Note (if different from category name)
                    val noteText = transaction.note ?: transaction.description
                    if (noteText != null && noteText != transaction.categoryName) {
                        Text(
                            text = noteText,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    // Labels
                    if (transaction.labels.isNotEmpty() && availableLabels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            transaction.labels.forEach { labelId ->
                                val label = availableLabels.find { it.id == labelId }
                                if (label != null) {
                                    val labelColor = parseColorSafe(label.color)
                                    Box(
                                        modifier =
                                            Modifier.clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    labelColor.copy(alpha = 0.2f)
                                                )
                                                .padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                )
                                    ) {
                                        Text(
                                            text = label.name,
                                            color = labelColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text =
                    "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"} RM ${
                        transaction.amount.formatDecimal(2)
                    }",
                color = if (transaction.type == TransactionType.EXPENSE) Expense else Income,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
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
