package com.sedsoftware.blinkly.domain.external

import kotlin.time.Instant

interface BlinklyTimeUtils {
    fun now(): Instant
}
