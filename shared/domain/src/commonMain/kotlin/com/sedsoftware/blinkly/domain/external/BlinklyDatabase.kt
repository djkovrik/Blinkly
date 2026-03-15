package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface BlinklyDatabase {
    fun currentCalendar(): Flow<List<Workout>>
    fun currentAchievements(): Flow<List<Achievement>>
    fun currentReminders(): Flow<List<Reminder>>
    suspend fun saveExercise(exercise: Exercise)
    suspend fun unlockAchievement(achievement: Achievement)
    suspend fun saveReminder(reminder: Reminder)
    suspend fun saveReminders(reminders: List<Reminder>)
    suspend fun deleteReminder(uuid: String)
    suspend fun deleteReminders()
}
