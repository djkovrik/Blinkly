package com.sedsoftware.blinkly.domain.external

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

interface BlinklyTimeUtils {
    fun now(): Instant
    fun timeZone(): TimeZone
}
