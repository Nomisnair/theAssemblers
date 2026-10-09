package com.zombiethumb.domain.biomarker

import com.zombiethumb.domain.model.ScrollEvent
import com.zombiethumb.domain.model.EventType
import kotlin.math.abs

/**
 * Biomarker 1: Repetitive Flick Rhythm
 *
 * Reading behavior = irregular pauses of 4-6s between scrolls.
 * Doomscrolling = steady swipe every 1.2-2.0s with low variance.
 *
 * Score: Higher when flick intervals are consistent and within the
 * doomscroll range (1200-2000ms). Uses coefficient of variation (CV)
 * of intervals within a sliding window.
 */
class FlickRhythmBiomarker(
    private val windowSize: Int = 20,
) {
    private val intervals = mutableListOf<Long>()

    /**
     * Processes a new scroll event and returns the current biomarker score [0,1].
     */
    fun onEvent(event: ScrollEvent): Float {
        if (event.type != EventType.SCROLL) return lastScore
        if (lastScrollTimestamp > 0) {
            val interval = event.timestampMs - lastScrollTimestamp
            // Only consider reasonable intervals (100ms - 15s)
            if (interval in 100..15_000) {
                intervals.add(interval)
                if (intervals.size > windowSize) {
                    intervals.removeAt(0)
                }
            }
        }
        lastScrollTimestamp = event.timestampMs
        lastScore = calculateScore()
        return lastScore
    }

    private var lastScrollTimestamp: Long = 0
    private var lastScore: Float = 0f

    /**
     * Calculate the flick rhythm score.
     * Low variance + interval in doomscroll range (1.2-2.0s) = high score.
     */
    internal fun calculateScore(): Float {
        if (intervals.size < 3) return 0f

        val mean = intervals.average()
        val variance = intervals.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        val cv = if (mean > 0) stdDev / mean else 1.0 // coefficient of variation

        // Regularity score: low CV = high regularity
        // CV < 0.2 = very regular, CV > 0.8 = very irregular
        val regularityScore = (1.0 - (cv / 0.8).coerceIn(0.0, 1.0)).toFloat()

        // Interval score: peaks in doomscroll range (1200-2000ms)
        val intervalScore = when {
            mean < 800 -> 0.5f  // Very fast — could be doomscrolling but less certain
            mean < 1200 -> 0.7f + 0.3f * ((mean - 800) / 400).toFloat()
            mean in 1200.0..2000.0 -> 1.0f  // Sweet spot for doomscrolling
            mean < 3000 -> (1.0 - (mean - 2000) / 1000).toFloat().coerceAtLeast(0.3f)
            mean < 4000 -> 0.2f  // Probably reading
            else -> 0.1f  // Long pauses — definitely reading/engaged
        }

        return (regularityScore * 0.6f + intervalScore * 0.4f).coerceIn(0f, 1f)
    }

    /** Get the average flick interval in ms. */
    fun getAverageIntervalMs(): Float {
        return if (intervals.isEmpty()) 0f else intervals.average().toFloat()
    }

    fun reset() {
        intervals.clear()
        lastScrollTimestamp = 0
        lastScore = 0f
    }
}
