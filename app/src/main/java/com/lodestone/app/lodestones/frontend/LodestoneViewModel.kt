package com.lodestone.app.lodestones.frontend

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import arrow.core.getOrHandle
import com.lodestone.app.lodestones.backend.LodestonesRepository
import com.lodestone.app.lodestones.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class LodestoneViewModel @Inject constructor(
    private val repo: LodestonesRepository
) : ViewModel() {

    val state = MutableStateFlow(LodestoneState.default())

    val action = MutableSharedFlow<LodestoneAction>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val errorMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val lodestonesPager = Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) {
        repo.findAll()
    }.flow.cachedIn(viewModelScope)

    suspend fun onCreateLodestone(lodestoneCreator: LodestoneCreator): Lodestone<Lodestone.Retrieved>? = withContext(Dispatchers.IO) {
        if (!lodestoneCreator.canCreate) return@withContext null
        repo.add(Lodestone(
            origin = Lodestone.Created,
            name = lodestoneCreator.name,
            coordinates = lodestoneCreator.coordinates,
            mapAddress = lodestoneCreator.mapAddress
        )).getOrHandle {
            errorMessages.tryEmit("Something went wrong")
            null
        }
    }
}

@Parcelize
data class LodestoneState(
    val bottomSheetState: BottomSheetState,
    val isInputBlocked: Boolean = false
) : Parcelable {
    companion object {
        fun default() = LodestoneState(
            bottomSheetState = LodestoneSelector,
            isInputBlocked = false
        )
    }
}

sealed interface LodestoneAction {
    data class CreateLodestone(val state: LodestoneCreator) : LodestoneAction
}

sealed interface BottomSheetState : Parcelable

@Parcelize
object LodestoneSelector : BottomSheetState

@Parcelize
data class LodestoneCreator(
    val name: String,
    val coordinates: Coordinates,
    val mapAddress: String
) : BottomSheetState {
    @IgnoredOnParcel
    val canCreate = name.isNotBlank() && name.length < 50
}
