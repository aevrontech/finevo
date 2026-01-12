package com.aevrontech.finevo.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

expect fun getCurrentTimeMillis(): Long

expect fun getCurrentLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate
