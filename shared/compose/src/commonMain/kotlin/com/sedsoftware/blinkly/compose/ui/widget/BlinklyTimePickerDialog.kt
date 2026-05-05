package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.common_cancel
import blinkly.shared.compose.generated.resources.common_ok
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun BlinklyTimePickerDialog(
    timePickerState: TimePickerState,
    modifier: Modifier = Modifier,
    dialogShape: Shape = MaterialTheme.shapes.medium,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        BlinklyTimePickerDialogDialogContent(
            timePickerState = timePickerState,
            dialogShape = dialogShape,
            modifier = modifier,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun BlinklyTimePickerDialogDialogContent(
    timePickerState: TimePickerState,
    dialogShape: Shape,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Card(
        modifier = modifier.padding(all = 16.dp),
        shape = dialogShape,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            TimeInput(
                state = timePickerState,
                modifier = Modifier.padding(all = 16.dp)
            )

            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.common_ok),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onConfirm)
                )

                Text(
                    text = stringResource(resource = Res.string.common_cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}

@Preview
@Composable
private fun BlinklyTimePickerDialogPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyTimePickerDialogPreviewContent()
    }
}

@Preview
@Composable
private fun BlinklyTimePickerDialogPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyTimePickerDialogPreviewContent()
    }
}

@Composable
private fun BlinklyTimePickerDialogPreviewContent() {
    val timePickerState: TimePickerState = rememberTimePickerState(
        initialHour = 23,
        initialMinute = 59,
        is24Hour = true,
    )

    BlinklyTimePickerDialog(timePickerState)
}
