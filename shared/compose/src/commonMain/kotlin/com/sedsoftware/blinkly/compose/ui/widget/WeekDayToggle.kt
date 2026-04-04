package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.alsoIf
import com.sedsoftware.blinkly.compose.ui.extension.clickableOnce
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import kotlinx.datetime.DayOfWeek

@Composable
fun WeekDayToggle(
    dayOfWeek: DayOfWeek,
    toggled: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    toggledTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    toggledBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    size: Dp = 44.dp,
    borderWidth: Dp = 1.dp,
    animationDurationMs: Int = 250,
    onToggle: () -> Unit = {},
) {
    val transition: Transition<Boolean> = updateTransition(
        targetState = toggled,
        label = "WeekDayToggleTransition",
    )

    val animatedBackground: Color by transition.animateColor(
        transitionSpec = { tween(durationMillis = animationDurationMs) },
        label = "BackgroundColor",
    ) { isToggled ->
        if (isToggled) toggledBackgroundColor else backgroundColor
    }

    val animatedTextColor: Color by transition.animateColor(
        transitionSpec = { tween(durationMillis = animationDurationMs) },
        label = "TextColor",
    ) { isToggled ->
        if (isToggled) toggledTextColor else textColor
    }

    val animatedBorderWidth by transition.animateDp(
        transitionSpec = { tween(durationMillis = animationDurationMs) },
        label = "BorderWidth"
    ) { isToggled ->
        if (isToggled) 0.dp else borderWidth
    }

    val shape = CircleShape

    Box(
        modifier = modifier
            .size(size = size)
            .clip(shape = shape)
            .background(animatedBackground, shape)
            .alsoIf(
                condition = animatedBorderWidth > 0.dp,
                other = Modifier.border(
                    width = animatedBorderWidth,
                    color = toggledBackgroundColor,
                    shape = shape,
                )
            )
            .clickableOnce(
                onClick = onToggle,
                debounceMs = 150L,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayOfWeek.asLabel(),
            color = animatedTextColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun WeekDayTogglePreviewLight() {
    BlinklyWidgetPreview {
        WeekDayTogglePreviewContent()
    }
}

@Preview
@Composable
private fun WeekDayTogglePreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        WeekDayTogglePreviewContent()
    }
}


@Composable
private fun WeekDayTogglePreviewContent() {
    Row {
        WeekDayToggle(
            dayOfWeek = DayOfWeek.MONDAY,
            toggled = false,
            modifier = Modifier.padding(all = 4.dp),
        )

        WeekDayToggle(
            dayOfWeek = DayOfWeek.SUNDAY,
            toggled = true,
            modifier = Modifier.padding(all = 4.dp),
        )
    }
}
