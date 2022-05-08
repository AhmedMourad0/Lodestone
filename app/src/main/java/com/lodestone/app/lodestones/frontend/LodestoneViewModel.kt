package com.lodestone.app.lodestones.frontend

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import com.lodestone.app.lodestones.backend.LodestonesRepository
import com.lodestone.app.lodestones.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
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
}

@Parcelize
data class LodestoneState(
    val lodestoneSelector: LodestoneSelectorState,
    val lodestoneCreator: LodestoneCreatorState,
    val showLodestonesBottomSheet: Boolean = false
) : Parcelable {
    companion object {
        fun default() = LodestoneState(
            lodestoneSelector = LodestoneSelectorState.default(),
            lodestoneCreator = LodestoneCreatorState.default(),
            showLodestonesBottomSheet = false
        )
    }
}

sealed interface LodestoneAction {
    object AddLodestone
}

@Parcelize
data class LodestoneSelectorState(
    val isShown: Boolean
) : Parcelable {
    companion object {
        fun default() = LodestoneSelectorState(isShown = false)
    }
}

@Parcelize
data class LodestoneCreatorState(
    val isShown: Boolean,
    val name: String,
    val coordinates: Coordinates?,
    val mapId: String?,
    val mapAddress: String?
) : Parcelable {
    companion object {
        fun default() = LodestoneCreatorState(
            isShown = false,
            name = "",
            coordinates = null,
            mapId = null,
            mapAddress = null
        )
    }
}
