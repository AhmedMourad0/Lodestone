package com.lodestone.app.lodestones.backend

import androidx.paging.PagingSource
import arrow.core.Either
import com.lodestone.app.db.LocationQueries
import com.lodestone.app.lodestones.backend.directions.DirectionsManager
import com.lodestone.app.lodestones.backend.location.LocationManager
import com.lodestone.app.lodestones.di.InternalApi
import com.lodestone.app.lodestones.models.Coordinates
import com.lodestone.app.lodestones.models.Directions
import com.lodestone.app.lodestones.models.Lodestone
import com.lodestone.app.lodestones.models.LodestoneId
import com.lodestone.app.utils.LocalReadWriteException
import com.lodestone.app.utils.local
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@Reusable
class LodestonesRepositoryImpl @Inject constructor(
    @InternalApi private val locationQueries: LocationQueries,
    @InternalApi private val directionsManager: DirectionsManager,
    @InternalApi private val locationManager: LocationManager
) : LodestonesRepository {

    override suspend fun add(
        lodestone: Lodestone<Lodestone.Created>
    ): Either<LocalReadWriteException, Lodestone<Lodestone.Retrieved>> = local {
        locationQueries.transactionWithResult {

            locationQueries.insertOrReplaceLocation(
                id = null,
                timestamp = System.currentTimeMillis(),
                name = lodestone.name,
                latitude = lodestone.coordinates.latitude,
                longitude = lodestone.coordinates.longitude,
                map_id = lodestone.mapId,
                map_address = lodestone.mapAddress
            )

            locationQueries.findLocationById(
                id = locationQueries.lastInsertRowId().executeAsOne(),
                mapper = ::retrievedLocationMapper
            ).executeAsOne()
        }
    }

    override fun findAll(): PagingSource<Long, Lodestone<Lodestone.Retrieved>> {
        return QueryPagingSource(
            countQuery = locationQueries.countLocations(),
            transacter = locationQueries,
            dispatcher = Dispatchers.IO
        ) { limit, offset -> locationQueries.findAllLocations(limit, offset, ::retrievedLocationMapper) }
    }

    override suspend fun find(
        id: LodestoneId
    ): Either<LocalReadWriteException, Lodestone<Lodestone.Retrieved>> = local {
        locationQueries.findLocationById(id.v, ::retrievedLocationMapper).executeAsOne()
    }

    override suspend fun delete(id: LodestoneId): Either<LocalReadWriteException, LodestoneId> = local {
        locationQueries.deleteLocation(id.v)
        id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDirections(destination: Coordinates): Flow<Directions?> {
        return locationManager.getLocationUpdates().flatMapLatest {
            directionsManager.getDirections(it, destination)
        }
    }
}

private fun retrievedLocationMapper(
    id: Long,
    timestamp: Long,
    name: String,
    latitude: Double,
    longitude: Double,
    mapId: String?,
    mapAddress: String?
) = Lodestone(
    origin = Lodestone.Retrieved(id = LodestoneId(id), timestamp = timestamp),
    name = name,
    coordinates = Coordinates(
        latitude = latitude,
        longitude = longitude
    ), mapId = mapId,
    mapAddress = mapAddress
)
