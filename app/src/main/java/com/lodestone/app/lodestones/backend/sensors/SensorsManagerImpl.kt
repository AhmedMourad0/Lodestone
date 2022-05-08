package com.lodestone.app.lodestones.backend.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@Reusable
class SensorsManagerImpl @Inject constructor(
    @ApplicationContext context: Context
) : SensorsManager {

    private val sensorManager: SensorManager? =
        ContextCompat.getSystemService(context, SensorManager::class.java)

    override fun asFlow(sensorType: Int, samplingPeriodUs: Int): Flow<Either<SensorException, SensorEvent>> {
        sensorManager ?: return flowOf(SensorManagerNotSupported.left())
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return flowOf(SensorNotFound(sensorType).left())
        return sensorManager.asFlow(sensor, samplingPeriodUs).map { it.right() }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun SensorManager.asFlow(sensor: Sensor, samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<SensorEvent> {
    return callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                trySend(sensorEvent)
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                /* no-op */
            }
        }
        this@asFlow.registerListener(listener, sensor, samplingPeriodUs)
        awaitClose { this@asFlow.unregisterListener(listener) }
    }.buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST).conflate()
}
