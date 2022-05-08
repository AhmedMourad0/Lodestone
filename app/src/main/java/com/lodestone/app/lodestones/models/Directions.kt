package com.lodestone.app.lodestones.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Directions constructor(
    val destinationDirection: Float,
    val polesDirection: Float
) : Parcelable {
    companion object {
        fun default() = Directions(
            destinationDirection = 0f,
            polesDirection = 0f
        )
    }
}
