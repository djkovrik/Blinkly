package com.sedsoftware.blinkly.compose.ui.newreminder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.add_reminder_day
import blinkly.shared.compose.generated.resources.add_reminder_description
import blinkly.shared.compose.generated.resources.add_reminder_error_empty_days
import blinkly.shared.compose.generated.resources.add_reminder_error_invalid_interval
import blinkly.shared.compose.generated.resources.add_reminder_error_invalid_period
import blinkly.shared.compose.generated.resources.add_reminder_interval_custom
import blinkly.shared.compose.generated.resources.add_reminder_interval_minutes
import blinkly.shared.compose.generated.resources.add_reminder_preview_daily
import blinkly.shared.compose.generated.resources.add_reminder_preview_period
import blinkly.shared.compose.generated.resources.add_reminder_preview_title
import blinkly.shared.compose.generated.resources.add_reminder_preview_weekly
import blinkly.shared.compose.generated.resources.add_reminder_save
import blinkly.shared.compose.generated.resources.add_reminder_saving
import blinkly.shared.compose.generated.resources.add_reminder_time
import blinkly.shared.compose.generated.resources.add_reminder_title
import blinkly.shared.compose.generated.resources.add_reminder_type
import blinkly.shared.compose.generated.resources.add_reminder_type_daily
import blinkly.shared.compose.generated.resources.add_reminder_type_daily_description
import blinkly.shared.compose.generated.resources.add_reminder_type_weekly_period
import blinkly.shared.compose.generated.resources.add_reminder_type_weekly_period_description
import blinkly.shared.compose.generated.resources.add_reminder_type_weekly_single
import blinkly.shared.compose.generated.resources.add_reminder_type_weekly_single_description
import blinkly.shared.compose.generated.resources.content_description_back
import blinkly.shared.compose.generated.resources.exercise_twenty_x3
import blinkly.shared.compose.generated.resources.exercise_twenty_x3_desc
import blinkly.shared.compose.generated.resources.icon_back
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_from
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_interval
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_until
import blinkly.shared.compose.generated.resources.onboarding_initial_setup_weekdays
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ScheduleType
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ValidationError
import com.sedsoftware.blinkly.component.newreminder.integration.AddNewReminderComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyOutlineButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyTimePickerDialog
import com.sedsoftware.blinkly.compose.ui.widget.WeekDayToggle
import com.sedsoftware.blinkly.domain.extension.toHumanReadableString
import com.sedsoftware.blinkly.utils.PreviewContent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val INTERVAL_MIN = 20
private const val INTERVAL_STEP_COUNT = 4

