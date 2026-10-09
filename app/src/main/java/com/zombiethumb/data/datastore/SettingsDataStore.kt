package com.zombiethumb.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zombie_settings")

/**
 * Settings stored in DataStore Preferences.
 */
class SettingsDataStore(private val context: Context) {

    // Keys
    private object Keys {
        val THRESHOLD = floatPreferencesKey("threshold")
        val COOLDOWN_MINUTES = intPreferencesKey("cooldown_minutes")
        val MILESTONE_METERS = intPreferencesKey("milestone_meters")
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start") // hour 0-23
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        // Monitored apps stored as comma-separated package names
        val MONITORED_APPS = stringPreferencesKey("monitored_apps")
    }

    // Defaults
    companion object {
        const val DEFAULT_THRESHOLD = 0.70f
        const val DEFAULT_COOLDOWN_MINUTES = 10
        const val DEFAULT_MILESTONE_METERS = 50
        const val DEFAULT_QUIET_START = 23  // 11 PM
        const val DEFAULT_QUIET_END = 7     // 7 AM
        val DEFAULT_MONITORED_APPS = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.x.android",
            "com.facebook.katana",
            "com.reddit.frontpage",
        )
    }

    // Flows
    val threshold: Flow<Float> = context.dataStore.data.map {
        it[Keys.THRESHOLD] ?: DEFAULT_THRESHOLD
    }

    val cooldownMinutes: Flow<Int> = context.dataStore.data.map {
        it[Keys.COOLDOWN_MINUTES] ?: DEFAULT_COOLDOWN_MINUTES
    }

    val milestoneMeters: Flow<Int> = context.dataStore.data.map {
        it[Keys.MILESTONE_METERS] ?: DEFAULT_MILESTONE_METERS
    }

    val demoMode: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.DEMO_MODE] ?: false
    }

    val monitoringEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.MONITORING_ENABLED] ?: false
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ONBOARDING_COMPLETE] ?: false
    }

    val quietHoursEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.QUIET_HOURS_ENABLED] ?: false
    }

    val quietHoursStart: Flow<Int> = context.dataStore.data.map {
        it[Keys.QUIET_HOURS_START] ?: DEFAULT_QUIET_START
    }

    val quietHoursEnd: Flow<Int> = context.dataStore.data.map {
        it[Keys.QUIET_HOURS_END] ?: DEFAULT_QUIET_END
    }

    val monitoredApps: Flow<Set<String>> = context.dataStore.data.map {
        val stored = it[Keys.MONITORED_APPS]
        if (stored != null) stored.split(",").toSet()
        else DEFAULT_MONITORED_APPS
    }

    // Setters
    suspend fun setThreshold(value: Float) {
        context.dataStore.edit { it[Keys.THRESHOLD] = value }
    }

    suspend fun setCooldownMinutes(value: Int) {
        context.dataStore.edit { it[Keys.COOLDOWN_MINUTES] = value }
    }

    suspend fun setMilestoneMeters(value: Int) {
        context.dataStore.edit { it[Keys.MILESTONE_METERS] = value }
    }

    suspend fun setDemoMode(value: Boolean) {
        context.dataStore.edit { it[Keys.DEMO_MODE] = value }
    }

    suspend fun setMonitoringEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.MONITORING_ENABLED] = value }
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = value }
    }

    suspend fun setQuietHoursEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.QUIET_HOURS_ENABLED] = value }
    }

    suspend fun setQuietHours(start: Int, end: Int) {
        context.dataStore.edit {
            it[Keys.QUIET_HOURS_START] = start
            it[Keys.QUIET_HOURS_END] = end
        }
    }

    suspend fun setMonitoredApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.MONITORED_APPS] = apps.joinToString(",") }
    }
}
