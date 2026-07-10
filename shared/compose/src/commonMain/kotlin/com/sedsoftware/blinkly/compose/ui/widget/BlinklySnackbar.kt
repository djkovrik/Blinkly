package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.notification_achievement_unlocked
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.domain.model.AchievementType
import org.jetbrains.compose.resources.stringResource

internal enum class BlinklySnackbarType {
    ERROR,
    NOTIFICATION,
}

internal data class BlinklySnackbarVisuals(
    override val message: String,
    val type: BlinklySnackbarType,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = type == BlinklySnackbarType.ERROR,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

@Composable
internal fun BlinklySnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val type = (snackbarData.visuals as? BlinklySnackbarVisuals)?.type ?: BlinklySnackbarType.NOTIFICATION
    val colors = when (type) {
        BlinklySnackbarType.ERROR -> SnackbarColors(
            container = MaterialTheme.colorScheme.error,
            content = MaterialTheme.colorScheme.onError,
        )

        BlinklySnackbarType.NOTIFICATION -> SnackbarColors(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }

    Snackbar(
        snackbarData = snackbarData,
        containerColor = colors.container,
        contentColor = colors.content,
        actionColor = colors.content,
        dismissActionContentColor = colors.content,
        modifier = modifier,
    )
}

private data class SnackbarColors(
    val container: Color,
    val content: Color,
)

private class PreviewSnackbarData(
    override val visuals: SnackbarVisuals,
) : SnackbarData {
    override fun performAction() = Unit

    override fun dismiss() = Unit
}

@Preview(widthDp = 420, heightDp = 96)
@Composable
private fun BlinklySnackbarPreviewLight() {
    BlinklyWidgetPreview {
        BlinklySnackbarPreviewContent()
    }
}

@Preview(widthDp = 420, heightDp = 96)
@Composable
private fun BlinklySnackbarPreviewDark() {
    BlinklyWidgetPreview(isDarkTheme = true) {
        BlinklySnackbarPreviewContent()
    }
}

@Composable
private fun BlinklySnackbarPreviewContent() {
    val message = stringResource(
        Res.string.notification_achievement_unlocked,
        AchievementType.FIRST_SPARK.asTitle(),
    )

    BlinklySnackbar(
        snackbarData = PreviewSnackbarData(
            BlinklySnackbarVisuals(
                message = message,
                type = BlinklySnackbarType.NOTIFICATION,
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}
