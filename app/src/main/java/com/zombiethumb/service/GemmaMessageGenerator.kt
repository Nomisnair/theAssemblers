package com.zombiethumb.service

import android.content.Context
import com.zombiethumb.domain.model.TranceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Generates compassionate break notification messages.
 *
 * Primary: On-device Gemma 2B via MediaPipe LLM Inference.
 * Fallback: Handwritten template messages filled with telemetry data.
 *
 * The fallback is ALWAYS used if:
 * - Model file is not found
 * - Inference fails
 * - Inference takes more than ~1 second
 *
 * This ensures notifications are never delayed or broken.
 */
class GemmaMessageGenerator(private val context: Context) {

    /**
     * No warm up needed for lightweight templates.
     */
    suspend fun warmUp() {
        // No-op
    }

    /**
     * Generate a compassionate break message from the trance result using dynamic templates.
     */
    suspend fun generateMessage(result: TranceResult): String {
        return generateTemplateMessage(result)
    }

    /**
     * Generate a template-based message filled with real telemetry data.
     */
    fun generateTemplateMessage(result: TranceResult): String {
        val meters = result.totalMeters.toInt()
        val durationMin = (result.sessionDurationMs / 60_000).toInt().coerceAtLeast(1)
        val app = result.appName.ifBlank { "your feed" }
        val isNight = result.currentLux in 0f..5f
        val isLateHour = LocalTime.now().let { it.hour >= 22 || it.hour < 6 }

        return when {
            // Nighttime + dark (bedtime variant)
            (isNight || isLateHour) && result.bedtimeSlump > 0.5f -> {
                BEDTIME_TEMPLATES.random()
                    .replace("{meters}", meters.toString())
                    .replace("{duration}", durationMin.toString())
                    .replace("{app}", app)
            }

            // Nighttime but not full bedtime slump
            isNight || isLateHour -> {
                NIGHT_TEMPLATES.random()
                    .replace("{meters}", meters.toString())
                    .replace("{duration}", durationMin.toString())
                    .replace("{app}", app)
            }

            // Daytime
            else -> {
                DAY_TEMPLATES.random()
                    .replace("{meters}", meters.toString())
                    .replace("{duration}", durationMin.toString())
                    .replace("{app}", app)
            }
        }
    }

    companion object {
        private val DAY_TEMPLATES = listOf(
            "You've scrolled {meters} meters of {app} in {duration} minutes. Time for a short break — your eyes will thank you! 🌿",
            "That's {meters} meters of feed in {duration} minutes on {app}. How about stretching your legs for a minute? 🚶",
            "Hey, you've been scrolling {app} for {duration} minutes ({meters}m of feed). A quick break might feel really good right now. ☀️",
            "Zombie-scroll detected: {meters} meters in {duration} minutes on {app}. Your thumbs deserve a rest! 👍",
            "{meters} meters of {app} in {duration} minutes — that's quite a journey! Time to look up from the screen. 🌤️",
        )

        private val NIGHT_TEMPLATES = listOf(
            "You've scrolled {meters} meters in the dark over the last {duration} minutes. Your brain is asking for rest — consider putting the phone down. 🌙",
            "It's late and you've scrolled {meters}m of {app} in {duration} minutes. Tomorrow-you will appreciate some sleep right now. 💤",
            "Late-night {app}: {meters} meters in {duration} minutes. Your body's ready for sleep, even if your brain wants one more scroll. 🌜",
            "{meters} meters of nighttime scrolling in {duration} minutes. The feed will still be there tomorrow — get some rest. 🛌",
        )

        private val BEDTIME_TEMPLATES = listOf(
            "You've scrolled {meters} meters in the dark over the last {duration} minutes. Your brain is asking for rest, so put the phone down and sleep. 🌙💤",
            "Phone overhead in the dark, {meters}m of {app} in {duration} minutes. This is the classic bedtime scroll — your sleep is worth more. 😴",
            "Bedtime scroll detected: {meters} meters of {app} in {duration} minutes with nearly zero light. Your body is begging for sleep. 🛏️",
            "{meters}m of feed in the dark for {duration} minutes. You're in the zombie-scroll zone — time to put the phone on the nightstand. 🧟💤",
        )
    }
}
