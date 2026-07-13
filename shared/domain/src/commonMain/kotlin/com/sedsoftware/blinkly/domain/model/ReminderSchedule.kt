package com.sedsoftware.blinkly.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ReminderSchedule(
    val id: String,
    val reminderType: ReminderType,
    val configuration: ReminderScheduleConfiguration,
)

sealed interface ReminderScheduleConfiguration {
    data class Daily(
        val time: LocalTime,
    ) : ReminderScheduleConfiguration

    data class WeeklySingle(
        val time: LocalTime,
        val day: DayOfWeek,
    ) : ReminderScheduleConfiguration

    data class WorkdayPeriod(
        val from: LocalTime,
        val until: LocalTime,
        val intervalMinutes: Int,
        val days: List<DayOfWeek>,
    ) : ReminderScheduleConfiguration
}

enum class ReminderScheduleType {
    DAILY,
    WEEKLY_SINGLE,
    WORKDAY_PERIOD,
}

data class ScheduledReminder(
    val schedule: ReminderSchedule,
    val alarms: List<Reminder>,
) {
    val nextAlarm: Reminder?
        get() = alarms.minByOrNull(Reminder::date)
}
