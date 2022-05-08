package com.lodestone.app.lodestones.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) : Parcelable

val KaabaCoordinates = Coordinates(latitude = 21.422487, longitude = 39.826206)
