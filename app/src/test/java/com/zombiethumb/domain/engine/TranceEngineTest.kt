package com.zombiethumb.domain.engine

import com.google.common.truth.Truth.assertThat
import com.zombiethumb.domain.model.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TranceEngine with synthetic event streams.
 *
 * Tests three scenarios:
 * 1. Reading behavior — irregular scrolling with frequent taps → low score
 * 2. Doomscrolling behavior — steady rhythmic swiping → high score
 * 3. Bedtime scrolling — dark room, phone overhead, rhythmic swiping → highest score
 */
class TranceEngineTest {

    private lateinit var engine: TranceEngine

    @Before
    fun setUp() {
        engine = TranceEngine(
            TranceConfig(
                threshold = 0.85f,
                screenXDpi = 400f,
                emaAlpha = 0.5f, // Higher alpha for faster response in tests
                flickWindowSize = 10,
            )
        )
    }

    // ================================================================
    // Scenario 1: Normal reading behavior
    // Irregular scrolling with frequent taps and long pauses
    // Expected: Low trance score
    // ================================================================

    @Test
    fun `reading behavior produces low trance score`() {
        var time = 0L
        val pkg = "com.instagram.android"

        // Simulate reading: scroll, pause 4-6s, tap, scroll, long pause, tap...
        val readingPattern = listOf(
            Pair(EventType.SCROLL, 200),   // small scroll
            Pair(EventType.CLICK, 0),      // tap/like
            Pair(EventType.SCROLL, 150),   // read more
            Pair(EventType.SCROLL, 100),   // small scroll
            Pair(EventType.CLICK, 0),      // comment
            Pair(EventType.CLICK, 0),      // like
            Pair(EventType.SCROLL, 180),   // next post
            Pair(EventType.CLICK, 0),      // tap
            Pair(EventType.SCROLL, 120),
            Pair(EventType.CLICK, 0),      // interact
        )

        var result: TranceResult? = null

        // Run the pattern 3 times with irregular intervals (4-6 seconds)
        repeat(3) {
            readingPattern.forEach { (type, scrollDelta) ->
                val interval = when (type) {
                    EventType.SCROLL -> (4000..6000).random().toLong()
                    EventType.CLICK -> (500..1500).random().toLong()
                    else -> 1000L
                }
                time += interval

                result = engine.onScrollEvent(
                    ScrollEvent(
                        timestampMs = time,
                        scrollDeltaY = scrollDelta,
                        packageName = pkg,
                        type = type,
                    )
                )
            }
        }

        assertThat(result).isNotNull()
        assertThat(result!!.score).isLessThan(0.5f)
        assertThat(result!!.isInTrance).isFalse()
        assertThat(result!!.interactionRatio).isGreaterThan(0.2f) // ~50% clicks
    }

    // ================================================================
    // Scenario 2: Doomscrolling behavior
    // Steady rhythmic swiping every 1.5s, almost no taps
    // Expected: High trance score
    // ================================================================

    @Test
    fun `doomscrolling behavior produces high trance score`() {
        var time = 0L
        val pkg = "com.zhiliaoapp.musically" // TikTok

        var result: TranceResult? = null

        // 50 rapid scrolls at 1.5s intervals — classic doomscrolling
        repeat(50) { i ->
            time += 1500L // steady 1.5s interval

            result = engine.onScrollEvent(
                ScrollEvent(
                    timestampMs = time,
                    scrollDeltaY = 1800, // Full-screen swipe
                    packageName = pkg,
                    type = EventType.SCROLL,
                )
            )

            // One click every 30 scrolls (very low interaction)
            if (i == 25) {
                time += 200
                result = engine.onScrollEvent(
                    ScrollEvent(
                        timestampMs = time,
                        scrollDeltaY = 0,
                        packageName = pkg,
                        type = EventType.CLICK,
                    )
                )
            }
        }

        assertThat(result).isNotNull()
        assertThat(result!!.score).isGreaterThan(0.5f)
        assertThat(result!!.flickRhythm).isGreaterThan(0.5f)
        assertThat(result!!.consumptionRatio).isGreaterThan(0.5f)
        assertThat(result!!.interactionRatio).isLessThan(0.1f)
        assertThat(result!!.appName).isEqualTo("TikTok")
    }

    // ================================================================
    // Scenario 3: Bedtime doomscrolling
    // Dark room, phone overhead, rhythmic swiping
    // Expected: Highest trance score (bedtime amplifier)
    // ================================================================

