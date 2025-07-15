package com.whoisacat.android.alfredchoose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorEvent
import java.lang.UnsupportedOperationException

/*
 ** https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android
 */
// package com.hlidskialf.android.hardware;
class ShakeListener(private val context: Context) : SensorEventListener {
    private var sensorMgr: SensorManager? = null
    private var lastX = -1.0f
    private var lastY = -1.0f
    private var lastZ = -1.0f
    private var lastTime: Long = 0
    private var shakeListener: OnShakeListener? = null
    private var shakeCount = 0
    private var lastShake: Long = 0
    private var lastForce: Long = 0
    private var sensor: Sensor? = null

    interface OnShakeListener {
        fun onShake()
    }

    fun setOnShakeListener(listener: OnShakeListener?) {
        shakeListener = listener
    }

    fun resume() {
        sensorMgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorMgr == null) {
            throw UnsupportedOperationException("Sensors not supported")
        }
        sensor = sensorMgr!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val supported = sensorMgr!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        if (!supported) {
            sensorMgr!!.unregisterListener(this, sensor)
            throw UnsupportedOperationException("Accelerometer not supported")
        }
    }

    fun pause() {
        if (sensorMgr != null) {
            sensorMgr!!.unregisterListener(this, sensor)
            sensorMgr = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor != sensor) return
        val now = System.currentTimeMillis()
        if (now - lastForce > SHAKE_TIMEOUT) {
            shakeCount = 0
        }
        if (now - lastTime > TIME_THRESHOLD) {
            val diff = now - lastTime
            val speed = Math.abs(event.values[SensorManager.DATA_X] + event.values[SensorManager.DATA_Y] + event.values[SensorManager.DATA_Z] - lastX -
                    lastY - lastZ) / diff * 10000
            if (speed > FORCE_THRESHOLD) {
                if (++shakeCount >= SHAKE_COUNT && now - lastShake > SHAKE_DURATION) {
                    lastShake = now
                    shakeCount = 0
                    if (shakeListener != null) {
                        shakeListener!!.onShake()
                    }
                }
                lastForce = now
            }
            lastTime = now
            lastX = event.values[SensorManager.DATA_X]
            lastY = event.values[SensorManager.DATA_Y]
            lastZ = event.values[SensorManager.DATA_Z]
        }
    }

    companion object {
        private const val FORCE_THRESHOLD = 350
        private const val TIME_THRESHOLD = 150
        private const val SHAKE_TIMEOUT = 500
        private const val SHAKE_DURATION = 1000
        private const val SHAKE_COUNT = 3
    }

    init {
        resume()
    }
}