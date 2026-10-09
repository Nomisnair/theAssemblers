package com.zombiethumb

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService

class ZombieThumbApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService<NotificationManager>() ?: return

        // Monitoring channel (low priority, persistent)
        val monitoringChannel = NotificationChannel(
            CHANNEL_MONITORING,
            getString(R.string.channel_monitoring),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification while Snapout is monitoring"
            setShowBadge(false)
        }

        // Break notification channel (high priority, heads-up)
        val breakChannel = NotificationChannel(
            CHANNEL_BREAK,
            getString(R.string.channel_break),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when doomscrolling is detected"
            enableVibration(true)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannels(listOf(monitoringChannel, breakChannel))
    }

    companion object {
        const val CHANNEL_MONITORING = "monitoring"
        const val CHANNEL_BREAK = "take_a_break"

        lateinit var instance: ZombieThumbApp
            private set
    }
}
