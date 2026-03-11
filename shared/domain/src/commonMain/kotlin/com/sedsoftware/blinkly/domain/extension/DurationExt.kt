package com.sedsoftware.blinkly.domain.extension

import com.sedsoftware.blinkly.domain.model.DurationMs

internal val Int.ms get() = DurationMs(toLong())

internal val Int.seconds get() = DurationMs(this * 1000L)

internal val Float.seconds get() = DurationMs((this * 1000).toLong())
