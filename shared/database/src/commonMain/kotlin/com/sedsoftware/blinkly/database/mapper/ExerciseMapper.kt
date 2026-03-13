package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.ExerciseEntity
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout

internal object ExerciseMapper {
    fun toDomain(from: List<ExerciseEntity>): List<Exercise> =
        from.map { item: ExerciseEntity ->
            Exercise(
                block = ExerciseBlock.fromIndex(item.block),
                type = ExerciseType.fromIndex(item.type),
                completedAt = item.completedAt,
            )
        }

    fun toWorkout(from: List<Exercise>): List<Workout> {
        val grouped = from.groupBy { exercise ->
            exercise.completedAt.asLocalDate()
        }

        return grouped.entries
            .sortedBy { it.key }
            .map { (_, exercises) -> Workout(exercises) }
    }
}
