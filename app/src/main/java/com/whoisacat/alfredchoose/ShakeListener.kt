package com.whoisacat.android.alfreddice

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
class ShakeListener(private val mContext: Context) : SensorEventListener {
    private var mSensorMgr: SensorManager? = null
    private var mLastX = -1.0f
    private var mLastY = -1.0f
    private var mLastZ = -1.0f
    private var mLastTime: Long = 0
    private var mShakeListener: OnShakeListener? = null
    private var mShakeCount = 0
    private var mLastShake: Long = 0
    private var mLastForce: Long = 0
    private var mSensor: Sensor? = null

    interface OnShakeListener {
        fun onShake()
    }

    fun setOnShakeListener(listener: OnShakeListener?) {
        mShakeListener = listener
    }

    fun resume() {
        mSensorMgr = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorMgr == null) {
            throw UnsupportedOperationException("Sensors not supported")
        }
        mSensor = mSensorMgr!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val supported = mSensorMgr!!.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI)
        if (!supported) {
            mSensorMgr!!.unregisterListener(this, mSensor)
            throw UnsupportedOperationException("Accelerometer not supported")
        }
    }

    fun pause() {
        if (mSensorMgr != null) {
            mSensorMgr!!.unregisterListener(this, mSensor)
            mSensorMgr = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor != mSensor) return
        val now = System.currentTimeMillis()
        if (now - mLastForce > SHAKE_TIMEOUT) {
            mShakeCount = 0
        }
        if (now - mLastTime > TIME_THRESHOLD) {
            val diff = now - mLastTime
            val speed = Math.abs(event.values[SensorManager.DATA_X] + event.values[SensorManager.DATA_Y] + event.values[SensorManager.DATA_Z] - mLastX -
                    mLastY - mLastZ) / diff * 10000
            if (speed > FORCE_THRESHOLD) {
                if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
                    mLastShake = now
                    mShakeCount = 0
                    if (mShakeListener != null) {
                        mShakeListener!!.onShake()
                    }
                }
                mLastForce = now
            }
            mLastTime = now
            mLastX = event.values[SensorManager.DATA_X]
            mLastY = event.values[SensorManager.DATA_Y]
            mLastZ = event.values[SensorManager.DATA_Z]
        }
    }

    companion object {
        private const val FORCE_THRESHOLD = 350
        private const val TIME_THRESHOLD = 100
        private const val SHAKE_TIMEOUT = 500
        private const val SHAKE_DURATION = 1000
        private const val SHAKE_COUNT = 3
    }

    init {
        resume()
    }
}