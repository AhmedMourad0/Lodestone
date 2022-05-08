package com.lodestone.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("ComposableNaming")
@Composable
fun <T> Flow<T>.collectAsEffect(
    block: suspend (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        onEach(block).launchIn(this)
    }
}

fun <T> Flow<T>.collectLatestIn(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (value: T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state = state) {
            this@collectLatestIn.collectLatest(action)
        }
    }
}

suspend fun <R> local(
    block: suspend () -> R
): Either<LocalReadWriteException, R> = withContext(Dispatchers.IO) {
    try {
        block.invoke().right()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        UnknownException(e).left()
    }
}
