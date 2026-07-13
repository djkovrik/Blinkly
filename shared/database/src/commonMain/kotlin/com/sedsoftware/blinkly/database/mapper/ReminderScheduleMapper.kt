package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.ReminderScheduleEntity
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderScheduleType

internal class ReminderScheduleMapper {
    fun toDomain(from: List<ReminderScheduleEntity>): List<ReminderSchedule> =
        from.map(::toDomain)

    private fun toDomain(from: ReminderScheduleEntity): ReminderSchedule =
        ReminderSchedule(
            id = from.id,
            reminderType = from.reminderType,
            configuration = when (from.scheduleType) {
                ReminderScheduleType.DAILY -> ReminderScheduleConfiguration.Daily(
                    time = from.timeFrom,
                )

                ReminderScheduleType.WEEKLY_SINGLE -> ReminderScheduleConfiguration.WeeklySingle(
                    time = from.timeFrom,
                    day = requireNotNull(from.weekDays.singleOrNull()) {
                        "Weekly reminder schedule must contain exactly one day"
                    },
                )

                ReminderScheduleType.WORKDAY_PERIOD -> ReminderScheduleConfiguration.WorkdayPeriod(
                    from = from.timeFrom,
                    until = requireNotNull(from.timeUntil) {
                        "Workday period schedule must contain an end time"
                    },
                    intervalMinutes = requireNotNull(from.intervalMinutes) {
                        "Workday period schedule must contain an interval"
                    },
                    days = from.weekDays,
                )
            },
        )
}
