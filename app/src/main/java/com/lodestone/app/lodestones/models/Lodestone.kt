package com.lodestone.app.lodestones.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LodestoneId(val v: Long) : Parcelable

@Parcelize
data class Lodestone<O : Lodestone.Origin>(
    val origin: O,
    val name: String,
    val coordinates: Coordinates,
    val mapAddress: String?
) : Parcelable {
    sealed interface Origin : Parcelable
    @Parcelize
    object Created : Origin
    @Parcelize
    data class Retrieved(
        val id: LodestoneId,
        val timestamp: Long
    ) : Origin
}

val AlQiblaLodestone = Lodestone(
    origin = Lodestone.Retrieved(LodestoneId(-1), System.currentTimeMillis()),
    name = "Al Qibla",
    coordinates = KaabaCoordinates,
    mapAddress = "Masjid al Haram, Mecca, SA"
)
