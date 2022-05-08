package com.lodestone.app.lodestones.backend.sensors

import android.hardware.SensorEvent
import android.hardware.SensorManager
import arrow.core.Either
import kotlinx.coroutines.flow.Flow

sealed interface SensorException
data class SensorNotFound(val sensorType: Int) : SensorException
object SensorManagerNotSupported : SensorException

interface SensorsManager {
    fun asFlow(
        sensorType: Int,
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL,
    ): Flow<Either<SensorException, SensorEvent>>
}
