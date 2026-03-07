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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
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

    fun getEarlyBirdWorkoutCorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(
                        ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 15, 5, 30).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 15, 6, 15).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 15, 7, 45).toInstant(TimeZone.currentSystemDefault())
                    )
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(
                        ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 16, 6, 0).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 16, 10, 0).toInstant(TimeZone.currentSystemDefault())
                    )
                )
            )
        )

        return workouts
    }

    fun getEarlyBirdWorkoutIncorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(
                        ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 15, 5, 30).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 15, 6, 15).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 15, 8, 1).toInstant(TimeZone.currentSystemDefault())
                    )
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(
                        ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 16, 6, 0).toInstant(TimeZone.currentSystemDefault())
                    ),
                    Exercise(
                        ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 16, 10, 0).toInstant(TimeZone.currentSystemDefault())
                    )
                )
            )
        )

        return workouts
    }

    fun getNightOwlWorkoutCorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 18, 23, 30).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 19, 0, 15).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3,
                        LocalDateTime(2024, 1, 19, 1, 45).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 20, 23, 0).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 20, 12, 0).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        return workouts
    }

    fun getNightOwlWorkoutIncorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 18, 23, 30).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 19, 0, 15).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3,
                        LocalDateTime(2024, 1, 19, 2, 2).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 20, 23, 0).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 20, 12, 0).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        return workouts
    }

    fun getTimelessGazeWorkoutCorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 18, 23, 30).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 19, 0, 0).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3,
                        LocalDateTime(2024, 1, 19, 1, 45).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 20, 23, 0).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 20, 12, 0).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        return workouts
    }

    fun getTimelessGazeWorkoutIncorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK,
                        LocalDateTime(2024, 1, 18, 23, 30).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT,
                        LocalDateTime(2024, 1, 19, 0, 1).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.C, ExerciseType.TWENTY_X3,
                        LocalDateTime(2024, 1, 19, 2, 2).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        workouts.add(
            Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS,
                        LocalDateTime(2024, 1, 20, 23, 0).toInstant(TimeZone.currentSystemDefault())),
                    Exercise(ExerciseBlock.B, ExerciseType.CLOCK_ROLLS,
                        LocalDateTime(2024, 1, 20, 12, 0).toInstant(TimeZone.currentSystemDefault()))
                )
            )
        )

        return workouts
    }

    fun getThinkTankWorkoutCorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()
        repeat(3) {
            workouts.add(Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, Instant.DISTANT_PAST),
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, Instant.DISTANT_PAST),
                    Exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, Instant.DISTANT_PAST)
                )
            ))
        }
        workouts.add(Workout(
            exercises = listOf(
                Exercise(ExerciseBlock.B, ExerciseType.FIGURE_EIGHT, Instant.DISTANT_PAST)
            )
        ))

        return workouts
    }

    fun getThinkTankWorkoutIncorrect(): List<Workout> {
        val workouts = mutableListOf<Workout>()

        repeat(2) {
            workouts.add(Workout(
                exercises = listOf(
                    Exercise(ExerciseBlock.A, ExerciseType.BLINK_BREAK, Instant.DISTANT_PAST),
                    Exercise(ExerciseBlock.A, ExerciseType.NEAR_FAR_FOCUS, Instant.DISTANT_PAST),
                    Exercise(ExerciseBlock.A, ExerciseType.DIAGONAL_GAZES, Instant.DISTANT_PAST),
                    Exercise(ExerciseBlock.B, ExerciseType.NEAR_FAR_FOCUS, Instant.DISTANT_PAST),
                )
            ))
        }
        workouts.add(Workout(
            exercises = listOf(
                Exercise(ExerciseBlock.B, ExerciseType.NEAR_FAR_FOCUS, Instant.DISTANT_PAST)
            )
        ))

        return workouts
    }
}
