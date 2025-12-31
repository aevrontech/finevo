package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Account
import com.aevrontech.finevo.domain.model.CurrencyProvider
import com.aevrontech.finevo.ui.theme.*

/** Horizontal scrollable row of account cards. */
@Composable
fun AccountCardsRow(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountClick: (Account) -> Unit,
    onAccountLongClick: ((Account) -> Unit)? = null,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalAlignment =
            Alignment.CenterVertically // Center align so size changes grow from center
    ) {
        items(accounts) { account ->
            AccountCard(
                account = account,
                isSelected = account == selectedAccount,
                onClick = { onAccountClick(account) },
                onLongClick = onAccountLongClick?.let { { it(account) } }
            )
        }

        // Add account button
        item { AddAccountCard(onClick = onAddAccountClick) }
    }
}

/** Individual account card with balance and icon. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountCard(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardColor = parseAccountColor(account.color)
    val borderColor = if (isSelected) Primary else Color.Transparent

    val width by animateDpAsState(if (isSelected) 125.dp else 110.dp, label = "cardWidth")
    val height by animateDpAsState(if (isSelected) 135.dp else 120.dp, label = "cardHeight")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "cardAlpha")

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardColor, // Always use the account's color
        // border removed as requested
        modifier =
            modifier.width(width)
                .height(height) // Height animation
                .alpha(alpha)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon with white transparency background
            Box(
                modifier =
                    Modifier.size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(account.icon, fontSize = 14.sp) }

            // Account name
            Text(
                account.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White, // Assume colored bg needs white text
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Balance
            Text(
                formatAccountBalance(account),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Account type badge
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    account.type.displayName,
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier.padding(
                            horizontal = 4.dp,
                            vertical = 1.dp
                        ),
                    maxLines = 1
                )
            }
        }
    }
}

/** Card to add new account. */
@Composable
fun AddAccountCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainer,
        border = BorderStroke(1.5.dp, SurfaceContainerHighest.copy(alpha = 0.5f)),
        modifier = modifier.width(70.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Account",
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Add",
                fontSize = 10.sp,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Account summary card showing income, expense, and remaining. */
@Composable
fun AccountSummaryCard(
    account: Account?,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val remaining = income - expense
    val percentageUsed =
        if (account != null && account.balance > 0) {
            (expense / account.balance * 100).coerceIn(0.0, 100.0)
        } else 0.0

    Box(
        modifier =
            modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush =
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors =
                                listOf(
                                    DashboardGradientStart,
                                    DashboardGradientMid,
                                    DashboardGradientEnd
                                )
                        )
                )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    account?.name ?: "All Accounts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    "This Month",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Progress bar (optional)
            if (account != null && account.balance > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = {
                            (percentageUsed / 100)
                                .toFloat()
                                .coerceIn(0f, 1f)
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        "${percentageUsed.toInt()}% of balance spent",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Income, Expense, Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItemWhite(
                    label = "Income",
                    amount = income,
                    isPositive = true,
                    currency = account?.currency ?: "MYR"
                )
                SummaryItemWhite(
                    label = "Expense",
                    amount = expense,
                    isPositive = false,
                    currency = account?.currency ?: "MYR"
                )
                SummaryItemWhite(
                    label = "Net",
                    amount = remaining,
                    isPositive = remaining >= 0,
                    currency = account?.currency ?: "MYR"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: Color, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${CurrencyProvider.getCurrency(currency)?.symbol ?: currency} ${formatAmount(kotlin.math.abs(amount))}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun SummaryItemWhite(label: String, amount: Double, isPositive: Boolean, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${CurrencyProvider.getCurrency(currency)?.symbol ?: currency} ${formatAmount(kotlin.math.abs(amount))}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

private fun formatAccountBalance(account: Account): String {
    val symbol = CurrencyProvider.getCurrency(account.currency)?.symbol ?: account.currency
    return "$symbol ${formatAmount(account.balance)}"
}

// getCurrencySymbol removed - use Currency.fromCode(code).symbol

private fun formatAmount(amount: Double): String {
    return String.format("%.2f", amount)
}

private fun parseAccountColor(hex: String): Color {
    return try {
        val colorString = hex.removePrefix("#")
        when (colorString.length) {
            6 -> {
                val r = colorString.substring(0, 2).toInt(16)
                val g = colorString.substring(2, 4).toInt(16)
                val b = colorString.substring(4, 6).toInt(16)
                Color(red = r, green = g, blue = b, alpha = 255)
            }
            else -> Primary
        }
    } catch (e: Exception) {
        Primary
    }
}
