package com.sedsoftware.blinkly.database.impl

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.sedsoftware.blinkly.database.AchievementEntity
import com.sedsoftware.blinkly.database.BlinklyAppDatabase
import com.sedsoftware.blinkly.database.BlinklyAppDatabaseQueries
import com.sedsoftware.blinkly.database.ExerciseEntity
import com.sedsoftware.blinkly.database.ReminderEntity
import com.sedsoftware.blinkly.database.adapter.DayOfWeekAdapter
import com.sedsoftware.blinkly.database.adapter.InstantAdapter
import com.sedsoftware.blinkly.database.adapter.LocalDateTimeAdapter
import com.sedsoftware.blinkly.database.mapper.AchievementMapper
import com.sedsoftware.blinkly.database.mapper.ExerciseMapper
import com.sedsoftware.blinkly.database.mapper.ReminderMapper
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class BlinklyDatabaseImpl(
    private val dispatchers: BlinklyDispatchers,
    private val driver: SqlDriver,
    timeUtils: BlinklyTimeUtils,
) : BlinklyDatabase {

    private val achievementMapper: AchievementMapper = AchievementMapper()
    private val exerciseMapper: ExerciseMapper = ExerciseMapper(timeUtils.timeZone())
    private val reminderMapper: ReminderMapper = ReminderMapper()

    private val database: BlinklyAppDatabase =
        BlinklyAppDatabase(
            driver = driver,
            AchievementEntityAdapter = AchievementEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
                levelAdapter = EnumColumnAdapter(),
                unlockedAtAdapter = InstantAdapter,
            ),
            ExerciseEntityAdapter = ExerciseEntity.Adapter(
                idAdapter = IntColumnAdapter,
                typeAdapter = EnumColumnAdapter(),
                blockAdapter = EnumColumnAdapter(),
                completedAtAdapter = InstantAdapter,
            ),
            ReminderEntityAdapter = ReminderEntity.Adapter(
                dateAdapter = LocalDateTimeAdapter,
                typeAdapter = EnumColumnAdapter(),
                intervalAdapter = EnumColumnAdapter(),
                weekDaysAdapter = DayOfWeekAdapter,
            )
        )

    private val queries: BlinklyAppDatabaseQueries
        get() = database.blinklyAppDatabaseQueries

    override fun currentCalendar(): Flow<List<Workout>> =
        queries.getExercises()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(exerciseMapper::toDomain)
            .map(exerciseMapper::toWorkout)

    override fun currentAchievements(): Flow<List<Achievement>> =
        queries.getAchievements()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(achievementMapper::toDomain)

    override fun currentReminders(): Flow<List<Reminder>> =
        queries.getReminders()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(reminderMapper::toDomain)

    override suspend fun currentSnapshot(): BlinklyDatabaseSnapshot =
        withContext(dispatchers.io) {
            BlinklyDatabaseSnapshot(
                exercises = exerciseMapper.toDomain(queries.getExercises().executeAsList()),
                achievements = achievementMapper.toDomain(queries.getAchievements().executeAsList()),
                reminders = reminderMapper.toDomain(queries.getReminders().executeAsList()),
            )
        }

    override suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot) {
        withContext(dispatchers.io) {
            queries.transaction {
                queries.deleteExercises()
                queries.deleteAchievements()
                queries.deleteReminders()

                snapshot.exercises.forEach(::insertExercise)
                snapshot.achievements.forEach(::insertAchievement)
                snapshot.reminders.forEach(::insertReminder)
            }
        }
    }

    override suspend fun saveExercise(exercise: Exercise) {
        withContext(dispatchers.io) {
            insertExercise(exercise)
        }
    }

    override suspend fun saveExercises(exercises: List<Exercise>) {
        withContext(dispatchers.io) {
            queries.transaction {
                exercises.forEach(::insertExercise)
            }
        }
    }

    override suspend fun unlockAchievement(achievement: Achievement) {
        withContext(dispatchers.io) {
            insertAchievement(achievement)
        }
    }

    override suspend fun saveAchievements(achievements: List<Achievement>) {
        withContext(dispatchers.io) {
            queries.transaction {
                achievements.forEach(::insertAchievement)
            }
        }
    }

    override suspend fun deleteAchievements() {
        withContext(dispatchers.io) {
            queries.deleteAchievements()
        }
    }

    override suspend fun deleteExercises() {
        withContext(dispatchers.io) {
            queries.deleteExercises()
        }
    }

    override suspend fun saveReminder(reminder: Reminder) {
        withContext(dispatchers.io) {
            insertReminder(reminder)
        }
    }

    override suspend fun saveReminders(reminders: List<Reminder>) {
        withContext(dispatchers.io) {
            queries.transaction {
                reminders.forEach(::insertReminder)
            }
        }
    }

    override suspend fun deleteReminder(uuid: String) {
        withContext(dispatchers.io) {
            queries.deleteReminder(uuid)
        }
    }

    override suspend fun deleteReminders() {
        withContext(dispatchers.io) {
            queries.deleteReminders()
        }
    }

    private fun insertExercise(exercise: Exercise) {
        queries.insertExercise(
            type = exercise.type,
            block = exercise.block,
            completedAt = exercise.completedAt,
        )
    }

    private fun insertAchievement(achievement: Achievement) {
        queries.insertAchievement(
            type = achievement.type,
            level = achievement.level,
            unlockedAt = achievement.unlockedAt,
        )
    }

    private fun insertReminder(reminder: Reminder) {
        queries.insertReminder(
            uuid = reminder.uuid,
            date = reminder.date,
            type = reminder.type,
            interval = reminder.interval,
            weekDays = reminder.weekDays,
        )
    }
}
