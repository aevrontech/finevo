package com.aevrontech.finevo.presentation.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aevrontech.finevo.ui.theme.ThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelectionSheet(
    currentRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val ranges =
        listOf(
            TimeRange.Today,
            TimeRange.ThisWeek,
            TimeRange.ThisMonth,
            TimeRange.ThisYear,
            LastDaysRange(7, "Last 7 Days"),
            LastDaysRange(30, "Last 30 Days"),
            CalendarTimeRange(
                FilterPeriod.YEAR,
                -1
            ), // Last Year logic handled in getLabel/getTimeRangeDates
            AllTimeRange
        )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ThemeColors.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                text = "Select Time Period",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.onSurface,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn {
                items(ranges) { range ->
                    TimeRangeItem(
                        range = range,
                        isSelected = range == currentRange,
                        onClick = {
                            onRangeSelected(range)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeRangeItem(range: TimeRange, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = range.label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (isSelected) MaterialTheme.colorScheme.primary
                else ThemeColors.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
