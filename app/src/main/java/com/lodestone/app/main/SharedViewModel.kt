package com.lodestone.app.main

import android.Manifest
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.lodestone.app.lodestones.backend.LodestonesRepository
import com.lodestone.app.lodestones.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: LodestonesRepository
) : ViewModel() {

    val state = MutableStateFlow(SharedState.default())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getDirections(): Flow<Directions?> {
        return state.distinctUntilChangedBy(SharedState::destination).flatMapLatest { state ->
            repo.getDirections(state.destination.coordinates)
        }
    }
}

@Parcelize
data class SharedState(
    val destination: Lodestone<Lodestone.Retrieved>,
    val directions: Directions?
) : Parcelable {
    companion object {
        fun default() = SharedState(
            destination = AlQiblaLodestone,
            directions = null
        )
    }
}
