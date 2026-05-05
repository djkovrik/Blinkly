package com.sedsoftware.blinkly.compose.ui.newreminder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_20
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_40
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_clear
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_create
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_created
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_custom
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_from
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_interval
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_label
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_until
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_weekdays
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyOutlineButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyTimePickerDialog
import com.sedsoftware.blinkly.compose.ui.widget.WeekDayToggle
import com.sedsoftware.blinkly.domain.extension.toHumanReadableString
import com.sedsoftware.blinkly.utils.PreviewContent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

private const val SLIDER_STEP_VALUE = 20
private const val SLIDER_TOTAL_STEPS = 4

@Composable
fun AddWeeklyPeriodContent(
    selectedTimeFrom: LocalTime,
    selectedTimeUntil: LocalTime,
    selectedDays: List<DayOfWeek>,
    selectedInterval: Int,
    createdRemindersCount: Int,
    modifier: Modifier = Modifier,
    onTimeFromSelect: (LocalTime) -> Unit = {},
    onTimeUntilSelect: (LocalTime) -> Unit = {},
    onIntervalSelect: (Int) -> Unit = {},
    onDayClick: (DayOfWeek) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
) {
    val timePickerStateFrom: TimePickerState = rememberTimePickerState(
        initialHour = selectedTimeFrom.hour,
        initialMinute = selectedTimeFrom.minute,
        is24Hour = true, // TODO 24hr format hardcoded, fix later?
    )

    val defaultIntervals: Set<Int> = setOf(
        SLIDER_STEP_VALUE,
        SLIDER_STEP_VALUE * 2,
    )

    var timePickerFromVisible: Boolean by remember { mutableStateOf(false) }

    val timePickerStateUntil: TimePickerState = rememberTimePickerState(
        initialHour = selectedTimeUntil.hour,
        initialMinute = selectedTimeUntil.minute,
        is24Hour = true,
    )

    var timePickerUntilVisible: Boolean by remember { mutableStateOf(false) }

    var showSlider: Boolean by remember { mutableStateOf(!defaultIntervals.contains(selectedInterval)) }

    val periodOptions: List<String> = listOf(
        stringResource(Res.string.onboarding_initial_setup_20),
        stringResource(Res.string.onboarding_initial_setup_40),
        stringResource(Res.string.onboarding_initial_setup_custom),
    )

    if (timePickerFromVisible) {
        BlinklyTimePickerDialog(
            timePickerState = timePickerStateFrom,
            onConfirm = {
                val selectedTime = LocalTime(timePickerStateFrom.hour, timePickerStateFrom.minute)
                onTimeFromSelect.invoke(selectedTime)
                timePickerFromVisible = false
            },
            onDismiss = {
                timePickerFromVisible = false
            },
        )
    }

    if (timePickerUntilVisible) {
        BlinklyTimePickerDialog(
            timePickerState = timePickerStateUntil,
            onConfirm = {
                val selectedTime = LocalTime(timePickerStateUntil.hour, timePickerStateUntil.minute)
                onTimeUntilSelect.invoke(selectedTime)
                timePickerUntilVisible = false
            },
            onDismiss = {
                timePickerUntilVisible = false
            },
        )
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(resource = Res.string.onboarding_initial_setup_label),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            BlinklyOutlineButton(
                text = "${stringResource(resource = Res.string.onboarding_initial_setup_from)} ${selectedTimeFrom.toHumanReadableString()}",
                buttonShape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    timePickerFromVisible = true
                }
            )

            Text(
                text = "->",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            BlinklyOutlineButton(
                text = "${stringResource(
                    resource = Res.string.onboarding_initial_setup_until)
                } ${selectedTimeUntil.toHumanReadableString()}",
                buttonShape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    timePickerUntilVisible = true
                }
            )
        }

        Text(
            text = stringResource(resource = Res.string.onboarding_initial_setup_interval),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(modifier = Modifier.selectableGroup()) {
            periodOptions.forEachIndexed { index, text ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 32.dp)
                        .selectable(
                            selected = when (index) {
                                0 -> selectedInterval == SLIDER_STEP_VALUE
                                1 -> selectedInterval == SLIDER_STEP_VALUE * 2
                                else -> !defaultIntervals.contains(selectedInterval)
                            },
                            onClick = {
                                when(index) {
                                    0 -> onIntervalSelect.invoke(SLIDER_STEP_VALUE)
                                    1 -> onIntervalSelect.invoke(SLIDER_STEP_VALUE * 2)
                                }

                                showSlider = index != 0 && index != 1
                            },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = when (index) {
                            0 -> selectedInterval == SLIDER_STEP_VALUE
                            1 -> selectedInterval == SLIDER_STEP_VALUE * 2
                            else -> !defaultIntervals.contains(selectedInterval)
                        },
                        onClick = null,
                    )
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSlider
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = selectedInterval.toFloat(),
                    onValueChange = { onIntervalSelect.invoke(it.toInt()) },
                    valueRange = (SLIDER_STEP_VALUE.toFloat()..(SLIDER_STEP_VALUE * (SLIDER_TOTAL_STEPS + 2)).toFloat()),
                    steps = SLIDER_TOTAL_STEPS,
                    modifier = Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = "$selectedInterval",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Text(
            text = stringResource(resource = Res.string.onboarding_initial_setup_weekdays),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        FlowRow {
            DayOfWeek.entries.forEach { dayOfWeek ->
                WeekDayToggle(
                    dayOfWeek = dayOfWeek,
                    toggled = selectedDays.contains(dayOfWeek),
                    onToggle = { onDayClick.invoke(dayOfWeek) },
                    modifier = Modifier.padding(all = 6.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
        ) {
            BlinklyOutlineButton(
                text = if (createdRemindersCount != 0) {
                    "${stringResource(resource = Res.string.onboarding_initial_setup_created)} $createdRemindersCount"
                } else {
                    stringResource(resource = Res.string.onboarding_initial_setup_create)
                },
                onClick = onCreateClick,
                modifier = Modifier.padding(horizontal = 8.dp),
                enabled = createdRemindersCount == 0,
            )

            BlinklyOutlineButton(
                text = stringResource(resource = Res.string.onboarding_initial_setup_clear),
                onClick = onClearClick,
                modifier = Modifier.padding(horizontal = 8.dp),
                enabled = createdRemindersCount != 0,
            )
        }
    }
}

@Preview(heightDp = 1600)
@Composable
private fun AddWeeklyPeriodContentPreviewLight() {
    BlinklyWidgetPreview {
        AddWeeklyPeriodContentPreviewContent()
    }
}

@Preview(heightDp = 1600)
@Composable
private fun AddWeeklyPeriodContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        AddWeeklyPeriodContentPreviewContent()
    }
}

@Composable
@PreviewContent
private fun AddWeeklyPeriodContentPreviewContent() {
    Column {
        AddWeeklyPeriodContent(
            selectedTimeFrom = LocalTime(10, 0),
            selectedTimeUntil = LocalTime(18, 0),
            selectedDays = DayOfWeek.entries.toList(),
            selectedInterval = SLIDER_STEP_VALUE,
            createdRemindersCount = 0,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        AddWeeklyPeriodContent(
            selectedTimeFrom = LocalTime(14, 0),
            selectedTimeUntil = LocalTime(16, 0),
            selectedDays = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
            ),
            selectedInterval = SLIDER_STEP_VALUE * 2,
            createdRemindersCount = 2,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        AddWeeklyPeriodContent(
            selectedTimeFrom = LocalTime(11, 0),
            selectedTimeUntil = LocalTime(13, 0),
            selectedDays = listOf(DayOfWeek.SUNDAY),
            selectedInterval = SLIDER_STEP_VALUE * 3,
            createdRemindersCount = 99,
        )
    }
}
