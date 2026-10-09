package com.zombiethumb.domain.biomarker

import com.zombiethumb.domain.model.SensorReading

/**
 * Biomarker 3: "Bedtime Slump" Signature
 *
 * Detected when:
 * - Phone tilt ~80-90° overhead (lying in bed holding phone above face)
 * - Near-total stillness (very low shake magnitude)
 * - ~0 lux ambient light (dark room)
 *
 * This is a contextual amplifier — when active, it boosts the overall
 * trance score because bedtime scrolling is particularly harmful.
 */
class BedtimeSlumpBiomarker {

    private var lastScore: Float = 0f

    fun onSensorReading(reading: SensorReading): Float {
        lastScore = calculateScore(reading)
        return lastScore
    }

    internal fun calculateScore(reading: SensorReading): Float {
        // Tilt score: peaks at 50-90° (phone overhead or tilted back in bed)
        val tiltScore = when {
            reading.tiltAngleDeg >= 60f -> 1.0f
            reading.tiltAngleDeg >= 45f -> 0.7f + (reading.tiltAngleDeg - 45f) / 15f * 0.3f
            reading.tiltAngleDeg >= 30f -> 0.3f + (reading.tiltAngleDeg - 30f) / 15f * 0.4f
            else -> 0f
        }

        // Stillness score: low shake = high score
        val stillnessScore = when {
            reading.shakeMagnitude < 0.1f -> 1.0f
            reading.shakeMagnitude < 0.3f -> 0.7f + (0.3f - reading.shakeMagnitude) / 0.2f * 0.3f
            reading.shakeMagnitude < 0.8f -> 0.3f + (0.8f - reading.shakeMagnitude) / 0.5f * 0.4f
            reading.shakeMagnitude < 1.5f -> (1.5f - reading.shakeMagnitude) / 0.7f * 0.3f
            else -> 0f
        }

        // Darkness score: low lux = high score (expanded so dim rooms trigger it too)
        val darknessScore = when {
            reading.ambientLux < 5f -> 1.0f
            reading.ambientLux < 20f -> 0.8f + (20f - reading.ambientLux) / 15f * 0.2f
            reading.ambientLux < 50f -> 0.4f + (50f - reading.ambientLux) / 30f * 0.4f
            reading.ambientLux < 100f -> (100f - reading.ambientLux) / 50f * 0.4f
            else -> 0f
        }

        // All three conditions must be somewhat present for a bedtime slump
        // Using geometric-ish combination so any one being 0 kills the score
        return (tiltScore * 0.35f + stillnessScore * 0.30f + darknessScore * 0.35f)
            .coerceIn(0f, 1f)
    }

    fun reset() {
        lastScore = 0f
    }
}
