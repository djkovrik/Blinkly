package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface BlinklyDatabase {
    fun currentCalendar(): Flow<List<Workout>>
    fun currentAchievements(): Flow<List<Achievement>>
    fun currentReminderSchedules(): Flow<List<ReminderSchedule>>
    fun currentReminders(): Flow<List<Reminder>>
    suspend fun currentSnapshot(): BlinklyDatabaseSnapshot
    suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot)
    suspend fun saveExercise(exercise: Exercise)
    suspend fun saveExercises(exercises: List<Exercise>)
    suspend fun unlockAchievement(achievement: Achievement)
    suspend fun saveAchievements(achievements: List<Achievement>)
    suspend fun deleteAchievements()
    suspend fun deleteExercises()
    suspend fun remindersBySchedule(scheduleId: String): List<Reminder>
    suspend fun saveReminderSchedule(schedule: ReminderSchedule, reminders: List<Reminder>)
    suspend fun deleteReminderSchedule(scheduleId: String)
    suspend fun deleteReminders()
}
