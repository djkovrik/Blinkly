package com.sedsoftware.blinkly.component.sync.mapper

import com.sedsoftware.blinkly.component.sync.dto.AchievementDto
import com.sedsoftware.blinkly.component.sync.dto.BlinklyRemoteSnapshotDto
import com.sedsoftware.blinkly.component.sync.dto.ExerciseDto
import com.sedsoftware.blinkly.component.sync.dto.ReminderDto
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
import com.sedsoftware.blinkly.domain.model.ReminderScheduleType
import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

internal const val SYNC_SCHEMA_VERSION = 3

internal fun RemoteBlinklySnapshot.toDto(): BlinklyRemoteSnapshotDto =
    BlinklyRemoteSnapshotDto(
        schemaVersion = SYNC_SCHEMA_VERSION,
        updatedAtEpochMillis = updatedAt.toEpochMilliseconds(),
        lastSyncedAtEpochMillis = lastSyncedAt?.toEpochMilliseconds(),
        databaseUpdatedAtEpochMillis = databaseUpdatedAt.toEpochMilliseconds(),
        settingsUpdatedAtEpochMillis = settingsUpdatedAt.toEpochMilliseconds(),
        settings = settings.toDto(),
        exercises = database.exercises.map(Exercise::toDto),
        achievements = database.achievements.map(Achievement::toDto),
        reminderSchedules = database.reminderSchedules.map(ReminderSchedule::toDto),
        reminders = database.reminders.map(Reminder::toDto),
    )

internal fun BlinklyRemoteSnapshotDto.toDomain(): RemoteBlinklySnapshot {
    if (schemaVersion != SYNC_SCHEMA_VERSION) {
        throw BlinklyError.SyncConflictFailed(
            IllegalArgumentException("Unsupported sync schema version: $schemaVersion")
        )
    }

    return RemoteBlinklySnapshot(
        updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMillis),
        lastSyncedAt = lastSyncedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
        settings = settings.toDomain(),
        database = BlinklyDatabaseSnapshot(
            exercises = exercises.map(ExerciseDto::toDomain),
            achievements = achievements.map(AchievementDto::toDomain),
            reminderSchedules = reminderSchedules.map(ReminderScheduleDto::toDomain),
            reminders = reminders.map(ReminderDto::toDomain),
        ),
        databaseUpdatedAt = Instant.fromEpochMilliseconds(databaseUpdatedAtEpochMillis ?: updatedAtEpochMillis),
        settingsUpdatedAt = Instant.fromEpochMilliseconds(settingsUpdatedAtEpochMillis ?: updatedAtEpochMillis),
    )
}

internal fun BlinklySettingsSnapshot.toDto(): SettingsDto =
    SettingsDto(
        blinkBreakCount = blinkBreakCount,
        nearFarFocusCount = nearFarFocusCount,
        nearFarFocusDuration = nearFarFocusDuration,
        diagonalGazesCount = diagonalGazesCount,
        diagonalGazesDuration = diagonalGazesDuration,
        figureEightCount = figureEightCount,
        clockRollsEachSide = clockRollsEachSide,
        palmingDuration = palmingDuration,
        themeState = themeState.name,
        lightThemeWorkoutIndex = lightThemeWorkoutIndex,
        darkThemeWorkoutIndex = darkThemeWorkoutIndex,
        lastTreeProgressCheckDateIso = lastTreeProgressCheckDate?.toString(),
        displayedHighlights = displayedHighlights,
        currentHighlightDateIso = currentHighlightDate?.toString(),
        onboardingDisplayed = onboardingDisplayed,
    )

internal fun SettingsDto.toDomain(): BlinklySettingsSnapshot =
    BlinklySettingsSnapshot(
        blinkBreakCount = blinkBreakCount,
        nearFarFocusCount = nearFarFocusCount,
        nearFarFocusDuration = nearFarFocusDuration,
        diagonalGazesCount = diagonalGazesCount,
        diagonalGazesDuration = diagonalGazesDuration,
        figureEightCount = figureEightCount,
        clockRollsEachSide = clockRollsEachSide,
        palmingDuration = palmingDuration,
        themeState = enumValueOf(themeState),
        lightThemeWorkoutIndex = lightThemeWorkoutIndex,
        darkThemeWorkoutIndex = darkThemeWorkoutIndex,
        lastTreeProgressCheckDate = lastTreeProgressCheckDateIso?.let(LocalDate::parse),
        displayedHighlights = displayedHighlights,
        currentHighlightDate = currentHighlightDateIso?.let(LocalDate::parse),
        onboardingDisplayed = onboardingDisplayed,
    )

