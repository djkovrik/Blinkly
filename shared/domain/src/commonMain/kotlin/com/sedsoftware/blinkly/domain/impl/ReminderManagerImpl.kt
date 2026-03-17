package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.ReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid

internal class ReminderManagerImpl(
    private val alarmManager: BlinklyAlarmManager,
    private val database: BlinklyDatabase,
    private val timeUtils: BlinklyTimeUtils,
    dispatchers: BlinklyDispatchers,
) : ReminderManager {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())

    override val reminders: Flow<List<Reminder>> = database.currentReminders()
        .flowOn(dispatchers.io)
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
            replay = 1,
        )

    override suspend fun scheduleDaily(time: LocalTime) {
        val now = timeUtils.now()
        val timeZone = timeUtils.timeZone()
        val today = now.toLocalDateTime(timeZone)
        val candidate = LocalDateTime(today.date, time)

        val finalDateTime = if (candidate.toInstant(timeZone) > now) {
            candidate
        } else {
            LocalDateTime(today.date.plus(1, DateTimeUnit.DAY), time)
        }

        val uuid = Uuid.random().toString()

        val reminder = Reminder(
            uuid = uuid,
            date = finalDateTime,
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
            weekDays = emptyList(),
        )

        database.saveReminder(reminder)

        alarmManager.scheduleDaily(
            uuid = uuid,
            type = ReminderType.TWENTY_X3,
            startingDate = finalDateTime,
        )
    }

    override suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek) {
        val now = timeUtils.now()
        val timeZone = timeUtils.timeZone()
        val today = now.toLocalDateTime(timeZone)

        val daysToAdd = (dayOfWeek.ordinal - today.dayOfWeek.ordinal + FULL_WEEK_DAYS) % FULL_WEEK_DAYS
        val targetDate = today.date.plus(daysToAdd.toLong(), DateTimeUnit.DAY)
        val candidate = LocalDateTime(targetDate, time)

        val finalDateTime = if (candidate.toInstant(timeZone) > now) {
            candidate
        } else {
            LocalDateTime(targetDate.plus(FULL_WEEK_DAYS, DateTimeUnit.DAY), time)
        }

        val uuid = Uuid.random().toString()

        val reminder = Reminder(
            uuid = uuid,
            date = finalDateTime,
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
            weekDays = listOf(dayOfWeek),
        )

        database.saveReminder(reminder)

        alarmManager.scheduleWeekly(
            uuid = uuid,
            type = ReminderType.TWENTY_X3,
            startingDate = finalDateTime,
        )
    }

    override suspend fun scheduleWeeklyDayPeriod(from: LocalTime, to: LocalTime, intervalMinutes: Int, days: List<DayOfWeek>) {
        if (intervalMinutes <= 0 || days.isEmpty() || from >= to) return

        val nowInstant = timeUtils.now()
        val timeZone = timeUtils.timeZone()
        val currentLocal = nowInstant.toLocalDateTime(timeZone)

        val reminderTimes = mutableListOf<LocalTime>()

        val baseDate = currentLocal.date
        var currentInstant = LocalDateTime(baseDate, from)
            .toInstant(timeZone)
            .plus(intervalMinutes.toLong(), DateTimeUnit.MINUTE)

        val endInstant = LocalDateTime(baseDate, to).toInstant(timeZone)

        while (currentInstant <= endInstant) {
            val localTime = currentInstant.toLocalDateTime(timeZone).time
            reminderTimes.add(localTime)

            currentInstant = currentInstant.plus(intervalMinutes.toLong(), DateTimeUnit.MINUTE)
        }

        val remindersToSave = mutableListOf<Reminder>()

        for (reminderTime in reminderTimes) {
            for (targetDay in days) {
                val daysToAdd = (targetDay.ordinal - currentLocal.dayOfWeek.ordinal + FULL_WEEK_DAYS) % FULL_WEEK_DAYS
                var targetDate = currentLocal.date.plus(daysToAdd.toLong(), DateTimeUnit.DAY)
                var candidate = LocalDateTime(targetDate, reminderTime)

                if (candidate.toInstant(timeZone) <= nowInstant) {
                    targetDate = targetDate.plus(FULL_WEEK_DAYS, DateTimeUnit.DAY)
                    candidate = LocalDateTime(targetDate, reminderTime)
                }

                val uuid = Uuid.random().toString()

                val reminder = Reminder(
                    uuid = uuid,
                    date = candidate,
                    type = ReminderType.TWENTY_X3,
                    interval = ReminderInterval.WEEKLY,
                    weekDays = listOf(targetDay)
                )

                remindersToSave.add(reminder)
            }
        }

        if (remindersToSave.isNotEmpty()) {
            database.saveReminders(remindersToSave)

            for (reminder in remindersToSave) {
                alarmManager.scheduleWeekly(
                    uuid = reminder.uuid,
                    type = ReminderType.TWENTY_X3,
                    startingDate = reminder.date,
                )
            }
        }
    }

    override suspend fun cancel(uuid: String) {
        alarmManager.cancel(uuid)
        database.deleteReminder(uuid)
    }

    override suspend fun rescheduleAll() {
        alarmManager.cancelAll()
        val remindersToSchedule = database.currentReminders().first()

        for (reminder in remindersToSchedule) {
            if (reminder.interval == ReminderInterval.DAILY) {
                alarmManager.scheduleWeekly(
                    uuid = reminder.uuid,
                    type = ReminderType.TWENTY_X3,
                    startingDate = reminder.date,
                )
            } else {
                alarmManager.scheduleWeekly(
                    uuid = reminder.uuid,
                    type = ReminderType.TWENTY_X3,
                    startingDate = reminder.date,
                )
            }
        }
    }

    override suspend fun cancelAll() {
        alarmManager.cancelAll()
        database.deleteReminders()
    }

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
        const val FULL_WEEK_DAYS = 7
    }
}
