package com.lodestone.app.lodestones.backend.directions

import android.hardware.Sensor
import android.hardware.SensorManager
import arrow.core.getOrHandle
import com.lodestone.app.lodestones.di.InternalApi
import com.lodestone.app.lodestones.models.Coordinates
import com.lodestone.app.lodestones.models.Directions
import com.lodestone.app.lodestones.backend.sensors.SensorsManager
import dagger.Reusable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.lang.RuntimeException
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Reusable
class DirectionsManagerImpl @Inject constructor(
    @InternalApi private val sensorsManager: SensorsManager
) : DirectionsManager {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDirections(
        currentLocation: Coordinates,
        destination: Coordinates
    ): Flow<Directions?> {
        val alpha = 0.97f
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        return merge(
            sensorsManager.asFlow(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME),
            sensorsManager.asFlow(Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_GAME)
        ).mapLatest { result ->

            val event = result.getOrHandle {
                Timber.e(RuntimeException(it.toString()))
                return@mapLatest null
            }

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                    geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                    geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
                }
            }

            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {

                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                val azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                val destinationAzimuth = azimuth - bearing(currentLocation, destination)

                Directions(
                    polesDirection = azimuth,
                    destinationDirection = destinationAzimuth.toFloat()
                )
            } else {
                null
            }
        }
    }
}

private fun bearing(start: Coordinates, end: Coordinates): Double {
    val latitude1 = Math.toRadians(start.latitude)
    val latitude2 = Math.toRadians(end.latitude)
    val longDiff = Math.toRadians(end.longitude - start.longitude)
    val y = sin(longDiff) * cos(latitude2)
    val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)
    return (Math.toDegrees(atan2(y, x)) + 360) % 360
}
