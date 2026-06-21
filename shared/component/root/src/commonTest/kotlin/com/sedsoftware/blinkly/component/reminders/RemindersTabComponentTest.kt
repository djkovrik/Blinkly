package com.sedsoftware.blinkly.component.reminders

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.reminders.integration.RemindersTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class RemindersTabComponentTest : ComponentTest<RemindersTabComponent>() {

    private val reminderManager: FakeReminderManager = FakeReminderManager()

    @Test
    fun `when reminders emitted then model contains reminder items`() = runTest(testScheduler) {
        // given
        reminderManager.reminders.value = listOf(
            reminder(
                uuid = "weekly",
                date = LocalDateTime(year = 2026, month = 6, day = 22, hour = 14, minute = 30),
                interval = ReminderInterval.WEEKLY,
                weekDays = listOf(DayOfWeek.MONDAY),
            ),
            reminder(
                uuid = "daily",
                date = LocalDateTime(year = 2026, month = 6, day = 20, hour = 10, minute = 0),
                interval = ReminderInterval.DAILY,
            ),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.reminders.map { it.uuid }).isEqualTo(listOf("daily", "weekly"))
        assertThat(model.reminders.first().interval).isEqualTo(RemindersTabComponent.Interval.DAILY)
        assertThat(model.reminders.last().days).isEqualTo(listOf(DayOfWeek.MONDAY))
    }

    @Test
    fun `when add new clicked then output opens add new screen`() = runTest(testScheduler) {
        // when
        component.onAddNewClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Reminders.OpenAddNew)
    }

    @Test
    fun `when reminder deleted then manager cancels and undo restores reminder`() = runTest(testScheduler) {
        // given
        val dailyReminder = reminder(
            uuid = "daily",
            date = LocalDateTime(year = 2026, month = 6, day = 20, hour = 10, minute = 0),
            interval = ReminderInterval.DAILY,
        )
        reminderManager.reminders.value = listOf(dailyReminder)
        testScheduler.advanceUntilIdle()

        // when
        component.onDeleteReminder("daily")
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.cancelled).isEqualTo(listOf("daily"))
        assertThat(component.model.value.reminders).isEqualTo(emptyList())
        assertThat(component.model.value.deletedReminder?.uuid).isEqualTo("daily")

        // when
        component.onUndoDelete()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.scheduledDaily).isEqualTo(listOf(LocalTime(hour = 10, minute = 0)))
        assertThat(component.model.value.deletedReminder).isNull()
    }

    @Test
    fun `when weekly reminder undo clicked then weekly schedule is restored`() = runTest(testScheduler) {
        // given
        val weeklyReminder = reminder(
            uuid = "weekly",
            date = LocalDateTime(year = 2026, month = 6, day = 24, hour = 12, minute = 15),
            interval = ReminderInterval.WEEKLY,
            weekDays = listOf(DayOfWeek.WEDNESDAY),
        )
        reminderManager.reminders.value = listOf(weeklyReminder)
        testScheduler.advanceUntilIdle()

        // when
        component.onDeleteReminder("weekly")
        testScheduler.advanceUntilIdle()
        component.onUndoDelete()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.scheduledWeeklySingle).isEqualTo(
            listOf(LocalTime(hour = 12, minute = 15) to DayOfWeek.WEDNESDAY)
        )
    }

    @Test
    fun `when delete fails then error output is published and reminder remains`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("delete failed")
        reminderManager.cancelException = exception
        reminderManager.reminders.value = listOf(
            reminder(
                uuid = "daily",
                date = LocalDateTime(year = 2026, month = 6, day = 20, hour = 10, minute = 0),
                interval = ReminderInterval.DAILY,
            )
        )
        testScheduler.advanceUntilIdle()

        // when
        component.onDeleteReminder("daily")
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.reminders.map { it.uuid }).isEqualTo(listOf("daily"))
        assertThat(component.model.value.deletedReminder).isNull()
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): RemindersTabComponent =
        RemindersTabComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            reminderManager = reminderManager,
            remindersTabOutput = { componentOutput.add(it) },
        )

    private fun reminder(
        uuid: String,
        date: LocalDateTime,
        interval: ReminderInterval,
        weekDays: List<DayOfWeek> = emptyList(),
    ): Reminder =
        Reminder(
            uuid = uuid,
            date = date,
            type = ReminderType.TWENTY_X3,
            interval = interval,
            weekDays = weekDays,
        )

    private class FakeReminderManager : BlinklyReminderManager {
        val reminders: MutableStateFlow<List<Reminder>> = MutableStateFlow(emptyList())
        val cancelled: MutableList<String> = mutableListOf()
        val scheduledDaily: MutableList<LocalTime> = mutableListOf()
        val scheduledWeeklySingle: MutableList<Pair<LocalTime, DayOfWeek>> = mutableListOf()
        var cancelException: Throwable? = null

        override fun createdReminders(): Flow<List<Reminder>> = reminders

        override suspend fun scheduleDaily(time: LocalTime) {
            scheduledDaily.add(time)
        }

        override suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek) {
            scheduledWeeklySingle.add(time to dayOfWeek)
        }

        override suspend fun scheduleWeeklyDayPeriod(
            from: LocalTime,
            until: LocalTime,
            intervalMinutes: Int,
            days: List<DayOfWeek>,
        ) = Unit

        override suspend fun rescheduleAll() = Unit

        override suspend fun cancel(uuid: String) {
            cancelException?.let { throw it }
            cancelled.add(uuid)
            reminders.value = reminders.value.filterNot { it.uuid == uuid }
        }

        override suspend fun cancelAll() = Unit
    }
}
