package com.zombiethumb.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zombiethumb.ZombieThumbApp
import com.zombiethumb.domain.model.TranceResult
import com.zombiethumb.ui.MainActivity

/**
 * Handles posting break and milestone notifications.
 *
 * Notifications are:
 * - High priority heads-up (IMPORTANCE_HIGH channel)
 * - Empathetic and never punitive
 * - Include real telemetry data (meters, duration, app name)
 * - Two actions: "Take a break" and "5 more minutes"
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    /**
     * Post a break notification with the generated message.
     */
    fun postBreakNotification(message: String, result: TranceResult) {
        // "Take a break" action — opens launcher
        val breakIntent = Intent(context, TakeBreakReceiver::class.java)
        val breakPending = PendingIntent.getBroadcast(
            context, 0, breakIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // "5 more minutes" action — snooze
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java)
        val snoozePending = PendingIntent.getBroadcast(
            context, 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Tap notification to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, 2, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ZombieThumbApp.CHANNEL_BREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🧟 ZombieThumb")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Take a break",
                breakPending,
            )
            .addAction(
                android.R.drawable.ic_media_play,
                "5 more minutes",
                snoozePending,
            )
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_BREAK, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted — silently ignore
        }
    }

    /**
     * Post a mileage milestone notification.
     */
    fun postMilestoneNotification(message: String, meters: Int) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, 3, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ZombieThumbApp.CHANNEL_BREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📏 Feed Mileage Milestone")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_MILESTONE + meters, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    companion object {
        const val NOTIFICATION_ID_BREAK = 100
        const val NOTIFICATION_ID_MILESTONE = 200
    }
}
