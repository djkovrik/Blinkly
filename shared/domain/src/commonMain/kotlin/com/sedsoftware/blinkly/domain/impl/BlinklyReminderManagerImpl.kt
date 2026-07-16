package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ScheduledReminder
import com.sedsoftware.blinkly.domain.model.nextOccurrence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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

internal class BlinklyReminderManagerImpl(
    private val alarmManager: BlinklyAlarmManager,
    private val database: BlinklyDatabase,
    private val timeUtils: BlinklyTimeUtils,
    private val dispatchers: BlinklyDispatchers,
) : BlinklyReminderManager {

    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob())

    override fun createdReminders(): Flow<List<Reminder>> =
        database.currentReminders()
            .flowOn(dispatchers.io)
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT),
                replay = 1,
            )

    override fun createdSchedules(): Flow<List<ScheduledReminder>> =
        combine(
            database.currentReminderSchedules(),
            database.currentReminders(),
        ) { schedules, reminders ->
            schedules.map { schedule ->
                ScheduledReminder(
                    schedule = schedule,
                    alarms = reminders.filter { reminder -> reminder.scheduleId == schedule.id },
                )
            }
        }
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

        val scheduleId = Uuid.random().toString()
        val uuid = Uuid.random().toString()

        val schedule = ReminderSchedule(
            id = scheduleId,
            reminderType = ReminderType.TWENTY_X3,
            configuration = ReminderScheduleConfiguration.Daily(time),
        )

        val reminder = Reminder(
            uuid = uuid,
            scheduleId = scheduleId,
            date = finalDateTime,
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.DAILY,
        )

        database.saveReminderSchedule(schedule, listOf(reminder))

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

        val scheduleId = Uuid.random().toString()
        val uuid = Uuid.random().toString()

        val schedule = ReminderSchedule(
            id = scheduleId,
            reminderType = ReminderType.TWENTY_X3,
            configuration = ReminderScheduleConfiguration.WeeklySingle(
                time = time,
                day = dayOfWeek,
            ),
        )

        val reminder = Reminder(
            uuid = uuid,
            scheduleId = scheduleId,
            date = finalDateTime,
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
        )

        database.saveReminderSchedule(schedule, listOf(reminder))

        alarmManager.scheduleWeekly(
            uuid = uuid,
            type = ReminderType.TWENTY_X3,
            startingDate = finalDateTime,
        )
    }

    override suspend fun scheduleWeeklyDayPeriod(from: LocalTime, until: LocalTime, intervalMinutes: Int, days: List<DayOfWeek>) {
        if (intervalMinutes <= 0 || days.isEmpty() || from >= until) return

        val normalizedDays = days.distinct().sortedBy(DayOfWeek::ordinal)

        val nowInstant = timeUtils.now()
        val timeZone = timeUtils.timeZone()
        val currentLocal = nowInstant.toLocalDateTime(timeZone)

        val reminderTimes = mutableListOf<LocalTime>()

        val baseDate = currentLocal.date
        var currentInstant = LocalDateTime(baseDate, from)
            .toInstant(timeZone)
            .plus(intervalMinutes.toLong(), DateTimeUnit.MINUTE)

        val endInstant = LocalDateTime(baseDate, until).toInstant(timeZone)

        while (currentInstant <= endInstant) {
            val localTime = currentInstant.toLocalDateTime(timeZone).time
            reminderTimes.add(localTime)

            currentInstant = currentInstant.plus(intervalMinutes.toLong(), DateTimeUnit.MINUTE)
        }

        val remindersToSave = mutableListOf<Reminder>()
        val scheduleId = Uuid.random().toString()

        for (reminderTime in reminderTimes) {
            for (targetDay in normalizedDays) {
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
                    scheduleId = scheduleId,
                    date = candidate,
                    type = ReminderType.TWENTY_X3,
                    interval = ReminderInterval.WEEKLY,
                )

                remindersToSave.add(reminder)
            }
        }

        if (remindersToSave.isNotEmpty()) {
            database.saveReminderSchedule(
                schedule = ReminderSchedule(
                    id = scheduleId,
                    reminderType = ReminderType.TWENTY_X3,
                    configuration = ReminderScheduleConfiguration.WorkdayPeriod(
                        from = from,
                        until = until,
                        intervalMinutes = intervalMinutes,
                        days = normalizedDays,
                    ),
                ),
                reminders = remindersToSave,
            )

            for (reminder in remindersToSave) {
                alarmManager.scheduleWeekly(
                    uuid = reminder.uuid,
                    type = ReminderType.TWENTY_X3,
                    startingDate = reminder.date,
                )
            }
        }
    }

    override suspend fun cancelSchedule(scheduleId: String) {
        val reminders = database.remindersBySchedule(scheduleId)
        reminders.forEach { reminder -> alarmManager.cancel(reminder.uuid) }
        database.deleteReminderSchedule(scheduleId)
    }

    override fun canScheduleExactAlarms(): Boolean =
        alarmManager.canScheduleExactAlarms()

    override fun requestExactAlarmPermission() {
        alarmManager.requestExactAlarmPermission()
    }

    override suspend fun rescheduleAll() {
        val remindersToSchedule = database.currentReminders().first()
        remindersToSchedule.forEach { reminder -> alarmManager.cancel(reminder.uuid) }
        val now = timeUtils.now()
        val timeZone = timeUtils.timeZone()

        for (reminder in remindersToSchedule) {
            val nextOccurrence = reminder.nextOccurrence(now, timeZone)
            if (reminder.interval == ReminderInterval.DAILY) {
                alarmManager.scheduleDaily(
                    uuid = reminder.uuid,
                    type = reminder.type,
                    startingDate = nextOccurrence,
                )
            } else {
                alarmManager.scheduleWeekly(
                    uuid = reminder.uuid,
                    type = reminder.type,
                    startingDate = nextOccurrence,
                )
            }
        }
    }

    override suspend fun cancelAll() {
        val remindersToCancel = database.currentReminders().first()
        remindersToCancel.forEach { reminder -> alarmManager.cancel(reminder.uuid) }
        database.deleteReminders()
    }

    private companion object {
        const val SUBSCRIPTION_STOP_TIMEOUT = 5000L
        const val FULL_WEEK_DAYS = 7
    }
}
