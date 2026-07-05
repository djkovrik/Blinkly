package com.sedsoftware.blinkly.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal object BlinklyShapes {

    fun shapes(): Shapes = Shapes(
        extraSmall = RoundedCornerShape(size = 8.dp),
        small = RoundedCornerShape(size = 10.dp),
        medium = RoundedCornerShape(size = 16.dp),
        large = RoundedCornerShape(size = 22.dp),
        extraLarge = RoundedCornerShape(size = 28.dp),
    )
}
