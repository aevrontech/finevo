package com.aevrontech.finevo.presentation.habit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientMid
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import com.aevrontech.finevo.ui.theme.HabitProgressTrack
import finevo.composeapp.generated.resources.Res
import finevo.composeapp.generated.resources.character_medium
import org.jetbrains.compose.resources.painterResource

/** Gradient progress card showing daily habit completion status. */
@Composable
fun HabitProgressCard(completedCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progress * 100).toInt()

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by
    animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    // Dynamic text based on progress
    val statusText =
        when {
            percentage == 100 -> "Your Daily Goal Complete!"
            percentage >= 80 -> "Your Daily Goal Almost Done"
            percentage >= 50 -> "Great Progress Today!"
            percentage >= 25 -> "Keep Going, You Got This!"
            totalCount == 0 -> "No habits for today"
            else -> "Let's Start Your Day!"
        }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
        // Card Background and Content
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = 28.dp) // Space for character head pop-out
                    .padding(horizontal = 16.dp) // Outer horizontal padding
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        DashboardGradientStart,
                                        DashboardGradientMid,
                                        DashboardGradientEnd
                                    )
                            )
                    )
                    .padding(
                        start = 130.dp, // Space for character on left
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status text
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Completion count
                Text(
                    text =
                        if (totalCount > 0)
                            "$completedCount of $totalCount completed"
                        else "Add your first habit!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar with percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier =
                            Modifier.weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = HabitProgressTrack.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 3D Character Image
        Image(
            painter = painterResource(Res.drawable.character_medium),
            contentDescription = "Habit Character",
            modifier =
                Modifier.height(
                    120.dp
                ) // Slightly taller than card content to pop out
                    .offset(
                        x = 12.dp,
                        y = (-4).dp
                    ) // Positioned at bottom start with slight offset
                    .align(Alignment.BottomStart)
        )
    }
}
