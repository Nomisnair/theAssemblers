package com.zombiethumb.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zombiethumb.data.db.DailyStatsEntity
import com.zombiethumb.data.db.ZombieDatabase
import com.zombiethumb.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZombieDatabase.getInstance(application)
    private val statsRepo = StatsRepository(
        db.dailyStatsDao(),
        db.scrollSessionDao(),
        db.notificationLogDao(),
    )

    val weeklyStats: StateFlow<List<DailyStatsEntity>> = statsRepo.observeLastWeek()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bedtimeStreak = MutableStateFlow(0)
    val bedtimeStreak: StateFlow<Int> = _bedtimeStreak

    init {
        viewModelScope.launch {
            _bedtimeStreak.value = statsRepo.getBedtimeStreak()
        }
    }
}
