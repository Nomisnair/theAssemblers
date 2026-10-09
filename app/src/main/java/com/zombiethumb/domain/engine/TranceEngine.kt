package com.zombiethumb.domain.engine

import com.zombiethumb.domain.biomarker.BedtimeSlumpBiomarker
import com.zombiethumb.domain.biomarker.ConsumptionRatioBiomarker
import com.zombiethumb.domain.biomarker.FeedMileageBiomarker
import com.zombiethumb.domain.biomarker.FlickRhythmBiomarker
import com.zombiethumb.domain.model.*

/**
 * TranceEngine — the core doomscrolling detection engine.
 *
 * Combines 4 biomarkers into a single Trance Score (0-100%) using a
 * transparent weighted formula, smoothed with an exponential moving average.
 *
 * ## Biomarker Weights (documented here and configurable)
 *
 * | Biomarker          | Weight | Rationale                                        |
 * |--------------------|--------|--------------------------------------------------|
 * | Flick Rhythm       | 0.35   | Primary signal — rhythmic swiping is strongest   |
 * | Consumption Ratio  | 0.30   | High scroll:tap = passive consumption            |
 * | Bedtime Slump      | 0.20   | Contextual amplifier — late-night in bed          |
 * | Feed Mileage       | 0.15   | Cumulative distance/duration adds context         |
 *
 * Score = Σ(biomarker_i × weight_i), smoothed with EMA(α=0.3)
 *
 * This class is pure Kotlin with no Android dependencies, enabling
 * full unit testing with synthetic event streams.
 */
