package com.aevrontech.finevo.domain.manager

import kotlinx.datetime.LocalTime

interface NotificationManager {
    suspend fun requestPermission(): Boolean
    suspend fun scheduleDailyReminder(time: LocalTime)
    suspend fun cancelDailyReminder()
    suspend fun showNotification(id: Int, title: String, message: String)
}
