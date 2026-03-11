package com.sedsoftware.blinkly.domain.model

sealed interface EyeMovement {

    data class Blink(val count: Int) : EyeMovement

    data object AccommodationClose : EyeMovement
    data object AccommodationFar : EyeMovement

    data object DiagonalTopLeft : EyeMovement
    data object DiagonalBottomRight : EyeMovement
    data object DiagonalTopRight : EyeMovement
    data object DiagonalBottomLeft : EyeMovement

    data object EightClockwise : EyeMovement
    data object EightCounterclockwise : EyeMovement

    data object CircleClockwise : EyeMovement
    data object CircleCounterclockwise : EyeMovement
}
