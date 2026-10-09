package com.zombiethumb.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun observeLastWeek(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :days")
    suspend fun getLastDays(days: Int): List<DailyStatsEntity>

    @Query("DELETE FROM daily_stats")
    suspend fun deleteAll()
}

@Dao
interface ScrollSessionDao {

    @Insert
    suspend fun insert(session: ScrollSessionEntity)

    @Query("SELECT * FROM scroll_sessions WHERE date = :date ORDER BY startTimeMs DESC")
    fun observeByDate(date: String): Flow<List<ScrollSessionEntity>>

    @Query("SELECT * FROM scroll_sessions ORDER BY startTimeMs DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ScrollSessionEntity>

    @Query("SELECT COUNT(*) FROM scroll_sessions WHERE wasBedtime = 0 AND date >= :sinceDate")
    suspend fun getNonBedtimeDaysSince(sinceDate: String): Int

    @Query("DELETE FROM scroll_sessions")
    suspend fun deleteAll()
}

@Dao
interface NotificationLogDao {

    @Insert
    suspend fun insert(log: NotificationLogEntity)

    @Query("SELECT COUNT(*) FROM notification_log WHERE timestampMs >= :sinceMs")
    fun observeCountSince(sinceMs: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notification_log WHERE timestampMs >= :sinceMs")
    suspend fun getCountSince(sinceMs: Long): Int

    @Query("UPDATE notification_log SET userAction = :action WHERE id = :id")
    suspend fun updateAction(id: Long, action: String)

    @Query("DELETE FROM notification_log")
    suspend fun deleteAll()
}
