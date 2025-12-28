package com.aevrontech.finevo.presentation.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.domain.model.Habit
import com.aevrontech.finevo.domain.model.HabitFrequency
import com.aevrontech.finevo.ui.theme.HabitComplete
import com.aevrontech.finevo.ui.theme.Primary

/** Individual habit item card with circular progress indicator. */
@Composable
fun HabitItemCard(
        habit: Habit,
        isCompleted: Boolean,
        progress: Float = if (isCompleted) 1f else 0f,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val habitColor = parseHabitColor(habit.color)

    Card(
            modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
            shape = RoundedCornerShape(16.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Colored icon container
                Box(
                        modifier =
                                Modifier.size(48.dp)
                                        .clip(CircleShape)
                                        .background(habitColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                ) { Text(text = habit.icon, style = MaterialTheme.typography.headlineSmall) }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Habit name
                    Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )

                    // Status text with checkmark if completed
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                                text =
                                        when {
                                            isCompleted -> "Completed"
                                            else -> habit.frequency.toDisplayText()
                                        },
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                        if (isCompleted) HabitComplete
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isCompleted) {
                            Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HabitComplete
                            )
                        }
                    }

                    // Time display (if reminder is set)
                    habit.reminderTime?.let { time ->
                        Text(
                                text = formatTime(time),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f
                                        )
                        )
                    }
                }
            }

            // Circular progress indicator
            HabitCircularProgress(
                    progress = progress,
                    color = if (isCompleted) HabitComplete else habitColor
            )
        }
    }
}

private fun HabitFrequency.toDisplayText(): String =
        when (this) {
            HabitFrequency.DAILY -> "Repeat every day"
            HabitFrequency.WEEKLY -> "Weekly"
            HabitFrequency.MONTHLY -> "Monthly"
            HabitFrequency.SPECIFIC_DAYS -> "Specific days"
        }

private fun formatTime(time: kotlinx.datetime.LocalTime): String {
    val hour = if (time.hour > 12) time.hour - 12 else if (time.hour == 0) 12 else time.hour
    val amPm = if (time.hour >= 12) "pm" else "am"
    return "$hour:${time.minute.toString().padStart(2, '0')} $amPm"
}

private fun parseHabitColor(colorString: String): Color {
    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            6 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(red = r, green = g, blue = b, alpha = 255)
            }
            else -> Primary
        }
    } catch (e: Exception) {
        Primary
    }
}
