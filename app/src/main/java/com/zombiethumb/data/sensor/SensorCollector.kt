package com.zombiethumb.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.zombiethumb.domain.model.SensorReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Collects sensor data (accelerometer, gravity, light) and emits [SensorReading] objects.
 *
 * Registers sensors only while a monitored app is in the foreground to save battery.
 * Computes tilt angle from gravity vector and shake magnitude from accelerometer.
 */
class SensorCollector(private val context: Context) {

    private val sensorManager: SensorManager? = context.getSystemService()

    private var isRegistered = false

    /**
     * Produce a Flow of SensorReadings. The flow registers sensors on collection
     * and unregisters on cancellation.
     */
    fun sensorReadings(): Flow<SensorReading> = callbackFlow {
        val sm = sensorManager ?: run {
            close()
            return@callbackFlow
        }

        var lastAccelX = 0f
        var lastAccelY = 0f
        var lastAccelZ = 0f
        var gravityX = 0f
        var gravityY = 0f
        var gravityZ = 9.81f
        var ambientLux = -1f
        var lastAccelTimestamp = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GRAVITY -> {
                        gravityX = event.values[0]
                        gravityY = event.values[1]
                        gravityZ = event.values[2]
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]

                        // Compute micro-shake: difference from gravity
                        val linearX = ax - gravityX
                        val linearY = ay - gravityY
                        val linearZ = az - gravityZ
                        val shakeMag = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

                        // Tilt angle: angle between gravity vector and -Z axis (screen normal)
                        // When phone is flat face-up: gravity = (0, 0, 9.81), tilt = 0°
                        // When phone is vertical: gravity = (0, 9.81, 0), tilt = 90°
                        // When phone is overhead (lying in bed): gravity ≈ (0, 0, -9.81) or tilted
                        val gravMag = sqrt(gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ)
                        val tiltAngle = if (gravMag > 0.1f) {
                            // Angle of phone screen from horizontal
                            val cosAngle = (abs(gravityZ) / gravMag).coerceIn(-1f, 1f)
                            val angleDeg = Math.toDegrees(acos(cosAngle.toDouble())).toFloat()
                            angleDeg
                        } else {
                            0f
                        }

                        // Throttle: emit at most every 500ms
                        val now = android.os.SystemClock.elapsedRealtime()
                        if (now - lastAccelTimestamp >= 500) {
                            lastAccelTimestamp = now
                            trySend(
                                SensorReading(
                                    timestampMs = now,
                                    tiltAngleDeg = tiltAngle,
                                    shakeMagnitude = shakeMag,
                                    ambientLux = ambientLux,
                                )
                            )
                        }

                        lastAccelX = ax
                        lastAccelY = ay
                        lastAccelZ = az
                    }

                    Sensor.TYPE_LIGHT -> {
                        ambientLux = event.values[0]
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register sensors
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gravity = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val light = sm.getDefaultSensor(Sensor.TYPE_LIGHT)

        accel?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        gravity?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        light?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }

        isRegistered = true

        awaitClose {
            sm.unregisterListener(listener)
            isRegistered = false
        }
    }
}
