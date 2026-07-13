package com.sedsoftware.blinkly.component.sync.tracking

import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

internal class TrackingBlinklyDatabase(
    private val delegate: BlinklyDatabase,
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
) : BlinklyDatabase {

    override fun currentCalendar(): Flow<List<Workout>> =
        delegate.currentCalendar()

    override fun currentAchievements(): Flow<List<Achievement>> =
        delegate.currentAchievements()

    override fun currentReminders(): Flow<List<Reminder>> =
        delegate.currentReminders()

    override fun currentReminderSchedules(): Flow<List<ReminderSchedule>> =
        delegate.currentReminderSchedules()

    override suspend fun currentSnapshot(): BlinklyDatabaseSnapshot =
        delegate.currentSnapshot()

    override suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot) {
        delegate.replaceSnapshot(snapshot)
        markChanged()
    }

    override suspend fun saveExercise(exercise: Exercise) {
        delegate.saveExercise(exercise)
        markChanged()
    }

    override suspend fun saveExercises(exercises: List<Exercise>) {
        delegate.saveExercises(exercises)
        markChanged()
    }

    override suspend fun unlockAchievement(achievement: Achievement) {
        delegate.unlockAchievement(achievement)
        markChanged()
    }

    override suspend fun saveAchievements(achievements: List<Achievement>) {
        delegate.saveAchievements(achievements)
        markChanged()
    }

    override suspend fun deleteAchievements() {
        delegate.deleteAchievements()
        markChanged()
    }

    override suspend fun deleteExercises() {
        delegate.deleteExercises()
        markChanged()
    }

    override suspend fun remindersBySchedule(scheduleId: String): List<Reminder> =
        delegate.remindersBySchedule(scheduleId)

    override suspend fun saveReminderSchedule(schedule: ReminderSchedule, reminders: List<Reminder>) {
        delegate.saveReminderSchedule(schedule, reminders)
        markChanged()
    }

    override suspend fun deleteReminderSchedule(scheduleId: String) {
        delegate.deleteReminderSchedule(scheduleId)
        markChanged()
    }

    override suspend fun deleteReminders() {
        delegate.deleteReminders()
        markChanged()
    }

    private fun markChanged() {
        settings.lastLocalDatabaseChangeAt = timeUtils.now()
    }
}
