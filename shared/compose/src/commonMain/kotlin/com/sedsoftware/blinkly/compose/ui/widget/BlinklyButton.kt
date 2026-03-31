package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce

@Composable
internal fun BlinklyButton(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    buttonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    buttonShape: Shape = MaterialTheme.shapes.extraLarge,
    buttonHeight: Dp = 44.dp,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(shape = buttonShape)
            .background(
                color = buttonColor,
                shape = buttonShape,
            )
            .clickableOnce(onClick = onClick)
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
private fun BlinklyIconButtonPreviewLight() {
    BlinklyWidgetPreview {
        Column {
            BlinklyButton(
                text = "OK",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyButton(
                text = "Cancel",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyButton(
                text = "Long text here",
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun BlinklyIconButtonPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        Column {
            BlinklyButton(
                text = "OK",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyButton(
                text = "Cancel",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyButton(
                text = "Long text here",
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}
