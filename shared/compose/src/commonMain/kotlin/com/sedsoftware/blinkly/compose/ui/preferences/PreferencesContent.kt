package com.sedsoftware.blinkly.compose.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_back
import blinkly.shared.compose.generated.resources.content_description_decrease
import blinkly.shared.compose.generated.resources.content_description_increase
import blinkly.shared.compose.generated.resources.exercise_blink_break
import blinkly.shared.compose.generated.resources.exercise_clock_rolls
import blinkly.shared.compose.generated.resources.exercise_diagonal_gazes
import blinkly.shared.compose.generated.resources.exercise_figure_eight
import blinkly.shared.compose.generated.resources.exercise_near_far_focus
import blinkly.shared.compose.generated.resources.exercise_palming
import blinkly.shared.compose.generated.resources.icon_minus
import blinkly.shared.compose.generated.resources.icon_plus
import blinkly.shared.compose.generated.resources.icon_back
import blinkly.shared.compose.generated.resources.preferences_count_label
import blinkly.shared.compose.generated.resources.preferences_duration_label
import blinkly.shared.compose.generated.resources.preferences_exercises_title
import blinkly.shared.compose.generated.resources.preferences_theme_dark
import blinkly.shared.compose.generated.resources.preferences_theme_light
import blinkly.shared.compose.generated.resources.preferences_theme_system
import blinkly.shared.compose.generated.resources.preferences_theme_title
import blinkly.shared.compose.generated.resources.preferences_title
import blinkly.shared.compose.generated.resources.preferences_unit_repetitions
import blinkly.shared.compose.generated.resources.preferences_unit_seconds
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent
import com.sedsoftware.blinkly.component.preferences.integration.PreferencesComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.domain.model.ThemeState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val MIN_INT_VALUE = 10
private const val PALMING_STEP = 5
private const val MIN_FLOAT_VALUE = 10f

