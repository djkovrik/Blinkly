package com.sedsoftware.blinkly.component.sync.mapper

import com.sedsoftware.blinkly.component.sync.dto.BlinklyRemoteSnapshotDto
import com.sedsoftware.blinkly.component.sync.dto.SettingsDto
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklySettingsSnapshot
import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class RemoteSnapshotMappersTest {

    @Test
    fun `schedule DTO round trips every configuration`() {
        val schedules = listOf(
            ReminderSchedule(
                "daily",
                ReminderType.TWENTY_X3,
                ReminderScheduleConfiguration.Daily(LocalTime(10, 0)),
            ),
            ReminderSchedule(
                "weekly",
                ReminderType.TWENTY_X3,
                ReminderScheduleConfiguration.WeeklySingle(LocalTime(14, 30), DayOfWeek.WEDNESDAY),
            ),
            ReminderSchedule(
                "period",
                ReminderType.TWENTY_X3,
                ReminderScheduleConfiguration.WorkdayPeriod(
                    LocalTime(9, 0),
                    LocalTime(18, 0),
                    20,
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                ),
            ),
        )

        assertEquals(schedules, schedules.map { schedule -> schedule.toDto().toDomain() })
    }

    @Test
    fun `remote snapshot round trips schedule parent and physical children`() {
        val schedule = ReminderSchedule(
            "period",
            ReminderType.TWENTY_X3,
            ReminderScheduleConfiguration.WorkdayPeriod(
                LocalTime(9, 0),
                LocalTime(18, 0),
                20,
                listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            ),
        )
        val reminder = Reminder(
            uuid = "alarm",
            scheduleId = schedule.id,
            date = LocalDateTime(2026, 7, 14, 9, 20),
            type = ReminderType.TWENTY_X3,
            interval = ReminderInterval.WEEKLY,
        )
        val snapshot = RemoteBlinklySnapshot(
            updatedAt = Instant.fromEpochMilliseconds(3),
            lastSyncedAt = Instant.fromEpochMilliseconds(2),
            settings = settingsSnapshot(),
            database = BlinklyDatabaseSnapshot(emptyList(), emptyList(), listOf(schedule), listOf(reminder)),
            databaseUpdatedAt = Instant.fromEpochMilliseconds(2),
            settingsUpdatedAt = Instant.fromEpochMilliseconds(1),
        )

        assertEquals(snapshot, snapshot.toDto().toDomain())
    }

    @Test
    fun `old sync schema is rejected strictly`() {
        val dto = BlinklyRemoteSnapshotDto(
            schemaVersion = 2,
            updatedAtEpochMillis = 1,
            settings = settingsDto(),
            reminderSchedules = emptyList(),
            reminders = emptyList(),
        )

        assertFailsWith<BlinklyError.SyncConflictFailed> { dto.toDomain() }
    }

    private fun settingsSnapshot(): BlinklySettingsSnapshot =
        BlinklySettingsSnapshot(
            blinkBreakCount = 60,
            nearFarFocusCount = 10,
            nearFarFocusDuration = 5f,
            diagonalGazesCount = 5,
            diagonalGazesDuration = 3f,
            figureEightCount = 10,
            clockRollsEachSide = 5,
            palmingDuration = 120,
            themeState = ThemeState.SYSTEM,
            lightThemeWorkoutIndex = 0,
            darkThemeWorkoutIndex = 0,
            lastTreeProgressCheckDate = null,
            displayedHighlights = emptyList(),
            currentHighlightDate = null,
            onboardingDisplayed = false,
        )

    private fun settingsDto(): SettingsDto = settingsSnapshot().toDto()
}