    @Test
    fun `bedtime scrolling produces highest trance score`() {
        var time = 0L
        val pkg = "com.instagram.android"

        // Feed sensor readings: dark room, phone overhead, still
        val bedtimeSensor = SensorReading(
            timestampMs = time,
            tiltAngleDeg = 85f,   // Phone overhead
            shakeMagnitude = 0.05f, // Nearly still
            ambientLux = 0.5f,     // Dark room
        )

        var result: TranceResult? = null

        // Simulate: sensor readings + rhythmic scrolling
        repeat(40) {
            time += 1600L // steady 1.6s interval

            // Update sensor every few scrolls
            engine.onSensorReading(bedtimeSensor.copy(timestampMs = time))

            result = engine.onScrollEvent(
                ScrollEvent(
                    timestampMs = time,
                    scrollDeltaY = 1500,
                    packageName = pkg,
                    type = EventType.SCROLL,
                )
            )
        }

        assertThat(result).isNotNull()
        assertThat(result!!.score).isGreaterThan(0.5f)
        assertThat(result!!.bedtimeSlump).isGreaterThan(0.7f)
        assertThat(result!!.flickRhythm).isGreaterThan(0.5f)
    }

    // ================================================================
    // TranceEngine: Cooldown and notification logic
    // ================================================================

    @Test
    fun `notification respects cooldown`() {
        val config = TranceConfig(
            threshold = 0.1f, // Very low threshold to trigger
            cooldownMs = 10_000L,
        )
        val testEngine = TranceEngine(config)

        // Generate a high score
        var time = 0L
        repeat(30) {
            time += 1500
            testEngine.onScrollEvent(
                ScrollEvent(time, 1800, "com.instagram.android", EventType.SCROLL)
            )
        }

        val result = testEngine.getCurrentResult()
        assertThat(result.isInTrance).isTrue()

        // First notification should be allowed
        assertThat(testEngine.shouldNotify(result, time)).isTrue()
        testEngine.onNotificationSent(time)

        // Second notification within cooldown should NOT be allowed
        assertThat(testEngine.shouldNotify(result, time + 5000)).isFalse()

        // After cooldown, should be allowed again
        assertThat(testEngine.shouldNotify(result, time + 15_000)).isTrue()
    }

    // ================================================================
    // Feed Mileage: pixel-to-meter conversion
    // ================================================================

    @Test
    fun `feed mileage converts pixels to meters correctly`() {
        val testEngine = TranceEngine(TranceConfig(screenXDpi = 440f))
        var time = 0L

        // Scroll 440 * 39.3701 ≈ 17,323 pixels = 1 meter
        repeat(100) {
            time += 1500
            testEngine.onScrollEvent(
                ScrollEvent(time, 174, "com.instagram.android", EventType.SCROLL)
            )
        }

        val result = testEngine.getCurrentResult()
        // 100 * 174 = 17,400 pixels at 440 dpi = ~1.006 meters
        assertThat(result.totalMeters).isWithin(0.1f).of(1.0f)
    }

    // ================================================================
    // App name mapping
    // ================================================================

    @Test
    fun `app display names are correct`() {
        assertThat(TranceEngine.getAppDisplayName("com.instagram.android")).isEqualTo("Instagram")
        assertThat(TranceEngine.getAppDisplayName("com.zhiliaoapp.musically")).isEqualTo("TikTok")
        assertThat(TranceEngine.getAppDisplayName("com.google.android.youtube")).isEqualTo("YouTube")
        assertThat(TranceEngine.getAppDisplayName("com.twitter.android")).isEqualTo("X (Twitter)")
        assertThat(TranceEngine.getAppDisplayName("com.x.android")).isEqualTo("X (Twitter)")
        assertThat(TranceEngine.getAppDisplayName("com.facebook.katana")).isEqualTo("Facebook")
        assertThat(TranceEngine.getAppDisplayName("com.reddit.frontpage")).isEqualTo("Reddit")
    }

    // ================================================================
    // Reset
    // ================================================================

    @Test
    fun `reset clears all state`() {
        var time = 0L
        repeat(20) {
            time += 1500
            engine.onScrollEvent(
                ScrollEvent(time, 1800, "com.instagram.android", EventType.SCROLL)
            )
        }

        val before = engine.getCurrentResult()
        assertThat(before.score).isGreaterThan(0f)

        engine.reset()

        val after = engine.getCurrentResult()
        assertThat(after.score).isEqualTo(0f)
        assertThat(after.totalMeters).isEqualTo(0f)
    }

    // ================================================================
    // Demo mode configuration
    // ================================================================

    @Test
    fun `demo config has lower threshold`() {
        val demoConfig = TranceConfig.demoConfig()
        assertThat(demoConfig.threshold).isEqualTo(0.50f)
        assertThat(demoConfig.cooldownMs).isEqualTo(15_000L)
        assertThat(demoConfig.demoMode).isTrue()
    }
}
