package com.lodestone.app.lodestones.backend

import androidx.paging.PagingSource
import arrow.core.Either
import com.lodestone.app.lodestones.models.Coordinates
import com.lodestone.app.lodestones.models.Lodestone
import com.lodestone.app.lodestones.models.LodestoneId
import com.lodestone.app.lodestones.models.Directions
import com.lodestone.app.utils.LocalReadWriteException
import kotlinx.coroutines.flow.Flow

interface LodestonesRepository {
    suspend fun add(lodestone: Lodestone<Lodestone.Created>): Either<LocalReadWriteException, Lodestone<Lodestone.Retrieved>>
    fun findAll(): PagingSource<Long, Lodestone<Lodestone.Retrieved>>
    suspend fun find(id: LodestoneId): Either<LocalReadWriteException, Lodestone<Lodestone.Retrieved>>
    suspend fun delete(id: LodestoneId): Either<LocalReadWriteException, LodestoneId>
    fun getDirections(destination: Coordinates): Flow<Directions?>
}
