package com.sedsoftware.blinkly.database.impl

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.sedsoftware.blinkly.database.AchievementEntity
import com.sedsoftware.blinkly.database.BlinklyAppDatabase
import com.sedsoftware.blinkly.database.BlinklyAppDatabaseQueries
import com.sedsoftware.blinkly.database.ExerciseEntity
import com.sedsoftware.blinkly.database.adapter.InstantAdapter
import com.sedsoftware.blinkly.database.mapper.AchievementMapper
import com.sedsoftware.blinkly.database.mapper.ExerciseMapper
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class BlinklyDatabaseImpl(
    private val dispatchers: BlinklyDispatchers,
    private val driver: SqlDriver,
) : BlinklyDatabase {

    private val database: BlinklyAppDatabase =
        BlinklyAppDatabase(
            driver = driver,
            AchievementEntityAdapter = AchievementEntity.Adapter(
                typeAdapter = IntColumnAdapter,
                levelAdapter = IntColumnAdapter,
                unlockedAtAdapter = InstantAdapter,
            ),
            ExerciseEntityAdapter = ExerciseEntity.Adapter(
                idAdapter = IntColumnAdapter,
                typeAdapter = IntColumnAdapter,
                blockAdapter = IntColumnAdapter,
                completedAtAdapter = InstantAdapter,
            ),
        )

    private val queries: BlinklyAppDatabaseQueries
        get() = database.blinklyAppDatabaseQueries

    override suspend fun currentCalendar(): Flow<List<Workout>> =
        queries.getExercises()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(ExerciseMapper::toDomain)
            .map(ExerciseMapper::toWorkout)

    override suspend fun saveExercise(exercise: Exercise) {
        withContext(dispatchers.io) {
            queries.insertExercise(
                type = exercise.type.index,
                block = exercise.block.index,
                completedAt = exercise.completedAt,
            )
        }
    }

    override suspend fun currentAchievements(): Flow<List<Achievement>> =
        queries.getAchievements()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(AchievementMapper::toDomain)

    override suspend fun unlockAchievement(achievement: Achievement) {
        withContext(dispatchers.io) {
            queries.insertAchievement(
                type = achievement.type.index,
                level = achievement.level.index,
                unlockedAt = achievement.unlockedAt,
            )
        }
    }
}
