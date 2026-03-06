package com.sedsoftware.blinkly.domain.model

import kotlin.time.Instant

data class Exercise(
    val block: ExerciseBlock,
    val type: ExerciseType,
    val completedAt: Instant,
)
