package com.zombiethumb.domain.biomarker

import com.zombiethumb.domain.model.EventType
import com.zombiethumb.domain.model.ScrollEvent

/**
 * Biomarker 2: Consumption-to-Interaction Ratio
 *
 * Healthy usage = taps, likes, typing mixed with scrolling.
 * Doomscrolling = ~98% vertical flings, <2% interactions.
 *
 * Score: Higher when the ratio of scrolls to taps is more extreme.
 */
class ConsumptionRatioBiomarker(
    private val windowSize: Int = 50,
) {
    private val events = mutableListOf<EventType>()
    private var lastScore: Float = 0f

    fun onEvent(event: ScrollEvent): Float {
        if (event.type == EventType.WINDOW_CHANGED) return lastScore

        events.add(event.type)
        if (events.size > windowSize) {
            events.removeAt(0)
        }
        lastScore = calculateScore()
        return lastScore
    }

    internal fun calculateScore(): Float {
        if (events.size < 5) return 0f

        val scrollCount = events.count { it == EventType.SCROLL }
        val clickCount = events.count { it == EventType.CLICK }
        val total = scrollCount + clickCount

        if (total == 0) return 0f

        val scrollRatio = scrollCount.toFloat() / total

        // Score mapping:
        // scrollRatio >= 0.98 → score 1.0 (pure doomscrolling)
        // scrollRatio ~0.80 → score 0.5 (moderate)
        // scrollRatio <= 0.50 → score 0.0 (healthy interaction)
        return when {
            scrollRatio >= 0.98f -> 1.0f
            scrollRatio >= 0.95f -> 0.9f + (scrollRatio - 0.95f) / 0.03f * 0.1f
            scrollRatio >= 0.80f -> 0.5f + (scrollRatio - 0.80f) / 0.15f * 0.4f
            scrollRatio >= 0.50f -> (scrollRatio - 0.50f) / 0.30f * 0.5f
            else -> 0f
        }
    }

    /** Returns the current interaction ratio (clicks / total) */
    fun getInteractionRatio(): Float {
        val scrollCount = events.count { it == EventType.SCROLL }
        val clickCount = events.count { it == EventType.CLICK }
        val total = scrollCount + clickCount
        return if (total > 0) clickCount.toFloat() / total else 0f
    }

    fun reset() {
        events.clear()
        lastScore = 0f
    }
}