@Composable
fun AddNewReminderContent(
    component: AddNewReminderComponent,
    modifier: Modifier = Modifier,
) {
    val model: AddNewReminderComponent.Model by component.model.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val validationError = model.validationError

    if (validationError != null) {
        val message = validationError.asMessage()

        LaunchedEffect(validationError) {
            snackbarHostState.showSnackbar(message = message)
            component.onValidationMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = component::onBackClick) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.icon_back),
                            contentDescription = stringResource(resource = Res.string.content_description_back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 28.dp),
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(resource = Res.string.add_reminder_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.add_reminder_description),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyLarge,
            )

            ReminderTypeHeader()

            ScheduleTypeSection(
                selectedType = model.scheduleType,
                onTypeClick = component::onScheduleTypeSelect,
            )

            when (model.scheduleType) {
                ScheduleType.DAILY -> DailyReminderSection(
                    time = model.dailyTime,
                    onTimeSelect = component::onDailyTimeSelect,
                )

                ScheduleType.WEEKLY_SINGLE -> WeeklySingleReminderSection(
                    time = model.weeklyTime,
                    day = model.weeklyDay,
                    onTimeSelect = component::onWeeklyTimeSelect,
                    onDaySelect = component::onWeeklyDaySelect,
                )

                ScheduleType.WEEKLY_DAY_PERIOD -> WeeklyPeriodReminderSection(
                    timeFrom = model.periodTimeFrom,
                    timeUntil = model.periodTimeUntil,
                    interval = model.periodInterval,
                    selectedDays = model.periodDays,
                    onTimeFromSelect = component::onPeriodTimeFromSelect,
                    onTimeUntilSelect = component::onPeriodTimeUntilSelect,
                    onIntervalSelect = component::onPeriodIntervalSelect,
                    onDayClick = component::onPeriodDayToggle,
                )
            }

            ReminderPreview(model = model)

            BlinklyButton(
                text = if (model.isSaving) {
                    stringResource(resource = Res.string.add_reminder_saving)
                } else {
                    stringResource(resource = Res.string.add_reminder_save)
                },
                onClick = component::onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ReminderTypeHeader(
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 6.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.exercise_twenty_x3),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = stringResource(resource = Res.string.exercise_twenty_x3_desc),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ScheduleTypeSection(
    selectedType: ScheduleType,
    onTypeClick: (ScheduleType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
        modifier = modifier.selectableGroup(),
    ) {
        Text(
            text = stringResource(resource = Res.string.add_reminder_type),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            ScheduleType.entries.forEach { type ->
                ScheduleTypeChip(
                    type = type,
                    selected = selectedType == type,
                    onClick = { onTypeClick(type) },
                )
            }
        }

        Text(
            text = selectedType.asDescription(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ScheduleTypeChip(
    type: ScheduleType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
    ) {
        Text(
            text = type.asTitle(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun DailyReminderSection(
    time: LocalTime,
    onTimeSelect: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.add_reminder_time),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )

            TimeSelector(
                time = time,
                onTimeSelect = onTimeSelect,
            )
        }
    }
}

@Composable
private fun WeeklySingleReminderSection(
    time: LocalTime,
    day: DayOfWeek,
    onTimeSelect: (LocalTime) -> Unit,
    onDaySelect: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.add_reminder_time),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )

            TimeSelector(
                time = time,
                onTimeSelect = onTimeSelect,
            )

            Text(
                text = stringResource(resource = Res.string.add_reminder_day),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            ) {
                DayOfWeek.entries.forEach { dayOfWeek ->
                    WeekDayToggle(
                        dayOfWeek = dayOfWeek,
                        toggled = day == dayOfWeek,
                        onToggle = { onDaySelect(dayOfWeek) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyPeriodReminderSection(
    timeFrom: LocalTime,
    timeUntil: LocalTime,
    interval: Int,
    selectedDays: List<DayOfWeek>,
    onTimeFromSelect: (LocalTime) -> Unit,
    onTimeUntilSelect: (LocalTime) -> Unit,
    onIntervalSelect: (Int) -> Unit,
    onDayClick: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TimeSelector(
                    time = timeFrom,
                    prefix = stringResource(resource = Res.string.onboarding_initial_setup_from),
                    onTimeSelect = onTimeFromSelect,
                    modifier = Modifier.weight(weight = 1f),
                )

                TimeSelector(
                    time = timeUntil,
                    prefix = stringResource(resource = Res.string.onboarding_initial_setup_until),
                    onTimeSelect = onTimeUntilSelect,
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            Text(
                text = stringResource(resource = Res.string.onboarding_initial_setup_interval),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )

            IntervalSelector(
                interval = interval,
                onIntervalSelect = onIntervalSelect,
            )

            Text(
                text = stringResource(resource = Res.string.onboarding_initial_setup_weekdays),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            ) {
                DayOfWeek.entries.forEach { dayOfWeek ->
                    WeekDayToggle(
                        dayOfWeek = dayOfWeek,
                        toggled = selectedDays.contains(dayOfWeek),
                        onToggle = { onDayClick(dayOfWeek) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSelector(
    time: LocalTime,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    onTimeSelect: (LocalTime) -> Unit,
) {
    val timePickerState: TimePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true,
    )
    var pickerVisible: Boolean by remember { mutableStateOf(false) }

    if (pickerVisible) {
        BlinklyTimePickerDialog(
            timePickerState = timePickerState,
            onConfirm = {
                onTimeSelect(LocalTime(timePickerState.hour, timePickerState.minute))
                pickerVisible = false
            },
            onDismiss = {
                pickerVisible = false
            },
        )
    }

    BlinklyOutlineButton(
        text = listOfNotNull(prefix, time.toHumanReadableString()).joinToString(separator = " "),
        buttonShape = MaterialTheme.shapes.small,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { pickerVisible = true },
        modifier = modifier,
    )
}

@Composable
private fun IntervalSelector(
    interval: Int,
    onIntervalSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customIntervalVisible: Boolean by remember { mutableStateOf(interval != INTERVAL_MIN && interval != INTERVAL_MIN * 2) }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            IntervalOption(
                text = stringResource(resource = Res.string.add_reminder_interval_minutes, INTERVAL_MIN),
                selected = interval == INTERVAL_MIN && !customIntervalVisible,
                onClick = {
                    customIntervalVisible = false
                    onIntervalSelect(INTERVAL_MIN)
                },
                modifier = Modifier.weight(weight = 1f),
            )

            IntervalOption(
                text = stringResource(resource = Res.string.add_reminder_interval_minutes, INTERVAL_MIN * 2),
                selected = interval == INTERVAL_MIN * 2 && !customIntervalVisible,
                onClick = {
                    customIntervalVisible = false
                    onIntervalSelect(INTERVAL_MIN * 2)
                },
                modifier = Modifier.weight(weight = 1f),
            )

            IntervalOption(
                text = stringResource(resource = Res.string.add_reminder_interval_custom),
                selected = customIntervalVisible,
                onClick = { customIntervalVisible = true },
                modifier = Modifier.weight(weight = 1f),
            )
        }

        AnimatedVisibility(visible = customIntervalVisible) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Slider(
                    value = interval.toFloat(),
                    onValueChange = { onIntervalSelect(it.toInt()) },
                    valueRange = INTERVAL_MIN.toFloat()..(INTERVAL_MIN * (INTERVAL_STEP_COUNT + 2)).toFloat(),
                    steps = INTERVAL_STEP_COUNT,
                    modifier = Modifier.weight(weight = 1f),
                )

                Text(
                    text = stringResource(resource = Res.string.add_reminder_interval_minutes, interval),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun IntervalOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.RadioButton,
        ),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ReminderPreview(
    model: AddNewReminderComponent.Model,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.add_reminder_preview_title),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = model.previewText(),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ScheduleType.asTitle(): String =
    when (this) {
        ScheduleType.DAILY -> stringResource(resource = Res.string.add_reminder_type_daily)
        ScheduleType.WEEKLY_SINGLE -> stringResource(resource = Res.string.add_reminder_type_weekly_single)
        ScheduleType.WEEKLY_DAY_PERIOD -> stringResource(resource = Res.string.add_reminder_type_weekly_period)
    }

@Composable
private fun ScheduleType.asDescription(): String =
    when (this) {
        ScheduleType.DAILY -> stringResource(resource = Res.string.add_reminder_type_daily_description)
        ScheduleType.WEEKLY_SINGLE -> stringResource(resource = Res.string.add_reminder_type_weekly_single_description)
        ScheduleType.WEEKLY_DAY_PERIOD -> stringResource(resource = Res.string.add_reminder_type_weekly_period_description)
    }

@Composable
private fun AddNewReminderComponent.Model.previewText(): String =
    when (scheduleType) {
        ScheduleType.DAILY -> stringResource(
            resource = Res.string.add_reminder_preview_daily,
            dailyTime.toHumanReadableString(),
        )

        ScheduleType.WEEKLY_SINGLE -> stringResource(
            resource = Res.string.add_reminder_preview_weekly,
            weeklyDay.asLabel(),
            weeklyTime.toHumanReadableString(),
        )

        ScheduleType.WEEKLY_DAY_PERIOD -> stringResource(
            resource = Res.string.add_reminder_preview_period,
            periodDays.asDaysLabel(),
            periodTimeFrom.toHumanReadableString(),
            periodTimeUntil.toHumanReadableString(),
            periodInterval,
        )
    }

@Composable
private fun List<DayOfWeek>.asDaysLabel(): String {
    if (isEmpty()) {
        return stringResource(resource = Res.string.add_reminder_error_empty_days)
    }

    val labels = mutableListOf<String>()
    forEach { day ->
        labels.add(day.asLabel())
    }

    return labels.joinToString(separator = ", ")
}

@Composable
private fun ValidationError.asMessage(): String =
    when (this) {
        ValidationError.EMPTY_DAYS -> stringResource(resource = Res.string.add_reminder_error_empty_days)
        ValidationError.INVALID_PERIOD -> stringResource(resource = Res.string.add_reminder_error_invalid_period)
        ValidationError.INVALID_INTERVAL -> stringResource(resource = Res.string.add_reminder_error_invalid_interval)
    }

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun AddNewReminderContentPreviewLight() {
    BlinklyWidgetPreview {
        AddNewReminderPreviewContent()
    }
}

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun AddNewReminderContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        AddNewReminderPreviewContent()
    }
}

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun AddNewReminderContentDailyPreviewLight() {
    BlinklyWidgetPreview {
        AddNewReminderContent(
            component = AddNewReminderComponentPreview(scheduleType = ScheduleType.DAILY)
        )
    }
}

@Composable
@PreviewContent
private fun AddNewReminderPreviewContent() {
    AddNewReminderContent(
        component = AddNewReminderComponentPreview(
            scheduleType = ScheduleType.WEEKLY_DAY_PERIOD,
            periodTimeFrom = LocalTime(hour = 9, minute = 0),
            periodTimeUntil = LocalTime(hour = 18, minute = 0),
            periodInterval = 40,
            periodDays = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            ),
        )
    )
}