internal fun Exercise.toDto(): ExerciseDto =
    ExerciseDto(
        type = type.name,
        block = block.name,
        completedAtEpochMillis = completedAt.toEpochMilliseconds(),
    )

internal fun ExerciseDto.toDomain(): Exercise =
    Exercise(
        type = enumValueOf<ExerciseType>(type),
        block = enumValueOf<ExerciseBlock>(block),
        completedAt = Instant.fromEpochMilliseconds(completedAtEpochMillis),
    )

internal fun Achievement.toDto(): AchievementDto =
    AchievementDto(
        type = type.name,
        level = level.name,
        unlockedAtEpochMillis = unlockedAt?.toEpochMilliseconds(),
    )

internal fun AchievementDto.toDomain(): Achievement =
    Achievement(
        type = enumValueOf<AchievementType>(type),
        level = enumValueOf<AchievementLevel>(level),
        unlockedAt = unlockedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
    )

internal fun Reminder.toDto(): ReminderDto =
    ReminderDto(
        uuid = uuid,
        scheduleId = scheduleId,
        dateIso = date.toString(),
        type = type.name,
        interval = interval.name,
    )

internal fun ReminderDto.toDomain(): Reminder =
    Reminder(
        uuid = uuid,
        scheduleId = scheduleId,
        date = LocalDateTime.parse(dateIso),
        type = enumValueOf<ReminderType>(type),
        interval = enumValueOf<ReminderInterval>(interval),
    )

internal fun ReminderSchedule.toDto(): ReminderScheduleDto {
    val config = configuration
    return ReminderScheduleDto(
        id = id,
        reminderType = reminderType.name,
        scheduleType = when (config) {
            is ReminderScheduleConfiguration.Daily -> ReminderScheduleType.DAILY
            is ReminderScheduleConfiguration.WeeklySingle -> ReminderScheduleType.WEEKLY_SINGLE
            is ReminderScheduleConfiguration.WorkdayPeriod -> ReminderScheduleType.WORKDAY_PERIOD
        }.name,
        timeFromIso = when (config) {
            is ReminderScheduleConfiguration.Daily -> config.time
            is ReminderScheduleConfiguration.WeeklySingle -> config.time
            is ReminderScheduleConfiguration.WorkdayPeriod -> config.from
        }.toString(),
        timeUntilIso = (config as? ReminderScheduleConfiguration.WorkdayPeriod)?.until?.toString(),
        intervalMinutes = (config as? ReminderScheduleConfiguration.WorkdayPeriod)?.intervalMinutes,
        weekDays = when (config) {
            is ReminderScheduleConfiguration.Daily -> emptyList()
            is ReminderScheduleConfiguration.WeeklySingle -> listOf(config.day.name)
            is ReminderScheduleConfiguration.WorkdayPeriod -> config.days.map(DayOfWeek::name)
        },
    )
}

internal fun ReminderScheduleDto.toDomain(): ReminderSchedule {
    val timeFrom = LocalTime.parse(timeFromIso)
    return ReminderSchedule(
        id = id,
        reminderType = enumValueOf<ReminderType>(reminderType),
        configuration = when (enumValueOf<ReminderScheduleType>(scheduleType)) {
            ReminderScheduleType.DAILY -> ReminderScheduleConfiguration.Daily(timeFrom)
            ReminderScheduleType.WEEKLY_SINGLE -> ReminderScheduleConfiguration.WeeklySingle(
                time = timeFrom,
                day = enumValueOf(requireNotNull(weekDays.singleOrNull()) {
                    "Weekly reminder schedule must contain exactly one day"
                }),
            )
            ReminderScheduleType.WORKDAY_PERIOD -> ReminderScheduleConfiguration.WorkdayPeriod(
                from = timeFrom,
                until = LocalTime.parse(requireNotNull(timeUntilIso) {
                    "Workday period schedule must contain an end time"
                }),
                intervalMinutes = requireNotNull(intervalMinutes) {
                    "Workday period schedule must contain an interval"
                },
                days = weekDays.map { value -> enumValueOf<DayOfWeek>(value) },
            )
        },
    )
}
