@file:Suppress("LongMethod", "MagicNumber")

package com.sedsoftware.blinkly.compose.ui.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.block_a_desc
import blinkly.shared.compose.generated.resources.block_a_title
import blinkly.shared.compose.generated.resources.block_b_desc
import blinkly.shared.compose.generated.resources.block_b_title
import blinkly.shared.compose.generated.resources.block_c_desc
import blinkly.shared.compose.generated.resources.block_c_title
import blinkly.shared.compose.generated.resources.content_description_back
import blinkly.shared.compose.generated.resources.exercise_blink_break
import blinkly.shared.compose.generated.resources.exercise_blink_break_desc
import blinkly.shared.compose.generated.resources.exercise_clock_rolls
import blinkly.shared.compose.generated.resources.exercise_clock_rolls_desc
import blinkly.shared.compose.generated.resources.exercise_diagonal_gazes
import blinkly.shared.compose.generated.resources.exercise_diagonal_gazes_desc
import blinkly.shared.compose.generated.resources.exercise_figure_eight
import blinkly.shared.compose.generated.resources.exercise_figure_eight_desc
import blinkly.shared.compose.generated.resources.exercise_near_far_focus
import blinkly.shared.compose.generated.resources.exercise_near_far_focus_desc
import blinkly.shared.compose.generated.resources.exercise_palming
import blinkly.shared.compose.generated.resources.exercise_palming_desc
import blinkly.shared.compose.generated.resources.exercise_twenty_x3
import blinkly.shared.compose.generated.resources.exercise_twenty_x3_desc
import blinkly.shared.compose.generated.resources.icon_back
import blinkly.shared.compose.generated.resources.icon_done
import blinkly.shared.compose.generated.resources.workout_completed_desc
import blinkly.shared.compose.generated.resources.workout_completed_title
import blinkly.shared.compose.generated.resources.workout_exercise_counter
import blinkly.shared.compose.generated.resources.workout_finish
import blinkly.shared.compose.generated.resources.workout_intro_label
import blinkly.shared.compose.generated.resources.workout_pause
import blinkly.shared.compose.generated.resources.workout_paused_label
import blinkly.shared.compose.generated.resources.workout_progress_label
import blinkly.shared.compose.generated.resources.workout_ready_label
import blinkly.shared.compose.generated.resources.workout_resume
import blinkly.shared.compose.generated.resources.workout_running_label
import blinkly.shared.compose.generated.resources.workout_start_block
import blinkly.shared.compose.generated.resources.workout_start_exercise
import blinkly.shared.compose.generated.resources.workout_timer_label
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.component.workout.integration.WorkoutComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyAppCard
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyEyePanel
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyEyeRestState
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySpacing
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkoutContent(
    component: WorkoutComponent,
    modifier: Modifier = Modifier,
) {
    val model: WorkoutComponent.Model by component.model.subscribeAsState()

    WorkoutScreen(
        model = model,
        onBackClick = component::onBackClick,
        onStartClick = component::onStartClick,
        onPauseClick = component::onPauseClick,
        onResumeClick = component::onResumeClick,
        onFinishClick = component::onFinishClick,
        modifier = modifier,
    )
}

@Composable
private fun WorkoutScreen(
    model: WorkoutComponent.Model,
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.SectionGap),
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = BlinklySpacing.ScreenHorizontal, vertical = 16.dp),
    ) {
        WorkoutTopBar(onBackClick = onBackClick)

        when (model.phase) {
            WorkoutComponent.Phase.INTRO -> WorkoutIntro(
                model = model,
                onStartClick = onStartClick,
            )

            WorkoutComponent.Phase.READY,
            WorkoutComponent.Phase.RUNNING,
            WorkoutComponent.Phase.PAUSED,
                -> WorkoutExercise(
                model = model,
                onStartClick = onStartClick,
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
            )

            WorkoutComponent.Phase.COMPLETED -> WorkoutCompleted(
                block = model.block,
                onFinishClick = onFinishClick,
            )
        }
    }
}

