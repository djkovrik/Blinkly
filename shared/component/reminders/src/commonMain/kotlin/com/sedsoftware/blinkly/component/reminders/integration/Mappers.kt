package com.sedsoftware.blinkly.component.reminders.integration

import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Model
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.ReminderItem
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Schedule
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.State
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ScheduledReminder

internal val stateToModel: (State) -> Model = { state ->
    Model(
        reminders = state.reminders
            .mapNotNull(::reminderToItem)
            .sortedBy(ReminderItem::nextAt),
        deletedReminder = state.deletedReminder?.let(::reminderToItem),
    )
}

private fun reminderToItem(reminder: ScheduledReminder): ReminderItem? {
    val nextAt = reminder.nextAlarm?.date ?: return null
    return ReminderItem(
        id = reminder.schedule.id,
        nextAt = nextAt,
        schedule = when (val configuration = reminder.schedule.configuration) {
            is ReminderScheduleConfiguration.Daily -> Schedule.Daily(configuration.time)
            is ReminderScheduleConfiguration.WeeklySingle -> Schedule.Weekly(
                time = configuration.time,
                day = configuration.day,
            )
            is ReminderScheduleConfiguration.WorkdayPeriod -> Schedule.WorkdayPeriod(
                from = configuration.from,
                until = configuration.until,
                intervalMinutes = configuration.intervalMinutes,
                days = configuration.days,
            )
        },
    )
}
