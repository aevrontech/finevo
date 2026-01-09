package com.aevrontech.finevo.presentation.components

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis =
                        dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis

                    if (startMillis != null && endMillis != null) {
                        val startDate =
                            Instant.fromEpochMilliseconds(startMillis)
                                .toLocalDateTime(TimeZone.UTC)
                                .date
                        val endDate =
                            Instant.fromEpochMilliseconds(endMillis)
                                .toLocalDateTime(TimeZone.UTC)
                                .date
                        onDateRangeSelected(startDate, endDate)
                    }
                },
                enabled =
                    dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { Text(text = "Select date range") },
            headline = { Text(text = "Start Date - End Date") },
            showModeToggle = true,
        )
    }
}
