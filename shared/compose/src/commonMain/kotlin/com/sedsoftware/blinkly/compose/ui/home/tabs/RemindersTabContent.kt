package com.sedsoftware.blinkly.compose.ui.home.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.content_description_delete
import blinkly.shared.compose.generated.resources.exercise_twenty_x3
import blinkly.shared.compose.generated.resources.icon_delete
import blinkly.shared.compose.generated.resources.icon_plus
import blinkly.shared.compose.generated.resources.reminders_add
import blinkly.shared.compose.generated.resources.reminders_daily
import blinkly.shared.compose.generated.resources.reminders_days_every_day
import blinkly.shared.compose.generated.resources.reminders_deleted
import blinkly.shared.compose.generated.resources.reminders_empty_cta
import blinkly.shared.compose.generated.resources.reminders_empty_description
import blinkly.shared.compose.generated.resources.reminders_empty_title
import blinkly.shared.compose.generated.resources.reminders_next
import blinkly.shared.compose.generated.resources.reminders_overview_count
import blinkly.shared.compose.generated.resources.reminders_overview_hint
import blinkly.shared.compose.generated.resources.reminders_overview_next
import blinkly.shared.compose.generated.resources.reminders_overview_title
import blinkly.shared.compose.generated.resources.reminders_title
import blinkly.shared.compose.generated.resources.reminders_undo
import blinkly.shared.compose.generated.resources.reminders_weekly
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Interval
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.ReminderItem
import com.sedsoftware.blinkly.component.reminders.integration.RemindersTabComponentPreview
import com.sedsoftware.blinkly.compose.theme.BlinklyWidgetPreview
import com.sedsoftware.blinkly.compose.ui.extension.asLabel
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyAppCard
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyButton
import com.sedsoftware.blinkly.compose.ui.widget.BlinklyMetricRow
import com.sedsoftware.blinkly.compose.ui.widget.BlinklySpacing
import com.sedsoftware.blinkly.domain.extension.toHumanReadableString
import com.sedsoftware.blinkly.utils.PreviewContent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MAX_INLINE_REMINDERS_WITH_ADD_BUTTON = 4

