package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aevrontech.finevo.core.util.formatDecimal
import com.aevrontech.finevo.domain.model.Label
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable // Line 98
fun TimeFilterTabs(
    selectedPeriod: FilterPeriod,
    onPeriodSelected: (FilterPeriod) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE3F2FD),
    selectedContentColor: Color = Color.White,
    unselectedContentColor: Color = Color(0xFF64748B),
    indicatorBrush: Brush =
        Brush.horizontalGradient(
            colors =
                listOf(
                    DashboardGradientStart,
                    DashboardGradientMid,
                    DashboardGradientEnd
                )
        )
) {
    Row(
        modifier =
            modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)) // Pill shape for container
                .background(containerColor)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod

            Box(
                modifier =
                    Modifier.weight(1f)
                        .clip(
                            RoundedCornerShape(20.dp)
                        ) // Pill shape for item
                        .then(
                            if (isSelected)
                                Modifier.background(indicatorBrush)
                            else Modifier.background(Color.Transparent)
                        )
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    fontSize = 14.sp,
                    fontWeight =
                        if (isSelected) FontWeight.SemiBold
                        else FontWeight.Medium,
                    color =
                        if (isSelected) selectedContentColor
                        else unselectedContentColor
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            modifier = Modifier.weight(1f),
            onClick = onClick
        )

        // Expense Card
        SummaryTypeCard(
            title = "Expense",
            amount = expense,
            percentage =
                expensePercentageVal, // Can go > 1.0, progress indicator handles
            // scaling
            // usually
            displayPercentage = displayExpensePercentage,
            currencySymbol = currencySymbol,
            backgroundColor = ExpenseCardBg,
            accentColor = ExpenseCardAccent,
            amountColor = Color.Black, // User requested Black
            icon = Icons.Default.Close,
            modifier = Modifier.weight(1f),
            onClick = onClick
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
    amountColor: Color = Color.White,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text =
                        "$currencySymbol${amount.formatDecimal(2, useGrouping = true)}",
                    color = amountColor,
                    fontSize = 12.sp, // Reduced from 20.sp to fit millions
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow =
                        androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                    color = Color.Black, // User requested black for amount,
                    // assuming for % too
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
    barWidth: Dp = 16.dp,
    maxHeight: Dp = 200.dp,
    rotateLabels: Boolean = false,
    period: FilterPeriod? = null,
    monthPrefix: String = "",
    barBrush: Brush? = null,
    axisLabelColor: Color = OnSurfaceVariant,
    gridLineColor: Color = Color.Gray.copy(alpha = 0.15f)
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    // Round up max value to nice number for Y-axis
    val yAxisMax =
        if (maxValue == 0.0) 100.0 else (maxValue * 1.2).let { Math.ceil(it / 100) * 100 }

    val animatedProgress = remember { Animatable(0f) }
    var touchedIndex by remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }
    var chartWidth by remember { androidx.compose.runtime.mutableStateOf(0f) }

    LaunchedEffect(data) {
        touchedIndex = null // Reset selection on data change
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val defaultGradientBrush =
        Brush.verticalGradient(
            colors =
                listOf(
                    DashboardGradientStart,
                    DashboardGradientMid,
                    DashboardGradientEnd
                )
        )

    // Theme-aware label color
    val labelColor = axisLabelColor
    val labelHeight = if (rotateLabels) 32.dp else 20.dp

    // Main Container - Column layout: Chart Area + X-Labels
    Column(modifier = modifier.height(maxHeight + labelHeight)) {
        // Chart Area (Y-axis + Bars)
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Y-Axis Labels
            // Y-Axis Labels - positioned to align with Canvas grid lines
            Box(modifier = Modifier.width(36.dp).fillMaxHeight()) {
                // Position each label at exact percentages to match grid
                for (i in 5 downTo 0) {
                    val label = (yAxisMax * i / 5).toInt().toString()
                    val verticalOffset = 1f - (i / 5f) // 0->1, 1->0.8, etc
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = labelColor,
                        modifier =
                            Modifier.align(Alignment.TopEnd)
                                .fillMaxHeight(
                                    verticalOffset
                                        .coerceAtLeast(
                                            0.001f
                                        )
                                )
                                .wrapContentHeight(Alignment.Bottom)
                                .offset(y = 6.dp)
                                .padding(end = 4.dp)
                    )
                }
            }

            // Chart Content (Bars + Labels)
            Box(
                modifier =
                    Modifier.weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            chartWidth =
                                coordinates.size.width.toFloat()
                        }
                        .pointerInput(data.size) {
                            detectTapGestures(
                                onPress = { offset ->
                                    if (data.isNotEmpty()) {
                                        val barSlotWidth =
                                            chartWidth /
                                                data.size
                                        val index =
                                            (offset.x /
                                                barSlotWidth)
                                                .toInt()
                                                .coerceIn(
                                                    0,
                                                    data.lastIndex
                                                )
                                        touchedIndex = index
                                        tryAwaitRelease()
                                        touchedIndex = null
                                    }
                                }
                            )
                        }
                        .pointerInput(data.size) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    if (data.isNotEmpty()) {
                                        val barSlotWidth =
                                            chartWidth /
                                                data.size
                                        val index =
                                            (offset.x /
                                                barSlotWidth)
                                                .toInt()
                                                .coerceIn(
                                                    0,
                                                    data.lastIndex
                                                )
                                        touchedIndex = index
                                    }
                                },
                                onDragEnd = { touchedIndex = null },
                                onDragCancel = {
                                    touchedIndex = null
                                }
                            ) { change, _ ->
                                if (data.isNotEmpty()) {
                                    val barSlotWidth =
                                        chartWidth /
                                            data.size
                                    val index =
                                        (change.position.x /
                                            barSlotWidth)
                                            .toInt()
                                            .coerceIn(
                                                0,
                                                data.lastIndex
                                            )
                                    touchedIndex = index
                                }
                            }
                        }
            ) {
                // Canvas for precise bar rendering - bars drawn from bottom (0) up
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidthPx = barWidth.toPx()
                    val barSlotWidth = canvasWidth / data.size
                    val cornerRadius = 4.dp.toPx()

                    // Draw horizontal grid lines at Y-axis tick positions
                    val gridColor = gridLineColor
                    for (i in 0..5) {
                        val y = canvasHeight * (1f - i / 5f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw bars - bottom edge at canvasHeight (the 0 line)
                    data.forEachIndexed { index, item ->
                        val heightRatio =
                            (item.value / yAxisMax).toFloat() *
                                animatedProgress.value
                        val barHeight = canvasHeight * heightRatio
                        val isTouched = touchedIndex == index

                        if (item.value > 0) {
                            val actualBarWidth =
                                if (isTouched) barWidthPx + 8f
                                else barWidthPx
                            val barLeft =
                                barSlotWidth * index +
                                    (barSlotWidth -
                                        actualBarWidth) / 2
                            val barTop = canvasHeight - barHeight
                            val barAlpha = if (isTouched) 1f else 0.7f

                            val computedBrush =
                                barBrush ?: defaultGradientBrush

                            // Adjust alpha for touch
                            // Note: We can't easily modify a Brush
                            // instance's alpha unless we
                            // recreate it or draw with alpha.
                            // drawRoundRect has 'alpha' param.

                            drawRoundRect(
                                brush = computedBrush,
                                topLeft = Offset(barLeft, barTop),
                                size =
                                    androidx.compose.ui.geometry
                                        .Size(
                                            actualBarWidth,
                                            barHeight
                                        ),
                                cornerRadius =
                                    androidx.compose.ui.geometry
                                        .CornerRadius(
                                            cornerRadius,
                                            cornerRadius
                                        ),
                                alpha = barAlpha // Use alpha here
                                // instead of
                                // modifying brush
                            )
                        }
                    }
                }

                // Tooltip Overlay
                if (touchedIndex != null && data.isNotEmpty()) {
                    val index = touchedIndex!!
                    if (index in data.indices) {
                        val item = data[index]
                        Box(
                            modifier =
                                Modifier.align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                        ) {
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .surface
                                    ),
                                elevation =
                                    CardDefaults.cardElevation(
                                        defaultElevation =
                                            4.dp
                                    ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            8.dp
                                        ),
                                    horizontalAlignment =
                                        Alignment
                                            .CenterHorizontally
                                ) {
                                    // Show proper formatted
                                    // label in tooltip (e.g. "1
                                    // Jan" for
                                    // Month view)
                                    val tooltipLabel =
                                        if (period ==
                                            FilterPeriod
                                                .MONTH &&
                                            monthPrefix
                                                .isNotEmpty()
                                        ) {
                                            "$monthPrefix ${item.label}"
                                        } else {
                                            item.label
                                        }
                                    Text(
                                        text = tooltipLabel,
                                        fontSize = 12.sp,
                                        color =
                                            OnSurfaceVariant
                                    )
                                    Text(
                                        text =
                                            "$${item.value.formatDecimal(2, useGrouping = true)}",
                                        fontSize = 14.sp,
                                        fontWeight =
                                            FontWeight
                                                .Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // X-Axis Labels Row (below 0 baseline)
        Row(
            modifier =
                Modifier.fillMaxWidth().height(labelHeight).padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.forEachIndexed { index, item ->
                val isTouched = touchedIndex == index
                val showLabel =
                    if (period == FilterPeriod.MONTH) {
                        val day = item.label.toIntOrNull()
                        day != null &&
                            (day == 1 ||
                                day == 8 ||
                                day == 15 ||
                                day == 22 ||
                                day == 29)
                    } else {
                        true
                    }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLabel) {
                        // For Month view, just show day number to save
                        // space
                        // For Year view, show month abbreviation
                        // For Week view, show day name
                        val labelText = item.label
                        Text(
                            text = labelText,
                            fontSize = 10.sp,
                            color =
                                if (isTouched)
                                    MaterialTheme.colorScheme
                                        .primary
                                else labelColor,
                            maxLines = 1,
                            textAlign =
                                androidx.compose.ui.text.style
                                    .TextAlign.Center,
                            overflow =
                                androidx.compose.ui.text.style
                                    .TextOverflow.Visible,
                            softWrap = false,
                            modifier =
                                if (rotateLabels)
                                    Modifier.rotate(-45f)
                                else Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<BarChartItem>,
    modifier: Modifier = Modifier,
    monthName: String,
    period: FilterPeriod
) {
    // Generate X-Axis Labels dynamically
    val xLabels =
        when (period) {
            FilterPeriod.DAY -> listOf("0", "6", "12", "18")
            FilterPeriod.WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            FilterPeriod.MONTH -> listOf(1, 8, 15, 22, 29)
            FilterPeriod.YEAR ->
                listOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )
        }

    val maxY = data.maxOfOrNull { it.value } ?: 1.0
    val yAxisMax = if (maxY == 0.0) 100.0 else (maxY * 1.2)

    var touchedIndex by remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    val lineColor = Color.White
    val gradientBrush =
        Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
        )
    val labelColor = Color.White

    // Main Container
    Column(modifier = modifier) {

        // Chart Area (Y-axis + Canvas)
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {

            // Y-Axis Labels
            Box(modifier = Modifier.width(36.dp).fillMaxHeight()) {
                for (i in 5 downTo 0) {
                    val label = (yAxisMax * i / 5).toInt().toString()
                    val verticalOffset = 1f - (i / 5f)
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = labelColor,
                        modifier =
                            Modifier.align(Alignment.TopEnd)
                                .fillMaxHeight(
                                    verticalOffset
                                        .coerceAtLeast(
                                            0.001f
                                        )
                                )
                                .wrapContentHeight(Alignment.Bottom)
                                .offset(y = 6.dp)
                                .padding(end = 4.dp)
                    )
                }
            }

            // Chart Canvas
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Canvas(
                    modifier =
                        Modifier.fillMaxSize()
                            .pointerInput(data.size) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        if (data.isNotEmpty()
                                        ) {
                                            val spacing =
                                                size.width /
                                                    (data.size -
                                                        1)
                                                        .coerceAtLeast(
                                                            1
                                                        )
                                            val index =
                                                ((offset.x +
                                                    spacing /
                                                    2) /
                                                    spacing)
                                                    .toInt()
                                                    .coerceIn(
                                                        0,
                                                        data.lastIndex
                                                    )
                                            touchedIndex =
                                                index
                                            tryAwaitRelease()
                                            touchedIndex =
                                                null
                                        }
                                    }
                                )
                            }
                            .pointerInput(data.size) {
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        if (data.isNotEmpty()
                                        ) {
                                            val spacing =
                                                size.width /
                                                    (data.size -
                                                        1)
                                                        .coerceAtLeast(
                                                            1
                                                        )
                                            val index =
                                                ((offset.x +
                                                    spacing /
                                                    2) /
                                                    spacing)
                                                    .toInt()
                                                    .coerceIn(
                                                        0,
                                                        data.lastIndex
                                                    )
                                            touchedIndex =
                                                index
                                        }
                                    },
                                    onDragEnd = {
                                        touchedIndex = null
                                    },
                                    onDragCancel = {
                                        touchedIndex = null
                                    }
                                ) { change, _ ->
                                    if (data.isNotEmpty()) {
                                        val spacing =
                                            size.width /
                                                (data.size -
                                                    1)
                                                    .coerceAtLeast(
                                                        1
                                                    )
                                        val index =
                                            ((change.position
                                                .x +
                                                spacing /
                                                2) /
                                                spacing)
                                                .toInt()
                                                .coerceIn(
                                                    0,
                                                    data.lastIndex
                                                )
                                        touchedIndex = index
                                    }
                                }
                            }
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.size - 1).coerceAtLeast(1)

                    // Draw horizontal grid lines
                    val gridColor = Color.White.copy(alpha = 0.2f)
                    for (i in 0..5) {
                        val y = height * (1f - i / 5f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    val path = androidx.compose.ui.graphics.Path()
                    val fillPath = androidx.compose.ui.graphics.Path()

                    val points =
                        data.mapIndexed { index, item ->
                            val x = index * spacing
                            val y =
                                height -
                                    ((item.value / yAxisMax) *
                                        height)
                                        .toFloat()
                            Offset(x, y)
                        }

                    if (points.isNotEmpty()) {
                        path.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, height)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPoint1 =
                                Offset(p1.x + spacing / 2, p1.y)
                            val controlPoint2 =
                                Offset(p2.x - spacing / 2, p2.y)
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

                        drawPath(path = fillPath, brush = gradientBrush)
                        drawPath(
                            path = path,
                            color = lineColor,
                            style =
                                androidx.compose.ui.graphics
                                    .drawscope.Stroke(
                                        width = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                        )

                        // Draw dots
                        points.forEach { point ->
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }

                        touchedIndex?.let { index ->
                            val point = points.getOrNull(index)
                            if (point != null) {
                                drawLine(
                                    color =
                                        OnSurfaceVariant
                                            .copy(
                                                alpha =
                                                    0.5f
                                            ),
                                    start = Offset(point.x, 0f),
                                    end =
                                        Offset(
                                            point.x,
                                            height
                                        ),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect =
                                        androidx.compose.ui
                                            .graphics
                                            .PathEffect
                                            .dashPathEffect(
                                                floatArrayOf(
                                                    10f,
                                                    10f
                                                )
                                            )
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = point
                                )
                            }
                        }
                    }
                }

                // Tooltip
                touchedIndex?.let { index ->
                    val item = data.getOrNull(index)
                    if (item != null) {
                        val labelText =
                            when (period) {
                                FilterPeriod.DAY -> item.label
                                FilterPeriod.WEEK -> item.label
                                FilterPeriod.YEAR -> item.label
                                FilterPeriod.MONTH ->
                                    "$monthName ${item.label}"
                            }
                        Box(
                            modifier =
                                Modifier.align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                        ) {
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .surface
                                    ),
                                elevation =
                                    CardDefaults.cardElevation(
                                        defaultElevation =
                                            4.dp
                                    ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            8.dp
                                        ),
                                    horizontalAlignment =
                                        Alignment
                                            .CenterHorizontally
                                ) {
                                    Text(
                                        text = labelText,
                                        fontSize = 12.sp,
                                        color =
                                            OnSurfaceVariant
                                    )
                                    Text(
                                        text =
                                            "$${item.value.formatDecimal(2, useGrouping = true)}",
                                        fontSize = 14.sp,
                                        fontWeight =
                                            FontWeight
                                                .Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth().height(28.dp).padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            xLabels.forEach { label ->
                val text = label.toString()
                Text(text = text, fontSize = 10.sp, color = labelColor)
            }
        }
    }
}

@Composable
fun StatisticsCard(
    selectedPeriod: FilterPeriod,
    periodOffset: Int,
    barChartData: List<BarChartItem>, // Now accepts prepared data
    monthName: String = "", // New param for Line Chart labels
    modifier: Modifier = Modifier
) {
    // Default to Bar Chart as requested, but allow toggle for ALL periods
    var isBarChart by remember { androidx.compose.runtime.mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with Sort label and Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedPeriod.label}ly Statistics", // Changed
                    // "Sort by"
                    // to
                    // "{Period}ly Statistics"
                    // or just "{Period}" as
                    // "Sort by" is confusing
                    // for a view mode. User
                    // said "Sort by Month".
                    // I'll keep user text or
                    // improve? User said "Sort
                    // by Month, too many
                    // padding with the top". I
                    // will reduce padding.
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )

                // Toggle Button (Icon based) with Custom Icons
                IconButton(onClick = { isBarChart = !isBarChart }) {
                    // Custom Draw for Icons since dependency is removed
                    if (isBarChart) {
                        // Draw "Show Line Chart" icon (Zigzag)
                        Icon(
                            imageVector =
                                androidx.compose.ui.graphics.vector
                                    .ImageVector.Builder(
                                        defaultWidth =
                                            24.dp,
                                        defaultHeight =
                                            24.dp,
                                        viewportWidth = 24f,
                                        viewportHeight = 24f
                                    )
                                    .run {
                                        addPath(
                                            pathData =
                                                androidx.compose
                                                    .ui
                                                    .graphics
                                                    .vector
                                                    .PathParser()
                                                    .parsePathString(
                                                        "M3.5,18.49l6,-6.01l4,4L22,6.92l-1.41,-1.41l-7.09,7.97l-4,-4L2,16.99L3.5,18.49z"
                                                    )
                                                    .toNodes(),
                                            fillAlpha =
                                                1f,
                                            strokeAlpha =
                                                1f,
                                            fill =
                                                androidx.compose
                                                    .ui
                                                    .graphics
                                                    .SolidColor(
                                                        Primary
                                                    )
                                        )
                                        build()
                                    },
                            contentDescription = "Show Line Chart",
                            tint = Primary
                        )
                    } else {
                        // Draw "Show Bar Chart" icon
                        Icon(
                            imageVector =
                                androidx.compose.ui.graphics.vector
                                    .ImageVector.Builder(
                                        defaultWidth =
                                            24.dp,
                                        defaultHeight =
                                            24.dp,
                                        viewportWidth = 24f,
                                        viewportHeight = 24f
                                    )
                                    .run {
                                        addPath(
                                            pathData =
                                                androidx.compose
                                                    .ui
                                                    .graphics
                                                    .vector
                                                    .PathParser()
                                                    .parsePathString(
                                                        "M5,9.2h3V19H5V9.2z M10.6,5h2.8v14h-2.8V5z M16.2,13H19v6h-2.8V13z"
                                                    )
                                                    .toNodes(),
                                            fillAlpha =
                                                1f,
                                            strokeAlpha =
                                                1f,
                                            fill =
                                                androidx.compose
                                                    .ui
                                                    .graphics
                                                    .SolidColor(
                                                        Primary
                                                    )
                                        )
                                        build()
                                    },
                            contentDescription = "Show Bar Chart",
                            tint = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16.dp

            // Check if data is empty
            if (barChartData.isNotEmpty()) {
                if (!isBarChart) {
                    // Use Line Chart
                    LineChart(
                        data = barChartData,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        monthName = monthName,
                        period = selectedPeriod
                    )
                } else {
                    // Use Bar Chart
                    // Adjust bar width for Month view (30 bars) to avoid
                    // overlap
                    val barWidth =
                        if (selectedPeriod == FilterPeriod.MONTH) 6.dp
                        else 16.dp

                    GradientBarChart(
                        data = barChartData,
                        modifier = Modifier.fillMaxWidth(),
                        maxHeight = 200.dp,
                        barWidth = barWidth,
                        rotateLabels = selectedPeriod == FilterPeriod.YEAR,
                        period = selectedPeriod, // Pass period for custom
                        // label logic
                        monthPrefix =
                            monthName // Pass month name for sparse
                        // labels
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
fun TransactionHistorySection(
    transactions: List<Transaction>,
    availableLabels: List<Label>,
    currencySymbol: String = "RM",
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
            // 'See all' might be redundant if we show a good amount, but keeping as
            // requested
            TextButton(onClick = onSeeAllClick) {
                Text(text = "See all", color = Primary)
            }
        }

        Spacer(modifier = Modifier.height(0.dp)) // Reduced gap as requested

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions here",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            // Group transactions by Date
            val groupedTransactions = transactions.take(limit).groupBy { it.date }

            groupedTransactions.forEach { (date, dailyTransactions) ->
                // Date Header Logic: Today, Yesterday, or Date
                val today =
                    Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
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
                    modifier =
                        Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )

                // Card for the Day Group
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .surface // White in Light
                            // Mode
                        ),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 0.5.dp
                        )
                ) {
                    Column {
                        dailyTransactions.forEachIndexed { index,
                                                           transaction ->
                            TransactionHistoryItem(
                                transaction = transaction,
                                availableLabels = availableLabels,
                                currencySymbol = currencySymbol,
                                onClick = {
                                    onTransactionClick(
                                        transaction
                                    )
                                }
                            )
                            // Divider between items, but not after the
                            // last one
                            if (index < dailyTransactions.lastIndex) {
                                androidx.compose.material3
                                    .HorizontalDivider(
                                        modifier =
                                            Modifier.padding(
                                                horizontal =
                                                    16.dp
                                            ),
                                        thickness = 0.5.dp,
                                        color =
                                            OnSurfaceVariant
                                                .copy(
                                                    alpha =
                                                        0.1f
                                                )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionHistoryItem(
    transaction: Transaction,
    availableLabels: List<Label>,
    currencySymbol: String,
    onClick: () -> Unit
) {
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
                            val colorString =
                                (transaction.categoryColor
                                    ?: "#42A5F5")
                                    .removePrefix("#")
                            Color(("FF$colorString").toLong(16))
                        } catch (e: Exception) {
                            Primary
                        }.copy(alpha = 0.2f)
                    ),
            contentAlignment = Alignment.Center
        ) { Text(text = transaction.categoryIcon ?: "ðŸ’µ", fontSize = 18.sp) }

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
                    text = transaction.categoryName ?: "Transaction",
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.weight(1f, fill = false)
                            .padding(end = 8.dp)
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
                        "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}$currencySymbol${
                            transaction.amount.formatDecimal(
                                2
                            )
                        }",
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            // Row 2: Account Name & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Name
                Text(
                    text = transaction.accountName ?: "Unknown",
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.weight(1f, fill = false)
                            .padding(end = 8.dp)
                )

                // Time
                val timeString =
                    transaction.time?.let { formatTimeToAmPm(it) }
                        ?: formatTimeFromInstant(transaction.createdAt)
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Row 3: Note (if present)
            if (!transaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "\"${transaction.note}\"",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow =
                        androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                        val label =
                            availableLabels.find { it.id == labelId }
                        if (label != null) {
                            val labelColor =
                                try {
                                    val colorString =
                                        label.color
                                            .removePrefix(
                                                "#"
                                            )
                                    Color(
                                        ("FF$colorString")
                                            .toLong(16)
                                    )
                                } catch (e: Exception) {
                                    Primary
                                }

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
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.Medium
                                )
                            }
                        }
                    }
                }
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

/**
 * Category Report Section with Income/Expense summary cards and donut pie charts Displays category
 * breakdown with icons connected to chart segments
 */
@Composable
fun CategoryReportSection(
    incomeTotal: Double,
    expenseTotal: Double,
    incomeBreakdown: List<CategoryBreakdown>,
    expenseBreakdown: List<CategoryBreakdown>,
    currencySymbol: String = "RM",
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    onSeeReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedType by remember {
        androidx.compose.runtime.mutableStateOf(TransactionType.EXPENSE)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Text(
                text = "Report by Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Income and Expense Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                CategorySummaryCard(
                    title = "Income",
                    amount = incomeTotal,
                    currencySymbol = currencySymbol,
                    accentColor = Color(0xFF4CAF50),
                    onClick = { selectedType = TransactionType.INCOME },
                    isSelected = selectedType == TransactionType.INCOME,
                    modifier = Modifier.weight(1f)
                )

                // Expense Card
                CategorySummaryCard(
                    title = "Expense",
                    amount = expenseTotal,
                    currencySymbol = currencySymbol,
                    accentColor = Color(0xFFF26A6A),
                    onClick = { selectedType = TransactionType.EXPENSE },
                    isSelected = selectedType == TransactionType.EXPENSE,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Single Donut Chart (Centered)
            Box(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentBreakdown =
                    if (selectedType == TransactionType.EXPENSE)
                        expenseBreakdown
                    else incomeBreakdown
                val currentTotal =
                    if (selectedType == TransactionType.EXPENSE) expenseTotal
                    else incomeTotal

                CategoryDonutChartWithIcons(
                    breakdown = currentBreakdown,
                    totalAmount = currentTotal,
                    chartSize = 130.dp,
                    strokeWidth = 28.dp,
                    currencySymbol = currencySymbol
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // See report by categories link
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable(onClick = onSeeReportClick)
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Box icon
                Icon(
                    imageVector =
                        androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "See report by categories",
                    fontSize = 14.sp,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CategorySummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    accentColor: Color,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isSelected) 2.dp else 0.dp
    val borderColor = if (isSelected) accentColor else Color.Transparent

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) accentColor.copy(alpha = 0.1f)
                    else Color(0xFFF5F5F5)
            ),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 12.sp, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        "$currencySymbol ${amount.formatDecimal(2, useGrouping = true)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** Simple donut chart for income (without external icons) */
@Composable
private fun CategoryDonutChart(
    breakdown: List<CategoryBreakdown>,
    totalAmount: Double,
    chartSize: Dp,
    strokeWidth: Dp,
    showIcons: Boolean = false
) {
    val total = if (totalAmount > 0) totalAmount else 1.0

    Box(modifier = Modifier.size(chartSize + 40.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(chartSize)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.minDimension - strokePx) / 2
            val center = Offset(size.width / 2, size.height / 2)

            if (breakdown.isEmpty()) {
                // Draw empty ring
                drawArc(
                    color = Color(0xFFE0E0E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style =
                        androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round
                        )
                )
            } else {
                var startAngle = -90f
                breakdown.forEach { category ->
                    val sweepAngle = ((category.total / total) * 360f).toFloat()
                    val color =
                        try {
                            Color(
                                ("FF" +
                                    category.categoryColor
                                        .removePrefix(
                                            "#"
                                        ))
                                    .toLong(16)
                            )
                        } catch (e: Exception) {
                            Primary
                        }

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtLeast(1f),
                        useCenter = false,
                        style =
                            androidx.compose.ui.graphics.drawscope
                                .Stroke(
                                    width = strokePx,
                                    cap = StrokeCap.Butt
                                )
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Center percentage
        if (breakdown.isNotEmpty()) {
            val mainPercentage =
                ((breakdown.firstOrNull()?.total ?: 0.0) / total * 100).roundToInt()
            Text(
                text = "${mainPercentage}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Donut chart with category icons connected by lines (for expense) */
@Composable
fun CategoryDonutChartWithIcons(
    breakdown: List<CategoryBreakdown>,
    totalAmount: Double,
    chartSize: Dp,
    strokeWidth: Dp,
    currencySymbol: String = "RM"
) {
    val total = if (totalAmount > 0) totalAmount else 1.0
    val density = LocalDensity.current

    // State for tooltip and selection
    var selectedCategory by remember {
        androidx.compose.runtime.mutableStateOf<CategoryBreakdown?>(null)
    }

    // Helper to find category by touch angle
    fun getCategoryAtAngle(
        touchAngle: Double,
        layoutData: List<Pair<Triple<CategoryBreakdown, Float, Float>, Double>>
    ): CategoryBreakdown? {
        val normalizedTouch = if (touchAngle < -90) touchAngle + 360 else touchAngle
        return layoutData
            .minByOrNull { (_, adjustedAngle) ->
                val diff = abs(adjustedAngle - normalizedTouch)
                min(diff, 360 - diff)
            }
            ?.takeIf { (_, adjustedAngle) ->
                val diff = abs(adjustedAngle - normalizedTouch)
                min(diff, 360 - diff) < 25 // Increased hit threshold
            }
            ?.first
            ?.first
    }

    // Increased container size to accommodate pop-out and radial text
    Box(modifier = Modifier.size(chartSize + 220.dp), contentAlignment = Alignment.Center) {
        val strokePx = with(density) { strokeWidth.toPx() }
        val chartSizePx = with(density) { chartSize.toPx() }
        val chartRadius = chartSize.value / 2
        val iconDistance = chartRadius + 45f

        // Pre-calculate layout data
        val layoutData =
            remember(breakdown, total) {
                var currentStart = -90.0
                val minSeparation = 30.0
                var lastAngle = -1000.0

                breakdown.take(8).map { category ->
                    val sweep = category.total / total * 360.0
                    // Ensure minimum sweep for visibility if needed, but
                    // keeping actual for accuracy
                    val midAngle = currentStart + sweep / 2

                    // Collision Detection
                    var adjustedAngle = midAngle
                    if (adjustedAngle < lastAngle + minSeparation) {
                        adjustedAngle = lastAngle + minSeparation
                    }
                    lastAngle = adjustedAngle

                    val data =
                        Triple(
                            category,
                            currentStart.toFloat(),
                            sweep.toFloat()
                        ) to adjustedAngle
                    currentStart += sweep
                    data
                }
            }

        // Handle Gestures
        Box(
            modifier =
                Modifier.size(chartSize)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val dx = offset.x - size.width / 2
                                val dy = offset.y - size.height / 2
                                val angle =
                                    (atan2(
                                        dy.toDouble(),
                                        dx.toDouble()
                                    ) * 180 / PI)
                                selectedCategory =
                                    getCategoryAtAngle(
                                        angle,
                                        layoutData
                                    )
                            },
                            onTap = { selectedCategory = null }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val dx = offset.x - size.width / 2
                                val dy = offset.y - size.height / 2
                                val angle =
                                    (atan2(
                                        dy.toDouble(),
                                        dx.toDouble()
                                    ) * 180 / PI)
                                selectedCategory =
                                    getCategoryAtAngle(
                                        angle,
                                        layoutData
                                    )
                            },
                            onDrag = { change, _ ->
                                val offset = change.position
                                val dx = offset.x - size.width / 2
                                val dy = offset.y - size.height / 2
                                val angle =
                                    (atan2(
                                        dy.toDouble(),
                                        dx.toDouble()
                                    ) * 180 / PI)
                                selectedCategory =
                                    getCategoryAtAngle(
                                        angle,
                                        layoutData
                                    )
                            },
                            onDragEnd = { selectedCategory = null },
                            onDragCancel = { selectedCategory = null }
                        )
                    }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = chartSizePx / 2

                // 1. Draw Donut Segments with Pop-out
                if (breakdown.isEmpty()) {
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style =
                            Stroke(
                                width = strokePx,
                                cap = StrokeCap.Round
                            )
                    )
                } else {
                    // Draw Inner Track
                    drawCircle(
                        color = Color(0xFFF0F0F0),
                        radius = radius,
                        style =
                            Stroke(
                                width = strokePx,
                                cap = StrokeCap.Butt
                            )
                    )

                    layoutData.forEach { (sliceData, _) ->
                        val (category, startAngle, sweepAngle) = sliceData
                        val isSelected = selectedCategory == category

                        val shiftPx = if (isSelected) 24f else 0f
                        val midRad =
                            (startAngle + sweepAngle / 2.0) * PI / 180.0
                        val shiftX = (shiftPx * cos(midRad)).toFloat()
                        val shiftY = (shiftPx * sin(midRad)).toFloat()

                        val color =
                            try {
                                Color(
                                    ("FF" +
                                        category.categoryColor
                                            .removePrefix(
                                                "#"
                                            ))
                                        .toLong(16)
                                )
                            } catch (e: Exception) {
                                Primary
                            }

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style =
                                Stroke(
                                    width = strokePx,
                                    cap = StrokeCap.Butt
                                ),
                            topLeft =
                                Offset(
                                    center.x - radius + shiftX,
                                    center.y - radius + shiftY
                                ),
                            size =
                                androidx.compose.ui.geometry.Size(
                                    chartSizePx,
                                    chartSizePx
                                )
                        )
                    }
                }

                // 2. Draw Connector Lines
                if (breakdown.isNotEmpty()) {
                    layoutData.forEach { (sliceData, adjustedAngle) ->
                        val (category, startAngle, sweepAngle) = sliceData
                        val isSelected = selectedCategory == category

                        // Calculate start point with same pop-out shift
                        val shiftPx = if (isSelected) 24f else 0f
                        val sliceMidRad =
                            (startAngle + sweepAngle / 2.0) * PI / 180.0
                        val shiftX = (shiftPx * cos(sliceMidRad)).toFloat()
                        val shiftY = (shiftPx * sin(sliceMidRad)).toFloat()

                        val startRad = sliceMidRad
                        val chartOuterRadiusPx = radius + strokePx / 2

                        val startX =
                            center.x +
                                shiftX +
                                (chartOuterRadiusPx + 2) *
                                cos(startRad).toFloat()
                        val startY =
                            center.y +
                                shiftY +
                                (chartOuterRadiusPx + 2) *
                                sin(startRad).toFloat()

                        // End point
                        val endRad = adjustedAngle * PI / 180.0
                        val iconDistPx =
                            with(density) { iconDistance.dp.toPx() }

                        val lineEndDist = iconDistPx - 24
                        val endX =
                            center.x +
                                lineEndDist * cos(endRad).toFloat()
                        val endY =
                            center.y +
                                lineEndDist * sin(endRad).toFloat()

                        drawLine(
                            color = OnSurfaceVariant.copy(alpha = 0.5f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }

        // 3. Icons and Text
        if (breakdown.isNotEmpty()) {
            layoutData.forEachIndexed { index, (sliceData, adjustedAngle) ->
                val (category, _, _) = sliceData
                val percentage = (category.total / total * 100).roundToInt()

                val isSelected = selectedCategory == category
                val scale by
                animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1.0f,
                    animationSpec =
                        spring(
                            dampingRatio = 0.7f,
                            stiffness = 300f
                        )
                )

                val angleRad = adjustedAngle * PI / 180.0
                val offsetX = (iconDistance * cos(angleRad)).toFloat()
                val offsetY = (iconDistance * sin(angleRad)).toFloat()

                // Icon
                Column(
                    modifier =
                        Modifier.offset(x = offsetX.dp, y = offsetY.dp)
                            .zIndex(if (isSelected) 10f else 1f)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier =
                            Modifier.size(24.dp)
                                .shadow(
                                    if (isSelected) 8.dp
                                    else 2.dp,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(
                                            ("FF" +
                                                category.categoryColor
                                                    .removePrefix(
                                                        "#"
                                                    ))
                                                .toLong(
                                                    16
                                                )
                                        )
                                    } catch (e: Exception) {
                                        Primary
                                    }
                                )
                                .clickable {
                                    selectedCategory =
                                        if (selectedCategory ==
                                            category
                                        )
                                            null
                                        else category
                                },
                        contentAlignment = Alignment.Center
                    ) { Text(text = category.categoryIcon, fontSize = 12.sp) }

                    // Tooltip Popup
                    if (isSelected) {
                        androidx.compose.ui.window.Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = {
                                selectedCategory = null
                            },
                            offset = IntOffset(0, -110)
                        ) {
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .inverseSurface
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                elevation =
                                    CardDefaults.cardElevation(
                                        4.dp
                                    )
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            8.dp
                                        ),
                                    horizontalAlignment =
                                        Alignment
                                            .CenterHorizontally
                                ) {
                                    Text(
                                        text =
                                            category.categoryName,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .inverseOnSurface,
                                        fontSize = 12.sp,
                                        fontWeight =
                                            FontWeight
                                                .Bold
                                    )
                                    Text(
                                        text =
                                            "$percentage%",
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .inverseOnSurface,
                                        fontSize = 12.sp,
                                        fontWeight =
                                            FontWeight
                                                .Normal
                                    )
                                    Text(
                                        text =
                                            "$currencySymbol${category.total.formatDecimal(2)}",
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .inverseOnSurface
                                                .copy(
                                                    alpha =
                                                        0.8f
                                                ),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Percentage Text - Radial Placement
                val textDist = iconDistance + 35f
                val textOffsetX = (textDist * cos(angleRad)).toFloat()
                val textOffsetY = (textDist * sin(angleRad)).toFloat()

                Text(
                    text = "$percentage%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    modifier =
                        Modifier.offset(
                            x = textOffsetX.dp,
                            y = textOffsetY.dp
                        )
                )
            }
        }
    }
}
