package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.error_unknown
import blinkly.shared.compose.generated.resources.notification_achievement_unlocked
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asImage
import com.sedsoftware.blinkly.compose.ui.extension.asTitle
import com.sedsoftware.blinkly.domain.model.AchievementType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal enum class BlinklySnackbarType {
    ERROR,
    NOTIFICATION,
    ACHIEVEMENT,
}

internal data class BlinklySnackbarVisuals(
    override val message: String,
    val type: BlinklySnackbarType,
    val title: String? = null,
    val icon: DrawableResource? = null,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = type == BlinklySnackbarType.ERROR,
    override val duration: SnackbarDuration = if (type == BlinklySnackbarType.ACHIEVEMENT) {
        SnackbarDuration.Long
    } else {
        SnackbarDuration.Short
    },
) : SnackbarVisuals

@Composable
internal fun BlinklySnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = snackbarData.visuals as? BlinklySnackbarVisuals
    val type = visuals?.type ?: BlinklySnackbarType.NOTIFICATION

    if (type == BlinklySnackbarType.ACHIEVEMENT && visuals != null) {
        AchievementSnackbar(
            visuals = visuals,
            modifier = modifier,
        )
        return
    }

    val colors = when (type) {
        BlinklySnackbarType.ERROR -> SnackbarColors(
            container = MaterialTheme.colorScheme.error,
            content = MaterialTheme.colorScheme.onError,
        )

        BlinklySnackbarType.NOTIFICATION -> SnackbarColors(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        BlinklySnackbarType.ACHIEVEMENT -> SnackbarColors(
            container = MaterialTheme.colorScheme.tertiary,
            content = MaterialTheme.colorScheme.onTertiary,
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

@Composable
private fun AchievementSnackbar(
    visuals: BlinklySnackbarVisuals,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    var entered by remember(visuals, animate) { mutableStateOf(!animate) }
    val entranceProgress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = ENTRANCE_DURATION_MILLIS,
            easing = LinearOutSlowInEasing,
        ),
        label = "achievement_snackbar_entrance",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (entered) 1f else ICON_INITIAL_SCALE,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "achievement_snackbar_icon_scale",
    )
    val entranceOffsetPx = with(LocalDensity.current) { ENTRANCE_OFFSET.toPx() }

    LaunchedEffect(visuals, animate) {
        if (animate) {
            entered = true
        }
    }

    Snackbar(
        modifier = modifier
            .graphicsLayer {
                alpha = entranceProgress
                translationY = (1f - entranceProgress) * -entranceOffsetPx
            },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            visuals.icon?.let { icon ->
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(ACHIEVEMENT_ICON_SIZE)
                        .clip(MaterialTheme.shapes.medium)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = ACHIEVEMENT_TEXT_BOTTOM_PADDING),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                visuals.title?.let { title ->
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = ACHIEVEMENT_TITLE_ALPHA),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = visuals.message,
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
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
        BlinklyAchievementSnackbarPreviewContent()
    }
}

@Preview(widthDp = 420, heightDp = 96)
@Composable
private fun BlinklySnackbarPreviewDark() {
    BlinklyWidgetPreview(isDarkTheme = true) {
        BlinklyAchievementSnackbarPreviewContent()
    }
}

@Preview(widthDp = 420, heightDp = 96)
@Composable
private fun BlinklyErrorSnackbarPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyErrorSnackbarPreviewContent()
    }
}

@Preview(widthDp = 420, heightDp = 96)
@Composable
private fun BlinklyErrorSnackbarPreviewDark() {
    BlinklyWidgetPreview(isDarkTheme = true) {
        BlinklyErrorSnackbarPreviewContent()
    }
}

@Composable
private fun BlinklyAchievementSnackbarPreviewContent() {
    AchievementSnackbar(
        visuals = BlinklySnackbarVisuals(
            message = AchievementType.FIRST_SPARK.asTitle(),
            type = BlinklySnackbarType.ACHIEVEMENT,
            title = stringResource(Res.string.notification_achievement_unlocked),
            icon = AchievementType.FIRST_SPARK.asImage(),
        ),
        animate = false,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun BlinklyErrorSnackbarPreviewContent() {
    BlinklySnackbar(
        snackbarData = PreviewSnackbarData(
            BlinklySnackbarVisuals(
                message = stringResource(Res.string.error_unknown),
                type = BlinklySnackbarType.ERROR,
            )
        ),
        modifier = Modifier
            .padding(16.dp),
    )
}

private const val ENTRANCE_DURATION_MILLIS = 250
private const val ICON_INITIAL_SCALE = 0.78f
private const val ACHIEVEMENT_TITLE_ALPHA = 0.82f
private val ENTRANCE_OFFSET = 12.dp
private val ACHIEVEMENT_ICON_SIZE = 70.dp
private val ACHIEVEMENT_TEXT_BOTTOM_PADDING = 4.dp
