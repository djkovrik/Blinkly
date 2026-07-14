package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.Workout
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test

class BlinklyReminderManagerTest : BaseDomainTest() {

    private val alarmManager: BlinklyAlarmManager = mock {
        every { scheduleDaily(any(), any(), any()) } returns Unit
        every { scheduleWeekly(any(), any(), any()) } returns Unit
        every { cancel(any()) } returns Unit
    }

    private lateinit var database: FakeDatabase
    private lateinit var manager: BlinklyReminderManager

    @BeforeTest
    fun setup() {
        database = FakeDatabase()
        manager = createBlinklyReminderManager(
            alarmManager = alarmManager,
            database = database,
            timeUtils = timeUtils,
            dispatchers = testDispatchers,
        )
    }

    @Test
    fun `daily creates one logical schedule and one physical alarm`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())

        manager.scheduleDaily(LocalTime(13, 21))

        val schedule = database.schedules.single()
        val alarm = database.reminders.single()
        assertThat(schedule.configuration).isEqualTo(ReminderScheduleConfiguration.Daily(LocalTime(13, 21)))
        assertThat(alarm.scheduleId).isEqualTo(schedule.id)
        assertThat(alarm.date).isEqualTo(LocalDateTime(2026, 3, 15, 13, 21))
        assertThat(alarm.interval).isEqualTo(ReminderInterval.DAILY)
        verify(exactly(1)) { alarmManager.scheduleDaily(alarm.uuid, alarm.type, alarm.date) }
    }

    @Test
    fun `weekly single creates one logical schedule and one physical alarm`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())

        manager.scheduleWeeklySingle(LocalTime(10, 11), DayOfWeek.MONDAY)

        val schedule = database.schedules.single()
        val alarm = database.reminders.single()
        assertThat(schedule.configuration).isEqualTo(
            ReminderScheduleConfiguration.WeeklySingle(LocalTime(10, 11), DayOfWeek.MONDAY)
        )
        assertThat(alarm.scheduleId).isEqualTo(schedule.id)
        assertThat(alarm.date).isEqualTo(LocalDateTime(2026, 3, 16, 10, 11))
        assertThat(alarm.interval).isEqualTo(ReminderInterval.WEEKLY)
        verify(exactly(1)) { alarmManager.scheduleWeekly(alarm.uuid, alarm.type, alarm.date) }
    }

    @Test
    fun `workday period creates one normalized schedule and all physical alarms`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 22, 0).toInstant(timeUtils.timeZone())

        manager.scheduleWeeklyDayPeriod(
            from = LocalTime(10, 0),
            until = LocalTime(18, 0),
            intervalMinutes = 240,
            days = listOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        )

        val schedule = database.schedules.single()
        assertThat(schedule.configuration).isEqualTo(
            ReminderScheduleConfiguration.WorkdayPeriod(
                from = LocalTime(10, 0),
                until = LocalTime(18, 0),
                intervalMinutes = 240,
                days = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            )
        )
        assertThat(database.reminders.size).isEqualTo(4)
        assertThat(database.reminders.map(Reminder::scheduleId).distinct()).isEqualTo(listOf(schedule.id))
        assertThat(database.reminders.map(Reminder::date)).isEqualTo(
            listOf(
                LocalDateTime(2026, 3, 16, 14, 0),
                LocalDateTime(2026, 3, 20, 14, 0),
                LocalDateTime(2026, 3, 16, 18, 0),
                LocalDateTime(2026, 3, 20, 18, 0),
            )
        )
        database.reminders.forEach { alarm ->
            verify(exactly(1)) { alarmManager.scheduleWeekly(alarm.uuid, alarm.type, alarm.date) }
        }
    }

    @Test
    fun `invalid workday period creates neither schedule nor alarms`() = runTest(testScheduler) {
        manager.scheduleWeeklyDayPeriod(LocalTime(10, 0), LocalTime(18, 0), 0, listOf(DayOfWeek.MONDAY))
        manager.scheduleWeeklyDayPeriod(LocalTime(10, 0), LocalTime(18, 0), 20, emptyList())
        manager.scheduleWeeklyDayPeriod(LocalTime(18, 0), LocalTime(10, 0), 20, listOf(DayOfWeek.MONDAY))

        assertThat(database.schedules).isEqualTo(emptyList())
        assertThat(database.reminders).isEqualTo(emptyList())
        verify(exactly(0)) { alarmManager.scheduleWeekly(any(), any(), any()) }
    }

    @Test
    fun `created schedules emits one aggregate for workday period`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 0).toInstant(timeUtils.timeZone())
        manager.scheduleWeeklyDayPeriod(
            LocalTime(9, 0),
            LocalTime(10, 0),
            20,
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        )

        val aggregate = manager.createdSchedules().first().single()

        assertThat(aggregate.schedule.id).isEqualTo(database.schedules.single().id)
        assertThat(aggregate.alarms.size).isEqualTo(6)
        assertThat(aggregate.nextAlarm).isEqualTo(database.reminders.minByOrNull(Reminder::date))
    }

    @Test
    fun `cancel schedule cancels every child and preserves another aggregate`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 0).toInstant(timeUtils.timeZone())
        manager.scheduleWeeklyDayPeriod(
            LocalTime(9, 0),
            LocalTime(10, 0),
            20,
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        )
        manager.scheduleDaily(LocalTime(13, 0))
        val period = database.schedules.first { it.configuration is ReminderScheduleConfiguration.WorkdayPeriod }
        val periodAlarms = database.reminders.filter { it.scheduleId == period.id }

        manager.cancelSchedule(period.id)

        periodAlarms.forEach { alarm -> verify(exactly(1)) { alarmManager.cancel(alarm.uuid) } }
        assertThat(database.schedules.size).isEqualTo(1)
        assertThat(database.reminders.single().interval).isEqualTo(ReminderInterval.DAILY)
    }

    @Test
    fun `reschedule all schedules every physical child at next local occurrence`() = runTest(testScheduler) {
        val timeZone = TimeZone.of("Europe/Moscow")
        every { timeUtils.timeZone() } returns timeZone
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 0).toInstant(timeZone)
        val daily = reminder("daily", "daily-schedule", ReminderInterval.DAILY, LocalDateTime(2026, 3, 14, 10, 0))
        val weekly = reminder("weekly", "period", ReminderInterval.WEEKLY, LocalDateTime(2026, 3, 9, 9, 20))
        database.replaceReminders(listOf(daily, weekly))

        manager.rescheduleAll()

        verify(exactly(1)) { alarmManager.cancel(daily.uuid) }
        verify(exactly(1)) { alarmManager.cancel(weekly.uuid) }
        verify(exactly(1)) {
            alarmManager.scheduleDaily(daily.uuid, daily.type, LocalDateTime(2026, 3, 16, 10, 0))
        }
        verify(exactly(1)) {
            alarmManager.scheduleWeekly(weekly.uuid, weekly.type, LocalDateTime(2026, 3, 16, 9, 20))
        }
    }

    @Test
    fun `cancel all clears schedules and physical alarms`() = runTest(testScheduler) {
        every { timeUtils.now() } returns LocalDateTime(2026, 3, 15, 12, 0).toInstant(timeUtils.timeZone())
        manager.scheduleDaily(LocalTime(13, 0))
        manager.scheduleWeeklySingle(LocalTime(14, 0), DayOfWeek.MONDAY)
        val alarmsToCancel = database.reminders

        manager.cancelAll()

        alarmsToCancel.forEach { alarm ->
            verify(exactly(1)) { alarmManager.cancel(alarm.uuid) }
        }
        assertThat(database.schedules).isEqualTo(emptyList())
        assertThat(database.reminders).isEqualTo(emptyList())
        assertThat(manager.createdSchedules().first().firstOrNull()).isNull()
    }

    private fun reminder(uuid: String, scheduleId: String, interval: ReminderInterval, date: LocalDateTime) =
        Reminder(
            uuid = uuid,
            scheduleId = scheduleId,
            date = date,
            type = ReminderType.TWENTY_X3,
            interval = interval,
        )

    private class FakeDatabase : BlinklyDatabase {
        private val schedulesFlow = MutableStateFlow<List<ReminderSchedule>>(emptyList())
        private val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())

        val schedules: List<ReminderSchedule> get() = schedulesFlow.value
        val reminders: List<Reminder> get() = remindersFlow.value

        override fun currentCalendar(): Flow<List<Workout>> = MutableStateFlow(emptyList())
        override fun currentAchievements(): Flow<List<Achievement>> = MutableStateFlow(emptyList())
        override fun currentReminderSchedules(): Flow<List<ReminderSchedule>> = schedulesFlow
        override fun currentReminders(): Flow<List<Reminder>> = remindersFlow

        override suspend fun currentSnapshot(): BlinklyDatabaseSnapshot =
            BlinklyDatabaseSnapshot(emptyList(), emptyList(), schedules, reminders)

        override suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot) {
            schedulesFlow.value = snapshot.reminderSchedules
            remindersFlow.value = snapshot.reminders
        }

        override suspend fun saveExercise(exercise: Exercise) = Unit
        override suspend fun saveExercises(exercises: List<Exercise>) = Unit
        override suspend fun unlockAchievement(achievement: Achievement) = Unit
        override suspend fun saveAchievements(achievements: List<Achievement>) = Unit
        override suspend fun deleteAchievements() = Unit
        override suspend fun deleteExercises() = Unit

        override suspend fun remindersBySchedule(scheduleId: String): List<Reminder> =
            reminders.filter { it.scheduleId == scheduleId }

        override suspend fun saveReminderSchedule(schedule: ReminderSchedule, reminders: List<Reminder>) {
            schedulesFlow.value = schedules.filterNot { it.id == schedule.id } + schedule
            remindersFlow.value = this.reminders.filterNot { it.scheduleId == schedule.id } + reminders
        }

        override suspend fun deleteReminderSchedule(scheduleId: String) {
            remindersFlow.value = reminders.filterNot { it.scheduleId == scheduleId }
            schedulesFlow.value = schedules.filterNot { it.id == scheduleId }
        }

        override suspend fun deleteReminders() {
            remindersFlow.value = emptyList()
            schedulesFlow.value = emptyList()
        }

        fun replaceReminders(reminders: List<Reminder>) {
            remindersFlow.value = reminders
        }
    }
}
