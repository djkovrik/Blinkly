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
import com.sedsoftware.blinkly.database.ReminderScheduleEntity
import com.sedsoftware.blinkly.database.adapter.DayOfWeekAdapter
import com.sedsoftware.blinkly.database.adapter.InstantAdapter
import com.sedsoftware.blinkly.database.adapter.LocalDateTimeAdapter
import com.sedsoftware.blinkly.database.adapter.LocalTimeAdapter
import com.sedsoftware.blinkly.database.mapper.AchievementMapper
import com.sedsoftware.blinkly.database.mapper.ExerciseMapper
import com.sedsoftware.blinkly.database.mapper.ReminderMapper
import com.sedsoftware.blinkly.database.mapper.ReminderScheduleMapper
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.BlinklyDatabaseSnapshot
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderSchedule
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ReminderScheduleType
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
    private val reminderScheduleMapper: ReminderScheduleMapper = ReminderScheduleMapper()

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
            ),
            ReminderScheduleEntityAdapter = ReminderScheduleEntity.Adapter(
                reminderTypeAdapter = EnumColumnAdapter(),
                scheduleTypeAdapter = EnumColumnAdapter(),
                timeFromAdapter = LocalTimeAdapter,
                timeUntilAdapter = LocalTimeAdapter,
                intervalMinutesAdapter = IntColumnAdapter,
                weekDaysAdapter = DayOfWeekAdapter,
            ),
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

    override fun currentReminderSchedules(): Flow<List<ReminderSchedule>> =
        queries.getReminderSchedules()
            .asFlow()
            .mapToList(dispatchers.io)
            .map(reminderScheduleMapper::toDomain)

    override suspend fun currentSnapshot(): BlinklyDatabaseSnapshot =
        withContext(dispatchers.io) {
            BlinklyDatabaseSnapshot(
                exercises = exerciseMapper.toDomain(queries.getExercises().executeAsList()),
                achievements = achievementMapper.toDomain(queries.getAchievements().executeAsList()),
                reminderSchedules = reminderScheduleMapper.toDomain(queries.getReminderSchedules().executeAsList()),
                reminders = reminderMapper.toDomain(queries.getReminders().executeAsList()),
            )
        }

    override suspend fun replaceSnapshot(snapshot: BlinklyDatabaseSnapshot) {
        withContext(dispatchers.io) {
            queries.transaction {
                queries.deleteExercises()
                queries.deleteAchievements()
                queries.deleteReminders()
                queries.deleteReminderSchedules()

                snapshot.exercises.forEach(::insertExercise)
                snapshot.achievements.forEach(::insertAchievement)
                snapshot.reminderSchedules.forEach(::insertReminderSchedule)
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

    override suspend fun remindersBySchedule(scheduleId: String): List<Reminder> =
        withContext(dispatchers.io) {
            reminderMapper.toDomain(queries.getRemindersBySchedule(scheduleId).executeAsList())
        }

    override suspend fun saveReminderSchedule(schedule: ReminderSchedule, reminders: List<Reminder>) {
        withContext(dispatchers.io) {
            queries.transaction {
                queries.deleteRemindersBySchedule(schedule.id)
                insertReminderSchedule(schedule)
                reminders.forEach(::insertReminder)
            }
        }
    }

    override suspend fun deleteReminderSchedule(scheduleId: String) {
        withContext(dispatchers.io) {
            queries.transaction {
                queries.deleteRemindersBySchedule(scheduleId)
                queries.deleteReminderSchedule(scheduleId)
            }
        }
    }

    override suspend fun deleteReminders() {
        withContext(dispatchers.io) {
            queries.transaction {
                queries.deleteReminders()
                queries.deleteReminderSchedules()
            }
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
            scheduleId = reminder.scheduleId,
            date = reminder.date,
            type = reminder.type,
            interval = reminder.interval,
        )
    }

    private fun insertReminderSchedule(schedule: ReminderSchedule) {
        val configuration = schedule.configuration
        queries.insertReminderSchedule(
            id = schedule.id,
            reminderType = schedule.reminderType,
            scheduleType = when (configuration) {
                is ReminderScheduleConfiguration.Daily -> ReminderScheduleType.DAILY
                is ReminderScheduleConfiguration.WeeklySingle -> ReminderScheduleType.WEEKLY_SINGLE
                is ReminderScheduleConfiguration.WorkdayPeriod -> ReminderScheduleType.WORKDAY_PERIOD
            },
            timeFrom = when (configuration) {
                is ReminderScheduleConfiguration.Daily -> configuration.time
                is ReminderScheduleConfiguration.WeeklySingle -> configuration.time
                is ReminderScheduleConfiguration.WorkdayPeriod -> configuration.from
            },
            timeUntil = (configuration as? ReminderScheduleConfiguration.WorkdayPeriod)?.until,
            intervalMinutes = (configuration as? ReminderScheduleConfiguration.WorkdayPeriod)?.intervalMinutes,
            weekDays = when (configuration) {
                is ReminderScheduleConfiguration.Daily -> emptyList()
                is ReminderScheduleConfiguration.WeeklySingle -> listOf(configuration.day)
                is ReminderScheduleConfiguration.WorkdayPeriod -> configuration.days
            },
        )
    }
}
