package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.domain.model.Transaction
import com.aevrontech.finevo.domain.model.TransactionType
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.ExpenseCardAccent
import com.aevrontech.finevo.ui.theme.ExpenseCardBg
import com.aevrontech.finevo.ui.theme.IncomeCardAccent
import com.aevrontech.finevo.ui.theme.IncomeCardBg
import com.aevrontech.finevo.ui.theme.OnSurfaceVariant
import com.aevrontech.finevo.ui.theme.Primary
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
fun TimeFilterTabs(
        selectedPeriod: FilterPeriod,
        onPeriodSelected: (FilterPeriod) -> Unit,
        modifier: Modifier = Modifier
) {
    val gradientBrush =
            Brush.horizontalGradient(
                    colors =
                            listOf(
                                    DashboardGradientStart,
                                    DashboardGradientMid,
                                    DashboardGradientEnd
                            )
            )

    Row(
            modifier =
                    modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)) // Pill shape for container
                            .background(
                                    Color(0xFFE3F2FD)
                            ) // Soft Blue Background (Light Mode friendly)
                            .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod

            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .clip(RoundedCornerShape(20.dp)) // Pill shape for item
                                    .then(
                                            if (isSelected) Modifier.background(gradientBrush)
                                            else Modifier.background(Color.Transparent)
                                    )
                                    .clickable { onPeriodSelected(period) }
                                    .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = period.label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color =
                                if (isSelected) Color.White
                                else Color(0xFF64748B) // Slate Gray for unselected
                )
            }
        }
    }
}

