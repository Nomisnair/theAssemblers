package com.zombiethumb.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily aggregated scrolling statistics.
 * PRIVACY: Only numeric data — no text, no content, no personal info.
 */
@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String, // ISO date format: "2024-01-15"
    val totalMeters: Float = 0f,
    val totalScrollEvents: Int = 0,
    val totalClickEvents: Int = 0,
    val totalDoomscrollMinutes: Int = 0,
    val notificationsSent: Int = 0,
    val peakTranceScore: Float = 0f,
    val avgTranceScore: Float = 0f,
    val bedtimeScrollMinutes: Int = 0, // Minutes scrolled after 22:00 with low lux
    val peakHour: Int = -1, // Hour of day with most scrolling (0-23)
    val topApp: String = "", // Package name of most scrolled app
)

/**
 * Scroll session record for insights.
 * A session starts when a monitored app comes to foreground and
 * ends on app switch or inactivity.
 */
@Entity(tableName = "scroll_sessions")
data class ScrollSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val appPackageName: String,
    val metersScrolled: Float,
    val peakTranceScore: Float,
    val avgFlickIntervalMs: Float,
    val interactionRatio: Float,
    val wasBedtime: Boolean,
    val date: String, // ISO date
)

/**
 * Log of sent notifications for analytics.
 */
@Entity(tableName = "notification_log")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMs: Long,
    val tranceScore: Float,
    val metersScrolled: Float,
    val appPackageName: String,
    val message: String, // The notification text sent
    val type: String, // "trance", "milestone", "bedtime"
    val userAction: String = "", // "break", "snooze", "dismissed"
)
