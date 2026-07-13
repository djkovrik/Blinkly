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
import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class RemindersTabComponentTest : ComponentTest<RemindersTabComponent>() {

    private val reminderManager: FakeReminderManager = FakeReminderManager()
    private val notifier: FakeNotifier = FakeNotifier()

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
        // given
        notifier.permissionGranted = true

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Reminders.OpenAddNew)
    }

    @Test
    fun `when add new clicked without permission then request permission and open after grant`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(notifier.requestCount).isEqualTo(1)
        assertThat(componentOutput.contains(ComponentOutput.Reminders.OpenAddNew)).isFalse()

        // when
        notifier.permissionEvents.emit(PermissionResult.Granted)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Reminders.OpenAddNew)
    }

    @Test
    fun `when notification permission denied then add new screen is not opened`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()
        notifier.permissionEvents.emit(PermissionResult.Denied)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutput.contains(ComponentOutput.Reminders.OpenAddNew)).isFalse()
    }

    @Test
    fun `when notification permission denied always then settings error is emitted`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()
        notifier.permissionEvents.emit(PermissionResult.DeniedAlways)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(
            componentOutput
                .filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable is BlinklyError.NotificationPermissionDeniedAlways }
        ).isTrue()
    }

    @Test
    fun `when notification permission check fails then error is emitted`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("permission check failed")
        notifier.checkException = exception

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.NotificationPermissionChecking>(exception)).isTrue()
    }

    @Test
    fun `when notification permission request fails then error is emitted`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("permission request failed")
        notifier.requestException = exception

        // when
        component.onAddNewClick()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.NotificationPermissionRequesting>(exception)).isTrue()
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
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.ReminderDeleting>(exception)).isTrue()
    }

    @Test
    fun `when delete already pending then next delete is ignored`() = runTest(testScheduler) {
        // given
        val firstReminder = reminder(
            uuid = "first",
            date = LocalDateTime(year = 2026, month = 6, day = 20, hour = 10, minute = 0),
            interval = ReminderInterval.DAILY,
        )
        val secondReminder = reminder(
            uuid = "second",
            date = LocalDateTime(year = 2026, month = 6, day = 20, hour = 11, minute = 0),
            interval = ReminderInterval.DAILY,
        )
        val cancelGate = CompletableDeferred<Unit>()
        reminderManager.cancelGate = cancelGate
        reminderManager.reminders.value = listOf(firstReminder, secondReminder)
        testScheduler.advanceUntilIdle()

        // when
        component.onDeleteReminder("first")
        testScheduler.advanceUntilIdle()
        component.onDeleteReminder("second")
        testScheduler.advanceUntilIdle()
        cancelGate.complete(Unit)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(reminderManager.cancelled).isEqualTo(listOf("first"))
        assertThat(component.model.value.deletedReminder?.uuid).isEqualTo("first")
        assertThat(component.model.value.reminders.map { it.uuid }).isEqualTo(listOf("second"))
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

    private class FakeNotifier : BlinklyNotifier {
        val permissionEvents: MutableSharedFlow<PermissionResult> = MutableSharedFlow()
        var permissionGranted: Boolean = false
        var requestCount: Int = 0
        var checkException: Throwable? = null
        var requestException: Throwable? = null

        override fun permissionEvents(): Flow<PermissionResult> = permissionEvents

        override suspend fun isNotificationPermissionGranted(): Boolean {
            checkException?.let { throw it }
            return permissionGranted
        }

        override suspend fun requestNotificationPermission() {
            requestCount += 1
            requestException?.let { throw it }
        }

        override suspend fun achievementUnlocked(type: AchievementType) = Unit

        override fun unlockedAchievements(): Flow<AchievementType> =
            MutableSharedFlow()
    }

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
        var cancelGate: CompletableDeferred<Unit>? = null

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
            cancelGate?.await()
            cancelException?.let { throw it }
            cancelled.add(uuid)
            reminders.value = reminders.value.filterNot { it.uuid == uuid }
        }

        override suspend fun cancelAll() = Unit
    }
}
