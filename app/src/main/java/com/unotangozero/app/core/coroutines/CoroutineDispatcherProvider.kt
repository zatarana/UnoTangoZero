package com.unotangozero.app.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

data class CoroutineDispatcherProvider(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher
)
