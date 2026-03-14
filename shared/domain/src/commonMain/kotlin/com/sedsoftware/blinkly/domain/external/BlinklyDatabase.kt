package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface BlinklyDatabase {
    fun currentCalendar(): Flow<List<Workout>>
    fun currentAchievements(): Flow<List<Achievement>>
    suspend fun saveExercise(exercise: Exercise)
    suspend fun unlockAchievement(achievement: Achievement)
}
