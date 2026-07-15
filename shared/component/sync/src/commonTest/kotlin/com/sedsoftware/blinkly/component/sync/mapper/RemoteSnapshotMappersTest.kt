package com.sedsoftware.blinkly.component.sync.mapper

import com.sedsoftware.blinkly.component.sync.dto.BlinklyRemoteSnapshotDto
import com.sedsoftware.blinkly.component.sync.dto.ReminderScheduleDto
import com.sedsoftware.blinkly.component.sync.dto.SettingsDto
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklySettingsSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.RemoteBlinklySnapshot
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
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
    fun `remote snapshot round trips exercises achievements and optional settings values`() {
        val completedAt = Instant.fromEpochMilliseconds(4)
        val unlockedAt = Instant.fromEpochMilliseconds(5)
        val snapshot = RemoteBlinklySnapshot(
            updatedAt = Instant.fromEpochMilliseconds(6),
            lastSyncedAt = null,
            settings = settingsSnapshot().copy(
                lastTreeProgressCheckDate = LocalDate(2026, 7, 14),
                displayedHighlights = listOf(1, 3),
                currentHighlightDate = LocalDate(2026, 7, 15),
            ),
            database = BlinklyDatabaseSnapshot(
                exercises = listOf(Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, completedAt)),
                achievements = listOf(
                    Achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt),
                    Achievement(AchievementType.THINK_TANK, AchievementLevel.HIDDEN, null),
                ),
                reminderSchedules = emptyList(),
                reminders = emptyList(),
            ),
            databaseUpdatedAt = Instant.fromEpochMilliseconds(5),
            settingsUpdatedAt = Instant.fromEpochMilliseconds(4),
        )

        assertEquals(snapshot, snapshot.toDto().toDomain())
    }

    @Test
    fun `legacy snapshot timestamps fall back to overall update time`() {
        val updatedAt = Instant.fromEpochMilliseconds(42)
        val dto = BlinklyRemoteSnapshotDto(
            schemaVersion = SYNC_SCHEMA_VERSION,
            updatedAtEpochMillis = updatedAt.toEpochMilliseconds(),
            databaseUpdatedAtEpochMillis = null,
            settingsUpdatedAtEpochMillis = null,
            settings = settingsDto(),
            reminderSchedules = emptyList(),
            reminders = emptyList(),
        )

        val snapshot = dto.toDomain()

        assertEquals(null, snapshot.lastSyncedAt)
        assertEquals(updatedAt, snapshot.databaseUpdatedAt)
        assertEquals(updatedAt, snapshot.settingsUpdatedAt)
    }

    @Test
    fun `malformed reminder schedules are rejected`() {
        val weekly = ReminderScheduleDto(
            id = "weekly",
            reminderType = ReminderType.TWENTY_X3.name,
            scheduleType = "WEEKLY_SINGLE",
            timeFromIso = "14:30",
            weekDays = emptyList(),
        )
        val periodWithoutEnd = ReminderScheduleDto(
            id = "period-without-end",
            reminderType = ReminderType.TWENTY_X3.name,
            scheduleType = "WORKDAY_PERIOD",
            timeFromIso = "09:00",
            intervalMinutes = 20,
            weekDays = listOf(DayOfWeek.MONDAY.name),
        )
        val periodWithoutInterval = ReminderScheduleDto(
            id = "period-without-interval",
            reminderType = ReminderType.TWENTY_X3.name,
            scheduleType = "WORKDAY_PERIOD",
            timeFromIso = "09:00",
            timeUntilIso = "18:00",
            weekDays = listOf(DayOfWeek.MONDAY.name),
        )

        assertFailsWith<IllegalArgumentException> { weekly.toDomain() }
        assertFailsWith<IllegalArgumentException> { periodWithoutEnd.toDomain() }
        assertFailsWith<IllegalArgumentException> { periodWithoutInterval.toDomain() }
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