@Composable
private fun WorkoutTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(resource = Res.drawable.icon_back),
                contentDescription = stringResource(resource = Res.string.content_description_back),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun WorkoutIntro(
    model: WorkoutComponent.Model,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = model.block.blockCopy()

    Column(
        verticalArrangement = Arrangement.spacedBy(space = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        StatusPill(
            text = stringResource(resource = Res.string.workout_intro_label),
            color = model.block.blockAccentColor(),
        )

        Text(
            text = copy.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = copy.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        ExerciseQueue(
            exercises = model.exercises,
            currentExercise = model.currentExercise,
        )

        BlinklyButton(
            text = stringResource(resource = Res.string.workout_start_block),
            textColor = MaterialTheme.colorScheme.onPrimary,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonHeight = 52.dp,
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WorkoutExercise(
    model: WorkoutComponent.Model,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercise = model.currentExercise ?: return
    val copy = exercise.exerciseCopy()

    Column(
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.SectionGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        ExerciseHeader(model = model)

        BlinklyEyePanel(
            movement = model.movement,
            animationTrigger = model.movementTrigger,
            restState = exercise.restState(),
            modifier = Modifier.fillMaxWidth(),
        )

        ExerciseCopyBlock(
            title = copy.title,
            description = copy.description,
        )

        if (model.timerRemainingSeconds != null || model.progress != null) {
            WorkoutMetrics(model = model)
        }

        WorkoutControls(
            phase = model.phase,
            onStartClick = onStartClick,
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick,
        )
    }
}

@Composable
private fun ExerciseHeader(
    model: WorkoutComponent.Model,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        StatusPill(
            text = model.phase.statusText(),
            color = model.block.blockAccentColor(),
        )

        Text(
            text = stringResource(
                resource = Res.string.workout_exercise_counter,
                model.currentExerciseIndex + 1,
                model.exercises.size,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ExerciseCopyBlock(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WorkoutMetrics(
    model: WorkoutComponent.Model,
    modifier: Modifier = Modifier,
) {
    BlinklyAppCard(
        contentPadding = BlinklySpacing.CompactCardPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        model.timerRemainingSeconds?.let { seconds ->
            MetricValue(
                label = stringResource(resource = Res.string.workout_timer_label),
                value = formatTimer(seconds),
            )
        }

        model.progress?.let { progress ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(resource = Res.string.workout_progress_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = formatPercent(progress.percent),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            LinearProgressIndicator(
                progress = { progress.percent.coerceIn(0, 100) / 100f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 8.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@Composable
private fun MetricValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WorkoutControls(
    phase: WorkoutComponent.Phase,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (phase) {
        WorkoutComponent.Phase.READY -> BlinklyButton(
            text = stringResource(resource = Res.string.workout_start_exercise),
            textColor = MaterialTheme.colorScheme.onPrimary,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonHeight = 52.dp,
            onClick = onStartClick,
            modifier = modifier.fillMaxWidth(),
        )

        WorkoutComponent.Phase.RUNNING -> BlinklyButton(
            text = stringResource(resource = Res.string.workout_pause),
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            buttonColor = MaterialTheme.colorScheme.secondaryContainer,
            buttonHeight = 52.dp,
            onClick = onPauseClick,
            modifier = modifier.fillMaxWidth(),
        )

        WorkoutComponent.Phase.PAUSED -> BlinklyButton(
            text = stringResource(resource = Res.string.workout_resume),
            textColor = MaterialTheme.colorScheme.onPrimary,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonHeight = 52.dp,
            onClick = onResumeClick,
            modifier = modifier.fillMaxWidth(),
        )

        WorkoutComponent.Phase.INTRO,
        WorkoutComponent.Phase.COMPLETED,
            -> Unit
    }
}

@Composable
private fun WorkoutCompleted(
    block: ExerciseBlock,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.SectionGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(height = 40.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size = 86.dp)
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .background(
                    color = block.blockAccentColor().copy(alpha = 0.18f),
                    shape = MaterialTheme.shapes.extraLarge,
                ),
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.icon_done),
                contentDescription = null,
                tint = block.blockAccentColor(),
                modifier = Modifier.size(size = 42.dp),
            )
        }

        Text(
            text = stringResource(resource = Res.string.workout_completed_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = stringResource(resource = Res.string.workout_completed_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        BlinklyButton(
            text = stringResource(resource = Res.string.workout_finish),
            textColor = MaterialTheme.colorScheme.onPrimary,
            buttonColor = MaterialTheme.colorScheme.primary,
            buttonHeight = 52.dp,
            onClick = onFinishClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ExerciseQueue(
    exercises: List<ExerciseType>,
    currentExercise: ExerciseType?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        exercises.forEachIndexed { index, exercise ->
            val selected = exercise == currentExercise
            val containerColor =
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
            val contentColor =
                if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(color = containerColor, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "${index + 1}",
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(size = 22.dp),
                )

                Text(
                    text = exercise.exerciseCopy().title,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .background(color = color.copy(alpha = 0.16f), shape = MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WorkoutComponent.Phase.statusText(): String =
    when (this) {
        WorkoutComponent.Phase.READY -> stringResource(resource = Res.string.workout_ready_label)
        WorkoutComponent.Phase.RUNNING -> stringResource(resource = Res.string.workout_running_label)
        WorkoutComponent.Phase.PAUSED -> stringResource(resource = Res.string.workout_paused_label)
        WorkoutComponent.Phase.INTRO -> stringResource(resource = Res.string.workout_intro_label)
        WorkoutComponent.Phase.COMPLETED -> stringResource(resource = Res.string.workout_completed_title)
    }

@Composable
private fun ExerciseBlock.blockCopy(): WorkoutCopy =
    when (this) {
        ExerciseBlock.A -> WorkoutCopy(
            title = stringResource(resource = Res.string.block_a_title),
            description = stringResource(resource = Res.string.block_a_desc),
        )

        ExerciseBlock.B -> WorkoutCopy(
            title = stringResource(resource = Res.string.block_b_title),
            description = stringResource(resource = Res.string.block_b_desc),
        )

        ExerciseBlock.C -> WorkoutCopy(
            title = stringResource(resource = Res.string.block_c_title),
            description = stringResource(resource = Res.string.block_c_desc),
        )
    }

@Composable
private fun ExerciseType.exerciseCopy(): WorkoutCopy =
    when (this) {
        ExerciseType.BLINK_BREAK -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_blink_break),
            description = stringResource(resource = Res.string.exercise_blink_break_desc),
        )

        ExerciseType.NEAR_FAR_FOCUS -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_near_far_focus),
            description = stringResource(resource = Res.string.exercise_near_far_focus_desc),
        )

        ExerciseType.DIAGONAL_GAZES -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_diagonal_gazes),
            description = stringResource(resource = Res.string.exercise_diagonal_gazes_desc),
        )

        ExerciseType.FIGURE_EIGHT -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_figure_eight),
            description = stringResource(resource = Res.string.exercise_figure_eight_desc),
        )

        ExerciseType.CLOCK_ROLLS -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_clock_rolls),
            description = stringResource(resource = Res.string.exercise_clock_rolls_desc),
        )

        ExerciseType.PALMING -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_palming),
            description = stringResource(resource = Res.string.exercise_palming_desc),
        )

        ExerciseType.TWENTY_X3 -> WorkoutCopy(
            title = stringResource(resource = Res.string.exercise_twenty_x3),
            description = stringResource(resource = Res.string.exercise_twenty_x3_desc),
        )
    }

@Composable
private fun ExerciseBlock.blockAccentColor(): Color =
    when (this) {
        ExerciseBlock.A -> MaterialTheme.colorScheme.primary
        ExerciseBlock.B -> MaterialTheme.colorScheme.secondary
        ExerciseBlock.C -> MaterialTheme.colorScheme.tertiary
    }

private fun ExerciseType.restState(): BlinklyEyeRestState =
    when (this) {
        ExerciseType.PALMING -> BlinklyEyeRestState.Closed
        else -> BlinklyEyeRestState.Open
    }

private fun formatTimer(totalSeconds: Int): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val minutesPart = seconds / SECONDS_IN_MINUTE
    val secondsPart = seconds % SECONDS_IN_MINUTE

    return "${minutesPart.twoDigits()}:${secondsPart.twoDigits()}"
}

private fun formatPercent(value: Int): String =
    "${value.coerceIn(0, 100)}%"

private fun Int.twoDigits(): String =
    if (this < TWO_DIGIT_THRESHOLD) {
        "0$this"
    } else {
        toString()
    }

private data class WorkoutCopy(
    val title: String,
    val description: String,
)

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutIntroPreview() {
    BlinklyWidgetPreview {
        WorkoutContent(component = WorkoutComponentPreview())
    }
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutBlinkBreakPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.A,
        exercise = ExerciseType.BLINK_BREAK,
        movement = EyeMovement.Blink(count = 3),
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutNearFarPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.A,
        exercise = ExerciseType.NEAR_FAR_FOCUS,
        movement = EyeMovement.AccommodationFar,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutDiagonalPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.A,
        exercise = ExerciseType.DIAGONAL_GAZES,
        movement = EyeMovement.DiagonalBottomRight,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutFigureEightPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.B,
        exercise = ExerciseType.FIGURE_EIGHT,
        movement = EyeMovement.EightClockwise,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutClockRollsPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.B,
        exercise = ExerciseType.CLOCK_ROLLS,
        movement = EyeMovement.CircleClockwise,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutPalmingPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.B,
        exercise = ExerciseType.PALMING,
        timerRemainingSeconds = 42,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutTwentyPreview() {
    WorkoutExercisePreview(
        block = ExerciseBlock.C,
        exercise = ExerciseType.TWENTY_X3,
        timerRemainingSeconds = 16,
    )
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun WorkoutCompletedPreview() {
    BlinklyWidgetPreview {
        WorkoutContent(
            component = WorkoutComponentPreview(
                block = ExerciseBlock.C,
                currentExercise = ExerciseType.TWENTY_X3,
                phase = WorkoutComponent.Phase.COMPLETED,
                progress = WorkoutComponent.Progress(percent = 100, remainingMs = 0L, totalMs = 20_000L),
            )
        )
    }
}

@Composable
private fun WorkoutExercisePreview(
    block: ExerciseBlock,
    exercise: ExerciseType,
    movement: EyeMovement? = null,
    timerRemainingSeconds: Int? = null,
) {
    BlinklyWidgetPreview {
        WorkoutContent(
            component = WorkoutComponentPreview(
                block = block,
                currentExercise = exercise,
                phase = WorkoutComponent.Phase.RUNNING,
                movement = movement,
                movementTrigger = 1,
                progress = WorkoutComponent.Progress(percent = 42, remainingMs = 9_000L, totalMs = 16_000L),
                timerRemainingSeconds = timerRemainingSeconds,
            )
        )
    }
}

private const val SECONDS_IN_MINUTE = 60
private const val TWO_DIGIT_THRESHOLD = 10
