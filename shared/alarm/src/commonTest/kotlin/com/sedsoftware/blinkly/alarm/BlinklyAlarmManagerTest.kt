package com.sedsoftware.blinkly.alarm

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.sedsoftware.blinkly.alarm.impl.BlinklyAlarmManagerImpl
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.fakes.FakeAlarmeePlatformConfiguration
import com.sedsoftware.blinkly.fakes.FakeAlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.RepeatInterval
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

class BlinklyAlarmManagerTest {

    private val fakeService: FakeAlarmeeService = FakeAlarmeeService()

    private val timeUtils: BlinklyTimeUtils = mock {
        every { now() } returns Clock.System.now()
        every { timeZone() } returns TimeZone.UTC
    }

    private val manager: BlinklyAlarmManager = BlinklyAlarmManagerImpl(
        timeUtils = timeUtils,
        notificationConfigurations = mapOf(ReminderType.TWENTY_X3 to ReminderConfig(FAKE_TITLE, FAKE_TITLE)),
        platformConfiguration = FakeAlarmeePlatformConfiguration(),
        alarmeeService = fakeService,
    )

    @BeforeTest
    fun setup() {
        fakeService.clear()
    }

    @Test
    fun `when scheduleDaily called then schedule daily reminder`() = runTest {
        // given
        val uuid = Uuid.random().toString()
        val type = ReminderType.TWENTY_X3
        val date = LocalDateTime(2026, 3, 1, 12, 35)

        // when
        manager.scheduleDaily(uuid, type, date)
        val alarms = fakeService.currentAlarms()
        val scheduledAlarmee: Alarmee? = alarms[uuid]

        // then
        assertThat(alarms).isNotEmpty()
        assertThat(scheduledAlarmee).isNotNull()
        scheduledAlarmee as Alarmee
        assertThat(scheduledAlarmee.uuid).isEqualTo(uuid)
        assertThat(scheduledAlarmee.notificationTitle).isEqualTo(FAKE_TITLE)
        assertThat(scheduledAlarmee.notificationBody).isEqualTo(FAKE_TITLE)
        assertThat(scheduledAlarmee.scheduledDateTime).isEqualTo(date)
        assertThat(scheduledAlarmee.repeatInterval).isEqualTo(RepeatInterval.Daily)
    }

    @Test
    fun `when scheduleWeekly called then schedule weekly reminder`() = runTest {
        // given
        val uuid = Uuid.random().toString()
        val type = ReminderType.TWENTY_X3
        val date = LocalDateTime(2026, 3, 1, 12, 35)

        // when
        manager.scheduleWeekly(uuid, type, date)
        val alarms = fakeService.currentAlarms()
        val scheduledAlarmee: Alarmee? = alarms[uuid]

        // then
        assertThat(alarms).isNotEmpty()
        assertThat(scheduledAlarmee).isNotNull()
        scheduledAlarmee as Alarmee
        assertThat(scheduledAlarmee.uuid).isEqualTo(uuid)
        assertThat(scheduledAlarmee.notificationTitle).isEqualTo(FAKE_TITLE)
        assertThat(scheduledAlarmee.notificationBody).isEqualTo(FAKE_TITLE)
        assertThat(scheduledAlarmee.scheduledDateTime).isEqualTo(date)
        assertThat(scheduledAlarmee.repeatInterval).isEqualTo(RepeatInterval.Weekly)
    }

    @Test
    fun `when cancel called then cancel selected reminder`() = runTest {
        // given
        val uuid1 = Uuid.random().toString()
        val type1 = ReminderType.TWENTY_X3
        val date1 = LocalDateTime(2026, 3, 1, 12, 35)
        val uuid2 = Uuid.random().toString()
        val type2 = ReminderType.TWENTY_X3
        val date2 = LocalDateTime(2026, 3, 4, 2, 3)
        manager.scheduleDaily(uuid1, type1, date1)
        manager.scheduleDaily(uuid2, type2, date2)
        var alarms = fakeService.currentAlarms()
        assertThat(alarms.size).isEqualTo(2)

        // when
        manager.cancel(uuid1)
        alarms = fakeService.currentAlarms()

        // then
        assertThat(alarms.size).isEqualTo(1)
        assertThat(alarms[uuid2]).isNotNull()
    }

    @Test
    fun `when cancelAll called then cancel all reminders`() = runTest {
        // given
        val uuid1 = Uuid.random().toString()
        val type1 = ReminderType.TWENTY_X3
        val date1 = LocalDateTime(2026, 3, 1, 12, 35)
        val uuid2 = Uuid.random().toString()
        val type2 = ReminderType.TWENTY_X3
        val date2 = LocalDateTime(2026, 3, 4, 2, 3)
        manager.scheduleDaily(uuid1, type1, date1)
        manager.scheduleDaily(uuid2, type2, date2)
        var alarms = fakeService.currentAlarms()
        assertThat(alarms.size).isEqualTo(2)

        // when
        manager.cancelAll()
        alarms = fakeService.currentAlarms()

        // then
        assertThat(alarms).isEmpty()
    }

    private companion object {
        const val FAKE_TITLE = "test"
    }
}