class TranceEngine(
    config: TranceConfig = TranceConfig(),
) {
    // Biomarker instances
    private val flickRhythm = FlickRhythmBiomarker(windowSize = config.flickWindowSize)
    private val consumptionRatio = ConsumptionRatioBiomarker()
    private val bedtimeSlump = BedtimeSlumpBiomarker()
    private val feedMileage = FeedMileageBiomarker(screenXDpi = config.screenXDpi)

    // Weights — documented and transparent
    // These are in the companion object at the bottom of the class

    // Configuration
    private var config: TranceConfig = config

    // EMA state
    private var emaScore: Float = 0f
    private var emaInitialized: Boolean = false

    // Individual biomarker scores
    private var flickScore: Float = 0f
    private var consumptionScore: Float = 0f
    private var bedtimeScore: Float = 0f
    private var mileageScore: Float = 0f

    // Cooldown state
    private var lastNotificationMs: Long = 0
    private var lastMilestoneMeters: Float = 0f

    // Current sensor reading
    private var currentSensorReading: SensorReading? = null
    private var currentPackageName: String = ""

    /**
     * Process a scroll/click event. Call this for every AccessibilityEvent.
     */
    fun onScrollEvent(event: ScrollEvent): TranceResult {
        currentPackageName = event.packageName

        flickScore = flickRhythm.onEvent(event)
        consumptionScore = consumptionRatio.onEvent(event)
        mileageScore = feedMileage.onEvent(event)

        return computeResult(event.timestampMs)
    }

    /**
     * Process a sensor reading. Call this from SensorCollector.
     */
    fun onSensorReading(reading: SensorReading): TranceResult {
        currentSensorReading = reading
        bedtimeScore = bedtimeSlump.onSensorReading(reading)
        return computeResult(reading.timestampMs)
    }

    /**
     * Compute the combined trance result.
     */
    private fun computeResult(timestampMs: Long): TranceResult {
        // Weighted combination
        val rawScore = flickScore * WEIGHT_FLICK_RHYTHM +
                consumptionScore * WEIGHT_CONSUMPTION_RATIO +
                bedtimeScore * WEIGHT_BEDTIME_SLUMP +
                mileageScore * WEIGHT_FEED_MILEAGE

        // EMA smoothing
        emaScore = if (!emaInitialized) {
            emaInitialized = true
            rawScore
        } else {
            config.emaAlpha * rawScore + (1 - config.emaAlpha) * emaScore
        }

        val finalScore = emaScore.coerceIn(0f, 1f)

        return TranceResult(
            score = finalScore,
            flickRhythm = flickScore,
            consumptionRatio = consumptionScore,
            bedtimeSlump = bedtimeScore,
            feedMileage = mileageScore,
            totalMeters = feedMileage.getTotalMeters(),
            sessionDurationMs = feedMileage.getSessionDurationMs(),
            isInTrance = finalScore >= config.threshold,
            avgFlickIntervalMs = flickRhythm.getAverageIntervalMs(),
            interactionRatio = consumptionRatio.getInteractionRatio(),
            currentTiltDeg = currentSensorReading?.tiltAngleDeg ?: 0f,
            currentLux = currentSensorReading?.ambientLux ?: -1f,
            appName = getAppDisplayName(currentPackageName),
        )
    }

    /**
     * Check whether a notification should be sent (respecting cooldown).
     */
    fun shouldNotify(result: TranceResult, currentTimeMs: Long): Boolean {
        if (!result.isInTrance) return false
        if (currentTimeMs - lastNotificationMs < config.cooldownMs) return false
        return true
    }

    /**
     * Check whether a mileage milestone has been reached.
     */
    fun shouldNotifyMilestone(result: TranceResult): Boolean {
        val currentMeters = result.totalMeters
        val nextMilestone = lastMilestoneMeters + config.milestoneMeters
        return currentMeters >= nextMilestone
    }

    /**
     * Mark that a notification was sent.
     */
    fun onNotificationSent(timestampMs: Long) {
        lastNotificationMs = timestampMs
    }

    /**
     * Mark that a milestone notification was sent.
     */
    fun onMilestoneNotified() {
        lastMilestoneMeters += config.milestoneMeters
    }

    /**
     * Update configuration (e.g. when settings change or demo mode toggled).
     */
    fun updateConfig(newConfig: TranceConfig) {
        config = newConfig
    }

    /**
     * Temporarily raise the threshold (for snooze functionality).
     */
    fun snooze(extraThreshold: Float = 0.10f, snoozeDurationMs: Long = 5 * 60 * 1000L) {
        config = config.copy(threshold = (config.threshold + extraThreshold).coerceAtMost(1f))
        // Note: actual timer to reset is handled by the service layer
    }

    /**
     * Reset all state.
     */
    fun reset() {
        flickRhythm.reset()
        consumptionRatio.reset()
        bedtimeSlump.reset()
        feedMileage.reset()
        emaScore = 0f
        emaInitialized = false
        flickScore = 0f
        consumptionScore = 0f
        bedtimeScore = 0f
        mileageScore = 0f
        lastNotificationMs = 0
        lastMilestoneMeters = 0f
        currentSensorReading = null
        currentPackageName = ""
    }

    /** Get a snapshot of current state for the debug panel */
    fun getCurrentResult(): TranceResult {
        return computeResult(System.currentTimeMillis())
    }

    companion object {
        const val WEIGHT_FLICK_RHYTHM = 0.35f
        const val WEIGHT_CONSUMPTION_RATIO = 0.30f
        const val WEIGHT_BEDTIME_SLUMP = 0.20f
        const val WEIGHT_FEED_MILEAGE = 0.15f

        /** Map package names to friendly display names */
        fun getAppDisplayName(packageName: String): String = when (packageName) {
            "com.instagram.android" -> "Instagram"
            "com.zhiliaoapp.musically" -> "TikTok"
            "com.google.android.youtube" -> "YouTube"
            "com.twitter.android", "com.x.android" -> "X (Twitter)"
            "com.facebook.katana" -> "Facebook"
            "com.reddit.frontpage" -> "Reddit"
            else -> packageName.substringAfterLast('.')
                .replaceFirstChar { it.uppercase() }
        }
    }
}
