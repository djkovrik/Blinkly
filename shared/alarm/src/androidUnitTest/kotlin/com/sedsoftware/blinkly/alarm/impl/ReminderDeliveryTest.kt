package com.sedsoftware.blinkly.alarm.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ReminderDeliveryTest {

    @Test
    fun `OEM notify failure is contained after chaining the next alarm`() = runTest {
        val events = mutableListOf<String>()
        val failure = NullPointerException("NotificationRecord.getUserId")
        deliverReminder(
            scheduleNext = { events += "scheduled" },
            showNotification = { events += "notify"; throw failure },
            onFailure = { assertSame(failure, it); events += "reported" },
        )
        assertEquals(listOf("scheduled", "notify", "reported"), events)
    }

    @Test
    fun `schedule failure does not suppress the current notification`() = runTest {
        val events = mutableListOf<String>()
        deliverReminder(
            scheduleNext = { throw SecurityException() },
            showNotification = { events += "notify" },
            onFailure = { events += "reported" },
        )
        assertEquals(listOf("reported", "notify"), events)
    }

    @Test
    fun `successful delivery schedules and publishes once`() = runTest {
        val events = mutableListOf<String>()
        deliverReminder(
            scheduleNext = { events += "scheduled" },
            showNotification = { events += "notify" },
            onFailure = { error("Unexpected failure") },
        )
        assertEquals(listOf("scheduled", "notify"), events)
    }

    @Test
    fun `cancellation is propagated`() = runTest {
        assertFailsWith<CancellationException> {
            deliverReminder(
                scheduleNext = {},
                showNotification = { throw CancellationException() },
                onFailure = { error("Cancellation must not be reported") },
            )
        }
    }

    @Test
    fun `future anchor stays on the schedule grid`() {
        assertEquals(110L, nextReminderTrigger(now = 100L, anchor = 110L, interval = 20L))
    }

    @Test
    fun `late alarms skip missed slots without a burst`() {
        assertEquals(130L, nextReminderTrigger(now = 125L, anchor = 90L, interval = 20L))
        assertEquals(110L, nextReminderTrigger(now = 90L, anchor = 90L, interval = 20L))
    }

    @Test
    fun `legacy alarms without an anchor continue repeating`() {
        assertEquals(120L, nextReminderTrigger(now = 100L, anchor = -1L, interval = 20L))
    }
}
