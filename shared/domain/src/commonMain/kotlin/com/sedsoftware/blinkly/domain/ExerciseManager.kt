package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseEvent
import kotlinx.coroutines.flow.Flow

interface ExerciseManager {
    val events: Flow<ExerciseEvent>

    fun startBlock(block: ExerciseBlock)
    fun startNextExercise()
    fun pause()
    fun resume()
    fun stop()
}
