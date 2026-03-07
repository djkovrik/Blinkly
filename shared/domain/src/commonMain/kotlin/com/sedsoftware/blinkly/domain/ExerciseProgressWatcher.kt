package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface ExerciseProgressWatcher {
    val achievements: Flow<List<Achievement>>
    val calendar: Flow<List<Workout>>

    fun start()
    fun stop()
}
