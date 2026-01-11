package com.aevrontech.finevo.presentation.expense

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun TimeFilterSection(
    currentRange: TimeRange,
    onNavigate: (Int) -> Unit,
    onFilterClick: () -> Unit,
    containerBrush: Brush =
        Brush.horizontalGradient(
            colors =
                listOf(
                    DashboardGradientStart,
                    DashboardGradientMid,
                    DashboardGradientEnd
                )
        )
) {
    val isNavigable = currentRange is CalendarTimeRange

    // Pill Container with Gradient Background
    Row(
        modifier =
            Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(brush = containerBrush)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Arrow
        IconButton(
            onClick = { onNavigate(-1) },
            enabled = isNavigable,
            modifier = Modifier.size(32.dp)
        ) {
            if (isNavigable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = Color.White
                )
            }
        }

        // Center Label (Clickable area)
        Row(
            modifier =
                Modifier.clickable(onClick = onFilterClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(targetState = currentRange.displayLabel) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Range",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Next Arrow
        IconButton(
            onClick = { onNavigate(1) },
            enabled = isNavigable,
            modifier = Modifier.size(32.dp)
        ) {
            if (isNavigable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}
