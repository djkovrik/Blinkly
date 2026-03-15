package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.ExerciseEntity
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

internal class ExerciseMapper(
    private val timeZone: TimeZone,
) {
    fun toDomain(from: List<ExerciseEntity>): List<Exercise> =
        from.map { item: ExerciseEntity ->
            Exercise(
                block = item.block,
                type = item.type,
                completedAt = item.completedAt,
            )
        }

    fun toWorkout(from: List<Exercise>): List<Workout> {
        val grouped = from.groupBy { exercise ->
            exercise.completedAt.asLocalDate(timeZone)
        }

        return grouped.entries
            .sortedBy { it.key }
            .map { (_, exercises) -> Workout(exercises) }
    }
}
