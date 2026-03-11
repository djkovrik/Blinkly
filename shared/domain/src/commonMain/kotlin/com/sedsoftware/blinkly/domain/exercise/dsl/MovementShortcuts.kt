package com.sedsoftware.blinkly.domain.exercise.dsl

import com.sedsoftware.blinkly.domain.model.EyeMovement

@Suppress("FunctionNaming")
internal fun Blink(count: Int) = EyeMovement.Blink(count)

internal val AccommodationClose = EyeMovement.AccommodationClose
internal val AccommodationFar = EyeMovement.AccommodationFar

internal val DiagonalTopLeft = EyeMovement.DiagonalTopLeft
internal val DiagonalBottomRight = EyeMovement.DiagonalBottomRight
internal val DiagonalTopRight = EyeMovement.DiagonalTopRight
internal val DiagonalBottomLeft = EyeMovement.DiagonalBottomLeft

internal val EightClockwise = EyeMovement.EightClockwise
internal val EightCounterclockwise = EyeMovement.EightCounterclockwise

internal val CircleClockwise = EyeMovement.CircleClockwise
internal val CircleCounterclockwise = EyeMovement.CircleCounterclockwise
