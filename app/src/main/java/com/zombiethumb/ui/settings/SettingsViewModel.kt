package com.zombiethumb.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zombiethumb.data.datastore.SettingsDataStore
import com.zombiethumb.data.db.ZombieDatabase
import com.zombiethumb.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsDataStore(application)
    private val db = ZombieDatabase.getInstance(application)
    private val statsRepo = StatsRepository(
        db.dailyStatsDao(),
        db.scrollSessionDao(),
        db.notificationLogDao(),
    )

    val threshold: StateFlow<Float> = settings.threshold
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_THRESHOLD)

    val cooldownMinutes: StateFlow<Int> = settings.cooldownMinutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_COOLDOWN_MINUTES)

    val milestoneMeters: StateFlow<Int> = settings.milestoneMeters
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_MILESTONE_METERS)

    val demoMode: StateFlow<Boolean> = settings.demoMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val quietHoursEnabled: StateFlow<Boolean> = settings.quietHoursEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val quietHoursStart: StateFlow<Int> = settings.quietHoursStart
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_QUIET_START)

    val quietHoursEnd: StateFlow<Int> = settings.quietHoursEnd
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_QUIET_END)

    val monitoredApps: StateFlow<Set<String>> = settings.monitoredApps
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsDataStore.DEFAULT_MONITORED_APPS)

    fun setThreshold(value: Float) = viewModelScope.launch { settings.setThreshold(value) }
    fun setCooldownMinutes(value: Int) = viewModelScope.launch { settings.setCooldownMinutes(value) }
    fun setMilestoneMeters(value: Int) = viewModelScope.launch { settings.setMilestoneMeters(value) }
    fun setDemoMode(value: Boolean) = viewModelScope.launch { settings.setDemoMode(value) }
    fun setQuietHoursEnabled(value: Boolean) = viewModelScope.launch { settings.setQuietHoursEnabled(value) }
    fun setQuietHours(start: Int, end: Int) = viewModelScope.launch { settings.setQuietHours(start, end) }

    fun toggleApp(pkg: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = monitoredApps.value.toMutableSet()
            if (enabled) current.add(pkg) else current.remove(pkg)
            settings.setMonitoredApps(current)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            statsRepo.deleteAllData()
        }
    }
}
