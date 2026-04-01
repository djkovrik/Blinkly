package com.sedsoftware.blinkly.compose.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.icon_back
import blinkly.shared.compose.generated.resources.icon_done
import blinkly.shared.compose.generated.resources.icon_next
import blinkly.shared.compose.generated.resources.onboarding_back
import blinkly.shared.compose.generated.resources.onboarding_done
import blinkly.shared.compose.generated.resources.onboarding_next
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationButtons(
    modifier: Modifier = Modifier,
    previousStepAvailable: Boolean = false,
    nextStepAvailable: Boolean = false,
    nextStepEnabled: Boolean = false,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        if (previousStepAvailable) {
            BlinklyIconButton(
                iconRes = Res.drawable.icon_back,
                rightSideText = stringResource(resource = Res.string.onboarding_back),
                onClick = { onPreviousClick?.invoke() },
            )
        } else {
            Spacer(modifier = Modifier.weight(weight = 1f, fill = false))
        }

        BlinklyIconButton(
            iconRes = if (nextStepAvailable) {
                Res.drawable.icon_next
            } else {
                Res.drawable.icon_done
            },
            enabled = nextStepEnabled,
            leftSideText = if (nextStepAvailable) {
                stringResource(resource = Res.string.onboarding_next)
            } else {
                stringResource(resource = Res.string.onboarding_done)
            },
            onClick = { onNextClick.invoke() },
        )
    }
}

@Preview
@Composable
private fun BottomNavigationButtonsPreviewLight() {
    BlinklyWidgetPreview {
        BottomNavigationButtonsPreviewContent()
    }
}

@Preview
@Composable
private fun BottomNavigationButtonsPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BottomNavigationButtonsPreviewContent()
    }
}


@Composable
private fun BottomNavigationButtonsPreviewContent() {
    Column {
        BottomNavigationButtons(
            previousStepAvailable = false,
            nextStepAvailable = true,
            nextStepEnabled = true,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        BottomNavigationButtons(
            previousStepAvailable = true,
            nextStepAvailable = true,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        BottomNavigationButtons(
            previousStepAvailable = true,
            nextStepAvailable = false,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
