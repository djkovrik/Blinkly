package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.icon_back
import blinkly.shared.compose.generated.resources.icon_done
import blinkly.shared.compose.generated.resources.icon_next
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.alsoIf
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BlinklyIconButton(
    iconRes: DrawableResource,
    modifier: Modifier = Modifier,
    leftSideText: String? = null,
    rightSideText: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    buttonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    buttonShape: Shape = MaterialTheme.shapes.extraLarge,
    buttonHeight: Dp = 44.dp,
    iconSize: Dp = 22.dp,
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
            .alsoIf(
                leftSideText != null || rightSideText != null,
                Modifier
                    .height(height = buttonHeight)
                    .defaultMinSize(minWidth = 100.dp)
            )
            .alsoIf(
                leftSideText == null && rightSideText == null,
                Modifier.size(size = buttonHeight)
            ),
    ) {
        if (leftSideText != null) {
            Text(
                text = leftSideText,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        Icon(
            painter = painterResource(resource = iconRes),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(size = iconSize),
        )

        if (rightSideText != null) {
            Text(
                text = rightSideText,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun BlinklyIconButtonPreviewLight() {
    BlinklyWidgetPreview {
        Column {
            BlinklyIconButton(
                iconRes = Res.drawable.icon_next,
                leftSideText = "Next",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_back,
                rightSideText = "Back",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_done,
                leftSideText = "Done",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_done,
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
            BlinklyIconButton(
                iconRes = Res.drawable.icon_next,
                leftSideText = "Далее",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_back,
                rightSideText = "Назад",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_done,
                leftSideText = "Готово",
                modifier = Modifier.padding(all = 4.dp)
            )

            BlinklyIconButton(
                iconRes = Res.drawable.icon_done,
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}