/** Income and Expense Summary Cards with Circular Progress (Percentage Round) */
@Composable
fun IncomeExpenseCards(
        income: Double,
        expense: Double,
        currencySymbol: String = "$",
        modifier: Modifier = Modifier
) {
    // Logic:
    // Income Percentage = (Income - Expense) / Income -> "Remaining" or "Safe" percentage?
    // User requested: "income 1000, expense 200 -> income 80%, expense 20%"
    // This implies Income % = (1000 - 200)/1000 = 80%.
    // Expense % = 200/1000 = 20%.

    val total = if (income > 0) income else 1.0 // Avoid div by zero

    // Calculate percentages
    // Ensure we don't go negative or above 100 visually for the progress bar,
    // though Expense could theoretically be > 100% of income.
    val expensePercentageVal = (expense / total).toFloat().coerceIn(0f, 1f)
    val incomePercentageVal = ((income - expense) / total).toFloat().coerceIn(0f, 1f)
    val displayExpensePercentage = (expensePercentageVal * 100).roundToInt()
    val displayIncomePercentage = (incomePercentageVal * 100).roundToInt()

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Income Card
        SummaryTypeCard(
                title = "Income",
                amount = income,
                percentage = incomePercentageVal,
                displayPercentage = displayIncomePercentage,
                currencySymbol = currencySymbol,
                backgroundColor = IncomeCardBg,
                accentColor = IncomeCardAccent,
                amountColor = Color.Black, // User requested Black
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f)
        )

        // Expense Card
        SummaryTypeCard(
                title = "Expense",
                amount = expense,
                percentage =
                        expensePercentageVal, // Can go > 1.0, progress indicator handles scaling
                // usually
                displayPercentage = displayExpensePercentage,
                currencySymbol = currencySymbol,
                backgroundColor = ExpenseCardBg,
                accentColor = ExpenseCardAccent,
                amountColor = Color.Black, // User requested Black
                icon = Icons.Default.Close,
                modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryTypeCard(
        title: String,
        amount: Double,
        percentage: Float,
        displayPercentage: Int,
        currencySymbol: String,
        backgroundColor: Color,
        accentColor: Color,
        amountColor: Color,
        icon: ImageVector,
        modifier: Modifier
) {
    Card(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content: Icon, Title, Amount
            Column(modifier = Modifier.weight(1f)) {
                // Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = title,
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = "$currencySymbol${String.format("%,.2f", amount)}",
                        color = amountColor,
                        fontSize = 12.sp, // Reduced from 20.sp to fit millions
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Right content: Progress Ring
            Box(contentAlignment = Alignment.Center) {
                // Background Ring (Faded accent)
                CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(50.dp),
                        color = accentColor.copy(alpha = 0.3f),
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent,
                )

                // Foreground Ring (Progress)
                // Note: CircularProgressIndicator progress is 0.0 .. 1.0.
                // If percentage > 1.0 (e.g. over budget), it just fills smoothly.
                CircularProgressIndicator(
                        progress = { percentage.coerceIn(0f, 1f) },
                        modifier = Modifier.size(50.dp),
                        color = accentColor,
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                )

                Text(
                        text = "$displayPercentage%",
                        color = Color.Black, // User requested black for amount, assuming for % too
                        // for readability?
                        // Or accent color? Reference image has dark text inside.
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class BarChartItem(val label: String, val value: Double, val isHighlighted: Boolean = false)

@Composable
fun GradientBarChart(
        data: List<BarChartItem>,
        modifier: Modifier = Modifier,
        barWidth: Dp = 16.dp, // Reduced width slightly to fit more
        maxHeight: Dp = 200.dp,
        rotateLabels: Boolean = false
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    // Round up max value to nice number for Y-axis
    val yAxisMax =
            if (maxValue == 0.0) 100.0 else (maxValue * 1.2).let { Math.ceil(it / 100) * 100 }

    val animatedProgress = remember { Animatable(0f) }
    var touchedIndex by remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }
    var chartWidth by remember { androidx.compose.runtime.mutableStateOf(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val gradientBrush =
            Brush.verticalGradient(
                    colors =
                            listOf(
                                    DashboardGradientStart,
                                    DashboardGradientMid,
                                    DashboardGradientEnd
                            )
            )

    // Theme-aware label color
    val labelColor = OnSurfaceVariant

    // Main Container
    Box(modifier = modifier.height(maxHeight)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Y-Axis Labels
            Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
            ) {
                // showing 5 ticks
                for (i in 5 downTo 0) {
                    Text(
                            text = (yAxisMax * i / 5).toInt().toString(),
                            fontSize = 10.sp,
                            color = labelColor
                    )
                }
            }

            // Chart Content (Bars + Labels)
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .fillMaxHeight()
                                    .onGloballyPositioned { coordinates ->
                                        chartWidth = coordinates.size.width.toFloat()
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                                onPress = { offset ->
                                                    if (data.isNotEmpty()) {
                                                        val barSlotWidth = chartWidth / data.size
                                                        val index =
                                                                (offset.x / barSlotWidth)
                                                                        .toInt()
                                                                        .coerceIn(0, data.lastIndex)
                                                        touchedIndex = index
                                                        tryAwaitRelease()
                                                        touchedIndex = null
                                                    }
                                                }
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                                onDragStart = { offset ->
                                                    if (data.isNotEmpty()) {
                                                        val barSlotWidth = chartWidth / data.size
                                                        val index =
                                                                (offset.x / barSlotWidth)
                                                                        .toInt()
                                                                        .coerceIn(0, data.lastIndex)
                                                        touchedIndex = index
                                                    }
                                                },
                                                onDragEnd = { touchedIndex = null },
                                                onDragCancel = { touchedIndex = null }
                                        ) { change, _ ->
                                            if (data.isNotEmpty()) {
                                                val barSlotWidth = chartWidth / data.size
                                                val index =
                                                        (change.position.x / barSlotWidth)
                                                                .toInt()
                                                                .coerceIn(0, data.lastIndex)
                                                touchedIndex = index
                                            }
                                        }
                                    }
            ) {
                Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween, // Distribute evenly
                        verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, item ->
                        // Calculate height percentage
                        val heightPercentage =
                                (item.value / yAxisMax).toFloat() * animatedProgress.value
                        val isTouched = touchedIndex == index

                        Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                        ) {
                            // Bar Area
                            Box(
                                    modifier =
                                            Modifier.weight(
                                                            1f
                                                    ) // Takes up remaining space above text
                                                    .fillMaxWidth(),
                                    contentAlignment = Alignment.BottomCenter
                            ) {
                                if (item.value > 0) {
                                    Box(
                                            modifier =
                                                    Modifier.width(
                                                                    if (isTouched) barWidth + 4.dp
                                                                    else barWidth
                                                            )
                                                            .fillMaxHeight(
                                                                    heightPercentage.coerceAtLeast(
                                                                            0.01f
                                                                    )
                                                            ) // Ensure tiny bar if > 0
                                                            .clip(
                                                                    RoundedCornerShape(
                                                                            topStart = 4.dp,
                                                                            topEnd = 4.dp
                                                                    )
                                                            )
                                                            .background(gradientBrush)
                                                            .alpha(if (isTouched) 1f else 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Label (Rotated or Normal)
                            Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    color =
                                            if (isTouched) MaterialTheme.colorScheme.primary
                                            else labelColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                                    softWrap = false,
                                    modifier =
                                            if (rotateLabels) {
                                                Modifier.rotate(-45f)
                                                        .padding(
                                                                top = 8.dp
                                                        ) // Push down slightly to avoid overlap
                                            } else {
                                                Modifier
                                            }
                            )
                        }
                    }
                }

                // Tooltip Overlay
                if (touchedIndex != null && data.isNotEmpty()) {
                    val item = data[touchedIndex!!]
                    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)) {
                        Card(
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = item.label, fontSize = 12.sp, color = OnSurfaceVariant)
                                Text(
                                        text = "$${String.format("%,.2f", item.value)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(
        selectedPeriod: FilterPeriod,
        periodOffset: Int,
        barChartData: List<BarChartItem>, // Now accepts prepared data
        onPeriodChange: (FilterPeriod) -> Unit, // Unused but kept for API stability
        monthName: String = "", // New param for Line Chart labels
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
            shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = "Sort by: ${selectedPeriod.label}",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Check if data is empty
            if (barChartData.isNotEmpty()) {
                if (selectedPeriod == FilterPeriod.MONTH) {
                    // Use Line Chart for Month
                    LineChart(
                            data = barChartData,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            monthName = monthName
                    )
                } else {
                    // Use Bar Chart for Week/Year
                    GradientBarChart(
                            data = barChartData,
                            modifier = Modifier.fillMaxWidth(),
                            maxHeight = 200.dp,
                            rotateLabels = selectedPeriod == FilterPeriod.YEAR
                    )
                }
            } else {
                Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                ) { Text("No data", color = OnSurfaceVariant) }
            }
        }
    }
}

@Composable
fun LineChart(data: List<BarChartItem>, modifier: Modifier = Modifier, monthName: String) {
    // Generate X-Axis Labels: Weekly (1st, 8th, 15th, 22nd, 30th)
    // Assuming data is 1..31 days
    val xLabels = listOf(1, 8, 15, 22, 30) // Simplified logic based on request
    val maxY = data.maxOfOrNull { it.value } ?: 1.0
    val yAxisMax = if (maxY == 0.0) 100.0 else (maxY * 1.2)

    var touchedIndex by remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    val lineColor = Primary
    val gradientBrush =
            Brush.verticalGradient(colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent))

    val labelColor = OnSurfaceVariant

    Row(modifier = modifier) {
        // Y-Axis Labels
        Column(
                modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
        ) {
            // showing 5 ticks
            for (i in 5 downTo 0) {
                Text(
                        text = (yAxisMax * i / 5).toInt().toString(),
                        fontSize = 10.sp,
                        color = labelColor
                )
            }
        }

        // Chart Area
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            Canvas(
                    modifier =
                            Modifier.fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                                onPress = { offset ->
                                                    val barSpacing =
                                                            size.width /
                                                                    (data.size - 1).coerceAtLeast(1)
                                                    val index =
                                                            (offset.x / barSpacing)
                                                                    .roundToInt()
                                                                    .coerceIn(0, data.lastIndex)
                                                    touchedIndex = index
                                                    tryAwaitRelease()
                                                    touchedIndex = null
                                                }
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                                onDragStart = { offset: Offset ->
                                                    val barSpacing =
                                                            size.width /
                                                                    (data.size - 1).coerceAtLeast(1)
                                                    val index =
                                                            (offset.x / barSpacing)
                                                                    .roundToInt()
                                                                    .coerceIn(0, data.lastIndex)
                                                    touchedIndex = index
                                                },
                                                onDragEnd = { touchedIndex = null },
                                                onDragCancel = { touchedIndex = null }
                                        ) { change: PointerInputChange, _: Float ->
                                            val barSpacing =
                                                    size.width / (data.size - 1).coerceAtLeast(1)
                                            val index =
                                                    (change.position.x / barSpacing)
                                                            .roundToInt()
                                                            .coerceIn(0, data.lastIndex)
                                            touchedIndex = index
                                        }
                                    }
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (data.size - 1).coerceAtLeast(1)

                val path = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()

                // Calculate points
                val points =
                        data.mapIndexed { index, item ->
                            val x = index * spacing
                            val y = height - ((item.value / yAxisMax) * height).toFloat()
                            Offset(x, y)
                        }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, height)
                    fillPath.lineTo(points.first().x, points.first().y)

                    // Smooth curve (Cubic Bezier)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlPoint1 = Offset(p1.x + spacing / 2, p1.y)
                        val controlPoint2 = Offset(p2.x - spacing / 2, p2.y)

                        path.cubicTo(
                                controlPoint1.x,
                                controlPoint1.y,
                                controlPoint2.x,
                                controlPoint2.y,
                                p2.x,
                                p2.y
                        )
                        fillPath.cubicTo(
                                controlPoint1.x,
                                controlPoint1.y,
                                controlPoint2.x,
                                controlPoint2.y,
                                p2.x,
                                p2.y
                        )
                    }

                    fillPath.lineTo(points.last().x, height)
                    fillPath.close()

                    // Draw Fill
                    drawPath(path = fillPath, brush = gradientBrush)

                    // Draw Line
                    drawPath(
                            path = path,
                            color = lineColor,
                            style =
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                    )
                    )

                    // Draw Tooltip if touched
                    touchedIndex?.let { index ->
                        val point = points.getOrNull(index)
                        if (point != null) {
                            // Vertical indicator line
                            drawLine(
                                    color = OnSurfaceVariant.copy(alpha = 0.5f),
                                    start = Offset(point.x, 0f),
                                    end = Offset(point.x, height),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect =
                                            androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(10f, 10f)
                                            )
                            )

                            // Dot
                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
                        }
                    }
                }
            }

            // Tooltip Overlay (Composable)
            touchedIndex?.let { index ->
                val item = data.getOrNull(index)
                if (item != null) {
                    // Calculate alignment
                    // Simple center-top placement, or dynamic
                    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)) {
                        Card(
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                        text = "$monthName ${item.label}",
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariant
                                )
                                Text(
                                        text = "$${String.format("%,.2f", item.value)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                )
                            }
                        }
                    }
                }
            }

            // X-Axis Labels
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // We want to show labels for specific days (simplified logic as placeholder)
                xLabels.forEach { day ->
                    Text(text = "$monthName $day", fontSize = 10.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TransactionHistorySection(
        transactions: List<Transaction>,
        limit: Int = 100, // Increased limit for scrolling
        onSeeAllClick: () -> Unit,
        onTransactionClick: (Transaction) -> Unit,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            // 'See all' might be redundant if we show a good amount, but keeping as requested
            TextButton(onClick = onSeeAllClick) { Text(text = "See all", color = Primary) }
        }

        Spacer(modifier = Modifier.height(0.dp)) // Reduced gap as requested

        if (transactions.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
            ) { Text(text = "No transactions here", color = OnSurfaceVariant, fontSize = 14.sp) }
        } else {
            // Group transactions by Date
            val groupedTransactions = transactions.take(limit).groupBy { it.date }

            groupedTransactions.forEach { (date, dailyTransactions) ->
                // Date Header Logic: Today, Yesterday, or Date
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val dateLabel =
                        when (date) {
                            today -> "Today"
                            today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
                            else -> formatDateFull(date)
                        }

                // Date Label
                Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )

                // Card for the Day Group
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme
                                                        .surface // White in Light Mode
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column {
                        dailyTransactions.forEachIndexed { index, transaction ->
                            TransactionHistoryItem(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction) }
                            )
                            // Divider between items, but not after the last one
                            if (index < dailyTransactions.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = OnSurfaceVariant.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TransactionHistoryItem(transaction: Transaction, onClick: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable(onClick = onClick)
                            .padding(16.dp), // Increased padding for better spacing
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
                modifier =
                        Modifier.size(40.dp)
                                .clip(
                                        androidx.compose.foundation.shape.CircleShape
                                ) // Circle shape as per new design ref
                                .background(
                                        try {
                                                    Color(
                                                            android.graphics.Color.parseColor(
                                                                    transaction.categoryColor
                                                                            ?: "#42A5F5"
                                                            )
                                                    )
                                                } catch (e: Exception) {
                                                    Primary
                                                }.copy(alpha = 0.2f)
                                ),
                contentAlignment = Alignment.Center
        ) { Text(text = transaction.categoryIcon ?: "💵", fontSize = 18.sp) }

        Spacer(modifier = Modifier.width(16.dp))

        // Main Content Column
        Column(modifier = Modifier.weight(1f)) {

            // Row 1: Category Name & Amount
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Name
                Text(
                        text = transaction.categoryName
                                        ?: "Transaction", // Default to category name
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface, // Black in Light Mode
                        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                )

                // Amount
                val amountColor =
                        if (transaction.type == TransactionType.EXPENSE) {
                            Color(0xFFF26A6A) // Salmon Red
                        } else {
                            Color(0xFF3CBFA6) // Teal Green
                        }

                Text(
                        text =
                                "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${transaction.currency}${
                            String.format("%.2f",
                                transaction.amount)
                        }",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 2: Account Name & Time
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Name
                Text(
                        text = transaction.accountName ?: "Unknown",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant, // Gray
                        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                )

                // Time
                val timeString =
                        transaction.time?.let { formatTimeToAmPm(it) }
                                ?: formatTimeFromInstant(transaction.createdAt)
                Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Row 3: Note (if present)
            if (!transaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                        text = transaction.note,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatDateFull(date: LocalDate): String {
    // "Mon 12 Aug 24"
    return "${
        date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    } ${date.dayOfMonth} ${date.month.name.take(3)} ${date.year.toString().takeLast(2)}"
}

private fun formatTimeFromInstant(instant: kotlinx.datetime.Instant): String {
    val localTime = instant.toLocalDateTime(TimeZone.currentSystemDefault()).time
    // Simple 12h format manually since standard formatter might need library
    val hour = localTime.hour
    val minute = localTime.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    return "${hour12}:${minute.toString().padStart(2, '0')} $amPm"
}

/** Convert 24-hour HH:mm format to 12-hour AM/PM format for display */
private fun formatTimeToAmPm(timeString: String): String {
    return try {
        val parts = timeString.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = if (hour % 12 == 0) 12 else hour % 12
            "${hour12}:${minute.toString().padStart(2, '0')} $amPm"
        } else {
            timeString // Return as-is if parsing fails
        }
    } catch (e: Exception) {
        timeString // Return as-is if parsing fails
    }
}

/** Period Navigator (Arrows + Label) */
@Composable
fun PeriodNavigator(
        periodLabel: String,
        onPrevious: () -> Unit,
        onNext: () -> Unit,
        canGoNext: Boolean,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
                text = periodLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint =
                            if (canGoNext) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
