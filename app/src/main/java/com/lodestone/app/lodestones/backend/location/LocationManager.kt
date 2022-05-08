package com.lodestone.app.lodestones.backend.location

import android.Manifest
import androidx.annotation.RequiresPermission
import com.lodestone.app.lodestones.models.Coordinates
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getLocationUpdates(): Flow<Coordinates>
}
