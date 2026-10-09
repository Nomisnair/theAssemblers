package com.zombiethumb.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zombiethumb.data.datastore.SettingsDataStore
import com.zombiethumb.data.db.ZombieDatabase
import com.zombiethumb.data.repository.StatsRepository
import com.zombiethumb.domain.model.TranceResult
import com.zombiethumb.service.MonitoringService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsDataStore(application)
    private val db = ZombieDatabase.getInstance(application)
    private val statsRepo = StatsRepository(
        db.dailyStatsDao(),
        db.scrollSessionDao(),
        db.notificationLogDao(),
    )

    val monitoringEnabled: StateFlow<Boolean> = settings.monitoringEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val tranceResult: StateFlow<TranceResult> = MonitoringService.latestResult

    val todayStats = statsRepo.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val todayStartMs: Long = run {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val notificationCount = statsRepo.observeNotificationCountToday(todayStartMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setMonitoringEnabled(enabled)
            val context: Context = getApplication()
            if (enabled) {
                MonitoringService.start(context)
            } else {
                MonitoringService.stop(context)
            }
        }
    }

    fun simulateTrance() {
        MonitoringService.simulateTrance(getApplication())
    }
}
