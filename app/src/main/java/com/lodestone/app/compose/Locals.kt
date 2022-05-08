package com.lodestone.app.compose

import androidx.compose.runtime.staticCompositionLocalOf
import com.lodestone.app.main.SharedViewModel

val LocalSharedViewModel = staticCompositionLocalOf<SharedViewModel> {
    error("CompositionLocal LocalSharedViewModel not present")
}
