package com.zombiethumb.data.repository

import com.zombiethumb.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for aggregated stats operations.
 * Handles daily stat updates and insights queries.
 */
class StatsRepository(
    private val dailyStatsDao: DailyStatsDao,
    private val scrollSessionDao: ScrollSessionDao,
    private val notificationLogDao: NotificationLogDao,
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(dateFormatter)

    // ---- Daily Stats ----

    fun observeToday(): Flow<DailyStatsEntity?> = dailyStatsDao.observeByDate(today())

    fun observeLastWeek(): Flow<List<DailyStatsEntity>> = dailyStatsDao.observeLastWeek()

    suspend fun updateTodayStats(
        metersAdded: Float = 0f,
        scrollEvents: Int = 0,
        clickEvents: Int = 0,
        doomscrollMinutes: Int = 0,
        tranceScore: Float = 0f,
        bedtimeMinutes: Int = 0,
    ) {
        val date = today()
        val existing = dailyStatsDao.getByDate(date) ?: DailyStatsEntity(date = date)
        val updated = existing.copy(
            totalMeters = existing.totalMeters + metersAdded,
            totalScrollEvents = existing.totalScrollEvents + scrollEvents,
            totalClickEvents = existing.totalClickEvents + clickEvents,
            totalDoomscrollMinutes = existing.totalDoomscrollMinutes + doomscrollMinutes,
            peakTranceScore = maxOf(existing.peakTranceScore, tranceScore),
            bedtimeScrollMinutes = existing.bedtimeScrollMinutes + bedtimeMinutes,
        )
        dailyStatsDao.upsert(updated)
    }

    suspend fun incrementNotificationCount() {
        val date = today()
        val existing = dailyStatsDao.getByDate(date) ?: DailyStatsEntity(date = date)
        dailyStatsDao.upsert(existing.copy(notificationsSent = existing.notificationsSent + 1))
    }

    // ---- Scroll Sessions ----

    suspend fun saveSession(session: ScrollSessionEntity) {
        scrollSessionDao.insert(session)
    }

    // ---- Notification Log ----

    suspend fun logNotification(log: NotificationLogEntity) {
        notificationLogDao.insert(log)
    }

    fun observeNotificationCountToday(todayStartMs: Long): Flow<Int> {
        return notificationLogDao.observeCountSince(todayStartMs)
    }

    // ---- Bedtime Streak ----

    suspend fun getBedtimeStreak(): Int {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(dateFormatter)
        val stats = dailyStatsDao.getLastDays(30)
        var streak = 0
        for (day in stats) {
            if (day.bedtimeScrollMinutes == 0) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    // ---- Delete All ----

    suspend fun deleteAllData() {
        dailyStatsDao.deleteAll()
        scrollSessionDao.deleteAll()
        notificationLogDao.deleteAll()
    }
}
