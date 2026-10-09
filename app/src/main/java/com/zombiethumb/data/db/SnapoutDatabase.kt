package com.zombiethumb.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyStatsEntity::class,
        ScrollSessionEntity::class,
        NotificationLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ZombieDatabase : RoomDatabase() {

    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun scrollSessionDao(): ScrollSessionDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: ZombieDatabase? = null

        fun getInstance(context: Context): ZombieDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZombieDatabase::class.java,
                    "zombie_thumb.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
