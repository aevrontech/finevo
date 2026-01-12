package com.aevrontech.finevo.presentation.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevrontech.finevo.core.util.getCurrentLocalDate
import com.aevrontech.finevo.ui.theme.DashboardGradientEnd
import com.aevrontech.finevo.ui.theme.DashboardGradientStart
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Data class for date item in selector. */
data class DateItem(
    val date: LocalDate,
    val dayOfWeek: String,
    val dayNumber: Int,
    val isToday: Boolean = false
)

/** Scrollable horizontal date selector showing a month of dates. */
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = getCurrentLocalDate()

    // Generate dates for the current month (15 days before and after today)
    val dates =
        remember(today) {
            val startDate = today.minus(15, DateTimeUnit.DAY)
            (0..30).map { offset ->
                val date = startDate.plus(offset, DateTimeUnit.DAY)
                DateItem(
                    date = date,
                    dayOfWeek = date.dayOfWeek.toShortName(),
                    dayNumber = date.dayOfMonth,
                    isToday = date == today
                )
            }
        }

    val listState = rememberLazyListState()

    // Scroll to selected date on first load
    LaunchedEffect(Unit) {
        val selectedIndex = dates.indexOfFirst { it.date == selectedDate }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(maxOf(0, selectedIndex - 2))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month and Year header
        Text(
            text = formatMonthYear(selectedDate),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable date row
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { dateItem ->
                DateItemCard(
                    dateItem = dateItem,
                    isSelected = dateItem.date == selectedDate,
                    onClick = { onDateSelected(dateItem.date) }
                )
            }
        }
    }
}

@Composable
private fun DateItemCard(dateItem: DateItem, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundBrush =
        if (isSelected) {
            Brush.verticalGradient(listOf(DashboardGradientStart, DashboardGradientEnd))
        } else {
            Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
            )
        }

    Box(
        modifier =
            Modifier.width(52.dp)
                .height(72.dp)
                .shadow(
                    elevation = if (isSelected) 4.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(brush = backgroundBrush)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateItem.dayOfWeek,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color =
                    if (isSelected) Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant // Lighter day name
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateItem.dayNumber.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color =
                    if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurface // Darker day number
            )
        }
    }
}

private fun DayOfWeek.toShortName(): String =
    when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> ""
    }

private fun formatMonthYear(date: LocalDate): String {
    val months =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )
    return "${date.dayOfMonth}, ${months[date.monthNumber - 1]} ${date.year}"
}
