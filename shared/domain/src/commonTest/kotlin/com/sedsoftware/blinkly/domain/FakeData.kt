package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.type.ExerciseBlock
import com.sedsoftware.blinkly.domain.type.ExerciseType
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
}
