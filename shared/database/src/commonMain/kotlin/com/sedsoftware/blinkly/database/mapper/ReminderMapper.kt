package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.ReminderEntity
import com.sedsoftware.blinkly.domain.model.Reminder

internal object ReminderMapper {
    fun toDomain(from: List<ReminderEntity>): List<Reminder> =
        from.map { item: ReminderEntity ->
            Reminder(
                uuid = item.uuid,
                date = item.date,
                type = item.type,
                interval = item.interval,
                weekDays = item.weekDays,
            )
        }
}
