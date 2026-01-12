package com.aevrontech.finevo.data.manager

import com.aevrontech.finevo.domain.manager.NotificationManager
import kotlinx.datetime.LocalTime

class IosNotificationManager : NotificationManager {
    override suspend fun requestPermission(): Boolean {
        // TODO: Implement iOS permissions
        return true
    }

    override suspend fun scheduleDailyReminder(time: LocalTime) {
        // TODO: Implement iOS scheduling
    }

    override suspend fun cancelDailyReminder() {
        // TODO: Implement iOS cancellation
    }

    override suspend fun showNotification(id: Int, title: String, message: String) {
        // TODO: Implement iOS notifications
    }
}
