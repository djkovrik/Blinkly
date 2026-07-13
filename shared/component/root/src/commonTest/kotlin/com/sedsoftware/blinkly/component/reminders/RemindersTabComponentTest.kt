package com.sedsoftware.blinkly.component.reminders

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.reminders.integration.RemindersTabComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ScheduledReminder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class RemindersTabComponentTest : ComponentTest<RemindersTabComponent>() {

    private val reminderManager: FakeReminderManager = FakeReminderManager()
    private val notifier: FakeNotifier = FakeNotifier()

    @Test
    fun `workday period with many alarms maps to one sorted item`() = runTest(testScheduler) {
        reminderManager.schedules.value = listOf(
            scheduled(
                id = "period",
                configuration = ReminderScheduleConfiguration.WorkdayPeriod(
                    from = LocalTime(9, 0),
                    until = LocalTime(18, 0),
                    intervalMinutes = 20,
                    days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                ),
                dates = listOf(
                    LocalDateTime(2026, 7, 14, 9, 40),
                    LocalDateTime(2026, 7, 14, 9, 20),
                    LocalDateTime(2026, 7, 15, 9, 20),
                ),
            ),
            scheduled(
                id = "daily",
                configuration = ReminderScheduleConfiguration.Daily(LocalTime(8, 0)),
                dates = listOf(LocalDateTime(2026, 7, 14, 8, 0)),
            ),
        )

        testScheduler.advanceUntilIdle()

        val model = component.model.value
        assertThat(model.reminders.map(RemindersTabComponent.ReminderItem::id)).isEqualTo(listOf("daily", "period"))
        val period = model.reminders.last()
        assertThat(period.nextAt).isEqualTo(LocalDateTime(2026, 7, 14, 9, 20))
        assertThat(period.schedule).isEqualTo(
            RemindersTabComponent.Schedule.WorkdayPeriod(
                LocalTime(9, 0),
                LocalTime(18, 0),
                20,
                listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            )
        )
    }

    @Test
    fun `daily and weekly schedules remain separate items`() = runTest(testScheduler) {
        reminderManager.schedules.value = listOf(
            scheduled(
                "weekly",
                ReminderScheduleConfiguration.WeeklySingle(LocalTime(14, 30), DayOfWeek.MONDAY),
                listOf(LocalDateTime(2026, 7, 20, 14, 30)),
            ),
            scheduled(
                "daily",
                ReminderScheduleConfiguration.Daily(LocalTime(10, 0)),
                listOf(LocalDateTime(2026, 7, 14, 10, 0)),
            ),
        )

        testScheduler.advanceUntilIdle()

        assertThat(component.model.value.reminders.map(RemindersTabComponent.ReminderItem::id))
            .isEqualTo(listOf("daily", "weekly"))
        assertThat(component.model.value.reminders.first().schedule)
            .isEqualTo(RemindersTabComponent.Schedule.Daily(LocalTime(10, 0)))
        assertThat(component.model.value.reminders.last().schedule)
            .isEqualTo(RemindersTabComponent.Schedule.Weekly(LocalTime(14, 30), DayOfWeek.MONDAY))
    }

    @Test
    fun `add new checks permission and opens screen`() = runTest(testScheduler) {
        notifier.permissionGranted = true

        component.onAddNewClick()
        testScheduler.advanceUntilIdle()

        assertThat(componentOutput).contains(ComponentOutput.Reminders.OpenAddNew)
    }

    @Test
    fun `permission grant after request opens screen`() = runTest(testScheduler) {
        testScheduler.advanceUntilIdle()
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()
        assertThat(notifier.requestCount).isEqualTo(1)
        assertThat(componentOutput.contains(ComponentOutput.Reminders.OpenAddNew)).isFalse()

        notifier.permissionEvents.emit(PermissionResult.Granted)
        testScheduler.advanceUntilIdle()

        assertThat(componentOutput).contains(ComponentOutput.Reminders.OpenAddNew)
    }

    @Test
    fun `permission denied always emits settings error`() = runTest(testScheduler) {
        testScheduler.advanceUntilIdle()
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()
        notifier.permissionEvents.emit(PermissionResult.DeniedAlways)
        testScheduler.advanceUntilIdle()

        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable is BlinklyError.NotificationPermissionDeniedAlways }
        ).isTrue()
    }

    @Test
    fun `deleting workday period removes one item and undo recreates full schedule once`() = runTest(testScheduler) {
        val period = scheduled(
            id = "period",
            configuration = ReminderScheduleConfiguration.WorkdayPeriod(
                LocalTime(9, 0),
                LocalTime(18, 0),
                20,
                listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            ),
            dates = listOf(
                LocalDateTime(2026, 7, 14, 9, 20),
                LocalDateTime(2026, 7, 14, 9, 40),
            ),
        )
        reminderManager.schedules.value = listOf(period)
        testScheduler.advanceUntilIdle()

        component.onDeleteReminder("period")
        testScheduler.advanceUntilIdle()

        assertThat(reminderManager.cancelled).isEqualTo(listOf("period"))
        assertThat(component.model.value.reminders).isEqualTo(emptyList())
        assertThat(component.model.value.deletedReminder?.id).isEqualTo("period")

        component.onUndoDelete()
        testScheduler.advanceUntilIdle()

        assertThat(reminderManager.scheduledPeriods).isEqualTo(
            listOf(
                PeriodCall(
                    LocalTime(9, 0),
                    LocalTime(18, 0),
                    20,
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                )
            )
        )
        assertThat(component.model.value.deletedReminder).isNull()
    }

    @Test
    fun `daily and weekly undo keep original configuration`() = runTest(testScheduler) {
        val daily = scheduled(
            "daily",
            ReminderScheduleConfiguration.Daily(LocalTime(10, 0)),
            listOf(LocalDateTime(2026, 7, 14, 10, 0)),
        )
        reminderManager.schedules.value = listOf(daily)
        testScheduler.advanceUntilIdle()
        component.onDeleteReminder("daily")
        testScheduler.advanceUntilIdle()
        component.onUndoDelete()
        testScheduler.advanceUntilIdle()
        assertThat(reminderManager.scheduledDaily).isEqualTo(listOf(LocalTime(10, 0)))

        val weekly = scheduled(
            "weekly",
            ReminderScheduleConfiguration.WeeklySingle(LocalTime(12, 15), DayOfWeek.WEDNESDAY),
            listOf(LocalDateTime(2026, 7, 15, 12, 15)),
        )
        reminderManager.schedules.value = listOf(weekly)
        testScheduler.advanceUntilIdle()
        component.onDeleteReminder("weekly")
        testScheduler.advanceUntilIdle()
        component.onUndoDelete()
        testScheduler.advanceUntilIdle()
        assertThat(reminderManager.scheduledWeekly).isEqualTo(listOf(LocalTime(12, 15) to DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `delete error keeps schedule and emits error`() = runTest(testScheduler) {
        val exception = IllegalStateException("delete failed")
        reminderManager.cancelException = exception
        reminderManager.schedules.value = listOf(
            scheduled(
                "daily",
                ReminderScheduleConfiguration.Daily(LocalTime(10, 0)),
                listOf(LocalDateTime(2026, 7, 14, 10, 0)),
            )
        )
        testScheduler.advanceUntilIdle()

        component.onDeleteReminder("daily")
        testScheduler.advanceUntilIdle()

        assertThat(component.model.value.reminders.map(RemindersTabComponent.ReminderItem::id)).isEqualTo(listOf("daily"))
        assertThat(component.model.value.deletedReminder).isNull()
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.ReminderDeleting>(exception)).isTrue()
    }

    @Test
    fun `pending delete blocks second schedule deletion`() = runTest(testScheduler) {
        val first = scheduled(
            "first",
            ReminderScheduleConfiguration.Daily(LocalTime(10, 0)),
            listOf(LocalDateTime(2026, 7, 14, 10, 0)),
        )
        val second = scheduled(
            "second",
            ReminderScheduleConfiguration.Daily(LocalTime(11, 0)),
            listOf(LocalDateTime(2026, 7, 14, 11, 0)),
        )
        val gate = CompletableDeferred<Unit>()
        reminderManager.cancelGate = gate
        reminderManager.schedules.value = listOf(first, second)
        testScheduler.advanceUntilIdle()

        component.onDeleteReminder("first")
        testScheduler.advanceUntilIdle()
        component.onDeleteReminder("second")
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertThat(reminderManager.cancelled).isEqualTo(listOf("first"))
        assertThat(component.model.value.reminders.map(RemindersTabComponent.ReminderItem::id)).isEqualTo(listOf("second"))
    }

    override fun createComponent(): RemindersTabComponent =
        RemindersTabComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            reminderManager = reminderManager,
            notifier = notifier,
            remindersTabOutput = { componentOutput.add(it) },
        )

    private fun scheduled(
        id: String,
        configuration: ReminderScheduleConfiguration,
        dates: List<LocalDateTime>,
    ): ScheduledReminder =
        ScheduledReminder(
            schedule = ReminderSchedule(id, ReminderType.TWENTY_X3, configuration),
            alarms = dates.mapIndexed { index, date ->
                Reminder(
                    uuid = "$id-$index",
                    scheduleId = id,
                    date = date,
                    type = ReminderType.TWENTY_X3,
                    interval = if (configuration is ReminderScheduleConfiguration.Daily) {
                        ReminderInterval.DAILY
                    } else {
                        ReminderInterval.WEEKLY
                    },
                )
            },
        )

    private class FakeNotifier : BlinklyNotifier {
        val permissionEvents: MutableSharedFlow<PermissionResult> = MutableSharedFlow()
        var permissionGranted: Boolean = false
        var requestCount: Int = 0

        override fun permissionEvents(): Flow<PermissionResult> = permissionEvents
        override suspend fun isNotificationPermissionGranted(): Boolean = permissionGranted
        override suspend fun requestNotificationPermission() { requestCount += 1 }
        override suspend fun achievementUnlocked(type: AchievementType) = Unit
        override fun unlockedAchievements(): Flow<AchievementType> = MutableSharedFlow()
    }

    private data class PeriodCall(
        val from: LocalTime,
        val until: LocalTime,
        val intervalMinutes: Int,
        val days: List<DayOfWeek>,
    )

    private class FakeReminderManager : BlinklyReminderManager {
        val schedules: MutableStateFlow<List<ScheduledReminder>> = MutableStateFlow(emptyList())
        val cancelled: MutableList<String> = mutableListOf()
        val scheduledDaily: MutableList<LocalTime> = mutableListOf()
        val scheduledWeekly: MutableList<Pair<LocalTime, DayOfWeek>> = mutableListOf()
        val scheduledPeriods: MutableList<PeriodCall> = mutableListOf()
        var cancelException: Throwable? = null
        var cancelGate: CompletableDeferred<Unit>? = null

        override fun createdReminders(): Flow<List<Reminder>> = schedules.map { items -> items.flatMap(ScheduledReminder::alarms) }
        override fun createdSchedules(): Flow<List<ScheduledReminder>> = schedules
        override suspend fun scheduleDaily(time: LocalTime) { scheduledDaily.add(time) }
        override suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek) {
            scheduledWeekly.add(time to dayOfWeek)
        }
        override suspend fun scheduleWeeklyDayPeriod(
            from: LocalTime,
            until: LocalTime,
            intervalMinutes: Int,
            days: List<DayOfWeek>,
        ) {
            scheduledPeriods.add(PeriodCall(from, until, intervalMinutes, days))
        }
        override suspend fun rescheduleAll() = Unit
        override suspend fun cancelSchedule(scheduleId: String) {
            cancelGate?.await()
            cancelException?.let { throw it }
            cancelled.add(scheduleId)
            schedules.value = schedules.value.filterNot { it.schedule.id == scheduleId }
        }
        override suspend fun cancelAll() = Unit
    }
}
