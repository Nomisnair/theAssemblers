package com.zombiethumb.domain.model

/**
 * Represents a single scroll event captured from the AccessibilityService.
 * Contains ONLY physics data — never any text or content.
 */
data class ScrollEvent(
    /** Timestamp in milliseconds (SystemClock.elapsedRealtime) */
    val timestampMs: Long,
    /** Vertical scroll delta in pixels (positive = scroll down) */
    val scrollDeltaY: Int,
    /** Package name of the foreground app */
    val packageName: String,
    /** Type of event: SCROLL, CLICK, or WINDOW_CHANGED */
    val type: EventType,
)

enum class EventType {
    SCROLL,
    CLICK,
    WINDOW_CHANGED,
}

/**
 * Sensor reading from accelerometer/gyroscope/light sensor.
 */
data class SensorReading(
    val timestampMs: Long,
    /** Device tilt angle in degrees (0 = flat, 90 = vertical) */
    val tiltAngleDeg: Float,
    /** Micro-shake magnitude (m/s²) */
    val shakeMagnitude: Float,
    /** Ambient light in lux */
    val ambientLux: Float,
)

/**
 * Combined trance assessment output.
 */
data class TranceResult(
    /** Overall trance score 0.0 to 1.0 */
    val score: Float,
    /** Individual biomarker scores */
    val flickRhythm: Float,
    val consumptionRatio: Float,
    val bedtimeSlump: Float,
    val feedMileage: Float,
    /** Accumulated metrics */
    val totalMeters: Float,
    val sessionDurationMs: Long,
    /** Whether the threshold has been crossed */
    val isInTrance: Boolean,
    /** Average flick interval in ms */
    val avgFlickIntervalMs: Float,
    /** Current interaction ratio (taps / total events) */
    val interactionRatio: Float,
    /** Current tilt angle */
    val currentTiltDeg: Float,
    /** Current ambient light */
    val currentLux: Float,
    /** Foreground app display name */
    val appName: String,
)

/**
 * Configuration for the TranceEngine.
 */
data class TranceConfig(
    /** Threshold 0.0-1.0 to trigger intervention */
    val threshold: Float = 0.85f,
    /** Cooldown between notifications in ms */
    val cooldownMs: Long = 10 * 60 * 1000L, // 10 minutes
    /** Milestone distance in meters */
    val milestoneMeters: Float = 50f,
    /** Screen DPI for pixel-to-meter conversion */
    val screenXDpi: Float = 400f,
    /** EMA smoothing alpha (higher = more responsive/sensitive) */
    val emaAlpha: Float = 0.45f,
    /** Sliding window size for flick rhythm analysis */
    val flickWindowSize: Int = 15,
    /** Demo mode flag */
    val demoMode: Boolean = false,
) {
    companion object {
        fun demoConfig(screenXDpi: Float = 400f) = TranceConfig(
            threshold = 0.50f,
            cooldownMs = 15_000L, // 15 seconds
            milestoneMeters = 10f,
            screenXDpi = screenXDpi,
            emaAlpha = 0.5f,
            flickWindowSize = 10,
            demoMode = true,
        )
    }
}
