package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.impl.ReminderManagerImpl
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReminderManagerTest : BaseDomainTest() {

    private val remindersFlow: MutableStateFlow<List<Reminder>> = MutableStateFlow(emptyList())
    private val createdReminders: MutableList<Reminder> = mutableListOf()

    private val alarmManager: BlinklyAlarmManager = mock {
        every { scheduleDaily(any(), any(), any()) } returns Unit
        every { scheduleWeekly(any(), any(), any()) } returns Unit
        every { cancel(any()) } returns Unit
        every { cancelAll() } returns Unit
    }

    private val database: BlinklyDatabase = mock {
        everySuspend { currentReminders() } returns remindersFlow
        everySuspend { saveReminder(reminder = any()) } calls { (reminder: Reminder) -> createdReminders.add(reminder) }
        everySuspend { saveReminders(reminders = any()) } calls { (reminders: List<Reminder>) -> createdReminders.addAll(reminders) }
        everySuspend { deleteReminder(any()) } calls { (uuid: String) ->
            createdReminders.removeAt(createdReminders.indexOfLast { it.uuid == uuid })
        }
        everySuspend { deleteReminders() } calls { createdReminders.clear() }
    }

    lateinit var manager: ReminderManager

    @BeforeTest
    fun setup() {
        createdReminders.clear()
        manager = createManager()
    }

    @Test
    fun `when data updated then reminders flow emits actual reminders list`() = runTest(testScheduler) {
        // given
        val reminder = FakeData.getReminder(now.toLocalDateTime(timeUtils.timeZone()))
        val expectedReminders: List<Reminder> = listOf(reminder)

        // when
        val collectJob = launch { manager.reminders.collect {} }
        remindersFlow.emit(expectedReminders)
        testScheduler.advanceUntilIdle()

        // then
        val actualReminders = manager.reminders.first()
        assertThat(expectedReminders).isEqualTo(actualReminders)
        collectJob.cancel()
    }

    @Test
    fun `when scheduleDaily with future time then reminder created with today date`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val reminderTarget = LocalDateTime(2026, 3, 15, 13, 21)
        val targetTime = LocalTime(13, 21)
        every { timeUtils.now() } returns todayInstant

        // when
        manager.scheduleDaily(targetTime)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(createdReminders).isNotEmpty()
        assertThat(createdReminders.first().interval).isEqualTo(ReminderInterval.DAILY)
        assertThat(createdReminders.first().date).isEqualTo(reminderTarget)
        assertThat(createdReminders.first().date.time).isEqualTo(targetTime)
        val created = createdReminders.first()
        verifySuspend(exactly(1)) { database.saveReminder(created) }
        verifySuspend(exactly(1)) { alarmManager.scheduleDaily(created.uuid, created.type, created.date) }
    }

    @Test
    fun `when scheduleDaily with time in the past then reminder created with tomorrow date`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val reminderTarget = LocalDateTime(2026, 3, 16, 10, 11)
        val targetTime = LocalTime(10, 11)
        every { timeUtils.now() } returns todayInstant

        // when
        manager.scheduleDaily(targetTime)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(createdReminders).isNotEmpty()
        assertThat(createdReminders.first().interval).isEqualTo(ReminderInterval.DAILY)
        assertThat(createdReminders.first().date).isEqualTo(reminderTarget)
        assertThat(createdReminders.first().date.time).isEqualTo(targetTime)
        val created = createdReminders.first()
        verifySuspend(exactly(1)) { database.saveReminder(created) }
        verifySuspend(exactly(1)) { alarmManager.scheduleDaily(created.uuid, created.type, created.date) }
    }

    @Test
    fun `when scheduleWeeklySingle with future time then reminder created with this week date`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val reminderTarget = LocalDateTime(2026, 3, 16, 10, 11)
        val targetTime = LocalTime(10, 11)
        every { timeUtils.now() } returns todayInstant

        // when
        manager.scheduleWeeklySingle(targetTime, DayOfWeek.MONDAY)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(createdReminders).isNotEmpty()
        assertThat(createdReminders.first().interval).isEqualTo(ReminderInterval.WEEKLY)
        assertThat(createdReminders.first().date).isEqualTo(reminderTarget)
        assertThat(createdReminders.first().date.time).isEqualTo(targetTime)
        val created = createdReminders.first()
        verifySuspend(exactly(1)) { database.saveReminder(created) }
        verifySuspend(exactly(1)) { alarmManager.scheduleWeekly(created.uuid, created.type, created.date) }
    }

    @Test
    fun `when scheduleWeeklySingle with time in the past then reminder created with next week date`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 17, 12, 34).toInstant(timeUtils.timeZone())
        val reminderTarget = LocalDateTime(2026, 3, 23, 10, 11)
        val targetTime = LocalTime(10, 11)
        every { timeUtils.now() } returns todayInstant

        // when
        manager.scheduleWeeklySingle(targetTime, DayOfWeek.MONDAY)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(createdReminders).isNotEmpty()
        assertThat(createdReminders.first().interval).isEqualTo(ReminderInterval.WEEKLY)
        assertThat(createdReminders.first().date).isEqualTo(reminderTarget)
        assertThat(createdReminders.first().date.time).isEqualTo(targetTime)
        val created = createdReminders.first()
        verifySuspend(exactly(1)) { database.saveReminder(created) }
        verifySuspend(exactly(1)) { alarmManager.scheduleWeekly(created.uuid, created.type, created.date) }
    }

    @Test
    fun `when schedule with all days in the future week then reminders created with upcoming week dates`() = runTest(testScheduler) {
        // given
        val expectedReminders = 14
        val todayInstant = LocalDateTime(2026, 3, 15, 22, 0).toInstant(timeUtils.timeZone())
        val targetDays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )
        every { timeUtils.now() } returns todayInstant

        // when
        manager.scheduleWeeklyDayPeriod(
            from = LocalTime(hour = 10, minute = 0),
            to = LocalTime(hour = 18, minute = 0),
            intervalHours = 4,
            days = targetDays,
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(createdReminders.size).isEqualTo(expectedReminders)
        assertThat(createdReminders[0].weekDays).isEqualTo(listOf(DayOfWeek.MONDAY))
        assertThat(createdReminders[1].weekDays).isEqualTo(listOf(DayOfWeek.TUESDAY))
        assertThat(createdReminders[2].weekDays).isEqualTo(listOf(DayOfWeek.WEDNESDAY))
        assertThat(createdReminders[3].weekDays).isEqualTo(listOf(DayOfWeek.THURSDAY))
        assertThat(createdReminders[4].weekDays).isEqualTo(listOf(DayOfWeek.FRIDAY))
        assertThat(createdReminders[5].weekDays).isEqualTo(listOf(DayOfWeek.SATURDAY))
        assertThat(createdReminders[6].weekDays).isEqualTo(listOf(DayOfWeek.SUNDAY))
        assertThat(createdReminders[7].weekDays).isEqualTo(listOf(DayOfWeek.MONDAY))
        assertThat(createdReminders[8].weekDays).isEqualTo(listOf(DayOfWeek.TUESDAY))
        assertThat(createdReminders[9].weekDays).isEqualTo(listOf(DayOfWeek.WEDNESDAY))
        assertThat(createdReminders[10].weekDays).isEqualTo(listOf(DayOfWeek.THURSDAY))
        assertThat(createdReminders[11].weekDays).isEqualTo(listOf(DayOfWeek.FRIDAY))
        assertThat(createdReminders[12].weekDays).isEqualTo(listOf(DayOfWeek.SATURDAY))
        assertThat(createdReminders[13].weekDays).isEqualTo(listOf(DayOfWeek.SUNDAY))

        assertThat(createdReminders[0].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 16, hour = 14, minute = 0))
        assertThat(createdReminders[1].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 17, hour = 14, minute = 0))
        assertThat(createdReminders[2].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 18, hour = 14, minute = 0))
        assertThat(createdReminders[3].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 19, hour = 14, minute = 0))
        assertThat(createdReminders[4].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 20, hour = 14, minute = 0))
        assertThat(createdReminders[5].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 21, hour = 14, minute = 0))
        assertThat(createdReminders[6].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 22, hour = 14, minute = 0))
        assertThat(createdReminders[7].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 16, hour = 18, minute = 0))
        assertThat(createdReminders[8].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 17, hour = 18, minute = 0))
        assertThat(createdReminders[9].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 18, hour = 18, minute = 0))
        assertThat(createdReminders[10].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 19, hour = 18, minute = 0))
        assertThat(createdReminders[11].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 20, hour = 18, minute = 0))
        assertThat(createdReminders[12].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 21, hour = 18, minute = 0))
        assertThat(createdReminders[13].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 22, hour = 18, minute = 0))

        verifySuspend(exactly(1)) { database.saveReminders(createdReminders) }
    }

    @Test
    fun `when schedule with period with some dates in the current week then reminders created with both weeks dates`() =
        runTest(testScheduler) {
            // given
            val expectedReminders = 14
            val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
            val targetDays = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            )
            every { timeUtils.now() } returns todayInstant

            // when
            manager.scheduleWeeklyDayPeriod(
                from = LocalTime(hour = 10, minute = 0),
                to = LocalTime(hour = 18, minute = 0),
                intervalHours = 4,
                days = targetDays,
            )
            testScheduler.advanceUntilIdle()

            // then
            assertThat(createdReminders.size).isEqualTo(expectedReminders)
            assertThat(createdReminders[0].weekDays).isEqualTo(listOf(DayOfWeek.MONDAY))
            assertThat(createdReminders[1].weekDays).isEqualTo(listOf(DayOfWeek.TUESDAY))
            assertThat(createdReminders[2].weekDays).isEqualTo(listOf(DayOfWeek.WEDNESDAY))
            assertThat(createdReminders[3].weekDays).isEqualTo(listOf(DayOfWeek.THURSDAY))
            assertThat(createdReminders[4].weekDays).isEqualTo(listOf(DayOfWeek.FRIDAY))
            assertThat(createdReminders[5].weekDays).isEqualTo(listOf(DayOfWeek.SATURDAY))
            assertThat(createdReminders[6].weekDays).isEqualTo(listOf(DayOfWeek.SUNDAY))
            assertThat(createdReminders[7].weekDays).isEqualTo(listOf(DayOfWeek.MONDAY))
            assertThat(createdReminders[8].weekDays).isEqualTo(listOf(DayOfWeek.TUESDAY))
            assertThat(createdReminders[9].weekDays).isEqualTo(listOf(DayOfWeek.WEDNESDAY))
            assertThat(createdReminders[10].weekDays).isEqualTo(listOf(DayOfWeek.THURSDAY))
            assertThat(createdReminders[11].weekDays).isEqualTo(listOf(DayOfWeek.FRIDAY))
            assertThat(createdReminders[12].weekDays).isEqualTo(listOf(DayOfWeek.SATURDAY))
            assertThat(createdReminders[13].weekDays).isEqualTo(listOf(DayOfWeek.SUNDAY))

            assertThat(createdReminders[0].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 16, hour = 14, minute = 0))
            assertThat(createdReminders[1].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 17, hour = 14, minute = 0))
            assertThat(createdReminders[2].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 18, hour = 14, minute = 0))
            assertThat(createdReminders[3].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 19, hour = 14, minute = 0))
            assertThat(createdReminders[4].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 20, hour = 14, minute = 0))
            assertThat(createdReminders[5].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 21, hour = 14, minute = 0))
            assertThat(createdReminders[6].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 15, hour = 14, minute = 0))
            assertThat(createdReminders[7].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 16, hour = 18, minute = 0))
            assertThat(createdReminders[8].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 17, hour = 18, minute = 0))
            assertThat(createdReminders[9].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 18, hour = 18, minute = 0))
            assertThat(createdReminders[10].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 19, hour = 18, minute = 0))
            assertThat(createdReminders[11].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 20, hour = 18, minute = 0))
            assertThat(createdReminders[12].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 21, hour = 18, minute = 0))
            assertThat(createdReminders[13].date).isEqualTo(LocalDateTime(year = 2026, month = 3, day = 15, hour = 18, minute = 0))

            verifySuspend(exactly(1)) { database.saveReminders(createdReminders) }
        }

    @Test
    fun `when rescheduleAll then calls for alarm manager`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val targetTime1 = LocalTime(13, 21)
        val targetTime2 = LocalTime(14, 15)
        every { timeUtils.now() } returns todayInstant
        manager.scheduleDaily(targetTime1)
        manager.scheduleWeeklySingle(targetTime2, DayOfWeek.SUNDAY)
        testScheduler.advanceUntilIdle()
        assertThat(createdReminders).isNotEmpty()

        // when
        manager.rescheduleAll()
        testScheduler.advanceUntilIdle()

        // then
        verify(exactly(1)) { alarmManager.cancelAll() }
        verify(exactly(1)) { alarmManager.scheduleDaily(any(), any(), any()) }
        verify(exactly(1)) { alarmManager.scheduleWeekly(any(), any(), any()) }
    }

    @Test
    fun `when cancel then calls for alarm manager`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val targetTime = LocalTime(13, 21)
        every { timeUtils.now() } returns todayInstant
        manager.scheduleDaily(targetTime)
        testScheduler.advanceUntilIdle()

        // when
        assertThat(createdReminders).isNotEmpty()
        val created = createdReminders.first()
        manager.cancel(created.uuid)

        // then
        verify(exactly(1)) { alarmManager.cancel(created.uuid) }
        verifySuspend(exactly(1)) { database.deleteReminder(created.uuid) }
    }

    @Test
    fun `when cancelAll then calls for alarm manager`() = runTest(testScheduler) {
        // given
        val todayInstant = LocalDateTime(2026, 3, 15, 12, 34).toInstant(timeUtils.timeZone())
        val targetTime1 = LocalTime(13, 21)
        val targetTime2 = LocalTime(14, 15)
        every { timeUtils.now() } returns todayInstant
        manager.scheduleDaily(targetTime1)
        manager.scheduleWeeklySingle(targetTime2, DayOfWeek.SUNDAY)
        testScheduler.advanceUntilIdle()
        assertThat(createdReminders).isNotEmpty()

        // when
        manager.cancelAll()
        testScheduler.advanceUntilIdle()

        // then
        verify(exactly(1)) { alarmManager.cancelAll() }
        verifySuspend(exactly(1)) { database.deleteReminders() }
    }

    private fun createManager(): ReminderManager =
        ReminderManagerImpl(
            alarmManager = alarmManager,
            database = database,
            timeUtils = timeUtils,
            dispatchers = testDispatchers,
        )
}
