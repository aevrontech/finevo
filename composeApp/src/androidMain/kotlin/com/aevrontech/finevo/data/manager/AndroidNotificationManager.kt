package com.aevrontech.finevo.data.manager

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aevrontech.finevo.data.receiver.NotificationReceiver
import kotlinx.datetime.LocalTime
import java.util.Calendar
import com.aevrontech.finevo.domain.manager.NotificationManager as AppNotificationManager

class AndroidNotificationManager(private val context: Context) : AppNotificationManager {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel =
                NotificationChannel(
                    "reminders",
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                    .apply { description = "Channel for daily budget reminders" }

            val alertChannel =
                NotificationChannel(
                    "alerts",
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                    .apply { description = "Channel for budget overrun alerts" }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    override suspend fun requestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override suspend fun scheduleDailyReminder(time: LocalTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent =
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Daily Reminder")
                putExtra("message", "Time to log your daily expenses!")
                putExtra("id", 1001)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
            }

        // If time is in past, add 1 day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            // Using setInexactRepeating for battery efficiency, or setExactAndAllowWhileIdle if
            // needed
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override suspend fun cancelDailyReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        alarmManager.cancel(pendingIntent)
    }

    override suspend fun showNotification(id: Int, title: String, message: String) {
        if (!requestPermission()) return

        val builder =
            NotificationCompat.Builder(context, "alerts")
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // TODO: Use app icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(id, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
