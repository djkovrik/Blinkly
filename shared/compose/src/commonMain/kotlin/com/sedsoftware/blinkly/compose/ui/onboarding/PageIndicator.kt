package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

@Composable
fun PageIndicator(
    pagesCount: Int,
    currentScrollPosition: Float,
    indicatorMarkerColor: Color,
    indicatorBackgroundColor: Color,
    indicatorSize: Dp,
    indicatorSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(indicatorSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pagesCount) {
                Box(
                    modifier = Modifier
                        .size(size = indicatorSize)
                        .background(
                            color = indicatorBackgroundColor,
                            shape = CircleShape,
                        )
                )
            }
        }

        Box(
            Modifier
                .wormTransition(currentScrollPosition, indicatorSpacing, indicatorMarkerColor)
                .size(indicatorSize)
        )
    }
}

private fun Modifier.wormTransition(scrollPosition: Float, spacing: Dp, color: Color) =
    drawBehind {
        val distance = size.width + spacing.roundToPx()
        val wormOffset = (scrollPosition % 1) * 2

        val xPos = scrollPosition.toInt() * distance
        val head = xPos + distance * 0f.coerceAtLeast(wormOffset - 1)
        val tail = xPos + size.width + 1f.coerceAtMost(wormOffset) * distance

        val worm = RoundRect(
            left = head,
            top = 0f,
            right = tail,
            bottom = size.height,
            cornerRadius = CornerRadius(x = 50f)
        )

        val path = Path().apply { addRoundRect(worm) }
        drawPath(path = path, color = color)
    }
