package com.zombiethumb.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.zombiethumb.R
import com.zombiethumb.ZombieThumbApp
import com.zombiethumb.data.accessibility.ZombieAccessibilityService
import com.zombiethumb.data.datastore.SettingsDataStore
import com.zombiethumb.data.db.NotificationLogEntity
import com.zombiethumb.data.db.ZombieDatabase
import com.zombiethumb.data.repository.StatsRepository
import com.zombiethumb.data.sensor.SensorCollector
import com.zombiethumb.domain.engine.TranceEngine
import com.zombiethumb.domain.model.TranceConfig
import com.zombiethumb.domain.model.TranceResult
import com.zombiethumb.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Foreground service that orchestrates the TranceEngine.
 *
 * - Collects scroll events from AccessibilityService via SharedFlow
 * - Collects sensor readings from SensorCollector
 * - Feeds both into TranceEngine
 * - Triggers notifications via NotificationHelper when thresholds are crossed
 * - Registers/unregisters sensors based on foreground app
 */
class MonitoringService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var tranceEngine: TranceEngine
    private lateinit var sensorCollector: SensorCollector
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var settings: SettingsDataStore
    private lateinit var statsRepository: StatsRepository
    private lateinit var gemmaGenerator: GemmaMessageGenerator



    private var sensorJob: Job? = null
    private var isMonitoringApp = false

    override fun onCreate() {
        super.onCreate()

        val dm = DisplayMetrics()
        val wm = getSystemService<WindowManager>()
        wm?.defaultDisplay?.getMetrics(dm)
        val screenXDpi = dm.xdpi.takeIf { it > 0 } ?: 400f

        tranceEngine = TranceEngine(TranceConfig(screenXDpi = screenXDpi))
        sensorCollector = SensorCollector(this)
        notificationHelper = NotificationHelper(this)
        settings = SettingsDataStore(this)
        gemmaGenerator = GemmaMessageGenerator(this)

        val db = ZombieDatabase.getInstance(this)
        statsRepository = StatsRepository(
            db.dailyStatsDao(),
            db.scrollSessionDao(),
            db.notificationLogDao(),
        )

        // Warm up Gemma model asynchronously
        scope.launch { gemmaGenerator.warmUp() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_SIMULATE_TRANCE -> simulateTrance()
            SnoozeReceiver.ACTION_SNOOZE -> handleSnooze()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        startForeground(NOTIFICATION_ID_MONITORING, buildMonitoringNotification())

        // Observe settings changes
        scope.launch {
            combine(
                settings.threshold,
                settings.cooldownMinutes,
                settings.milestoneMeters,
                settings.demoMode,
            ) { threshold, cooldown, milestone, demo ->
                if (demo) {
                    TranceConfig.demoConfig(tranceEngine.getCurrentResult().totalMeters.let { 400f })
                } else {
                    TranceConfig(
                        threshold = threshold,
                        cooldownMs = cooldown * 60_000L,
                        milestoneMeters = milestone.toFloat(),
                        screenXDpi = 400f,
                    )
                }
            }.collect { config ->
                tranceEngine.updateConfig(config)
            }
        }

        // Collect scroll events from AccessibilityService
        scope.launch {
            ZombieAccessibilityService.scrollEvents.collect { event ->
                val result = tranceEngine.onScrollEvent(event)
                _latestResultStatic.value = result
                handleResult(result)
            }
        }

        // Watch foreground app changes to register/unregister sensors
        scope.launch {
            ZombieAccessibilityService.foregroundApp.collect { pkg ->
                val shouldMonitor = pkg in ZombieAccessibilityService.MONITORED_PACKAGES
                if (shouldMonitor && !isMonitoringApp) {
                    startSensorCollection()
                } else if (!shouldMonitor && isMonitoringApp) {
                    stopSensorCollection()
                }
            }
        }
    }

    private fun startSensorCollection() {
        isMonitoringApp = true
        sensorJob = scope.launch {
            sensorCollector.sensorReadings().collect { reading ->
                val result = tranceEngine.onSensorReading(reading)
                _latestResultStatic.value = result
            }
        }
    }

    private fun stopSensorCollection() {
        isMonitoringApp = false
        sensorJob?.cancel()
        sensorJob = null
    }

    private suspend fun handleResult(result: TranceResult) {
        val now = SystemClock.elapsedRealtime()

        // Check quiet hours
        val quietEnabled = settings.quietHoursEnabled.first()
        if (quietEnabled) {
            val quietStart = settings.quietHoursStart.first()
            val quietEnd = settings.quietHoursEnd.first()
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val inQuiet = if (quietStart < quietEnd) {
                currentHour in quietStart until quietEnd
            } else {
                currentHour >= quietStart || currentHour < quietEnd
            }
            if (inQuiet) return
        }

        // Trance notification
        if (tranceEngine.shouldNotify(result, now)) {
            val message = gemmaGenerator.generateMessage(result)
            notificationHelper.postBreakNotification(message, result)
            tranceEngine.onNotificationSent(now)

            // Log to DB
            scope.launch {
                statsRepository.incrementNotificationCount()
                statsRepository.logNotification(
                    NotificationLogEntity(
                        timestampMs = System.currentTimeMillis(),
                        tranceScore = result.score,
                        metersScrolled = result.totalMeters,
                        appPackageName = result.appName,
                        message = message,
                        type = if (result.currentLux < 5f) "bedtime" else "trance",
                    )
                )
            }
        }

        // Milestone notification
        if (tranceEngine.shouldNotifyMilestone(result)) {
            val meters = result.totalMeters.toInt()
            val message = "📏 You've scrolled $meters meters of feed! That's getting up there."
            notificationHelper.postMilestoneNotification(message, meters)
            tranceEngine.onMilestoneNotified()
        }
    }

    private fun simulateTrance() {
        scope.launch {
            val fakeResult = TranceResult(
                score = 0.92f,
                flickRhythm = 0.95f,
                consumptionRatio = 0.90f,
                bedtimeSlump = 0.8f,
                feedMileage = 0.7f,
                totalMeters = 85f,
                sessionDurationMs = 25 * 60_000L,
                isInTrance = true,
                avgFlickIntervalMs = 1600f,
                interactionRatio = 0.02f,
                currentTiltDeg = 82f,
                currentLux = 0.5f,
                appName = "TikTok",
            )
            _latestResultStatic.value = fakeResult
            val message = gemmaGenerator.generateMessage(fakeResult)
            notificationHelper.postBreakNotification(message, fakeResult)
        }
    }

    private fun handleSnooze() {
        tranceEngine.snooze()
        // Reset the snooze after 5 minutes
        scope.launch {
            delay(5 * 60_000L)
            // Restore the config from settings
            val threshold = settings.threshold.first()
            val cooldown = settings.cooldownMinutes.first()
            val milestone = settings.milestoneMeters.first()
            val demo = settings.demoMode.first()
            val config = if (demo) {
                TranceConfig.demoConfig()
            } else {
                TranceConfig(
                    threshold = threshold,
                    cooldownMs = cooldown * 60_000L,
                    milestoneMeters = milestone.toFloat(),
                )
            }
            tranceEngine.updateConfig(config)
        }
    }

    private fun stopMonitoring() {
        stopSensorCollection()
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildMonitoringNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, ZombieThumbApp.CHANNEL_MONITORING)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(getString(R.string.notification_monitoring_title))
            .setContentText(getString(R.string.notification_monitoring_text))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val ACTION_START = "com.zombiethumb.START_MONITORING"
        const val ACTION_STOP = "com.zombiethumb.STOP_MONITORING"
        const val ACTION_SIMULATE_TRANCE = "com.zombiethumb.SIMULATE_TRANCE"
        const val NOTIFICATION_ID_MONITORING = 1

        private val _latestResultStatic = MutableStateFlow(
            TranceResult(0f, 0f, 0f, 0f, 0f, 0f, 0L, false, 0f, 0f, 0f, -1f, "")
        )
        /** Observable trance result for the UI layer */
        val latestResult: StateFlow<TranceResult> = _latestResultStatic.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun simulateTrance(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                action = ACTION_SIMULATE_TRANCE
            }
            context.startService(intent)
        }
    }
}
