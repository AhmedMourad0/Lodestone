package com.lodestone.app.lodestones.backend.location

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationRequest
import com.lodestone.app.lodestones.di.InternalApi
import com.lodestone.app.lodestones.models.Coordinates
import com.patloew.colocation.CoLocation
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@Reusable
class LocationManagerImpl @Inject constructor(
    @InternalApi private val locationProvider: CoLocation
) : LocationManager {

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun getLocationUpdates(): Flow<Coordinates> {
        return locationProvider.getLocationUpdates(createLocationRequest())
            .mapLatest { position -> Coordinates(position.latitude, position.longitude) }
            .flowOn(Dispatchers.IO)
            .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .conflate()
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(5000)
            .setSmallestDisplacement(10f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
}
