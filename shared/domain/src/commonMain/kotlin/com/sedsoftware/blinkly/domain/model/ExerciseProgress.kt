package com.sedsoftware.blinkly.domain.model

data class ExerciseProgress(
    val percent: Int,
    val remainingMs: Long,
    val totalMs: Long,
)
