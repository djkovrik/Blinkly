package com.sedsoftware.blinkly.domain.external

import kotlinx.coroutines.CoroutineDispatcher

interface BlinklyDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
}
