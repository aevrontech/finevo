package com.aevrontech.finevo.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.time.ZoneId

actual fun getCurrentTimeMillis(): Long = java.lang.System.currentTimeMillis()

actual fun getCurrentLocalDate(timeZone: TimeZone): LocalDate {
    val javaZoneId = ZoneId.of(timeZone.id)
    val javaLocalDate = java.time.LocalDate.now(javaZoneId)
    return LocalDate(javaLocalDate.year, javaLocalDate.monthValue, javaLocalDate.dayOfMonth)
}