@Composable
fun RemindersTabContent(
    component: RemindersTabComponent,
    modifier: Modifier = Modifier,
) {
    val model: RemindersTabComponent.Model by component.model.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedReminder = model.deletedReminder
    val deletedMessage = stringResource(resource = Res.string.reminders_deleted)
    val undoLabel = stringResource(resource = Res.string.reminders_undo)

    LaunchedEffect(deletedReminder?.uuid) {
        if (deletedReminder != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                withDismissAction = true,
            )

            if (result == SnackbarResult.ActionPerformed) {
                component.onUndoDelete()
            } else {
                component.onDeletedMessageShown()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.reminders_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (model.reminders.size > 4) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = stringResource(resource = Res.string.reminders_add))
                    },
                    icon = {
                        Icon(
                            painter = painterResource(resource = Res.drawable.icon_plus),
                            contentDescription = null,
                        )
                    },
                    onClick = component::onAddNewClick,
                )
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
        ) {
            if (model.reminders.isEmpty()) {
                EmptyReminders(
                    onCreateClick = component::onAddNewClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(all = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(space = BlinklySpacing.ItemGap),
                    contentPadding = PaddingValues(
                        start = BlinklySpacing.ScreenHorizontal,
                        top = BlinklySpacing.ScreenVertical,
                        end = BlinklySpacing.ScreenHorizontal,
                        bottom = 24.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        RemindersOverview(
                            reminders = model.reminders,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    items(
                        items = model.reminders,
                        key = { item -> item.uuid },
                    ) { item ->
                        ReminderCard(
                            item = item,
                            onDeleteClick = { component.onDeleteReminder(item.uuid) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (model.reminders.size <= MAX_INLINE_REMINDERS_WITH_ADD_BUTTON) {
                        item {
                            BlinklyButton(
                                text = stringResource(resource = Res.string.reminders_add),
                                onClick = component::onAddNewClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReminders(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
        ) {
            Text(
                text = stringResource(resource = Res.string.exercise_twenty_x3),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        Text(
            text = stringResource(resource = Res.string.reminders_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(resource = Res.string.reminders_empty_description),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        BlinklyButton(
            text = stringResource(resource = Res.string.reminders_empty_cta),
            onClick = onCreateClick,
        )
    }
}

@Composable
private fun RemindersOverview(
    reminders: List<ReminderItem>,
    modifier: Modifier = Modifier,
) {
    val nextReminder = reminders.minWithOrNull(
        compareBy<ReminderItem> { item -> item.nextDate }
            .thenBy { item -> item.time }
    )

    BlinklyAppCard(
        highlighted = true,
        contentPadding = BlinklySpacing.CompactCardPadding,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(resource = Res.string.reminders_overview_title),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        BlinklyMetricRow(
            label = stringResource(resource = Res.string.reminders_overview_count, reminders.size),
            value = nextReminder?.time?.toHumanReadableString().orEmpty(),
            valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        if (nextReminder != null) {
            Text(
                text = stringResource(
                    resource = Res.string.reminders_overview_next,
                    nextReminder.time.toHumanReadableString(),
                    nextReminder.nextDate.asShortDate(),
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text(
            text = stringResource(resource = Res.string.reminders_overview_hint),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReminderCard(
    item: ReminderItem,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BlinklyAppCard(
        contentPadding = BlinklySpacing.CompactCardPadding,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.widthIn(min = 72.dp, max = 88.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 4.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.primary),
                )

                Text(
                    text = item.time.toHumanReadableString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = false,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    text = item.interval.intervalLabel(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                modifier = Modifier.weight(weight = 1f),
            ) {
                Text(
                    text = stringResource(resource = Res.string.exercise_twenty_x3),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 6.dp),
                ) {
                    ReminderPill(text = item.daysLabel())
                    ReminderPill(text = "${stringResource(resource = Res.string.reminders_next)} ${item.nextDate.asShortDate()}")
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.36f),
                contentColor = MaterialTheme.colorScheme.error,
                shape = CircleShape,
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(size = 48.dp),
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.icon_delete),
                        contentDescription = stringResource(resource = Res.string.content_description_delete),
                        modifier = Modifier.size(size = 22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        ),
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun Interval.intervalLabel(): String =
    when (this) {
        Interval.DAILY -> stringResource(resource = Res.string.reminders_daily)
        Interval.WEEKLY -> stringResource(resource = Res.string.reminders_weekly)
    }

@Composable
private fun ReminderItem.daysLabel(): String {
    if (interval == Interval.DAILY || days.isEmpty()) {
        return stringResource(resource = Res.string.reminders_days_every_day)
    }

    val labels = mutableListOf<String>()
    days.forEach { day ->
        labels.add(day.asLabel())
    }

    return labels.joinToString(separator = ", ")
}

private fun LocalDate.asShortDate(): String =
    "${day.toString().padStart(2, '0')}.${(month.ordinal + 1).toString().padStart(2, '0')}"

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun RemindersTabContentPreviewLight() {
    BlinklyWidgetPreview {
        RemindersPreviewContent()
    }
}

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun RemindersTabContentPreviewDark() {
    BlinklyWidgetPreview(isDakTheme = true) {
        RemindersPreviewContent()
    }
}

@Preview(widthDp = 440, heightDp = 880)
@Composable
private fun RemindersTabContentEmptyPreviewLight() {
    BlinklyWidgetPreview {
        RemindersTabContent(RemindersTabComponentPreview(reminders = emptyList()))
    }
}

@Composable
@PreviewContent
private fun RemindersPreviewContent() {
    RemindersTabContent(
        component = RemindersTabComponentPreview(
            reminders = RemindersTabComponentPreview.defaultReminders + RemindersTabComponent.ReminderItem(
                uuid = "friday",
                title = "20-20-20",
                description = "Look 20 feet away for 20 seconds",
                time = kotlinx.datetime.LocalTime(hour = 16, minute = 0),
                nextDate = LocalDate(year = 2026, month = 6, day = 26),
                interval = Interval.WEEKLY,
                days = listOf(DayOfWeek.FRIDAY),
            )
        )
    )
}
