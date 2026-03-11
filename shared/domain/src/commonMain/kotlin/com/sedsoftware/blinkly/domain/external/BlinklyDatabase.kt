package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface BlinklyDatabase {
    suspend fun currentCalendar(): Flow<List<Workout>>
    suspend fun saveExercise(exercise: Exercise)
    suspend fun currentAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(achievement: Achievement)
    suspend fun saveCurrentTree(tree: Tree)
    suspend fun getCurrentTree(): Tree?
}
