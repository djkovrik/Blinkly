package com.sedsoftware.blinkly.domain.extension

import kotlinx.datetime.LocalTime

fun LocalTime.toHumanReadableString(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
