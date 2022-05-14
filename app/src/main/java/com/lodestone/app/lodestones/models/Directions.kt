package com.lodestone.app.lodestones.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Directions constructor(
    val currentLocation: Coordinates,
    val destinationDirection: Float,
    val polesDirection: Float
) : Parcelable
