package com.lodestone.app.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import com.lodestone.app.compose.LocalActivity
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
                    CompositionLocalProvider(
                        LocalSharedViewModel provides model,
                        LocalActivity provides this@MainActivity
                    ) {
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

    @SuppressLint("MissingPermission")
    private fun addObservers() {
        if (!hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            toast("Missing permissions")
        }
        model.getDirections().catch { e ->
            Timber.e(e)
            toast("Something went wrong")
        }.filterNotNull().collectLatestIn(this, Lifecycle.State.RESUMED) {
            model.state.value = model.state.value.copy(directions = it)
        }
    }
}

@Suppress("SameParameterValue")
private fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}