@Composable
fun PreferencesContent(
    component: PreferencesComponent,
    modifier: Modifier = Modifier,
) {
    val model: PreferencesComponent.Model by component.model.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.preferences_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = component::onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.icon_back),
                            contentDescription = stringResource(resource = Res.string.content_description_back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 24.dp)
                        )
                    }
                },
            )
        },
        modifier = modifier.systemBarsPadding(),
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ThemeSelector(
                selectedTheme = model.themeState,
                onThemeSelected = component::onThemeStateChanged,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(resource = Res.string.preferences_exercises_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_blink_break),
                subtitle = stringResource(resource = Res.string.preferences_count_label),
                value = model.blinkBreakCount.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_repetitions),
                onDecrease = { component.onBlinkBreakCountChanged((model.blinkBreakCount - 1).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onBlinkBreakCountChanged(model.blinkBreakCount + 1) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_near_far_focus),
                subtitle = stringResource(resource = Res.string.preferences_count_label),
                value = model.nearFarFocusCount.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_repetitions),
                onDecrease = { component.onNearFarFocusCountChanged((model.nearFarFocusCount - 1).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onNearFarFocusCountChanged(model.nearFarFocusCount + 1) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_near_far_focus),
                subtitle = stringResource(resource = Res.string.preferences_duration_label),
                value = model.nearFarFocusDuration.asDisplayValue(),
                unit = stringResource(resource = Res.string.preferences_unit_seconds),
                onDecrease = {
                    component.onNearFarFocusDurationChanged((model.nearFarFocusDuration - 1f).coerceAtLeast(MIN_FLOAT_VALUE))
                },
                onIncrease = { component.onNearFarFocusDurationChanged(model.nearFarFocusDuration + 1f) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_diagonal_gazes),
                subtitle = stringResource(resource = Res.string.preferences_count_label),
                value = model.diagonalGazesCount.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_repetitions),
                onDecrease = { component.onDiagonalGazesCountChanged((model.diagonalGazesCount - 1).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onDiagonalGazesCountChanged(model.diagonalGazesCount + 1) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_diagonal_gazes),
                subtitle = stringResource(resource = Res.string.preferences_duration_label),
                value = model.diagonalGazesDuration.asDisplayValue(),
                unit = stringResource(resource = Res.string.preferences_unit_seconds),
                onDecrease = {
                    component.onDiagonalGazesDurationChanged((model.diagonalGazesDuration - 1f).coerceAtLeast(MIN_FLOAT_VALUE))
                },
                onIncrease = { component.onDiagonalGazesDurationChanged(model.diagonalGazesDuration + 1f) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_figure_eight),
                subtitle = stringResource(resource = Res.string.preferences_count_label),
                value = model.figureEightCount.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_repetitions),
                onDecrease = { component.onFigureEightCountChanged((model.figureEightCount - 1).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onFigureEightCountChanged(model.figureEightCount + 1) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_clock_rolls),
                subtitle = stringResource(resource = Res.string.preferences_count_label),
                value = model.clockRollsEachSide.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_repetitions),
                onDecrease = { component.onClockRollsEachSideChanged((model.clockRollsEachSide - 1).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onClockRollsEachSideChanged(model.clockRollsEachSide + 1) },
            )

            PreferenceStepper(
                title = stringResource(resource = Res.string.exercise_palming),
                subtitle = stringResource(resource = Res.string.preferences_duration_label),
                value = model.palmingDuration.toString(),
                unit = stringResource(resource = Res.string.preferences_unit_seconds),
                onDecrease = { component.onPalmingDurationChanged((model.palmingDuration - PALMING_STEP).coerceAtLeast(MIN_INT_VALUE)) },
                onIncrease = { component.onPalmingDurationChanged(model.palmingDuration + PALMING_STEP) },
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: ThemeState,
    onThemeSelected: (ThemeState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(resource = Res.string.preferences_theme_title),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                )
                .selectableGroup(),
        ) {
            ThemeSegment(
                title = stringResource(resource = Res.string.preferences_theme_system),
                selected = selectedTheme == ThemeState.SYSTEM,
                onClick = { onThemeSelected(ThemeState.SYSTEM) },
                modifier = Modifier.weight(weight = 1f),
            )
            ThemeSegment(
                title = stringResource(resource = Res.string.preferences_theme_light),
                selected = selectedTheme == ThemeState.LIGHT,
                onClick = { onThemeSelected(ThemeState.LIGHT) },
                modifier = Modifier.weight(weight = 1f),
            )
            ThemeSegment(
                title = stringResource(resource = Res.string.preferences_theme_dark),
                selected = selectedTheme == ThemeState.DARK,
                onClick = { onThemeSelected(ThemeState.DARK) },
                modifier = Modifier.weight(weight = 1f),
            )
        }
    }
}

@Composable
private fun ThemeSegment(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreferenceStepper(
    title: String,
    subtitle: String,
    value: String,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                modifier = Modifier.weight(weight = 1f),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )

                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            StepperButton(
                iconRes = Res.drawable.icon_minus,
                contentDescription = stringResource(resource = Res.string.content_description_decrease),
                onClick = onDecrease,
            )

            StepperButton(
                iconRes = Res.drawable.icon_plus,
                contentDescription = stringResource(resource = Res.string.content_description_increase),
                onClick = onIncrease,
            )
        }
    }
}

@Composable
private fun StepperButton(
    iconRes: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(size = 40.dp),
    ) {
        Icon(
            painter = painterResource(resource = iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size = 18.dp),
        )
    }
}

private fun Float.asDisplayValue(): String {
    val rounded = (this * 10).roundToInt() / 10f
    return if (rounded % 1f == 0f) {
        rounded.roundToInt().toString()
    } else {
        rounded.toString()
    }
}

@Preview(widthDp = 420, heightDp = 1100)
@Composable
private fun PreferencesContentPreviewLight() {
    BlinklyWidgetPreview {
        PreferencesContent(component = PreferencesComponentPreview())
    }
}

@Preview(widthDp = 420, heightDp = 1100)
@Composable
private fun PreferencesContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        PreferencesContent(component = PreferencesComponentPreview(themeState = ThemeState.DARK))
    }
}
