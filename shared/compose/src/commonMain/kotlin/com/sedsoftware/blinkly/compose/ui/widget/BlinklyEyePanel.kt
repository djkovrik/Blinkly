@file:Suppress("LongMethod", "LongParameterList", "MagicNumber")

package com.sedsoftware.blinkly.compose.ui.widget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.blinkly_eye_focus_close_content_description
import blinkly.shared.compose.generated.resources.blinkly_eye_focus_close_label
import blinkly.shared.compose.generated.resources.blinkly_eye_focus_far_content_description
import blinkly.shared.compose.generated.resources.blinkly_eye_focus_far_label
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.domain.model.EyeMovement
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BlinklyEyePanel(
    movement: EyeMovement? = null,
    modifier: Modifier = Modifier,
    animationTrigger: Any? = movement,
    restState: BlinklyEyeRestState = BlinklyEyeRestState.Open,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val openness = remember { Animatable(OPEN_EYE) }
    val pathProgress = remember { Animatable(0f) }
    var focusState: EyeFocusState by remember { mutableStateOf(EyeFocusState.None) }
    var pathMovement: EyeMovement? by remember { mutableStateOf(null) }

    LaunchedEffect(animationTrigger, restState) {
        val currentMovement = movement
        if (currentMovement == null) {
            pathMovement = null
            pathProgress.snapTo(0f)
            focusState = EyeFocusState.None
            openness.animateTo(
                targetValue = restState.openness,
                animationSpec = tween(
                    durationMillis = EYE_OPEN_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
            return@LaunchedEffect
        }

        pathMovement = null
        pathProgress.snapTo(0f)

        when (currentMovement) {
            is EyeMovement.Blink -> {
                openness.snapTo(OPEN_EYE)
                openness.animateTo(
                    targetValue = CLOSED_EYE,
                    animationSpec = tween(
                        durationMillis = BLINK_CLOSE_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                )
                openness.animateTo(
                    targetValue = OPEN_EYE,
                    animationSpec = tween(
                        durationMillis = BLINK_OPEN_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }

            EyeMovement.AccommodationClose -> {
                focusState = EyeFocusState.Close
                openness.animateToOpen()
            }

            EyeMovement.AccommodationFar -> {
                focusState = EyeFocusState.Far
                openness.animateToOpen()
            }

            EyeMovement.DiagonalTopLeft -> {
                focusState = EyeFocusState.None
                openness.animateToOpen()
                animateOffsetTo(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    x = offsetX.value - DIAGONAL_STEP,
                    y = offsetY.value - DIAGONAL_STEP,
                )
            }

            EyeMovement.DiagonalBottomRight -> {
                focusState = EyeFocusState.None
                openness.animateToOpen()
                animateOffsetTo(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    x = offsetX.value + DIAGONAL_STEP,
                    y = offsetY.value + DIAGONAL_STEP,
                )
            }

            EyeMovement.DiagonalTopRight -> {
                focusState = EyeFocusState.None
                openness.animateToOpen()
                animateOffsetTo(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    x = offsetX.value + DIAGONAL_STEP,
                    y = offsetY.value - DIAGONAL_STEP,
                )
            }

            EyeMovement.DiagonalBottomLeft -> {
                focusState = EyeFocusState.None
                openness.animateToOpen()
                animateOffsetTo(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    x = offsetX.value - DIAGONAL_STEP,
                    y = offsetY.value + DIAGONAL_STEP,
                )
            }

            EyeMovement.CircleClockwise,
            EyeMovement.CircleCounterclockwise,
            EyeMovement.EightClockwise,
            EyeMovement.EightCounterclockwise,
                -> {
                focusState = EyeFocusState.None
                openness.animateToOpen()
                pathMovement = currentMovement
                pathProgress.snapTo(0f)
                pathProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = currentMovement.pathDuration(),
                        easing = LinearEasing,
                    ),
                )
                pathMovement = null
                pathProgress.snapTo(0f)
            }
        }
    }

    BlinklyEyeContent(
        focusState = focusState,
        openness = openness.value,
        movementOffset = Offset(
            x = offsetX.value + pathMovement.pathOffset(pathProgress.value).x,
            y = offsetY.value + pathMovement.pathOffset(pathProgress.value).y,
        ),
        modifier = modifier,
    )
}

enum class BlinklyEyeRestState {
    Open,
    Closed,
}

private suspend fun Animatable<Float, *>.animateToOpen() {
    animateTo(
        targetValue = OPEN_EYE,
        animationSpec = tween(
            durationMillis = EYE_OPEN_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
    )
}

private suspend fun animateOffsetTo(
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    x: Float,
    y: Float,
) {
    coroutineScope {
        launch {
            offsetX.animateTo(
                targetValue = x.coerceIn(-MAX_OFFSET, MAX_OFFSET),
                animationSpec = tween(
                    durationMillis = DIAGONAL_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        launch {
            offsetY.animateTo(
                targetValue = y.coerceIn(-MAX_OFFSET, MAX_OFFSET),
                animationSpec = tween(
                    durationMillis = DIAGONAL_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }
}

@Composable
private fun BlinklyEyeContent(
    focusState: EyeFocusState,
    openness: Float,
    movementOffset: Offset,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusLabel = when (focusState) {
        EyeFocusState.None -> null
        EyeFocusState.Close -> stringResource(resource = Res.string.blinkly_eye_focus_close_label)
        EyeFocusState.Far -> stringResource(resource = Res.string.blinkly_eye_focus_far_label)
    }
    val contentDescription = when (focusState) {
        EyeFocusState.None -> if (openness <= CLOSED_THRESHOLD) "Blinkly eye closed" else "Blinkly eye open"
        EyeFocusState.Close -> stringResource(resource = Res.string.blinkly_eye_focus_close_content_description)
        EyeFocusState.Far -> stringResource(resource = Res.string.blinkly_eye_focus_far_content_description)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(color = colorScheme.surfaceContainer)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBlinklyEye(
                openness = openness,
                movementOffset = movementOffset,
                focusState = focusState,
                surfaceColor = colorScheme.surface,
                outlineColor = colorScheme.primary,
                irisColor = colorScheme.tertiary,
                pupilColor = colorScheme.onSurface,
                highlightColor = colorScheme.surfaceContainerHighest,
            )
        }

        if (focusLabel != null) {
            Text(
                text = focusLabel,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
            )
        }
    }
}

private fun DrawScope.drawBlinklyEye(
    openness: Float,
    movementOffset: Offset,
    focusState: EyeFocusState,
    surfaceColor: Color,
    outlineColor: Color,
    irisColor: Color,
    pupilColor: Color,
    highlightColor: Color,
) {
    val minSide = min(size.width, size.height)
    val center = Offset(
        x = size.width / 2f + movementOffset.x.coerceIn(-MAX_OFFSET, MAX_OFFSET) * minSide * OFFSET_SCALE,
        y = size.height * EYE_CENTER_Y_FRACTION + movementOffset.y.coerceIn(-MAX_OFFSET, MAX_OFFSET) * minSide * OFFSET_SCALE,
    )
    val eyeWidth = minSide * EYE_WIDTH_FRACTION
    val eyeHeight = minSide * EYE_HEIGHT_FRACTION * openness.coerceIn(CLOSED_EYE, OPEN_EYE)
    val closedStroke = Stroke(
        width = minSide * CLOSED_STROKE_FRACTION,
        cap = StrokeCap.Round,
    )

    if (openness <= CLOSED_THRESHOLD) {
        drawLine(
            color = outlineColor,
            start = Offset(center.x - eyeWidth / 2f, center.y),
            end = Offset(center.x + eyeWidth / 2f, center.y),
            strokeWidth = closedStroke.width,
            cap = StrokeCap.Round,
        )
        return
    }

    val eyePath = Path().apply {
        moveTo(center.x - eyeWidth / 2f, center.y)
        quadraticTo(
            x1 = center.x,
            y1 = center.y - eyeHeight,
            x2 = center.x + eyeWidth / 2f,
            y2 = center.y,
        )
        quadraticTo(
            x1 = center.x,
            y1 = center.y + eyeHeight,
            x2 = center.x - eyeWidth / 2f,
            y2 = center.y,
        )
        close()
    }

    drawPath(
        path = eyePath,
        color = surfaceColor,
    )
    drawPath(
        path = eyePath,
        color = outlineColor,
        style = Stroke(width = minSide * OUTLINE_STROKE_FRACTION),
    )

    val irisRadius = when (focusState) {
        EyeFocusState.Close -> minSide * IRIS_CLOSE_FRACTION
        EyeFocusState.Far -> minSide * IRIS_FAR_FRACTION
        EyeFocusState.None -> minSide * IRIS_DEFAULT_FRACTION
    }
    val pupilRadius = when (focusState) {
        EyeFocusState.Close -> irisRadius * PUPIL_CLOSE_FRACTION
        EyeFocusState.Far -> irisRadius * PUPIL_FAR_FRACTION
        EyeFocusState.None -> irisRadius * PUPIL_DEFAULT_FRACTION
    }

    drawCircle(
        color = irisColor,
        radius = irisRadius,
        center = center,
    )
    drawCircle(
        color = pupilColor,
        radius = pupilRadius,
        center = center,
    )
    drawOval(
        color = highlightColor.copy(alpha = HIGHLIGHT_ALPHA),
        topLeft = Offset(
            x = center.x - irisRadius * HIGHLIGHT_LEFT_FRACTION,
            y = center.y - irisRadius * HIGHLIGHT_TOP_FRACTION,
        ),
        size = Size(
            width = irisRadius * HIGHLIGHT_WIDTH_FRACTION,
            height = irisRadius * HIGHLIGHT_HEIGHT_FRACTION,
        ),
    )
}

private fun EyeMovement?.pathOffset(progress: Float): Offset {
    val theta = (progress.coerceIn(0f, 1f) * 2f * PI).toFloat()

    return when (this) {
        EyeMovement.CircleClockwise -> Offset(
            x = sin(theta) * PATH_AMPLITUDE,
            y = (1f - cos(theta)) * PATH_AMPLITUDE,
        )

        EyeMovement.CircleCounterclockwise -> Offset(
            x = -sin(theta) * PATH_AMPLITUDE,
            y = (1f - cos(theta)) * PATH_AMPLITUDE,
        )

        EyeMovement.EightClockwise -> Offset(
            x = sin(theta) * PATH_AMPLITUDE,
            y = -sin(theta * 2f) * EIGHT_VERTICAL_AMPLITUDE,
        )

        EyeMovement.EightCounterclockwise -> Offset(
            x = -sin(theta) * PATH_AMPLITUDE,
            y = sin(theta * 2f) * EIGHT_VERTICAL_AMPLITUDE,
        )

        else -> Offset.Zero
    }
}

private fun EyeMovement.pathDuration(): Int =
    when (this) {
        EyeMovement.CircleClockwise,
        EyeMovement.CircleCounterclockwise,
            -> CIRCLE_DURATION_MS

        EyeMovement.EightClockwise,
        EyeMovement.EightCounterclockwise,
            -> EIGHT_DURATION_MS

        else -> DIAGONAL_DURATION_MS
    }

private enum class EyeFocusState {
    None,
    Close,
    Far,
}

private val BlinklyEyeRestState.openness: Float
    get() =
        when (this) {
            BlinklyEyeRestState.Open -> OPEN_EYE
            BlinklyEyeRestState.Closed -> CLOSED_EYE
        }

@Preview
@Composable
private fun BlinklyEyeOpenPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.None,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Preview
@Composable
private fun BlinklyEyeClosedPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.None,
            openness = CLOSED_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Preview
@Composable
private fun BlinklyEyeFocusPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.Close,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}


@Preview
@Composable
private fun BlinklyEyeFarFocusPreviewLight() {
    BlinklyWidgetPreview {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.Far,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Preview
@Composable
private fun BlinklyEyeOpenPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.None,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Preview
@Composable
private fun BlinklyEyeClosedPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.None,
            openness = CLOSED_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Preview
@Composable
private fun BlinklyEyeFocusPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.Close,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}


@Preview
@Composable
private fun BlinklyEyeFarFocusPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        BlinklyEyePreviewContent(
            focusState = EyeFocusState.Far,
            openness = OPEN_EYE,
            movementOffset = Offset.Zero,
        )
    }
}

@Composable
private fun BlinklyEyePreviewContent(
    focusState: EyeFocusState,
    openness: Float,
    movementOffset: Offset,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(all = 16.dp),
    ) {
        BlinklyEyeContent(
            focusState = focusState,
            openness = openness,
            movementOffset = movementOffset,
            modifier = Modifier.size(240.dp),
        )
    }
}

private const val OPEN_EYE = 1f
private const val CLOSED_EYE = 0f
private const val CLOSED_THRESHOLD = 0.08f
private const val MAX_OFFSET = 1f
private const val DIAGONAL_STEP = 0.7f
private const val PATH_AMPLITUDE = 0.7f
private const val EIGHT_VERTICAL_AMPLITUDE = 0.42f
private const val OFFSET_SCALE = 0.19f
private const val EYE_CENTER_Y_FRACTION = 0.46f
private const val EYE_WIDTH_FRACTION = 0.58f
private const val EYE_HEIGHT_FRACTION = 0.17f
private const val OUTLINE_STROKE_FRACTION = 0.018f
private const val CLOSED_STROKE_FRACTION = 0.022f
private const val IRIS_DEFAULT_FRACTION = 0.09f
private const val IRIS_CLOSE_FRACTION = 0.115f
private const val IRIS_FAR_FRACTION = 0.07f
private const val PUPIL_DEFAULT_FRACTION = 0.48f
private const val PUPIL_CLOSE_FRACTION = 0.58f
private const val PUPIL_FAR_FRACTION = 0.38f
private const val HIGHLIGHT_ALPHA = 0.86f
private const val HIGHLIGHT_LEFT_FRACTION = 0.35f
private const val HIGHLIGHT_TOP_FRACTION = 0.55f
private const val HIGHLIGHT_WIDTH_FRACTION = 0.42f
private const val HIGHLIGHT_HEIGHT_FRACTION = 0.32f
private const val BLINK_CLOSE_DURATION_MS = 90
private const val BLINK_OPEN_DURATION_MS = 160
private const val EYE_OPEN_DURATION_MS = 180
private const val DIAGONAL_DURATION_MS = 1_000
private const val CIRCLE_DURATION_MS = 2_000
private const val EIGHT_DURATION_MS = 4_000
