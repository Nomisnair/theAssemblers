package com.zombiethumb.domain.biomarker

import com.zombiethumb.domain.model.EventType
import com.zombiethumb.domain.model.ScrollEvent
import kotlin.math.abs

/**
 * Biomarker 4: Feed Mileage
 *
 * Converts cumulative scrolled pixels to meters using screen DPI.
 * Provides a duration-scaled score that increases the longer someone
 * scrolls continuously.
 *
 * Formula: meters = pixels / (dpi * 39.3701)
 * (39.3701 inches per meter, dpi = dots per inch)
 */
class FeedMileageBiomarker(
    private val screenXDpi: Float = 400f,
) {
    private var totalPixels: Long = 0
    private var sessionStartMs: Long = 0
    private var lastEventMs: Long = 0
    private var lastScore: Float = 0f

    /** Gap threshold: if no scroll for this long, the session resets */
    private val sessionGapMs = 5 * 60 * 1000L // 5 minutes

    fun onEvent(event: ScrollEvent): Float {
        if (event.type != EventType.SCROLL) return lastScore

        // Start or continue session
        if (sessionStartMs == 0L) {
            sessionStartMs = event.timestampMs
        } else if (event.timestampMs - lastEventMs > sessionGapMs) {
            // Session gap — reset
            resetSession()
            sessionStartMs = event.timestampMs
        }

        lastEventMs = event.timestampMs
        totalPixels += abs(event.scrollDeltaY)
        lastScore = calculateScore()
        return lastScore
    }

    internal fun calculateScore(): Float {
        val meters = getTotalMeters()
        val durationMinutes = getSessionDurationMs() / 60_000f

        // Score ramps up based on both distance and duration
        // 50m in 10min = moderate doomscroll (score ~0.5)
        // 100m in 20min = heavy doomscroll (score ~0.8)
        // 200m+ in 30min+ = extreme (score ~1.0)
        val distanceScore = when {
            meters < 10f -> meters / 10f * 0.2f
            meters < 50f -> 0.2f + (meters - 10f) / 40f * 0.3f
            meters < 100f -> 0.5f + (meters - 50f) / 50f * 0.3f
            meters < 200f -> 0.8f + (meters - 100f) / 100f * 0.15f
            else -> 0.95f + (meters / 1000f).coerceAtMost(0.05f)
        }

        val durationScore = when {
            durationMinutes < 5f -> durationMinutes / 5f * 0.3f
            durationMinutes < 15f -> 0.3f + (durationMinutes - 5f) / 10f * 0.3f
            durationMinutes < 30f -> 0.6f + (durationMinutes - 15f) / 15f * 0.25f
            else -> 0.85f + (durationMinutes / 120f).coerceAtMost(0.15f)
        }

        return (distanceScore * 0.6f + durationScore * 0.4f).coerceIn(0f, 1f)
    }

    fun getTotalMeters(): Float {
        // pixels / (dpi * 39.3701) = meters
        return totalPixels / (screenXDpi * 39.3701f)
    }

    fun getSessionDurationMs(): Long {
        return if (sessionStartMs > 0 && lastEventMs > sessionStartMs) {
            lastEventMs - sessionStartMs
        } else {
            0L
        }
    }

    private fun resetSession() {
        totalPixels = 0
        sessionStartMs = 0
        lastEventMs = 0
    }

    fun reset() {
        resetSession()
        lastScore = 0f
    }
}
