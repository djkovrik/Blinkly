package com.sedsoftware.blinkly.utils.impl

import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

internal class BlinklyTimeUtilsImpl : BlinklyTimeUtils {
    override fun now(): Instant {
        return Clock.System.now()
    }

    override fun timeZone(): TimeZone {
        return TimeZone.currentSystemDefault()
    }
}
