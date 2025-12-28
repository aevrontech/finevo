package com.aevrontech.finevo.presentation.habit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.ui.theme.Primary

/** Custom circular progress indicator with percentage text in center. */
@Composable
fun CircularProgressIndicatorWithText(
        progress: Float,
        modifier: Modifier = Modifier,
        size: Dp = 48.dp,
        strokeWidth: Dp = 4.dp,
        progressColor: Color = Primary,
        trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        showPercentage: Boolean = true,
        animationDuration: Int = 800
) {
        var animationPlayed by remember { mutableStateOf(false) }
        val animatedProgress by
                animateFloatAsState(
                        targetValue = if (animationPlayed) progress.coerceIn(0f, 1f) else 0f,
                        animationSpec = tween(durationMillis = animationDuration),
                        label = "progress"
                )

        LaunchedEffect(Unit) { animationPlayed = true }

        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(size)) {
                        val strokeWidthPx = strokeWidth.toPx()
                        val arcSize = this.size.minDimension - strokeWidthPx

                        // Track (background circle)
                        drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                                topLeft =
                                        androidx.compose.ui.geometry.Offset(
                                                strokeWidthPx / 2,
                                                strokeWidthPx / 2
                                        )
                        )

                        // Progress arc
                        drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                                topLeft =
                                        androidx.compose.ui.geometry.Offset(
                                                strokeWidthPx / 2,
                                                strokeWidthPx / 2
                                        )
                        )
                }

                if (showPercentage) {
                        val textStyle =
                                when {
                                        size < 40.dp -> MaterialTheme.typography.labelSmall
                                        size < 60.dp -> MaterialTheme.typography.labelMedium
                                        else -> MaterialTheme.typography.labelLarge
                                }
                        Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = textStyle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                }
        }
}

/** Compact circular progress for habit cards. */
@Composable
fun HabitCircularProgress(progress: Float, modifier: Modifier = Modifier, color: Color = Primary) {
        CircularProgressIndicatorWithText(
                progress = progress,
                modifier = modifier,
                size = 52.dp,
                strokeWidth = 5.dp,
                progressColor = color,
                trackColor = color.copy(alpha = 0.2f),
                showPercentage = true
        )
}
