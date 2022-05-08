package com.lodestone.app.lodestones.backend.directions

import com.lodestone.app.lodestones.models.Coordinates
import com.lodestone.app.lodestones.models.Directions
import kotlinx.coroutines.flow.Flow

interface DirectionsManager {
    fun getDirections(currentLocation: Coordinates, destination: Coordinates): Flow<Directions?>
}
