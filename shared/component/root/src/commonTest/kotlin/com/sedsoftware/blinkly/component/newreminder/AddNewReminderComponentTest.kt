package com.sedsoftware.blinkly.component.newreminder

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.newreminder.integration.AddNewReminderComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class AddNewReminderComponentTest : ComponentTest<AddNewReminderComponent>() {

    private val reminderManager: FakeReminderManager = FakeReminderManager()

    @Test
    fun `when component created then default model creates daily reminder`() = runTest(testScheduler) {
        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.scheduleType).isEqualTo(AddNewReminderComponent.ScheduleType.DAILY)
        assertThat(model.dailyTime).isEqualTo(LocalTime(hour = 10, minute = 0))
        assertThat(model.validationError).isNull()
    }

    @Test
    fun `when daily reminder created then manager schedules daily and screen closes`() = runTest(testScheduler) {
        // given
        component.onDailyTimeSelect(LocalTime(hour = 11, minute = 45))

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.scheduledDaily).isEqualTo(listOf(LocalTime(hour = 11, minute = 45)))
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when weekly reminder created then manager schedules selected day`() = runTest(testScheduler) {
        // given
        component.onScheduleTypeSelect(AddNewReminderComponent.ScheduleType.WEEKLY_SINGLE)
        component.onWeeklyTimeSelect(LocalTime(hour = 13, minute = 20))
        component.onWeeklyDaySelect(DayOfWeek.FRIDAY)

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.scheduledWeeklySingle).isEqualTo(
            listOf(LocalTime(hour = 13, minute = 20) to DayOfWeek.FRIDAY)
        )
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when weekly period reminder created then manager schedules period`() = runTest(testScheduler) {
        // given
        component.onScheduleTypeSelect(AddNewReminderComponent.ScheduleType.WEEKLY_DAY_PERIOD)
        component.onPeriodTimeFromSelect(LocalTime(hour = 9, minute = 0))
        component.onPeriodTimeUntilSelect(LocalTime(hour = 17, minute = 0))
        component.onPeriodIntervalSelect(40)
        DayOfWeek.entries
            .filterNot { it == DayOfWeek.MONDAY || it == DayOfWeek.WEDNESDAY }
            .forEach(component::onPeriodDayToggle)

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.scheduledWeeklyPeriods).isEqualTo(
            listOf(
                WeeklyPeriodCall(
                    from = LocalTime(hour = 9, minute = 0),
                    until = LocalTime(hour = 17, minute = 0),
                    intervalMinutes = 40,
                    days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                )
            )
        )
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when weekly period has no days then validation error is shown and manager is not called`() = runTest(testScheduler) {
        // given
        component.onScheduleTypeSelect(AddNewReminderComponent.ScheduleType.WEEKLY_DAY_PERIOD)
        DayOfWeek.entries.forEach(component::onPeriodDayToggle)

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.validationError).isEqualTo(AddNewReminderComponent.ValidationError.EMPTY_DAYS)
        assertThat(reminderManager.scheduledWeeklyPeriods).isEqualTo(emptyList())
    }

    @Test
    fun `when weekly period has invalid time range then validation error is shown`() = runTest(testScheduler) {
        // given
        component.onScheduleTypeSelect(AddNewReminderComponent.ScheduleType.WEEKLY_DAY_PERIOD)
        component.onPeriodTimeFromSelect(LocalTime(hour = 18, minute = 0))
        component.onPeriodTimeUntilSelect(LocalTime(hour = 9, minute = 0))

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.validationError).isEqualTo(AddNewReminderComponent.ValidationError.INVALID_PERIOD)
        assertThat(reminderManager.scheduledWeeklyPeriods).isEqualTo(emptyList())
    }

    @Test
    fun `when manager fails then error output is published`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("create failed")
        reminderManager.scheduleException = exception

        // when
        component.onCreateClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): AddNewReminderComponent =
        AddNewReminderComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            reminderManager = reminderManager,
            addNewReminderOutput = { componentOutput.add(it) },
        )

    private data class WeeklyPeriodCall(
        val from: LocalTime,
        val until: LocalTime,
        val intervalMinutes: Int,
        val days: List<DayOfWeek>,
    )

    private class FakeReminderManager : BlinklyReminderManager {
        val scheduledDaily: MutableList<LocalTime> = mutableListOf()
        val scheduledWeeklySingle: MutableList<Pair<LocalTime, DayOfWeek>> = mutableListOf()
        val scheduledWeeklyPeriods: MutableList<WeeklyPeriodCall> = mutableListOf()
        var scheduleException: Throwable? = null

        override fun createdReminders(): Flow<List<Reminder>> = flowOf(emptyList())

        override suspend fun scheduleDaily(time: LocalTime) {
            scheduleException?.let { throw it }
            scheduledDaily.add(time)
        }

        override suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek) {
            scheduleException?.let { throw it }
            scheduledWeeklySingle.add(time to dayOfWeek)
        }

        override suspend fun scheduleWeeklyDayPeriod(
            from: LocalTime,
            until: LocalTime,
            intervalMinutes: Int,
            days: List<DayOfWeek>,
        ) {
            scheduleException?.let { throw it }
            scheduledWeeklyPeriods.add(
                WeeklyPeriodCall(
                    from = from,
                    until = until,
                    intervalMinutes = intervalMinutes,
                    days = days,
                )
            )
        }

        override suspend fun rescheduleAll() = Unit
        override suspend fun cancel(uuid: String) = Unit
        override suspend fun cancelAll() = Unit
    }
}
