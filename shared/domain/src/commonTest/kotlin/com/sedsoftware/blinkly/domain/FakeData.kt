package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.random.Random
import kotlin.time.Instant

internal object FakeData {

    fun getSingleExerciseWorkout(now: Instant): Workout {
        return Workout(
            exercises = listOf(
                Exercise(block = ExerciseBlock.A, type = ExerciseType.BLINK_BREAK, completedAt = now)
            )
        )
    }

    fun getWorkoutDayFull(now: Instant): Workout {
        return Workout(
            exercises = listOf(
                Exercise(block = ExerciseBlock.A, type = ExerciseType.BLINK_BREAK, completedAt = now),
                Exercise(block = ExerciseBlock.A, type = ExerciseType.NEAR_FAR_FOCUS, completedAt = now),
                Exercise(block = ExerciseBlock.A, type = ExerciseType.DIAGONAL_GAZES, completedAt = now),
                Exercise(block = ExerciseBlock.B, type = ExerciseType.FIGURE_EIGHT, completedAt = now),
                Exercise(block = ExerciseBlock.B, type = ExerciseType.CLOCK_ROLLS, completedAt = now),
                Exercise(block = ExerciseBlock.B, type = ExerciseType.PALMING, completedAt = now),
                Exercise(block = ExerciseBlock.C, type = ExerciseType.TWENTY_X3, completedAt = now),
                Exercise(block = ExerciseBlock.C, type = ExerciseType.TWENTY_X3, completedAt = now),
                Exercise(block = ExerciseBlock.C, type = ExerciseType.TWENTY_X3, completedAt = now),
            )
        )
    }

    fun getWorkoutDayPartial(now: Instant): Workout {
        val workout = getWorkoutDayFull(now)
        val exercises = workout.exercises
        val partialExercises = exercises.shuffled().take(exercises.size - 3)
        return Workout(partialExercises)
    }

    fun getCalendarWithFullDays(now: Instant, amountOfDays: Int): List<Workout> {
        val result = mutableListOf<Workout>()
        var currentDate = now
        var index = 0
        while (index < amountOfDays) {
            val day = getWorkoutDayFull(currentDate)
            currentDate = currentDate.minus(1, DateTimeUnit.DAY, TimeZone.UTC)
            index++
            result.add(day)
        }

        return result.reversed()
    }

    fun getCalendarWithPartialDays(now: Instant, amountOfDays: Int): List<Workout> {
        val result = mutableListOf<Workout>()
        var currentDate = now
        var index = 0
        while (index < amountOfDays) {
            val day = getWorkoutDayPartial(currentDate)
            currentDate = currentDate.minus(1, DateTimeUnit.DAY, TimeZone.UTC)
            index++
            result.add(day)
        }

        return result.reversed()
    }

    fun getCalendarWithFullAndPartialDays(now: Instant, amountOfDays: Int): List<Workout> {
        val result = mutableListOf<Workout>()
        var currentDate = now
        var index = 0
        while (index < amountOfDays) {
            val random = Random.nextBoolean()
            val day = if (random) {
                getWorkoutDayFull(currentDate)
            } else {
                getWorkoutDayPartial(currentDate)
            }
            currentDate = currentDate.minus(1, DateTimeUnit.DAY, TimeZone.UTC)
            index++
            result.add(day)
        }

        return result.reversed()
    }

    fun getCalendarWithMinExerciseCount(now: Instant, type: ExerciseType, requiredCount: Int): List<Workout> {
        val result = mutableListOf<Workout>()
        var currentDate = now
        var index = 0
        while (index <= requiredCount) {
            val random = Random.nextBoolean()
            val day = if (random) {
                getWorkoutDayFull(currentDate)
            } else {
                getWorkoutDayPartial(currentDate)
            }
            val exerciseCount = day.exercises.count { it.type == type }
            val randomMinus = Random.nextInt(1, 3)
            currentDate = currentDate.minus(randomMinus, DateTimeUnit.DAY, TimeZone.UTC)
            index += exerciseCount
            result.add(day)
        }

        return result.reversed()
    }

    fun getCalendarWithFullBlocksA(now: Instant, requiredCount: Int): List<Workout> {
        val calendar = mutableListOf<Workout>()
        repeat(requiredCount) { index ->
            calendar.add(
                Workout(
                    exercises = listOf(
                        Exercise(
                            block = ExerciseBlock.A,
                            type = ExerciseType.BLINK_BREAK,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.A,
                            type = ExerciseType.NEAR_FAR_FOCUS,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.A,
                            type = ExerciseType.DIAGONAL_GAZES,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.B,
                            type = ExerciseType.FIGURE_EIGHT,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        )
                    )
                )
            )
        }

        return calendar
    }

    fun getCalendarWithFullBlocksB(now: Instant, requiredCount: Int): List<Workout> {
        val calendar = mutableListOf<Workout>()
        repeat(requiredCount) { index ->
            calendar.add(
                Workout(
                    exercises = listOf(
                        Exercise(
                            block = ExerciseBlock.B,
                            type = ExerciseType.FIGURE_EIGHT,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.B,
                            type = ExerciseType.CLOCK_ROLLS,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.B,
                            type = ExerciseType.PALMING,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        ),
                        Exercise(
                            block = ExerciseBlock.A,
                            type = ExerciseType.BLINK_BREAK,
                            completedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                        )
                    )
                )
            )
        }

        return calendar
    }

    fun getFullAchievementsList(now: Instant): List<Achievement> =
        AchievementType.entries
            .filterNot { it == AchievementType.UNKNOWN }
            .mapIndexed { index, type ->
                Achievement(
                    type = type,
                    level = type.getLevel(),
                    unlockedAt = now.minus(index, DateTimeUnit.DAY, TimeZone.UTC)
                )
            }

    fun getRegularAchievementsList(now: Instant): List<Achievement> =
        getFullAchievementsList(now)
            .filterNot { it.level == AchievementLevel.HIDDEN }
}
