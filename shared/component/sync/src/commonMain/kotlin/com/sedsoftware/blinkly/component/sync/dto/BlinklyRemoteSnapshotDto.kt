package com.sedsoftware.blinkly.component.sync.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class BlinklyRemoteSnapshotDto(
    val schemaVersion: Int = 3,
    val updatedAtEpochMillis: Long,
    val lastSyncedAtEpochMillis: Long? = null,
    val databaseUpdatedAtEpochMillis: Long? = null,
    val settingsUpdatedAtEpochMillis: Long? = null,
    val settings: SettingsDto,
    val exercises: List<ExerciseDto> = emptyList(),
    val achievements: List<AchievementDto> = emptyList(),
    val reminderSchedules: List<ReminderScheduleDto>,
    val reminders: List<ReminderDto>,
)

@Serializable
internal data class SettingsDto(
    val blinkBreakCount: Int,
    val nearFarFocusCount: Int,
    val nearFarFocusDuration: Float,
    val diagonalGazesCount: Int,
    val diagonalGazesDuration: Float,
    val figureEightCount: Int,
    val clockRollsEachSide: Int,
    val palmingDuration: Int,
    val themeState: String,
    val lightThemeWorkoutIndex: Int,
    val darkThemeWorkoutIndex: Int,
    val lastTreeProgressCheckDateIso: String? = null,
    val displayedHighlights: List<Int> = emptyList(),
    val currentHighlightDateIso: String? = null,
    val onboardingDisplayed: Boolean,
)

@Serializable
internal data class ExerciseDto(
    val type: String,
    val block: String,
    val completedAtEpochMillis: Long,
)

@Serializable
internal data class AchievementDto(
    val type: String,
    val level: String,
    val unlockedAtEpochMillis: Long? = null,
)

@Serializable
internal data class ReminderDto(
    val uuid: String,
    val scheduleId: String,
    val dateIso: String,
    val type: String,
    val interval: String,
)

@Serializable
internal data class ReminderScheduleDto(
    val id: String,
    val reminderType: String,
    val scheduleType: String,
    val timeFromIso: String,
    val timeUntilIso: String? = null,
    val intervalMinutes: Int? = null,
    val weekDays: List<String>,
)
