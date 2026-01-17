package com.example.exe1_oran_tausi.logic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.example.exe1_oran_tausi.callback.TiltCallback
import kotlin.math.abs

class CarTiltController(
    context: Context,
    private val callback: TiltCallback
) {

    private val sensors =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val display =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val accelSensor: Sensor? =
        sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastLaneMoveAtMs = 0L

    // Tuning for 5 lanes
    private val moveThreshold = 2.4f     // required tilt to move a lane
    private val deadZone = 1.2f          // ignore small tilt
    private val moveCooldownMs = 220L    // min time between lane moves

    private val listener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            val tiltX = readHorizontalTilt(event)
            handleLaneMove(tiltX)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun handleLaneMove(tiltX: Float) {
        if (abs(tiltX) < deadZone) return

        val now = System.currentTimeMillis()
        if (now - lastLaneMoveAtMs < moveCooldownMs) return
        lastLaneMoveAtMs = now

        when {
            tiltX > moveThreshold -> callback.onMove(-1)
            tiltX < -moveThreshold -> callback.onMove(+1)
        }

    }

    private fun readHorizontalTilt(event: SensorEvent): Float {
        return when (display.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> event.values[0]
            Surface.ROTATION_90 -> -event.values[1]
            Surface.ROTATION_180 -> -event.values[0]
            Surface.ROTATION_270 -> event.values[1]
            else -> event.values[0]
        }
    }

    fun start() {
        val s = accelSensor ?: return
        sensors.registerListener(
            listener,
            s,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensors.unregisterListener(listener)
    }
}