package com.lodestone.app.compose

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf
import com.lodestone.app.main.SharedViewModel

val LocalSharedViewModel = staticCompositionLocalOf<SharedViewModel> {
    error("CompositionLocal LocalSharedViewModel not present")
}

val LocalActivity = staticCompositionLocalOf<Activity> {
    error("CompositionLocal LocalActivity not present")
}
