package com.aevrontech.finevo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.ui.theme.Expense
import com.aevrontech.finevo.ui.theme.Income
import com.aevrontech.finevo.ui.theme.OnSurface
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.SurfaceContainer

/** Circular progress indicator with animated fill */
@Composable
fun CircularProgressCard(
    title: String,
    amount: String,
    percentage: Float,
    color: Color,
    backgroundColor: Color = SurfaceContainer,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage.coerceIn(0f, 100f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier =
            modifier.clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            // Background circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }

            // Progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = animatedProgress.value * 3.6f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${animatedProgress.value.toInt()}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

/** Dual circle card showing income and expense side by side */
@Composable
fun IncomeExpenseCircleCard(
    income: Double,
    expense: Double,
    currencySymbol: String = "RM",
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val total = income + expense
    val incomePercentage = if (total > 0) ((income / total) * 100).toFloat() else 0f
    val expensePercentage = if (total > 0) ((expense / total) * 100).toFloat() else 0f
    val net = income - expense

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Income Circle
        CircularProgressCard(
            title = "Income",
            amount = "$currencySymbol ${formatAmount(income)}",
            percentage = incomePercentage,
            color = Income,
            onClick = onIncomeClick,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )

        // Net Amount in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(text = "Net", fontSize = 12.sp, color = OnSurfaceVariant)
            Text(
                text =
                    "${if (net >= 0) "+" else ""}$currencySymbol ${kotlin.math.abs(net).formatDecimal(2)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (net >= 0) Income else Expense
            )
        }

        // Expense Circle
        CircularProgressCard(
            title = "Expense",
            amount = "$currencySymbol ${formatAmount(expense)}",
            percentage = expensePercentage,
            color = Expense,
            onClick = onExpenseClick,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}

/** Bar chart for income vs expense comparison */
@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 32.dp,
    maxHeight: Dp = 150.dp
) {
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = modifier.fillMaxWidth().height(maxHeight + 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(maxHeight)
                ) {
                    // Income bar
                    val incomeHeight =
                        ((item.income / maxValue) * maxHeight.value * animatedProgress.value).dp
                    Box(
                        modifier =
                            Modifier.width(barWidth / 2)
                                .height(incomeHeight.coerceAtLeast(4.dp))
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp
                                    )
                                )
                                .background(Income)
                    )

                    // Expense bar
                    val expenseHeight =
                        ((item.expense / maxValue) * maxHeight.value * animatedProgress.value)
                            .dp
                    Box(
                        modifier =
                            Modifier.width(barWidth / 2)
                                .height(expenseHeight.coerceAtLeast(4.dp))
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp
                                    )
                                )
                                .background(Expense)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class BarChartData(val label: String, val income: Double, val expense: Double)

/** Pie chart for category breakdown */
@Composable
fun PieChart(data: List<PieChartData>, modifier: Modifier = Modifier, size: Dp = 200.dp) {
    val total = data.sumOf { it.value }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f

            data.forEach { item ->
                val sweepAngle =
                    if (total > 0) {
                        ((item.value / total) * 360 * animatedProgress.value).toFloat()
                    } else 0f

                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(this.size.width, this.size.height)
                )

                startAngle += sweepAngle
            }
        }

        // Center hole for donut effect
        Box(
            modifier =
                Modifier.size(size * 0.5f)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
        )
    }
}

data class PieChartData(
    val label: String,
    val value: Double,
    val color: Color,
    val icon: String = ""
)

/** Legend for pie chart */
@Composable
fun PieChartLegend(data: List<PieChartData>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.value }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { item ->
            val percentage = if (total > 0) ((item.value / total) * 100).toInt() else 0

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color dot
                Box(
                    modifier =
                        Modifier.size(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(item.color)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Icon + Label
                if (item.icon.isNotEmpty()) {
                    Text(text = item.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = OnSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

// Helper function to format amounts
private fun formatAmount(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "${(amount / 1_000_000).formatDecimal(1)}M"
        amount >= 1_000 -> "${(amount / 1_000).formatDecimal(1)}K"
        else -> amount.formatDecimal(2)
    }
}
