package com.sedsoftware.blinkly.compose.ui.extension

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import kotlinx.coroutines.delay

private const val DEFAULT_SHIMMER_DURATION_MS: Int = 1_200
private const val DEFAULT_SHIMMER_WIDTH_RATIO: Float = 0.7f

fun Modifier.alsoIf(condition: Boolean, other: Modifier) = if (condition) this.then(other) else this

fun Modifier.shimmering(
    visible: Boolean = true,
    shape: Shape = RectangleShape,
    colors: List<Color> = listOf(
        Color.White.copy(alpha = 0.00f),
        Color.White.copy(alpha = 0.32f),
        Color.White.copy(alpha = 0.00f),
    ),
    durationMillis: Int = DEFAULT_SHIMMER_DURATION_MS,
): Modifier = composed {
    if (!visible) {
        return@composed this
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val progress: Float by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    this
        .clip(shape)
        .drawWithContent {
            drawContent()

            if (size.width == 0f || size.height == 0f) {
                return@drawWithContent
            }

            val shimmerWidth: Float = size.width * DEFAULT_SHIMMER_WIDTH_RATIO
            val startX: Float = (size.width + shimmerWidth) * progress - shimmerWidth
            val brush: Brush = Brush.linearGradient(
                colors = colors,
                start = Offset(x = startX, y = 0f),
                end = Offset(x = startX + shimmerWidth, y = size.height),
            )

            drawRect(
                brush = brush,
            )
        }
}

fun Modifier.clickableOnce(
    onClick: () -> Unit,
    debounceMs: Long = 500L,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    var isEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(isEnabled) {
        if (!isEnabled) {
            delay(debounceMs)
            isEnabled = true
        }
    }

    this.clickable(
        enabled = isEnabled,
        interactionSource = interactionSource,
        indication = ripple(),
        onClick = {
            if (isEnabled) {
                isEnabled = false
                onClick()
            }
        }
    )
}
