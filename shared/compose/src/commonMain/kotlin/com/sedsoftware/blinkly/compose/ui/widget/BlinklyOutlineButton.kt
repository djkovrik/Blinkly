package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.alsoIf
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce

@Composable
internal fun BlinklyOutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    buttonShape: Shape = MaterialTheme.shapes.extraLarge,
    borderColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    borderWidth: Dp = 2.dp,
    buttonHeight: Dp = 44.dp,
    alphaEnabled: Float = 1f,
    alphaDisabled: Float = 0.4f,
    animationDuration: Int = 150,
    onClick: () -> Unit = {},
) {
    val animatedAlpha: Float by animateFloatAsState(
        targetValue = if (enabled) alphaEnabled else alphaDisabled,
        animationSpec = tween(
            easing = LinearEasing,
            durationMillis = animationDuration,
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(shape = buttonShape)
            .alpha(alpha = animatedAlpha)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = buttonShape,
            )
            .alsoIf(
                enabled,
                Modifier.clickableOnce(onClick = onClick)
            )
            .defaultMinSize(minWidth = 100.dp)
            .height(height = buttonHeight),
    ) {
        Text(
            text = text,
            color = textColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview
@Composable
private fun BlinklyOutlineButtonPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyOutlineButtonPreviewContent()
    }
}

@Preview
@Composable
private fun BlinklyOutlineButtonPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyOutlineButtonPreviewContent()
    }
}

@Composable
private fun BlinklyOutlineButtonPreviewContent() {
    Column {
        BlinklyOutlineButton(
            text = "OK",
            modifier = Modifier.padding(all = 4.dp)
        )

        BlinklyOutlineButton(
            text = "Cancel",
            modifier = Modifier.padding(all = 4.dp)
        )

        BlinklyOutlineButton(
            text = "Long text here",
            modifier = Modifier.padding(all = 4.dp),
            enabled = false,
        )
    }
}
