package com.aevrontech.finevo.presentation.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.max

@Composable
fun BudgetTrendChart(data: BudgetTrendData, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val textPaint =
        remember(density) {
            androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = with(density) { 10.sp.toPx() }
                color = android.graphics.Color.GRAY
            }
        }

    val primaryColor = Color(0xFF10B981) // Green for spent
    val forecastColor = Color(0xFF3B82F6) // Blue for forecast
    val limitColor = Color.LightGray

    Box(modifier = modifier) {
        Canvas(
            modifier =
                Modifier.fillMaxSize()
                    .padding(start = 30.dp, end = 10.dp, bottom = 20.dp, top = 20.dp)
        ) {
            val width = size.width
            val height = size.height

            // Calculate Ranges
            val totalDays = data.visibleStartDate.daysUntil(data.visibleEndDate) + 1
            if (totalDays <= 0) return@Canvas

            // Y-Axis Max: Max of (Budget Limit, Max Spent, Max Forecast)
            val maxSpent = data.trendPoints.maxOfOrNull { it.second } ?: 0.0
            val maxForecast = data.forecastPoints.maxOfOrNull { it.second } ?: 0.0
            val maxY = max(data.budgetLimit, max(maxSpent, maxForecast)) * 1.1 // 10% buffering

            // Helpers for coordinate mapping
            fun xForDate(date: LocalDate): Float {
                val daysFromStart = data.visibleStartDate.daysUntil(date)
                val divisor = max(1, totalDays - 1)
                return (daysFromStart.toFloat() / divisor) * width
            }

            fun yForAmount(amount: Double): Float {
                return (height - (amount / maxY * height)).toFloat()
            }

            // --- Draw Grid Lines & Y-Labels ---
            val steps = 5
            val stepValue = maxY / steps
            for (i in 0..steps) {
                val value = stepValue * i
                val y = yForAmount(value)

                // Grid line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )

                // Label
                drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    -25f, // Offset to left
                    y + 10f, // Center vertically
                    textPaint
                )
            }

            // --- Draw Budget Limit Line (Dashed) ---
            val limitY = yForAmount(data.budgetLimit)
            drawLine(
                color = limitColor,
                start = Offset(0f, limitY),
                end = Offset(width, limitY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // --- Draw Trend (Spent) ---
            if (data.trendPoints.isNotEmpty()) {
                val path = Path()
                val firstPoint = data.trendPoints.first()
                path.moveTo(xForDate(firstPoint.first), yForAmount(firstPoint.second))

                data.trendPoints.drop(1).forEach { point ->
                    path.lineTo(xForDate(point.first), yForAmount(point.second))
                }

                // Fill Area
                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(xForDate(data.trendPoints.last().first), height)
                fillPath.lineTo(xForDate(data.trendPoints.first().first), height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.0f)
                                )
                        )
                )

                // Stroke
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // --- Draw Forecast ---
            if (data.forecastPoints.isNotEmpty()) {
                val path = Path()
                val firstPoint = data.forecastPoints.first()
                path.moveTo(xForDate(firstPoint.first), yForAmount(firstPoint.second))

                data.forecastPoints.drop(1).forEach { point ->
                    path.lineTo(xForDate(point.first), yForAmount(point.second))
                }

                drawPath(
                    path = path,
                    color = forecastColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // --- Draw "Today" Vertical Line ---
            val today = getCurrentLocalDate()
            if (today >= data.visibleStartDate && today <= data.visibleEndDate) {
                val todayX = xForDate(today)
                drawLine(
                    color = Color.Gray,
                    start = Offset(todayX, 0f),
                    end = Offset(todayX, height),
                    strokeWidth = 1.dp.toPx()
                )

                // "Today" Label with smart positioning to avoid overlap
                val labelWidth = textPaint.measureText("Today")
                val labelX =
                    if (todayX < labelWidth + 10f) {
                        todayX + 10f // Draw to right if too close to left edge
                    } else {
                        todayX - labelWidth - 5f // Draw to left normally
                    }
                drawContext.canvas.nativeCanvas.drawText("Today", labelX, 15f, textPaint)
            }

            // --- X-Axis Labels ---
            if (totalDays <= 9) {
                // For weekly/short periods, show every day
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (i in 0 until totalDays) {
                    val date = data.visibleStartDate.plus(kotlinx.datetime.DatePeriod(days = i))
                    val x = xForDate(date)
                    val label = days[date.dayOfWeek.ordinal]

                    // Measure text to center it
                    val labelW = textPaint.measureText(label)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x - (labelW / 2),
                        height + 30f,
                        textPaint
                    )
                }
            } else {
                // For longer periods, show Start and End dates
                drawContext.canvas.nativeCanvas.drawText(
                    "${data.visibleStartDate.dayOfMonth}/${data.visibleStartDate.monthNumber}",
                    0f,
                    height + 30f,
                    textPaint
                )

                val endLabel =
                    "${data.visibleEndDate.dayOfMonth}/${data.visibleEndDate.monthNumber}"
                val endLabelW = textPaint.measureText(endLabel)
                drawContext.canvas.nativeCanvas.drawText(
                    endLabel,
                    width - endLabelW,
                    height + 30f,
                    textPaint
                )
            }
        }
    }
}
