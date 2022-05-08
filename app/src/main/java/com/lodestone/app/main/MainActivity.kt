package com.lodestone.app.main

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import com.lodestone.app.compose.LocalSharedViewModel
import com.lodestone.app.compose.LodestoneTheme
import com.lodestone.app.lodestones.frontend.LodestoneScreen
import com.lodestone.app.utils.collectLatestIn
import com.lodestone.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val model: SharedViewModel by viewModels()

    private val permissionsRequester = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        /* no-op */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LodestoneTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CompositionLocalProvider(LocalSharedViewModel provides model) {
                        LodestoneScreen()
                    }
                }
            }
        }
        permissionsRequester.launch(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
        addObservers()
    }

    private fun addObservers() {
        model.getDirections().catch { e ->
            Timber.e(e)
            toast("Something went wrong")
        }.filterNotNull().collectLatestIn(this, Lifecycle.State.RESUMED) {
            model.state.value = model.state.value.copy(directions = it)
        }
    }
}
