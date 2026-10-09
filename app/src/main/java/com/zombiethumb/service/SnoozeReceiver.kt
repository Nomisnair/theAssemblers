package com.zombiethumb.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Handles "5 more minutes" notification action.
 * Dismisses the notification and suppresses further notifications for 5 minutes
 * by raising the threshold temporarily.
 */
class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Dismiss the break notification
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID_BREAK)

        // The MonitoringService's TranceEngine will handle the snooze timing.
        // For now we send a snooze intent to the service.
        val snoozeIntent = Intent(context, MonitoringService::class.java).apply {
            action = ACTION_SNOOZE
        }
        context.startService(snoozeIntent)
    }

    companion object {
        const val ACTION_SNOOZE = "com.zombiethumb.SNOOZE"
    }
}
